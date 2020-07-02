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

import com.baldapps.artemis.checkers.ForLoopChecker;

/**
 * Test for {@link ForLoopChecker} class
 */
public class ForLoopCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = ForLoopChecker.FLOAT_COUNTER_ID;
	public static final String CNT_MODIFICATION_ID = ForLoopChecker.CNT_MODIFICATION_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID, CNT_MODIFICATION_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//void foo() {
	//	for (float f = 0; f < 4.3; f++) {
	//	}
	//}
	public void testForWithFloat() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, ERR_ID);
	}

	//void foo() {
	//	for (int f = 0; f < 4; f++) {
	//	}
	//}
	public void testForWithoutFloat() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//void foo() {
	//	for (int f = 0; f < 4; f++) {
	//		f++;
	//	}
	//}
	public void testCounterMod1() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, CNT_MODIFICATION_ID);
	}

	//void foo() {
	//	for (int f = 0; f < 4; f++) {
	//		f += 8;
	//	}
	//}
	public void testCounterMod2() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, CNT_MODIFICATION_ID);
	}

	//void foo() {
	//	int array[4];
	//	for (int f = 0; f < 4; f++) {
	//		int a = array[f];
	//	}
	//}
	public void testCounterMod3() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(CNT_MODIFICATION_ID);
	}

	//void mod(int f) {};
	//void foo() {
	//	int array[4];
	//	for (int f = 0; f < 4; f++) {
	//		mod(f);
	//	}
	//}
	public void testCounterMod4() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(CNT_MODIFICATION_ID);
	}

	//void foo() {
	//	int array1[4];
	//	int array2[4];
	//	for (int f = 0; f < 4; f++) {
	//		array1[f] = array2[f];
	//	}
	//}
	public void testCounterMod5() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(CNT_MODIFICATION_ID);
	}

	//void foo() {
	//	int array1[4];
	//	int array2[4];
	//	for (int f = 0; f < 4; f++) {
	//		array1[f++] = 8; 
	//	}
	//}
	public void testCounterMod6() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5);
	}
}
