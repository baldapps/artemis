/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.baldapps.artemis.tests;

import com.baldapps.artemis.checkers.IncludeBlacklistChecker;

/**
 * Test for {@link IncludeBlacklistChecker} class
 */
public class IncludeBlacklistCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = IncludeBlacklistChecker.ERR_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//#include <cstdio>
	//int main() {
	//   return 0;
	//}
	public void testWithSystemStdIo() throws Exception {
		setPreferenceValue(IncludeBlacklistChecker.ERR_ID, IncludeBlacklistChecker.PARAM_BLACKLIST,
				new String[] { "cstdio" });
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//#include "foo.h"
	//int main() {
	//   return 0;
	//}
	public void testWithStd3() throws Exception {
		setPreferenceValue(IncludeBlacklistChecker.ERR_ID, IncludeBlacklistChecker.PARAM_BLACKLIST,
				new String[] { "cstdio" });
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
