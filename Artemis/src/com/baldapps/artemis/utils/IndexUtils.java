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
package com.baldapps.artemis.utils;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.core.runtime.CoreException;

public class IndexUtils {

	public static IASTFileLocation getLocation(IIndex index, IBinding binding)
			throws InterruptedException, CoreException {
		try {
			index.acquireReadLock();
			IIndexName[] declarations = index.findDeclarations(binding);
			if (declarations.length == 0)
				return null;
			return declarations[0].getFileLocation();
		} finally {
			index.releaseReadLock();
		}
	}

	public static boolean areEquivalentBindings(IBinding binding1, IBinding binding2, IIndex index) {
		if (binding1.equals(binding2)) {
			return true;
		}
		if ((binding1 instanceof IIndexBinding) != (binding2 instanceof IIndexBinding) && index != null) {
			if (binding1 instanceof IIndexBinding) {
				binding2 = index.adaptBinding(binding2);
			} else {
				binding1 = index.adaptBinding(binding1);
			}
			if (binding1 == null || binding2 == null) {
				return false;
			}
			if (binding1.equals(binding2)) {
				return true;
			}
		}
		return false;
	}
}
