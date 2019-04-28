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

import com.baldapps.artemis.checkers.ForLoopChecker;

/**
 * Test for {@link ForLoopChecker} class
 */
public class ForLoopCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = ForLoopChecker.FLOAT_COUNTER_ID;

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
	//	for (float f = 0; f < 4.3; f++) {
	//	}
	//}
	public void testAddToStd() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, ERR_ID);
	}

	//void foo() {
	//	for (int f = 0; f < 4; f++) {
	//	}
	//}
	public void testAddToOther() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
