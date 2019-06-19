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

import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IMarker;

public abstract class AbstractArtemisAstRewriteQuickFix extends AbstractAstRewriteQuickFix {

	@Override
	protected IASTNode getASTNodeFromPosition(IASTTranslationUnit ast, final int charStart, final int length) {
		IASTNode node = ast.getNodeSelector(null).findEnclosingNode(charStart, length);
		return node;
	}

	@Override
	protected IASTNode getASTNodeFromMarker(IMarker marker, IASTTranslationUnit ast) {
		final int charStart = marker.getAttribute(IMarker.CHAR_START, -1);
		final int length = marker.getAttribute(IMarker.CHAR_END, -1) - charStart;
		return getASTNodeFromPosition(ast, charStart, length);
	}
}
