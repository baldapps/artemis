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

import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

import com.baldapps.artemis.checkers.ArtemisCoreActivator;

@SuppressWarnings("restriction")
public class QuickFixAddNoexcept extends AbstractArtemisAstRewriteQuickFix {

	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixAddNoexcept_change_to_noexcept;
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
		if (astNode == null || !(astNode instanceof ICPPASTFunctionDeclarator)) {
			return;
		}
		ASTRewrite r = ASTRewrite.create(ast);
		INodeFactory factory = ast.getASTNodeFactory();
		ICPPASTFunctionDeclarator origDtor = (ICPPASTFunctionDeclarator) astNode;
		ICPPASTFunctionDeclarator newDtor = (ICPPASTFunctionDeclarator) factory
				.newFunctionDeclarator(origDtor.getName().copy(CopyStyle.withLocations));
		IASTLiteralExpression falseLit = null;
		if (origDtor.getExceptionSpecification() == IASTTypeId.EMPTY_TYPEID_ARRAY) {
			falseLit = ICPPASTFunctionDeclarator.NOEXCEPT_DEFAULT;
		} else {
			falseLit = factory.newLiteralExpression(IASTLiteralExpression.lk_false, "false");
		}
		newDtor.setNoexceptExpression((ICPPASTExpression) falseLit);
		r.replace(origDtor, newDtor, null);
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
