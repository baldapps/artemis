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

import com.baldapps.artemis.checkers.MemCompareChecker;

/**
 * Test for {@link MemCompareChecker} class
 */
public class MemCompareCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = MemCompareChecker.ERR_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//int memcmp(void* a, void* b, int s);
	//struct Foo {
	//	int a;
	//};
	//int foo() {
	//	struct Foo a1, a2;
	//	memcmp(&a1, &a2, sizeof(a1));
	//}
	public void testMemCmpOverStructs() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(7, ERR_ID);
	}

	//int memcmp(void* a, void* b, int s);
	//struct Foo {
	//	int a;
	//};
	//int foo() {
	//	int a, b;
	//	memcmp(&a, &b, sizeof(a1));
	//}
	public void testMemCmpOverInts() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
