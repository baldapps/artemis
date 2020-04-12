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
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

@SuppressWarnings("restriction")
public class StructsChecker extends AbstractIndexAstChecker {
	public static final String AVOID_STRUCTS_ID = "com.baldapps.artemis.checkers.AvoidStructsProblem"; //$NON-NLS-1$
	public static final String AVOID_UNIONS_ID = "com.baldapps.artemis.checkers.AvoidUnionsProblem"; //$NON-NLS-1$
	public static final String AVOID_VIRTUAL_BASES_ID = "com.baldapps.artemis.checkers.AvoidVirtualBasesProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		if (ast.getLinkage().getLinkageID() == Linkage.CPP_LINKAGE_ID)
			ast.accept(new NodeVisitor());
	}

	private class NodeVisitor extends ASTVisitor {

		public NodeVisitor() {
			shouldVisitDeclSpecifiers = true;
		}

		@Override
		public int visit(IASTDeclSpecifier element) {
			if (element instanceof ICPPASTCompositeTypeSpecifier) {
				ICPPASTCompositeTypeSpecifier s = (ICPPASTCompositeTypeSpecifier) element;
				if (s.getKey() == ICPPASTCompositeTypeSpecifier.k_struct) {
					reportProblem(AVOID_STRUCTS_ID, element, ASTStringUtil.getSimpleName(s.getName()));
				} else if (s.getKey() == ICPPASTCompositeTypeSpecifier.k_union) {
					reportProblem(AVOID_UNIONS_ID, element, ASTStringUtil.getSimpleName(s.getName()));
				}
				for (ICPPASTBaseSpecifier base : s.getBaseSpecifiers()) {
					if (base.isVirtual()) {
						reportProblem(AVOID_VIRTUAL_BASES_ID, base, new String(base.getNameSpecifier().toCharArray()));
					}
				}
			} else if (element instanceof ICPPASTElaboratedTypeSpecifier) {
				ICPPASTElaboratedTypeSpecifier s = (ICPPASTElaboratedTypeSpecifier) element;
				if (s.getKind() == ICPPASTElaboratedTypeSpecifier.k_struct) {
					reportProblem(AVOID_STRUCTS_ID, element, ASTStringUtil.getSimpleName(s.getName()));
				} else if (s.getKind() == ICPPASTElaboratedTypeSpecifier.k_union) {
					reportProblem(AVOID_UNIONS_ID, element, ASTStringUtil.getSimpleName(s.getName()));
				}
			}
			return PROCESS_CONTINUE;
		}
	}
}
