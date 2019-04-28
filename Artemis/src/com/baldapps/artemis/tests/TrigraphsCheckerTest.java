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

import com.baldapps.artemis.checkers.TrigraphsChecker;

/**
 * Test for {@link TrigraphsChecker} class
 */
public class TrigraphsCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = TrigraphsChecker.ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//int foo(int arr[]) {
	//	??< sizeof(arr); ??>
	//}
	public void testUsingTrigraphs() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, ERR_ID);
	}

	//int foo(int arr[]) {
	//	sizeof(arr);
	//}
	public void testNoUsingTrigraphs() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
