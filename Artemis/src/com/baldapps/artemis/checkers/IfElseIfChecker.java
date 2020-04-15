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

import java.util.Stack;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class IfElseIfChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "com.baldapps.artemis.checkers.IfElseIfProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new IfElseIfVisitor());
	}

	private static class Context {
		public boolean exitFound;
		public IASTNode faultyIf;
	}

	class IfElseIfVisitor extends ASTVisitor {

		private final Stack<Context> ifStack = new Stack<>();

		IfElseIfVisitor() {
			shouldVisitStatements = true;
		}

		@Override
		public int visit(IASTStatement statement) {
			if (statement instanceof IASTIfStatement) {
				IASTStatement elseClause = ((IASTIfStatement) statement).getElseClause();
				if (elseClause == null && !ifStack.isEmpty()) {
					ifStack.peek().faultyIf = statement;
					return PROCESS_SKIP;
				}
				Context c = new Context();
				ifStack.push(c);
			} else if (!ifStack.empty()) {
				Context c = ifStack.peek();
				if (!c.exitFound)
					c.exitFound = isBreakOrExitStatement(statement);
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTStatement statement) {
			if (statement instanceof IASTIfStatement && !ifStack.empty()) {
				Context c = ifStack.pop();
				if (!ifStack.isEmpty()) {
					ifStack.peek().exitFound |= c.exitFound;
					ifStack.peek().faultyIf = c.faultyIf;
				} else if (!c.exitFound && c.faultyIf != null) {
					reportProblem(ERR_ID, c.faultyIf);
				}
			}
			return PROCESS_CONTINUE;
		}
	}

	private boolean isBreakOrExitStatement(IASTStatement statement) {
		return (statement instanceof IASTBreakStatement) || statement instanceof IASTContinueStatement
				|| isExitStatement(statement);
	}

	private boolean isExitStatement(IASTStatement statement) {
		return statement instanceof IASTReturnStatement || statement instanceof IASTGotoStatement
				|| CxxAstUtils.isThrowStatement(statement) || CxxAstUtils.isExitStatement(statement);
	}
}
