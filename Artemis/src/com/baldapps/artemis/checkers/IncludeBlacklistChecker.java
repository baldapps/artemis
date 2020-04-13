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

import java.util.Arrays;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class IncludeBlacklistChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "com.baldapps.artemis.checkers.IncludeBlacklistProblem"; //$NON-NLS-1$
	public static final String PARAM_BLACKLIST = "blacklistInclude"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		Object[] list = (Object[]) getPreference(getProblemById(ERR_ID, getFile()), PARAM_BLACKLIST);
		if (list == null || list.length == 0)
			return;
		Arrays.sort(list);
		IASTPreprocessorIncludeStatement[] includes = ast.getIncludeDirectives();
		for (IASTPreprocessorIncludeStatement i : includes) {
			if (Arrays.binarySearch(list, new String(i.getName().getSimpleID())) >= 0) {
				reportProblem(ERR_ID, i.getName(), i.getName());
			}
		}
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addListPreference(problem, PARAM_BLACKLIST, CheckersMessages.IncludeBlacklistChecker_list,
				CheckersMessages.IncludeBlacklistChecker_list_item);
	}
}
