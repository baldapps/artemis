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

import com.baldapps.artemis.checkers.AutoPtrChecker;
import com.baldapps.artemis.checkers.ValueConventionLiteralsChecker;

/**
 * Test for {@link AutoPtrChecker} class
 */
public class ValueConventionLiteralsTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = ValueConventionLiteralsChecker.ERR_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//static unsigned int AA = 58l;
	public void testIntLowercase() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//static unsigned int AA = 58L;
	public void testIntUppercase() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//static float AA = 58f;
	public void testFloatLowercase() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//static float AA = 58F;
	public void testFloatUppercase() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
