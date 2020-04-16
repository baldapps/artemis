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
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class NullStatementsChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "com.baldapps.artemis.checkers.NullStatementsProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (enclosedInMacroExpansion(statement))
					return PROCESS_CONTINUE;
				if (statement instanceof IASTNullStatement) {
					IASTNode parent = statement.getParent();
					if (parent instanceof IASTIfStatement)
						return PROCESS_CONTINUE;
					if (parent instanceof IASTForStatement) {
						if (((IASTForStatement) parent).getInitializerStatement() == statement) {
							return PROCESS_CONTINUE;
						}
					}
					IASTFileLocation loc = statement.getFileLocation();
					if (loc == null)
						return PROCESS_CONTINUE;
					IASTNodeSelector selector = ast.getNodeSelector(ast.getFilePath());
					IASTPreprocessorMacroExpansion macro = selector
							.findEnclosingMacroExpansion(loc.asFileLocation().getNodeOffset() - 1, 1);
					if (macro != null) {
						String exp = macro.getMacroDefinition().getExpansion();
						if (exp.isEmpty() || exp.endsWith(";"))
							return PROCESS_CONTINUE;
					}
					reportProblem(ERR_ID, statement);
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
