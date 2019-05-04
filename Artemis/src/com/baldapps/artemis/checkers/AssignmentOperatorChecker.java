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
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

import com.baldapps.artemis.utils.SemanticUtils;

public class AssignmentOperatorChecker extends AbstractIndexAstChecker {
	public static final String MISS_REF_ID = "com.baldapps.artemis.checkers.MissReferenceProblem"; //$NON-NLS-1$
	public static final String MISS_SELF_CHECK_ID = "com.baldapps.artemis.checkers.MissSelfCheckProblem"; //$NON-NLS-1$
	private static final String OPERATOR_ASSIGN = "operator ="; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new OnEachClass());
	}

	class OnEachClass extends ASTVisitor {
		private IASTDeclaration decl;
		private ICPPASTParameterDeclaration[] parameters;

		OnEachClass() {
			shouldVisitDeclarations = true;
			shouldVisitExpressions = true;
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			ICPPMethod method = getOperatorEq(declaration);
			if (method != null) {
				IASTDeclarator declMethod = ((ICPPASTFunctionDefinition) declaration).getDeclarator();
				if (!(declMethod instanceof ICPPASTFunctionDeclarator))
					return PROCESS_SKIP;
				IASTPointerOperator[] pointers = declMethod.getPointerOperators();
				parameters = ((ICPPASTFunctionDeclarator) declMethod).getParameters();
				if (pointers.length != 1 || !(pointers[0] instanceof ICPPASTReferenceOperator))
					reportProblem(MISS_REF_ID, declaration);

				if (!SemanticUtils.isCopyOrMoveAssignmentOperator(method))
					return PROCESS_SKIP;

				decl = declaration;
				return PROCESS_CONTINUE;
			} else
				return PROCESS_SKIP;
		}

		@Override
		public int leave(IASTDeclaration declaration) {
			if (getOperatorEq(declaration) != null) {
				decl = null;
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTExpression expression) {
			if (decl == null)
				return PROCESS_SKIP;
			if (expression instanceof IASTBinaryExpression) {
				IASTBinaryExpression binary = (IASTBinaryExpression) expression;
				if ((binary.getOperator() == IASTBinaryExpression.op_equals
						|| binary.getOperator() == IASTBinaryExpression.op_notequals)) {
					if (SemanticUtils.referencesThis(binary.getOperand1())
							&& referencesParameter(binary.getOperand2())) {
						decl = null;
						return PROCESS_SKIP;
					} else if (SemanticUtils.referencesThis(binary.getOperand2())
							&& referencesParameter(binary.getOperand1())) {
						decl = null;
						return PROCESS_SKIP;
					} else {
						reportProblem(MISS_SELF_CHECK_ID, decl);
					}
				} else {
					reportProblem(MISS_SELF_CHECK_ID, decl);
				}
				decl = null;
				return PROCESS_SKIP;
			} else {
				reportProblem(MISS_SELF_CHECK_ID, decl);
				decl = null;
				return PROCESS_SKIP;
			}
		}

		/**
		 * Checks whether expression references the parameter (directly, by pointer or by reference)
		 */
		public boolean referencesParameter(IASTNode expr) {
			if (expr instanceof IASTIdExpression) {
				IASTIdExpression id = (IASTIdExpression) expr;
				if (Arrays.equals(id.getName().getSimpleID(), parameters[0].getDeclarator().getName().getSimpleID())) {
					return true;
				}
			} else if (expr instanceof ICPPASTUnaryExpression) {
				ICPPASTUnaryExpression unExpr = (ICPPASTUnaryExpression) expr;
				switch (unExpr.getOperator()) {
				case IASTUnaryExpression.op_amper:
				case IASTUnaryExpression.op_star:
				case IASTUnaryExpression.op_bracketedPrimary:
					return referencesParameter(unExpr.getOperand());
				}
			}
			return false;
		}

		private ICPPMethod getOperatorEq(IASTDeclaration decl) {
			if (decl instanceof ICPPASTFunctionDefinition) {
				ICPPASTFunctionDefinition functionDefinition = (ICPPASTFunctionDefinition) decl;
				if (functionDefinition.isDeleted())
					return null;
				IBinding binding = functionDefinition.getDeclarator().getName().resolveBinding();
				if (binding instanceof ICPPMethod) {
					ICPPMethod method = (ICPPMethod) binding;
					if (OPERATOR_ASSIGN.equals(method.getName()))
						return method;
				}
			}
			return null;
		}
	}
}
