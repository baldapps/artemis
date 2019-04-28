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

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
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

public abstract class AbstractQuickFixPointerToZero extends AbstractArtemisAstRewriteQuickFix {

	protected abstract String getNullString();

	protected abstract int getNullKind();

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
		IASTName astName;
		if (isCodanProblem(marker)) {
			astName = getASTNameFromMarker(marker, ast);
		} else {
			astName = getAstNameFromProblemArgument(marker, ast, 0);
		}
		if (astName == null) {
			return;
		}
		ASTRewrite r = ASTRewrite.create(ast);
		INodeFactory factory = ast.getASTNodeFactory();
		IASTLiteralExpression literal = factory.newLiteralExpression(getNullKind(), getNullString());
		IASTIdExpression id = factory.newIdExpression(astName.copy(CopyStyle.withLocations));
		IASTBinaryExpression binary = factory.newBinaryExpression(IASTBinaryExpression.op_assign, id, literal);
		IASTExpressionStatement newStatement = factory.newExpressionStatement(binary);
		IASTNode targetStatement = CxxAstUtils.getEnclosingStatement(astName);
		if (targetStatement == null) {
			return;
		}
		IASTNode[] children = targetStatement.getParent().getChildren();
		IASTNode nodeAfterMarker = null;
		for (int i = 0; i < children.length - 1; ++i) {
			if (children[i] == targetStatement)
				nodeAfterMarker = children[i + 1];
		}

		r.insertBefore(targetStatement.getParent(), nodeAfterMarker, newStatement, null);
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
