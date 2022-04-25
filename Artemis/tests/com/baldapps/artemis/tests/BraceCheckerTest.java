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

import com.baldapps.artemis.checkers.BraceChecker;

/**
 * Test for {@link BraceChecker} class
 */
public class BraceCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = BraceChecker.MISS_BRACE_ID;

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
	//	int a = 0;
	//	if (1)
	//		a++;
	//}
	public void testIfNoBrace() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//void foo() {
	//	int a = 0;
	//	if (1) {
	//		a++;
	//	}
	//}
	public void testIfBrace() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//void foo() {
	//	int a = 0;
	//	if (1)
	//		a++;
	//	else
	//		a--;
	//}
	public void testIfElseNoBrace() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//void foo() {
	//	int a = 0;
	//	if (1) {
	//		a++;
	//	} else
	//		a--;
	//}
	public void testIfBraceElseNoBrace() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6, ERR_ID);
	}

	//void foo() {
	//	int a = 0;
	//	if (1) {
	//		a++;
	//	} else if (0)
	//		a--;
	//}
	public void testIfBraceElseIfNoBrace() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, ERR_ID);
	}

	//void foo() {
	//	int a = 0;
	//	if (1) {
	//		a++;
	//	} else if (0) {
	//		a--;
	//	}
	//}
	public void testIfBraceElseBrace() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
