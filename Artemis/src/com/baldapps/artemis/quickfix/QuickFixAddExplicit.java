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

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

import com.baldapps.artemis.checkers.ArtemisCoreActivator;

@SuppressWarnings("restriction")
public class QuickFixAddExplicit extends AbstractArtemisAstRewriteQuickFix {

	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixAddExplicit_add_explicit;
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
		if (astNode == null || !(astNode instanceof IASTName)) {
			return;
		}
		IASTSimpleDeclaration declaration = ASTQueries.findAncestorWithType(astNode, IASTSimpleDeclaration.class);
		if (declaration.getDeclarators().length == 0
				|| !(declaration.getDeclarators()[0] instanceof ICPPASTFunctionDeclarator))
			return;
		ASTRewrite r = ASTRewrite.create(ast);
		INodeFactory factory = ast.getASTNodeFactory();
		ICPPASTDeclSpecifier spec = (ICPPASTDeclSpecifier) declaration.getDeclSpecifier().copy(CopyStyle.withLocations);
		spec.setExplicit(true);
		IASTSimpleDeclaration newNode = factory.newSimpleDeclaration(spec);
		newNode.addDeclarator(declaration.getDeclarators()[0].copy(CopyStyle.withLocations));
		r.replace(declaration, newNode, null);
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
