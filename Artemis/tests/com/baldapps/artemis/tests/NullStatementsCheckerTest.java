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

import com.baldapps.artemis.checkers.NullStatementsChecker;

/**
 * Test for {@link NullStatementsChecker} class
 */
public class NullStatementsCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = NullStatementsChecker.ERR_ID;

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
	//	while(1)
	//		;
	//}
	public void testEmptyWhile() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//void foo() {
	//	for(;;)
	//		;
	//}
	public void testEmptyFor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//#define A(x) x;
	//void foo() {
	//	A(5);
	//}
	public void testDoubleSemicolon() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//#define A(x)
	//void foo() {
	//	A(5);
	//}
	public void testEmptyMacro() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//void foo() {
	//	int a = 6;
	//	if (; a < 5)
	//		a++;
	//}
	public void testEmptyIfCpp17() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}
}
