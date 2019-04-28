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

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;

@SuppressWarnings("restriction")
public class TryCatchStatementChecker extends AbstractIndexAstChecker {
	public static final String CATCH_ALL_ID = "com.baldapps.artemis.checkers.CatchAllProblem"; //$NON-NLS-1$
	public static final String CATCH_ALL_ORDER_ID = "com.baldapps.artemis.checkers.CatchAllOrderProblem"; //$NON-NLS-1$
	public static final String CATCH_EMPTY_ID = "com.baldapps.artemis.checkers.CatchEmptyProblem"; //$NON-NLS-1$
	public static final String EMPTY_THROW_ID = "com.baldapps.artemis.checkers.EmptyThrowProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof ICPPASTTryBlockStatement) {
					ICPPASTTryBlockStatement tryBlock = (ICPPASTTryBlockStatement) statement;
					ICPPASTCatchHandler[] catches = tryBlock.getCatchHandlers();
					int catchAllIdx = -1;
					if (catches.length == 1 && catches[0].isCatchAll()) {
						reportProblem(CATCH_ALL_ID, catches[0]);
						IASTStatement body = catches[0].getCatchBody();
						if (body.getChildren().length == 0) {
							reportProblem(CATCH_EMPTY_ID, catches[0]);
						}
						return PROCESS_CONTINUE;
					}
					for (int i = 0; i < catches.length; ++i) {
						if (catches[i].isCatchAll()) {
							catchAllIdx = i;
						}
						IASTStatement body = catches[i].getCatchBody();
						if (body.getChildren().length == 0) {
							reportProblem(CATCH_EMPTY_ID, catches[i]);
						}
					}
					if (catchAllIdx >= 0 && !catches[catches.length - 1].isCatchAll())
						reportProblem(CATCH_ALL_ORDER_ID, catches[catchAllIdx]);
				} else if (statement instanceof IASTExpressionStatement) {
					if (((IASTExpressionStatement) statement).getExpression() instanceof IASTUnaryExpression) {
						IASTUnaryExpression unary = (IASTUnaryExpression) ((IASTExpressionStatement) statement)
								.getExpression();
						if (unary.getOperator() == IASTUnaryExpression.op_throw && unary.getOperand() == null) {
							ICPPASTCatchHandler catchHandler = ASTQueries.findAncestorWithType(unary,
									ICPPASTCatchHandler.class);
							if (catchHandler == null)
								reportProblem(EMPTY_THROW_ID, unary);
						}
					}
					return PROCESS_SKIP;
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
