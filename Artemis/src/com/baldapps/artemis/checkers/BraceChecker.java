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
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;

public class BraceChecker extends AbstractIndexAstChecker {
	public static final String MISS_BRACE_ID = "com.baldapps.artemis.checkers.MissBraceProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new OnEachClass());
	}

	class OnEachClass extends ASTVisitor {

		OnEachClass() {
			shouldVisitStatements = true;
		}

		@Override
		public int visit(IASTStatement statement) {
			if (statement instanceof IASTForStatement) {
				if (!(((IASTForStatement) statement).getBody() instanceof IASTCompoundStatement)) {
					reportProblem(MISS_BRACE_ID, statement);
				}
			} else if (statement instanceof IASTWhileStatement) {
				if (!(((IASTWhileStatement) statement).getBody() instanceof IASTCompoundStatement)) {
					reportProblem(MISS_BRACE_ID, statement);
				}
			} else if (statement instanceof IASTDoStatement) {
				if (!(((IASTDoStatement) statement).getBody() instanceof IASTCompoundStatement)) {
					reportProblem(MISS_BRACE_ID, statement);
				}
			} else if (statement instanceof IASTIfStatement) {
				if (!(((IASTIfStatement) statement).getThenClause() instanceof IASTCompoundStatement)) {
					reportProblem(MISS_BRACE_ID, statement);
				}
				IASTStatement elseClause = ((IASTIfStatement) statement).getElseClause();
				if (elseClause == null)
					return PROCESS_CONTINUE;
				if (!(elseClause instanceof IASTCompoundStatement) && !(elseClause instanceof IASTIfStatement)) {
					reportProblem(MISS_BRACE_ID, elseClause);
				}
			}
			return PROCESS_CONTINUE;
		}
	}
}
