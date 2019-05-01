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
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

@SuppressWarnings("restriction")
public class FunctionChecker extends AbstractAstFunctionChecker {
	public static final String PAR_BY_COPY_ID = "com.baldapps.artemis.checkers.ParByCopyProblem"; //$NON-NLS-1$

	@Override
	protected void processFunction(IASTFunctionDefinition func) {
		IASTFunctionDeclarator declarator = func.getDeclarator();
		if (declarator instanceof IASTStandardFunctionDeclarator) {
			IASTParameterDeclaration[] parans = ((IASTStandardFunctionDeclarator) declarator).getParameters();
			for (IASTParameterDeclaration p : parans) {
				IASTPointerOperator[] pointers = p.getDeclarator().getPointerOperators();
				IASTDeclSpecifier spec = p.getDeclSpecifier();
				if (!(spec instanceof IASTSimpleDeclSpecifier) && pointers.length == 0)
					reportProblem(PAR_BY_COPY_ID, p, ASTStringUtil.getSignatureString(p.getDeclarator()));
			}
		}
	}
}
