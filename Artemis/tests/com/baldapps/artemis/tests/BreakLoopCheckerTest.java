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

import com.baldapps.artemis.checkers.BreakLoopChecker;

/**
 * Test for {@link BreakLoopChecker} class
 */
public class BreakLoopCheckerTest extends ArtemisCheckerTestCase {

	public static final String BREAK_ID = BreakLoopChecker.BREAK_IN_LOOP;
	public static final String MORE_BREAK_ID = BreakLoopChecker.MORE_THAN_ONE_BREAK_IN_LOOP;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(BREAK_ID, MORE_BREAK_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//void foo() {
	//	for (int f = 0; f < 0; ++f) {
	//	}
	//}
	public void testForNoBreak() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(BREAK_ID);
		checkNoErrorsOfKind(MORE_BREAK_ID);
	}

	//void foo() {
	//	int i = 0;
	//	while (i < 10) {
	//	}
	//}
	public void testWhileNoBreak() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(BREAK_ID);
		checkNoErrorsOfKind(MORE_BREAK_ID);
	}

	//void foo() {
	//	int i = 0;
	//	do {
	//	} while(i < 10);
	//}
	public void testDoWhileNoBreak() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(BREAK_ID);
		checkNoErrorsOfKind(MORE_BREAK_ID);
	}

	//void foo() {
	//	for (int f = 0; f < 0; ++f) {
	//		break;
	//	}
	//}
	public void testForBreak() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, BREAK_ID);
		checkNoErrorsOfKind(MORE_BREAK_ID);
	}

	//void foo() {
	//	int i = 0;
	//	while (i < 10) {
	//		break;
	//	}
	//}
	public void testWhileBreak() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, BREAK_ID);
		checkNoErrorsOfKind(MORE_BREAK_ID);
	}

	//void foo() {
	//	int i = 0;
	//	do {
	//		break;
	//	} while(i < 10);
	//}
	public void testDoWhileBreak() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, BREAK_ID);
		checkNoErrorsOfKind(MORE_BREAK_ID);
	}

	//void foo() {
	//	for (int f = 0; f < 0; ++f) {
	//		if ( f == 1)
	//			break;
	//		break;
	//	}
	//}
	public void testForMoreBreak() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, BREAK_ID);
		checkErrorLine(5, BREAK_ID);
		checkErrorLine(4, MORE_BREAK_ID);
		checkErrorLine(5, MORE_BREAK_ID);
	}

	//void foo() {
	//	int i = 0;
	//	while (i < 10) {
	//		if ( f == 1)
	//			break;
	//		break;
	//	}
	//}
	public void testWhileMoreBreak() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, BREAK_ID);
		checkErrorLine(6, BREAK_ID);
		checkErrorLine(5, MORE_BREAK_ID);
		checkErrorLine(6, MORE_BREAK_ID);
	}

	//void foo() {
	//	int i = 0;
	//	do {
	//		if ( f == 1)
	//			break;
	//		break;
	//	} while(i < 10);
	//}
	public void testDoWhileMoreBreak() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, BREAK_ID);
		checkErrorLine(6, BREAK_ID);
		checkErrorLine(5, MORE_BREAK_ID);
		checkErrorLine(6, MORE_BREAK_ID);
	}
}
