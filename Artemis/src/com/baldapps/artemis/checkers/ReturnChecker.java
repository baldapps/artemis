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

import org.eclipse.cdt.codan.checkers.CodanCheckersActivator;
import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.codan.core.cxx.model.AbstractAstFunctionChecker;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

@SuppressWarnings("restriction")
public class ReturnChecker extends AbstractAstFunctionChecker {
	public static final String RET_LOCAL_ID = "com.baldapps.artemis.checkers.LocalVarReturn"; //$NON-NLS-1$
	public static final String RET_PRIVATE_FIELD_ID = "com.baldapps.artemis.checkers.RetPrivateField"; //$NON-NLS-1$
	public static final String NO_RET_THIS_ID = "com.baldapps.artemis.checkers.NoRetThisOpAssign"; //$NON-NLS-1$
	public static final String RET_FIELD_FROM_CONST_ID = "com.baldapps.artemis.checkers.RetFieldFromConstMethod"; //$NON-NLS-1$

	private IType cachedReturnType = null;

	private enum RET_TYPE {
		BY_REF, BY_PTR, BY_VALUE
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		switch (problem.getId()) {
		case RET_FIELD_FROM_CONST_ID:
			getLaunchModePreference(problem).setRunningMode(CheckerLaunchMode.RUN_ON_FULL_BUILD, false);
			getLaunchModePreference(problem).setRunningMode(CheckerLaunchMode.RUN_ON_INC_BUILD, false);
			break;
		default:
			break;
		}
	}

	private static class ReturnInfo {
		RET_TYPE type;
		boolean isConst;
		boolean mustBeThis;
		boolean isMethodConst;
		boolean isMethodPublic;
	}

	private class ReturnTypeAnalizer {
		private ReturnInfo retType;
		private Stack<Integer> innermostOp;
		private IASTFieldReference firstFieldReference;

		public ReturnTypeAnalizer(ReturnInfo info) {
			retType = info;
			innermostOp = new Stack<>();
			firstFieldReference = null;
		}

		public ReturnInfo getInfo() {
			return retType;
		}

		public void visit(IASTExpression expr) {
			if (expr instanceof IASTCastExpression) {
				visit((IASTCastExpression) expr);
			} else if (expr instanceof IASTConditionalExpression) {
				visit((IASTConditionalExpression) expr);
			} else if (expr instanceof IASTIdExpression) {
				visit((IASTIdExpression) expr);
			} else if (expr instanceof IASTUnaryExpression) {
				visit((IASTUnaryExpression) expr);
			} else if (expr instanceof IASTFieldReference) {
				visit((IASTFieldReference) expr);
			} else if (expr instanceof IASTLiteralExpression) {
				visit((IASTLiteralExpression) expr);
			}
		}

		private void visit(IASTLiteralExpression expr) {
			if (retType.mustBeThis && expr.getKind() != IASTLiteralExpression.lk_this)
				reportProblem(NO_RET_THIS_ID, expr);
		}

		private void visit(IASTFieldReference expr) {
			if (firstFieldReference == null)
				firstFieldReference = expr;
			visit(expr.getFieldOwner());
			firstFieldReference = null;
		}

		private void visit(IASTCastExpression expr) {
			IASTTypeId id = expr.getTypeId();
			IASTDeclarator declarator = id.getAbstractDeclarator();
			IASTPointerOperator[] ptr = declarator.getPointerOperators();
			if (ptr.length > 0 && ptr[0] instanceof ICPPASTReferenceOperator) {
				innermostOp.push(IASTUnaryExpression.op_amper);
			}
			visit(expr.getOperand());
			if (ptr.length > 0 && ptr[0] instanceof ICPPASTReferenceOperator) {
				innermostOp.pop();
			}
		}

		private void visit(IASTConditionalExpression expr) {
			visit(expr.getPositiveResultExpression());
			visit(expr.getNegativeResultExpression());
		}

		private void visit(IASTIdExpression expr) {
			IBinding binding = expr.getName().resolveBinding();
			if (binding instanceof IVariable && !(binding instanceof IParameter) && !(binding instanceof ICPPField)) {
				Integer op = null;
				if (!innermostOp.empty())
					op = innermostOp.peek();
				IType t = ((IVariable) binding).getType();
				t = SemanticUtil.getNestedType(t, SemanticUtil.TDEF);
				if (retType.type == RET_TYPE.BY_REF && !(t instanceof ICPPReferenceType)) {
					if (t instanceof IPointerType && op != null && op == IASTUnaryExpression.op_star) {
						return;
					}
					try {
						IScope scope = binding.getScope();
						if (scope.getKind() == EScopeKind.eLocal && !((IVariable) binding).isStatic()) {
							reportProblem(RET_LOCAL_ID, expr, binding.getName());
						}
					} catch (DOMException e) {
						CodanCheckersActivator.log(e);
					}
				} else if (retType.type == RET_TYPE.BY_PTR && op != null && op == IASTUnaryExpression.op_amper) {
					try {
						IScope scope = binding.getScope();
						if (scope.getKind() == EScopeKind.eLocal && !((IVariable) binding).isStatic()) {
							reportProblem(RET_LOCAL_ID, expr, binding.getName());
						}
					} catch (DOMException e) {
						CodanCheckersActivator.log(e);
					}
				}
			}
			if (!retType.isConst && binding instanceof ICPPField && retType.type != RET_TYPE.BY_VALUE) {
				if (retType.type == RET_TYPE.BY_PTR && isPointer((ICPPField) binding)) {
					return;
				}
				Integer op = null;
				if (!innermostOp.empty())
					op = innermostOp.peek();
				if (retType.type == RET_TYPE.BY_PTR && op != null && op == IASTUnaryExpression.op_star) {
					ICPPClassType klass = getClass((ICPPField) binding);
					if (klass != null) {
						ICPPMethod[] methods = klass.getAllDeclaredMethods();
						for (ICPPMethod m : methods) {
							if (m.getName().equals("operator *"))
								return;
						}
					}
				}
				if (((ICPPField) binding).getVisibility() != ICPPMember.v_public && retType.isMethodPublic) {
					report(RET_PRIVATE_FIELD_ID, expr);
				}
				if (retType.isMethodConst)
					report(RET_FIELD_FROM_CONST_ID, expr);
			}
		}

		private void report(String id, IASTIdExpression expr) {
			if (firstFieldReference != null) {
				reportProblem(id, firstFieldReference.getFieldName(),
						ASTStringUtil.getSimpleName(firstFieldReference.getFieldName()));
				return;
			}
			reportProblem(id, expr, ASTStringUtil.getExpressionString(expr));
		}

		private boolean isPointer(ICPPField binding) {
			if (firstFieldReference != null) {
				IBinding fRef = firstFieldReference.getFieldName().resolveBinding();
				if (fRef instanceof IVariable) {
					IType t = SemanticUtil.getNestedType(((IVariable) fRef).getType(), SemanticUtil.TDEF);
					return t instanceof IPointerType;
				}
				return false;
			} else {
				IType t = SemanticUtil.getNestedType(binding.getType(), SemanticUtil.TDEF);
				return t instanceof IPointerType;
			}
		}

		private ICPPClassType getClass(ICPPField binding) {
			if (firstFieldReference != null) {
				IBinding fRef = firstFieldReference.getFieldName().resolveBinding();
				if (fRef instanceof IVariable) {
					IType t = SemanticUtil.getNestedType(((IVariable) fRef).getType(), SemanticUtil.TDEF);
					if (t instanceof ICPPClassType)
						return (ICPPClassType) t;
				}
				return null;
			} else {
				IType t = SemanticUtil.getNestedType(binding.getType(), SemanticUtil.TDEF);
				if (t instanceof ICPPClassType)
					return (ICPPClassType) t;
			}
			return null;
		}

		private void visit(IASTUnaryExpression expr) {
			innermostOp.push(expr.getOperator());
			visit(expr.getOperand());
			innermostOp.pop();
		}
	}

	class ReturnStmpVisitor extends ASTVisitor {
		private final IASTFunctionDefinition func;
		private final ReturnTypeAnalizer analizer;

		ReturnStmpVisitor(IASTFunctionDefinition func, boolean isOpAssign) {
			shouldVisitStatements = true;
			shouldVisitDeclarations = true;
			shouldVisitExpressions = true;
			this.func = func;
			ReturnInfo info = new ReturnInfo();
			info.mustBeThis = isOpAssign;
			info.isConst = func.getDeclSpecifier().isConst();
			IBinding binding = func.getDeclarator().getName().resolveBinding();
			info.isMethodPublic = false;
			info.isMethodConst = false;
			if (binding instanceof ICPPMethod) {
				ICPPMethod method = (ICPPMethod) binding;
				info.isMethodPublic = method.getVisibility() == ICPPMember.v_public;
			}
			if (func.getDeclarator() instanceof ICPPASTFunctionDeclarator) {
				info.isMethodConst = ((ICPPASTFunctionDeclarator) func.getDeclarator()).isConst();
			}
			IASTPointerOperator[] ptr = func.getDeclarator().getPointerOperators();
			if (ptr.length > 0 && ptr[0] instanceof ICPPASTReferenceOperator) {
				info.type = RET_TYPE.BY_REF;
				analizer = new ReturnTypeAnalizer(info);
			} else if (ptr.length > 0 && ptr[0] instanceof IASTPointer) {
				info.type = RET_TYPE.BY_PTR;
				analizer = new ReturnTypeAnalizer(info);
			} else if (isOpAssign) {
				info.type = RET_TYPE.BY_VALUE;
				analizer = new ReturnTypeAnalizer(info);
			} else
				analizer = null;
		}

		@Override
		public int visit(IASTDeclaration element) {
			if (element != func)
				return PROCESS_SKIP; // skip inner functions
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTExpression expr) {
			if (expr instanceof ICPPASTLambdaExpression) {
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTStatement stmt) {
			if (stmt instanceof IASTReturnStatement) {
				IASTReturnStatement ret = (IASTReturnStatement) stmt;
				IASTInitializerClause returnValue = ret.getReturnArgument();
				ReturnTypeKind returnKind = getReturnTypeKind(func, analizer.getInfo());
				if (returnKind == ReturnTypeKind.NonVoid && !isConstructorDestructor(func)) {
					if (isExplicitReturn(func) && returnValue != null && analizer != null)
						analizer.visit(ret.getReturnValue());
				}
				return PROCESS_SKIP;
			}
			return PROCESS_CONTINUE;
		}
	}

	private static boolean isConstructorDestructor(IASTFunctionDefinition func) {
		if (func instanceof ICPPASTFunctionDefinition) {
			IBinding method = func.getDeclarator().getName().resolveBinding();
			if (method instanceof ICPPConstructor
					|| method instanceof ICPPMethod && ((ICPPMethod) method).isDestructor()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void processFunction(IASTFunctionDefinition func) {
		cachedReturnType = null;
		IASTName declName = func.getDeclarator().getName();
		IASTPointerOperator[] ptr = func.getDeclarator().getPointerOperators();
		if ((ptr.length == 0 || (!(ptr[0] instanceof ICPPASTReferenceOperator) && !(ptr[0] instanceof IASTPointer)))
				&& !CharArrayUtils.equals(declName.getSimpleID(), "operator =".toCharArray()))
			return;
		ReturnStmpVisitor visitor = new ReturnStmpVisitor(func,
				CharArrayUtils.equals(declName.getSimpleID(), "operator =".toCharArray()));
		func.accept(visitor);
	}

	private boolean isExplicitReturn(IASTFunctionDefinition func) {
		IASTDeclSpecifier declSpecifier = func.getDeclSpecifier();
		return !(declSpecifier instanceof IASTSimpleDeclSpecifier
				&& ((IASTSimpleDeclSpecifier) declSpecifier).getType() == IASTSimpleDeclSpecifier.t_unspecified);
	}

	enum ReturnTypeKind {
		Void, NonVoid, Unknown
	}

	/**
	 * Checks if the function has a return type other than void. Constructors and
	 * destructors don't have return type.
	 *
	 * @param func the function to check
	 * @return {@code true} if the function has a non void return type
	 */
	private ReturnTypeKind getReturnTypeKind(IASTFunctionDefinition func, ReturnInfo info) {
		if (isConstructorDestructor(func))
			return ReturnTypeKind.Void;
		IType returnType = getReturnType(func);
		if (info.type == RET_TYPE.BY_VALUE && CPPTemplates.isDependentType(returnType)) {
			// Could instantiate to void or not.
			// If we care to, we could do some more heuristic analysis.
			// For example, if C is a class template, `C<T>` will always be non-void,
			// but `typename C<T>::type` is still unknown.
			return ReturnTypeKind.Unknown;
		}
		return isVoid(returnType) ? ReturnTypeKind.Void : ReturnTypeKind.NonVoid;
	}

	private IType getReturnType(IASTFunctionDefinition func) {
		if (cachedReturnType == null) {
			cachedReturnType = CxxAstUtils.getReturnType(func);
		}
		return cachedReturnType;
	}

	private static boolean isVoid(IType type) {
		return type instanceof IBasicType && ((IBasicType) type).getKind() == IBasicType.Kind.eVoid;
	}
}
