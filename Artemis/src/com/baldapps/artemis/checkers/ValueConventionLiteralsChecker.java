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

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class ValueConventionLiteralsChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "com.baldapps.artemis.checkers.ValueConventionLiteralsProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTLiteralExpression) {
					/**
					 * Check if the expression is already inside a macro, if so it's ok
					 */
					if (enclosedInMacroExpansion(expression))
						return PROCESS_CONTINUE;
					IASTLiteralExpression literal = (IASTLiteralExpression) expression;
					int kind = literal.getKind();
					switch (kind) {
					case IASTLiteralExpression.lk_float_constant:
						if (isLowerCase(literal.getValue())) {
							reportProblem(ERR_ID, expression, literal.getValue());
						}
						break;
					case IASTLiteralExpression.lk_integer_constant:
						if (isLowerCase(literal.getValue())) {
							reportProblem(ERR_ID, expression, literal.getValue());
						}
						break;
					default:
						return PROCESS_CONTINUE;
					}
				}
				return PROCESS_CONTINUE;
			}

			private boolean isLowerCase(char[] value) {
				int len = value.length;
				for (int i = 0; i < len; ++i) {
					if (!Character.isDigit(value[i]) && Character.isLowerCase(value[i]))
						return true;
				}
				return false;
			}
		});
	}
}