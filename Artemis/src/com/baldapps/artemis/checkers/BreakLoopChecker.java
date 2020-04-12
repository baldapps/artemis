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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;

public class BreakLoopChecker extends AbstractIndexAstChecker {
	public static final String BREAK_IN_LOOP = "com.baldapps.artemis.checkers.BreakProblem"; //$NON-NLS-1$
	public static final String MORE_THAN_ONE_BREAK_IN_LOOP = "com.baldapps.artemis.checkers.MoreThanOneBreakGotoInLoopProblem"; //$NON-NLS-1$

	private class BreakFinderVisitor extends ASTVisitor {

		private List<IASTNode> nodes = new ArrayList<>();

		public BreakFinderVisitor() {
			shouldVisitStatements = true;
		}

		public List<IASTNode> getList() {
			return nodes;
		}

		@Override
		public int visit(IASTStatement statement) {
			if (statement instanceof IASTBreakStatement) {
				reportProblem(BREAK_IN_LOOP, statement);
				nodes.add(statement);
			} else if (statement instanceof IASTGotoStatement) {
				nodes.add(statement);
			}
			return PROCESS_CONTINUE;
		}
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTForStatement || statement instanceof IASTDoStatement
						|| statement instanceof IASTWhileStatement) {
					BreakFinderVisitor visitor = new BreakFinderVisitor();
					statement.accept(visitor);
					List<IASTNode> nodes = visitor.getList();
					if (nodes.size() > 1) {
						for (IASTNode n : nodes) {
							reportProblem(MORE_THAN_ONE_BREAK_IN_LOOP, n);
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
