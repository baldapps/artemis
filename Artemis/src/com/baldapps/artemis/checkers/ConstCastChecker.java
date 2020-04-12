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
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

@SuppressWarnings("restriction")
public class ConstCastChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "com.baldapps.artemis.checkers.ConstCastProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		if (ast.getLinkage().getLinkageID() != Linkage.CPP_LINKAGE_ID)
			return;
		ast.accept(new ASTVisitor() {

			{
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTExpression exp) {
				if (exp instanceof ICPPASTCastExpression
						&& ((ICPPASTCastExpression) exp).getOperator() == ICPPASTCastExpression.op_const_cast) {
					ICPPASTCastExpression constCast = (ICPPASTCastExpression) exp;
					IASTTypeId typeId = constCast.getTypeId();
					IASTExpression op = constCast.getOperand();
					IType type = op.getExpressionType();
					IASTDeclSpecifier spec = typeId.getDeclSpecifier();
					if (!spec.isConst() && SemanticUtil.isConst(type)) {
						reportProblem(ERR_ID, exp);
					} else if (!spec.isVolatile() && SemanticUtil.isVolatile(type)) {
						reportProblem(ERR_ID, exp);
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
