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

import com.baldapps.artemis.checkers.IdentifierChecker;

/**
 * Test for {@link IdentifierChecker} class
 */
public class IdentifierCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = IdentifierChecker.ERR_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//void thisIsAVeryVeryVeryVeryLongName1() {
	//}
	public void testLongName() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//void thisIsAVeryVeryVeryVeryLongName() {
	//}
	public void testShortName() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
