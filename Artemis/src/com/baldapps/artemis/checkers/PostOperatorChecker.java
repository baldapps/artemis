/*******************************************************************************
 * Copyright (c) 2020 Marco Stornelli
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

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;

public class PostOperatorChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "com.baldapps.artemis.checkers.PostOperatorProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {

			{
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTExpression exp) {
				if (exp instanceof IASTUnaryExpression
						&& (((IASTUnaryExpression) exp).getOperator() == IASTUnaryExpression.op_postFixDecr
								|| ((IASTUnaryExpression) exp).getOperator() == IASTUnaryExpression.op_postFixIncr)) {
					reportProblem(ERR_ID, exp);
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
