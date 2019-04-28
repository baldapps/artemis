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
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

public class NamespaceChecker extends AbstractIndexAstChecker {
	public static final String STD_NAMESPACE_ID = "com.baldapps.artemis.checkers.StdNamespaceProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitNamespaces = true;
			}

			@Override
			public int visit(ICPPASTNamespaceDefinition declaration) {
				IASTName name = declaration.getName();
				if (CharArrayUtils.equals(name.getSimpleID(), "std".toCharArray())) {
					reportProblem(STD_NAMESPACE_ID, declaration);
					return PROCESS_SKIP;
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
