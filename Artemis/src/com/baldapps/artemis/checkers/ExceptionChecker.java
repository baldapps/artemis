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

import org.eclipse.cdt.codan.core.cxx.model.AbstractAstFunctionChecker;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

@SuppressWarnings("restriction")
public class ExceptionChecker extends AbstractAstFunctionChecker {
	public static final String AVOID_THROWS_ID = "com.baldapps.artemis.checkers.ExceptionProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		if (ast.getLinkage().getLinkageID() == Linkage.CPP_LINKAGE_ID) {
			super.processAst(ast);
		}
	}

	@Override
	protected void processFunction(IASTFunctionDefinition func) {
		if (func.getDeclarator() instanceof ICPPASTFunctionDeclarator) {
			ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) func.getDeclarator();
			IASTTypeId[] excp = dtor.getExceptionSpecification();
			if (excp != ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION) {
				reportProblem(AVOID_THROWS_ID, dtor, ASTStringUtil.getSimpleName(dtor.getName()));
			}
		}
	}
}
