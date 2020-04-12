/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.baldapps.artemis.checkers;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;

import com.baldapps.artemis.utils.SemanticUtils;
import com.baldapps.artemis.utils.flags.VariableFlags;

@SuppressWarnings("restriction")
public class ClassFieldsCopiedChecker extends AbstractIndexAstChecker {
	public static final String ER_ID = "com.baldapps.artemis.checkers.ClassFieldsCopiedProblem"; //$NON-NLS-1$
	public static final String PARAM_SKIP = "skip"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new OnEachClass());
	}

	class OnEachClass extends ASTVisitor {
		// NOTE: Classes can be nested and even can be declared in constructors of the other classes
		private final Stack<Set<IField>> operatorEqStack = new Stack<>();
		private boolean skipMethodsWithFCalls = skipMethodWithFCalls();

		OnEachClass() {
			shouldVisitDeclarations = true;
			shouldVisitNames = true;
			shouldVisitExpressions = true;
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			ICPPMethod method = getOperatorEq(declaration);
			if (method != null) {
				Set<IField> fieldsInConstructor = operatorEqStack.push(new HashSet<IField>());
				// Add all class fields
				for (IField field : method.getClassOwner().getDeclaredFields()) {
					if (isSimpleType(field.getType()) && !field.isStatic()) {
						// In C++11, a field may have an initial value specified at its declaration.
						// Such a field does not need to be initialized in the constructor as well.
						if (field.getInitialValue() == null) {
							fieldsInConstructor.add(field);
						}
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTDeclaration declaration) {
			if (getOperatorEq(declaration) != null) {
				for (IField field : operatorEqStack.pop()) {
					reportProblem(ER_ID, declaration, field.getName());
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTExpression expression) {
			boolean skipCurrentMethod = false;

			if (skipMethodsWithFCalls && !operatorEqStack.empty() && expression instanceof IASTFunctionCallExpression) {
				Set<IField> actualConstructorFields = operatorEqStack.peek();
				if (!actualConstructorFields.isEmpty()) {
					IASTFunctionCallExpression fCall = (IASTFunctionCallExpression) expression;
					IASTExpression fNameExp = fCall.getFunctionNameExpression();
					IBinding fBinding = null;
					if (fNameExp instanceof IASTIdExpression) {
						IASTIdExpression fName = (IASTIdExpression) fNameExp;
						fBinding = fName.getName().resolveBinding();
					} else if (fNameExp instanceof ICPPASTFieldReference) {
						ICPPASTFieldReference fName = (ICPPASTFieldReference) fNameExp;
						fBinding = fName.getFieldName().resolveBinding();
					}
					if (fBinding != null) {
						if (fBinding instanceof ICPPMethod) {
							ICPPMethod method = (ICPPMethod) fBinding;
							ICompositeType constructorOwner = actualConstructorFields.iterator().next()
									.getCompositeTypeOwner();
							if (constructorOwner.equals(method.getClassOwner()) && !method.getType().isConst()) {
								skipCurrentMethod = true;
							}
						} else if (fBinding instanceof ICPPFunction) {
							for (IASTInitializerClause argument : fCall.getArguments()) {
								if (SemanticUtils.referencesThis(argument)) {
									skipCurrentMethod = true;
									break;
								}
							}
						}
					}
				}
			}

			//Skip method if pattern is *this = toBeCopied;
			if (expression instanceof IASTBinaryExpression) {
				IASTBinaryExpression binaryExpression = (IASTBinaryExpression) expression;
				if (SemanticUtils.referencesThis(binaryExpression.getOperand1())
						&& binaryExpression.getOperand1().isLValue()) {
					skipCurrentMethod = true;
				}
			}

			if (skipCurrentMethod && !operatorEqStack.empty()) {
				operatorEqStack.peek().clear();
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTName name) {
			if (!operatorEqStack.empty()) {
				if (name.getParent() instanceof IASTFieldReference) {
					IASTFieldReference ref = (IASTFieldReference) name.getParent();
					if (!SemanticUtils.referencesThis(ref.getFieldOwner()))
						return PROCESS_CONTINUE;
				}
				Set<IField> actualConstructorFields = operatorEqStack.peek();
				if (!actualConstructorFields.isEmpty()) {
					IBinding binding = name.resolveBinding();
					if (binding != null && !(binding instanceof IProblemBinding)) {
						IField equivalentFieldBinding = getContainedEquivalentBinding(actualConstructorFields, binding,
								name.getTranslationUnit().getIndex());
						if (equivalentFieldBinding != null) {
							if (VariableFlags.mayBeWriteAccess(VariableFlags.getFlags(name))) {
								actualConstructorFields.remove(equivalentFieldBinding);
							}
						}
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		private IField getContainedEquivalentBinding(Iterable<IField> fields, IBinding binding, IIndex index) {
			for (IField field : fields) {
				if (areEquivalentBindings(binding, field, index)) {
					return field;
				}
			}

			return null;
		}

		private boolean areEquivalentBindings(IBinding binding1, IBinding binding2, IIndex index) {
			if (binding1.equals(binding2)) {
				return true;
			}
			if ((binding1 instanceof IIndexBinding) != (binding2 instanceof IIndexBinding) && index != null) {
				if (binding1 instanceof IIndexBinding) {
					binding2 = index.adaptBinding(binding2);
				} else {
					binding1 = index.adaptBinding(binding1);
				}
				if (binding1 == null || binding2 == null) {
					return false;
				}
				if (binding1.equals(binding2)) {
					return true;
				}
			}
			return false;
		}

		/** Checks whether class member of the specified type should be initialized
		 *
		 * @param type	Type to check
		 * @return true if type is:
		 *     - basic type (int, float, ...)
		 *     - pointer
		 *     - enum
		 *     - reference (should be initialized in initialization list)
		 *     - typedef to the another native type.
		 *
		 * @note: Not supported types (but maybe should be):
		 *     - array
		 *     - union
		 *     - unknown type (need user preference?)
		 *     - template parameter (need user preference?)
		 */
		private boolean isSimpleType(IType type) {
			return (type instanceof IBasicType || type instanceof IPointerType || type instanceof IEnumeration
					|| type instanceof ICPPReferenceType
					|| (type instanceof ITypedef && isSimpleType(((ITypedef) type).getType())));
		}

		private ICPPMethod getOperatorEq(IASTDeclaration decl) {
			if (decl instanceof ICPPASTFunctionDefinition) {
				ICPPASTFunctionDefinition functionDefinition = (ICPPASTFunctionDefinition) decl;
				if (functionDefinition.isDeleted())
					return null;
				IBinding binding = functionDefinition.getDeclarator().getName().resolveBinding();
				if (binding instanceof ICPPMethod) {
					ICPPMethod method = (ICPPMethod) binding;
					if (SemanticUtils.isCopyOrMoveAssignmentOperator(method))
						return method;
				}
			}
			return null;
		}
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_SKIP, CheckersMessages.ClassMembersCopiedChecker_SkipMethodWithFCalls,
				Boolean.TRUE);
	}

	public boolean skipMethodWithFCalls() {
		return (Boolean) getPreference(getProblemById(ER_ID, getFile()), PARAM_SKIP);
	}
}
