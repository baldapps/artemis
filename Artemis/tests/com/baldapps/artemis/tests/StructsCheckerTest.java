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

import com.baldapps.artemis.checkers.StructsChecker;

/**
 * Test for {@link#StructsChecker} class
 */
public class StructsCheckerTest extends ArtemisCheckerTestCase {

	public static final String STRUCT_ERR_ID = StructsChecker.AVOID_STRUCTS_ID;
	public static final String UNION_ERR_ID = StructsChecker.AVOID_UNIONS_ID;
	public static final String VIRTUAL_ERR_ID = StructsChecker.AVOID_VIRTUAL_BASES_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(STRUCT_ERR_ID, UNION_ERR_ID, VIRTUAL_ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//struct Foo {
	//	int a;
	//}
	//void foo(void) {
	//  struct Foo f;
	//}
	public void testStruct() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(1, 5);
	}

	//union Foo {
	//	int a;
	//	double b;
	//}
	//void foo(void) {
	//  union Foo f;
	//}
	public void testUnion() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(1, 6);
	}

	//class Base {
	//}
	//class Child: public virtual Base {
	//}
	public void testVirtualBase() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, VIRTUAL_ERR_ID);
	}

	//class Base {
	//}
	//class Child: public Base {
	//}
	public void testNoVirtualBase() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(VIRTUAL_ERR_ID);
	}
}
