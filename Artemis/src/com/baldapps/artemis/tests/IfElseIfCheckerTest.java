/*******************************************************************************
 * Copyright (c) 2020 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.baldapps.artemis.tests;

import com.baldapps.artemis.checkers.IfElseIfChecker;

/**
 * Test for {@link IfElseIfChecker} class
 */
public class IfElseIfCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = IfElseIfChecker.ERR_ID;

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
	//	if (a == 0) {
	//		a++;
	//	} else if (a > 0) {
	//	}
	//}
	public void testIfElseIf() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, ERR_ID);
	}

	//void foo() {
	//	int a = 0;
	//	if (a == 0) {
	//		a++;
	//	} else if (a > 0) {
	//	} else if (a < 0) {
	//	}
	//}
	public void testIfElseIfElseIf() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6, ERR_ID);
	}

	//void foo() {
	//	int a = 0;
	//	if (a == 0) {
	//		a++;
	//	} else {
	//	}
	//}
	public void testIfElse() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//void foo() {
	//	int a = 0;
	//	if (a == 0) {
	//		a++;
	//	}
	//}
	public void testIf() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//void foo() {
	//	int a = 0;
	//	if (a == 0) {
	//		return;
	//	} else if (a > 0) {
	//		return;
	//	}
	//}
	public void testIfElseIfExit() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
