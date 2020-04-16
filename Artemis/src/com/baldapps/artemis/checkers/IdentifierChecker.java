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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

@SuppressWarnings("restriction")
public class IdentifierChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "com.baldapps.artemis.checkers.IdentifierProblem"; //$NON-NLS-1$
	private static final int MAX_NAME = 31;

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitNames = true;
			}

			@Override
			public int visit(IASTName name) {
				if (name.getSimpleID().length > MAX_NAME) {
					reportProblem(ERR_ID, name, ASTStringUtil.getSimpleName(name));
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
