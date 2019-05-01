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

	public static final String ERR_ID = StructsChecker.AVOID_STRUCTS_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
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
	public void testGlobalVSFuncLoc() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(1, 5);
	}
}
