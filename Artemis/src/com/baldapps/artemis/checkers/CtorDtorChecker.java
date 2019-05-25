/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package com.baldapps.artemis.checkers;

import java.util.Stack;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

import com.baldapps.artemis.utils.ClassUtils;
import com.baldapps.artemis.utils.SemanticUtils;

@SuppressWarnings("restriction")
public class CtorDtorChecker extends AbstractIndexAstChecker {
	public static final String VIRTUAL_CALL_ID = "com.baldapps.artemis.checkers.VirtualMethodCallProblem"; //$NON-NLS-1$
	public static final String THROW_ID = "com.baldapps.artemis.checkers.ThrowInDestructorProblem"; //$NON-NLS-1$
	public static final String GLOBALS_ID = "com.baldapps.artemis.checkers.GlobalsInCtorProblem"; //$NON-NLS-1$
	public static final String CALL_SUPER_CTOR_ID = "com.baldapps.artemis.checkers.CallSuperCtorProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new OnEachClass());
	}

	private enum DECL_TYPE {
		CTOR, DTOR, OTHER
	}

	class OnEachClass extends ASTVisitor {
		// NOTE: Classes can be nested and even can be declared in constructors of the other classes
		private final Stack<DECL_TYPE> ctorDtorStack = new Stack<>();

		OnEachClass() {
			shouldVisitDeclarations = true;
			shouldVisitExpressions = true;
			shouldVisitNames = true;
			shouldVisitDeclSpecifiers = true;
		}

		@Override
		public int visit(IASTDeclSpecifier declSpec) {
			if (declSpec instanceof ICPPASTCompositeTypeSpecifier && !ctorDtorStack.isEmpty()) {
				ctorDtorStack.push(DECL_TYPE.OTHER);
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTDeclSpecifier declSpec) {
			if (declSpec instanceof ICPPASTCompositeTypeSpecifier && !ctorDtorStack.isEmpty()) {
				ctorDtorStack.pop();
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			ICPPConstructor constructor = ClassUtils.getConstructor(declaration);
			if (constructor != null) {
				ICPPBase[] bases = constructor.getClassOwner().getBases();
				ICPPASTFunctionDefinition functionDefinition = (ICPPASTFunctionDefinition) declaration;
				if (functionDefinition.getMemberInitializers().length < bases.length)
					reportProblem(CALL_SUPER_CTOR_ID, declaration);
				ctorDtorStack.push(DECL_TYPE.CTOR);
			} else {
				ICPPMethod destructor = ClassUtils.getDestructor(declaration);
				if (destructor != null) {
					ctorDtorStack.push(DECL_TYPE.DTOR);
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTDeclaration declaration) {
			if (ClassUtils.getConstructor(declaration) != null || ClassUtils.getDestructor(declaration) != null) {
				ctorDtorStack.pop();
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTExpression expression) {
			if (!ctorDtorStack.empty()) {
				DECL_TYPE t = ctorDtorStack.peek();
				if (t == DECL_TYPE.OTHER)
					return PROCESS_CONTINUE;
				if (expression instanceof IASTFunctionCallExpression) {
					IASTFunctionCallExpression fCall = (IASTFunctionCallExpression) expression;
					IASTExpression fNameExp = fCall.getFunctionNameExpression();
					IBinding fBinding = null;
					IASTNode problemNode = expression;
					if (fNameExp instanceof IASTIdExpression) {
						IASTIdExpression fName = (IASTIdExpression) fNameExp;
						fBinding = fName.getName().resolveBinding();
					} else if (fNameExp instanceof IASTFieldReference) {
						IASTFieldReference fName = (IASTFieldReference) fNameExp;
						problemNode = fName.getFieldName();
						if (SemanticUtils.referencesThis(fName.getFieldOwner()))
							fBinding = fName.getFieldName().resolveBinding();
					}
					if (fBinding != null && fBinding instanceof ICPPMethod) {
						ICPPMethod method = (ICPPMethod) fBinding;
						if (method.isPureVirtual() || ClassTypeHelper.isVirtual(method)) {
							reportProblem(VIRTUAL_CALL_ID, problemNode);
						}
					}
				}
				if (t == DECL_TYPE.DTOR) {
					if (expression instanceof IASTUnaryExpression) {
						if (((IASTUnaryExpression) expression).getOperator() == IASTUnaryExpression.op_throw) {
							ICPPASTTryBlockStatement tryCatch = ASTQueries.findAncestorWithType(expression,
									ICPPASTTryBlockStatement.class);
							if (tryCatch == null)
								reportProblem(THROW_ID, expression);
							else {
								ICPPASTCatchHandler[] handlers = tryCatch.getCatchHandlers();
								for (ICPPASTCatchHandler h : handlers) {
									if (h.isCatchAll())
										return PROCESS_CONTINUE;
								}
								reportProblem(THROW_ID, expression);
							}
						}
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTName name) {
			if (!ctorDtorStack.empty()) {
				DECL_TYPE t = ctorDtorStack.peek();
				if (t == DECL_TYPE.CTOR) {
					IBinding binding = name.resolveBinding();
					if (binding instanceof ICPPVariable) {
						try {
							IScope scope = binding.getScope();
							if (scope.getKind() == EScopeKind.eGlobal) {
								reportProblem(GLOBALS_ID, name, ASTStringUtil.getSimpleName(name));
							}
						} catch (DOMException e) {
							ArtemisCoreActivator.log(e);
						}
					}
				}
			}
			return PROCESS_CONTINUE;
		}
	}
}
