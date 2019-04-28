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
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

@SuppressWarnings("restriction")
public class ForLoopChecker extends AbstractIndexAstChecker {
	public static final String FLOAT_COUNTER_ID = "com.baldapps.artemis.checkers.FloatCounterProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTForStatement) {
					IType type = ((IASTForStatement) statement).getIterationExpression().getExpressionType();
					if (isFloat(type)) {
						reportProblem(FLOAT_COUNTER_ID, statement);
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	private boolean isFloat(IType type) {
		type = SemanticUtil.getUltimateType(type, true);
		if (!(type instanceof IBasicType)) {
			return false;
		}
		IBasicType.Kind k = ((IBasicType) type).getKind();
		switch (k) {
		case eFloat:
		case eDouble:
		case eFloat128:
			return true;
		default:
			return false;
		}
	}
}
