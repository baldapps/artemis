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
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;

public class LambdaExpressionChecker extends AbstractIndexAstChecker {
	public static final String LAMBDA_CAPTURE_ID = "com.baldapps.artemis.checkers.LambdaCaptureProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof ICPPASTLambdaExpression) {
					ICPPASTLambdaExpression lambda = (ICPPASTLambdaExpression) expression;
					if (lambda.getCaptureDefault() != ICPPASTLambdaExpression.CaptureDefault.UNSPECIFIED)
						reportProblem(LAMBDA_CAPTURE_ID, expression);
					return PROCESS_SKIP;
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
