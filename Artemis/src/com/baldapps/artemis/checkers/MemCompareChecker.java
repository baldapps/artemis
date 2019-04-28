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
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

@SuppressWarnings("restriction")
public class MemCompareChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "com.baldapps.artemis.checkers.CompareCompositeTypesProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			private boolean checkArg(IASTInitializerClause arg, IASTExpression expression) {
				if (!(arg instanceof IASTExpression))
					return false;
				IType type = ((IASTExpression) arg).getExpressionType();
				type = SemanticUtil.getUltimateType(type, true);
				if (type instanceof ICompositeType) {
					reportProblem(ERR_ID, expression);
					return true;
				}
				return false;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTFunctionCallExpression) {
					IASTFunctionCallExpression fCall = (IASTFunctionCallExpression) expression;
					IASTExpression fNameExp = fCall.getFunctionNameExpression();
					IBinding fBinding = null;
					if (fNameExp instanceof IASTIdExpression) {
						IASTIdExpression fName = (IASTIdExpression) fNameExp;
						fBinding = fName.getName().resolveBinding();
					}
					if (fBinding != null && fBinding instanceof IFunction && "memcmp".equals(fBinding.getName())) {
						IASTInitializerClause[] args = fCall.getArguments();
						if (args.length < 3)
							return PROCESS_SKIP;
						boolean found = checkArg(args[0], expression);
						if (!found)
							checkArg(args[1], expression);
					}
					return PROCESS_SKIP;
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
