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
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

import com.baldapps.artemis.checkers.ArtemisCoreActivator;

public class QuickFixAddBlock extends AbstractArtemisAstRewriteQuickFix {

	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixAddBlock_add_block;
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
		if (astNode == null || !(astNode instanceof IASTStatement)) {
			return;
		}
		ASTRewrite r = ASTRewrite.create(ast);
		INodeFactory factory = ast.getASTNodeFactory();
		IASTCompoundStatement newCompound = factory.newCompoundStatement();
		IASTNode newNode;
		if (astNode instanceof IASTForStatement) {
			newNode = astNode.copy(CopyStyle.withLocations);
			IASTStatement body = ((IASTForStatement) newNode).getBody();
			newCompound.addStatement(body);
			((IASTForStatement) newNode).setBody(newCompound);
		} else if (astNode instanceof IASTDoStatement) {
			newNode = astNode.copy(CopyStyle.withLocations);
			IASTStatement body = ((IASTDoStatement) newNode).getBody();
			newCompound.addStatement(body);
			((IASTDoStatement) newNode).setBody(newCompound);
		} else if (astNode instanceof IASTWhileStatement) {
			newNode = astNode.copy(CopyStyle.withLocations);
			IASTStatement body = ((IASTWhileStatement) newNode).getBody();
			newCompound.addStatement(body);
			((IASTWhileStatement) newNode).setBody(newCompound);
		} else if (astNode instanceof IASTIfStatement) {
			newNode = astNode.copy(CopyStyle.withLocations);
			IASTStatement body = ((IASTIfStatement) newNode).getThenClause();
			newCompound.addStatement(body);
			((IASTIfStatement) newNode).setThenClause(newCompound);
		} else if (astNode instanceof IASTStatement && astNode.getParent() instanceof IASTIfStatement) {
			newNode = astNode.getParent().copy(CopyStyle.withLocations);
			IASTStatement body = ((IASTIfStatement) newNode).getElseClause();
			newCompound.addStatement(body);
			((IASTIfStatement) newNode).setElseClause(newCompound);
			astNode = astNode.getParent();
		} else
			return;
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
