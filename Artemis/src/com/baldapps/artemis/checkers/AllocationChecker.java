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

import java.util.Arrays;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ValueFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

@SuppressWarnings("restriction")
public class AllocationChecker extends AbstractIndexAstChecker {
	public static final String DYNAMIC_ARRAY_ID = "com.baldapps.artemis.checkers.DynamicArrayProblem"; //$NON-NLS-1$
	public static final String POINTER_RESET_ID = "com.baldapps.artemis.checkers.PointerResetProblem"; //$NON-NLS-1$
	public static final String DELETE_THIS_ID = "com.baldapps.artemis.checkers.DeleteThisProblem"; //$NON-NLS-1$
	public static final String DELETE_VOID_ID = "com.baldapps.artemis.checkers.DeleteVoidProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new OnEachClass());
	}

	private static class Context {
		IASTFunctionDefinition functionDefinition;
		IASTStatement currentStatement;
		IASTExpression latestDeletionOp;
		char[] latestDeletionOpName;
		boolean checkNextExpr;
		boolean changedStatement;

		public Context() {
			reset();
		}

		public void reset() {
			latestDeletionOp = null;
			checkNextExpr = false;
			functionDefinition = null;
			currentStatement = null;
			changedStatement = false;
		}
	}

	class OnEachClass extends ASTVisitor {
		private Context currentContext;

		public Context getCurrentContext() {
			return currentContext;
		}

		OnEachClass() {
			shouldVisitExpressions = true;
			shouldVisitDeclarations = true;
			shouldVisitStatements = true;
			currentContext = new Context();
		}

		@Override
		public int leave(IASTDeclaration declaration) {
			if (declaration instanceof IASTFunctionDefinition && currentContext.checkNextExpr
					&& declaration.equals(currentContext.functionDefinition)) {
				reportProblem(POINTER_RESET_ID, currentContext.latestDeletionOp,
						ASTStringUtil.getExpressionString(currentContext.latestDeletionOp));
				currentContext.reset();
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTStatement statement) {
			if (statement.equals(currentContext.currentStatement)) {
				currentContext.changedStatement = true;
			}
			return PROCESS_CONTINUE;
		}

		private boolean isVoid(IType type) {
			type = SemanticUtil.getUltimateType(type, true);
			if (!(type instanceof IBasicType)) {
				return false;
			}
			IBasicType.Kind k = ((IBasicType) type).getKind();
			switch (k) {
			case eVoid:
				return true;
			default:
				return false;
			}
		}

		@Override
		public int visit(IASTExpression expression) {
			verifyCurrentContext(expression, currentContext);
			if (expression instanceof ICPPASTNewExpression) {
				ICPPASTNewExpression newExpr = (ICPPASTNewExpression) expression;
				if (newExpr.isArrayAllocation()) {
					reportProblem(DYNAMIC_ARRAY_ID, expression);
				}
			} else if (expression instanceof ICPPASTDeleteExpression) {
				ICPPASTDeleteExpression deleteExpr = (ICPPASTDeleteExpression) expression;
				if (referencesThis(deleteExpr.getOperand())) {
					reportProblem(DELETE_THIS_ID, expression);
				} else {
					if (isVoid(deleteExpr.getOperand().getExpressionType())) {
						reportProblem(DELETE_VOID_ID, expression,
								ASTStringUtil.getExpressionString(deleteExpr.getOperand()));
					}
					currentContext.functionDefinition = ASTQueries.findAncestorWithType(deleteExpr,
							IASTFunctionDefinition.class);
					if (currentContext.functionDefinition != null) {
						IASTExpression operand = deleteExpr.getOperand();
						if (operand instanceof IASTIdExpression)
							currentContext.latestDeletionOpName = ((IASTIdExpression) operand).getName().getSimpleID();
						else if (operand instanceof IASTFieldReference)
							currentContext.latestDeletionOpName = ((IASTFieldReference) operand).getFieldName()
									.getSimpleID();
						else {
							currentContext.reset();
							return PROCESS_CONTINUE;
						}
						currentContext.latestDeletionOp = deleteExpr.getOperand();
						currentContext.currentStatement = ASTQueries.findAncestorWithType(deleteExpr,
								IASTStatement.class);
						currentContext.checkNextExpr = true;
						return PROCESS_CONTINUE;
					}
				}
			}
			return PROCESS_CONTINUE;
		}
	}

	/**
	 * Checks whether expression references this (directly, by pointer or by
	 * reference)
	 */
	private boolean referencesThis(IASTNode expr) {
		if (expr instanceof IASTLiteralExpression) {
			IASTLiteralExpression litArg = (IASTLiteralExpression) expr;
			if (litArg.getKind() == IASTLiteralExpression.lk_this) {
				return true;
			}
		} else if (expr instanceof ICPPASTUnaryExpression) {
			ICPPASTUnaryExpression unExpr = (ICPPASTUnaryExpression) expr;
			switch (unExpr.getOperator()) {
			case IASTUnaryExpression.op_amper:
			case IASTUnaryExpression.op_star:
			case IASTUnaryExpression.op_bracketedPrimary:
				return referencesThis(unExpr.getOperand());
			}
		}
		return false;
	}

	private void verifyCurrentContext(IASTExpression expression, Context currentContext) {
		if (currentContext != null && currentContext.checkNextExpr && currentContext.changedStatement) {
			if (expression instanceof IASTBinaryExpression) {
				IASTBinaryExpression binary = (IASTBinaryExpression) expression;
				if (binary.getOperator() != IASTBinaryExpression.op_assign) {
					reportProblem(POINTER_RESET_ID, currentContext.latestDeletionOp,
							ASTStringUtil.getExpressionString(currentContext.latestDeletionOp));
					currentContext.reset();
					return;
				}
				IASTExpression operand = binary.getOperand1();
				if (operand instanceof IASTIdExpression)
					if (!Arrays.equals(currentContext.latestDeletionOpName,
							((IASTIdExpression) operand).getName().getSimpleID())) {
						reportProblem(POINTER_RESET_ID, currentContext.latestDeletionOp,
								ASTStringUtil.getExpressionString(currentContext.latestDeletionOp));
						currentContext.reset();
						return;
					} else if (operand instanceof IASTFieldReference)
						if (!Arrays.equals(currentContext.latestDeletionOpName,
								((IASTFieldReference) operand).getFieldName().getSimpleID())) {
							reportProblem(POINTER_RESET_ID, currentContext.latestDeletionOp,
									ASTStringUtil.getExpressionString(currentContext.latestDeletionOp));
							currentContext.reset();
							return;
						} else {
							reportProblem(POINTER_RESET_ID, currentContext.latestDeletionOp,
									ASTStringUtil.getExpressionString(currentContext.latestDeletionOp));
							currentContext.reset();
							return;
						}
				IASTExpression op2 = binary.getOperand2();
				if (op2 instanceof IASTLiteralExpression) {
					IASTLiteralExpression li = (IASTLiteralExpression) op2;
					if (li.getKind() != IASTLiteralExpression.lk_integer_constant) {
						if (li.getKind() == IASTLiteralExpression.lk_nullptr) {
							currentContext.reset();
							return;
						}
						reportProblem(POINTER_RESET_ID, currentContext.latestDeletionOp,
								ASTStringUtil.getExpressionString(currentContext.latestDeletionOp));
						currentContext.reset();
						return;
					}
					Number n = ValueFactory.getConstantNumericalValue(li);
					if (n.longValue() != 0) {
						reportProblem(POINTER_RESET_ID, currentContext.latestDeletionOp,
								ASTStringUtil.getExpressionString(currentContext.latestDeletionOp));
						currentContext.reset();
						return;
					}
				}
			} else {
				reportProblem(POINTER_RESET_ID, currentContext.latestDeletionOp,
						ASTStringUtil.getExpressionString(currentContext.latestDeletionOp));
			}
			currentContext.reset();
		}
	}
}
