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

import com.baldapps.artemis.checkers.LambdaExpressionChecker;

/**
 * Test for {@link LambdaExpressionChecker} class
 */
public class LambdaExpressionCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = LambdaExpressionChecker.LAMBDA_CAPTURE_ID;

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
	//	auto f = [=](){};
	//}
	public void testCaptureByCopy() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, ERR_ID);
	}

	//void foo() {
	//	auto f = [&](){};
	//}
	public void testCaptureByRef() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, ERR_ID);
	}

	//void foo() {
	//	int i = 0;
	//	auto f = [&i](){};
	//}
	public void testCaptureExplicit() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
