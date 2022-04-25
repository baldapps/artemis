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

import com.baldapps.artemis.checkers.ContinueChecker;

/**
 * Test for {@link ContinueChecker} class
 */
public class ContinueCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = ContinueChecker.ERR_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//void foo() {
	//	for(;;) {
	//		continue;
	//	}
	//}
	public void testContinue() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}
}
