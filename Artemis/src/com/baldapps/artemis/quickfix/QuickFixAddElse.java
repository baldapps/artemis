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
package com.baldapps.artemis.quickfix;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

import com.baldapps.artemis.checkers.ArtemisCoreActivator;

public class QuickFixAddElse extends AbstractArtemisAstRewriteQuickFix {

	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixAddElse_add_else;
	}

	@Override
	public void modifyAST(IIndex index, IMarker marker) {
		IASTTranslationUnit ast;
		try {
			ITranslationUnit tu = getTranslationUnitViaEditor(marker);
			ast = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
		} catch (CoreException e) {
			ArtemisCoreActivator.log(e);
			return;
		}
		IASTNode astNode = null;
		if (isCodanProblem(marker)) {
			astNode = getASTNodeFromMarker(marker, ast);
		}
		if (astNode == null || !(astNode instanceof IASTIfStatement)) {
			return;
		}
		ASTRewrite r = ASTRewrite.create(ast);
		INodeFactory factory = ast.getASTNodeFactory();
		IASTCompoundStatement newCompound = factory.newCompoundStatement();
		IASTNode newNode = astNode.copy(CopyStyle.withLocations);
		((IASTIfStatement) newNode).setElseClause(newCompound);
		r.replace(astNode, newNode, null);
		Change c = r.rewriteAST();
		try {
			c.perform(new NullProgressMonitor());
		} catch (CoreException e) {
			ArtemisCoreActivator.log(e);
			return;
		}
		try {
			marker.delete();
		} catch (CoreException e) {
			ArtemisCoreActivator.log(e);
		}
	}
}
