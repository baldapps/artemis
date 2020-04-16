/*******************************************************************************
 * Copyright (c) 2020 Marco Stornelli
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
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class EnumChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "com.baldapps.artemis.checkers.EnumInitProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitDeclSpecifiers = true;
			}

			@Override
			public int visit(IASTDeclSpecifier decl) {
				if (decl instanceof IASTEnumerationSpecifier) {
					IASTEnumerator[] values = ((IASTEnumerationSpecifier) decl).getEnumerators();
					if (values.length <= 1)
						return PROCESS_CONTINUE;
					IASTExpression firstExpr = values[0].getValue();
					if (values.length == 2) {
						if (values[1].getValue() != null && firstExpr == null) {
							reportProblem(ERR_ID, decl, ((IASTEnumerationSpecifier) decl).getName());
						}
						return PROCESS_CONTINUE;
					}
					boolean allWithValue = true;
					boolean allWithNoValue = true;
					for (int i = 1; i < values.length; ++i) {
						allWithValue &= values[i].getValue() != null;
						allWithNoValue &= values[i].getValue() == null;
					}
					if (firstExpr == null && !allWithNoValue) {
						reportProblem(ERR_ID, decl, ((IASTEnumerationSpecifier) decl).getName());
					} else if (firstExpr == null || (!allWithValue && !allWithNoValue)) {
						reportProblem(ERR_ID, decl, ((IASTEnumerationSpecifier) decl).getName());
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
