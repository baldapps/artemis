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
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

public class AutoPtrChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "com.baldapps.artemis.checkers.AutoPtrProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitDeclSpecifiers = true;
			}

			@Override
			public int visit(IASTDeclSpecifier declSpecifier) {
				if (declSpecifier instanceof ICPPASTNamedTypeSpecifier) {
					ICPPASTNamedTypeSpecifier t = (ICPPASTNamedTypeSpecifier) declSpecifier;
					IASTName name = t.getName();
					if (name instanceof ICPPASTQualifiedName) {
						ICPPASTQualifiedName qual = (ICPPASTQualifiedName) name;
						if (qual.getQualifier().length == 1
								&& CharArrayUtils.equals(qual.getQualifier()[0].toCharArray(), "std".toCharArray())
								&& CharArrayUtils.equals(qual.getLastName().getSimpleID(), "auto_ptr".toCharArray())) {
							reportProblem(ERR_ID, declSpecifier);
						}
					} else {
						IBinding binding = name.resolveBinding();
						if (binding instanceof ICPPBinding) {
							try {
								String fully = String.join("::", ((ICPPBinding) binding).getQualifiedName());
								if ("std::auto_ptr".equals(fully)) {
									reportProblem(ERR_ID, declSpecifier);
								}
							} catch (DOMException e) {
								ArtemisCoreActivator.log(e);
							}
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
