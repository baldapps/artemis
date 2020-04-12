/*******************************************************************************
 * Copyright (c) 2020 Marco Stornelli
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
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

import com.baldapps.artemis.checkers.ArtemisCoreActivator;

public class QuickFixValueConventionLiterals extends AbstractArtemisAstRewriteQuickFix {

	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixValueConventionLiterals_to_uppercase;
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
		if (astNode == null || !(astNode instanceof IASTLiteralExpression)) {
			return;
		}
		ASTRewrite r = ASTRewrite.create(ast);
		IASTLiteralExpression newLiteral = (IASTLiteralExpression) astNode.copy(CopyStyle.withLocations);
		char[] input = ((IASTLiteralExpression) astNode).getValue();
		toUpperCase(input);
		newLiteral.setValue(input);
		r.replace(astNode, newLiteral, null);
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

	private void toUpperCase(char[] input) {
		int len = input.length;
		if (isHex(input)) {
			for (int i = len - 1; i > 0; --i) {
				char lower = Character.toLowerCase(input[i]);
				if ((lower >= 'a' && lower <= 'f') || lower == 'x')
					break;
				input[i] = Character.toUpperCase(input[i]);
			}
		} else {
			for (int i = 0; i < len; ++i) {
				input[i] = Character.toUpperCase(input[i]);
			}
		}
	}

	private boolean isHex(char[] value) {
		if (value.length >= 3 && value[0] == '0' && (value[1] == 'x' || value[1] == 'X')) {
			return true;
		}
		return false;
	}
}
