/*******************************************************************************
 * Copyright (c) 2010, 2019 Gil Barash
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Gil Barash  - Initial implementation
 *    Marco Stornelli - Improvements
 *******************************************************************************/
package com.baldapps.artemis.tests;

import com.baldapps.artemis.checkers.FunctionChecker;

/**
 * Test for {@link#FunctionChecker} class
 */
public class FunctionCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = FunctionChecker.PAR_BY_COPY_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public boolean isHeader() {
		return true;
	}

	//struct Foo {
	//};
	//void test2(Foo f) {
	//}
	public void testStructCopy() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(3);
	}

	//class Foo {
	//};
	//void test(Foo f) {
	//}
	public void testClassCopy() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}

	//void test(int f) {
	//}
	public void testIntCopy() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//class Foo {
	//public:
	//	Foo(Foo f) {}
	//};
	public void testInClassCopy() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3);
	}
}
