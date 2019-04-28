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

import com.baldapps.artemis.checkers.SwitchCaseChecker;


/**
 * Test for {@link SwitchCaseChecker} class
 */
public class SwitchCaseCheckerTest extends ArtemisCheckerTestCase {

	public static final String MISS_CASE_ID = SwitchCaseChecker.MISS_CASE_ID;
	public static final String MISS_DEFAULT_ID = SwitchCaseChecker.MISS_DEFAULT_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(MISS_CASE_ID, MISS_DEFAULT_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//enum FRUIT {
	//	APPLE, PEAR, BANANA
	//};
	//FRUIT getFruit() {
	//	return APPLE;
	//}
	//int main() {
	//	switch (FRUIT p = getFruit(); p) {
	//	case APPLE:
	//	case PEAR:
	//	case BANANA:
	//		break;
	//	}
	//	return 0;
	//}
	public void testSwitchWithInitClause() throws Exception {
		setPreferenceValue(MISS_DEFAULT_ID, SwitchCaseChecker.PARAM_DEFAULT_ALL_ENUMS, true);
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MISS_CASE_ID);
		checkErrorLine(8, MISS_DEFAULT_ID);
	}

	//enum FRUIT {
	//	APPLE, PEAR, BANANA
	//};
	//int main() {
	//	FRUIT p = APPLE;
	//	switch (p) {
	//	case APPLE:
	//	case PEAR:
	//	case BANANA:
	//		break;
	//	}
	//	return 0;
	//}
	public void testSwitchCompleteEnum() throws Exception {
		setPreferenceValue(MISS_DEFAULT_ID, SwitchCaseChecker.PARAM_DEFAULT_ALL_ENUMS, true);
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MISS_CASE_ID);
		checkErrorLine(6, MISS_DEFAULT_ID);
	}

	//enum FRUIT {
	//	APPLE, PEAR, BANANA
	//};
	//int main() {
	//	FRUIT p = APPLE;
	//	switch (p) {
	//	case APPLE:
	//	case PEAR:
	//		break;
	//	}
	//	return 0;
	//}
	public void testSwitchMissEnum() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6, MISS_CASE_ID);
	}

	//enum FRUIT {
	//	APPLE, PEAR, BANANA
	//};
	//int main() {
	//	FRUIT p = APPLE;
	//	switch (p) {
	//	case APPLE:
	//	case PEAR:
	//	case 2:
	//		break;
	//	}
	//	return 0;
	//}
	public void testSwitchWithMixedValues() throws Exception {
		setPreferenceValue(MISS_DEFAULT_ID, SwitchCaseChecker.PARAM_DEFAULT_ALL_ENUMS, true);
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MISS_CASE_ID);
		checkErrorLine(6, MISS_DEFAULT_ID);
	}

	//enum FRUIT {
	//	APPLE, PEAR, BANANA
	//};
	//int main() {
	//	FRUIT p = APPLE;
	//	switch (p) {
	//	case APPLE:
	//	case PEAR:
	//	case BANANA:
	//		break;
	//	}
	//	return 0;
	//}
	public void testSwitchEnumComplete() throws Exception {
		setPreferenceValue(MISS_DEFAULT_ID, SwitchCaseChecker.PARAM_DEFAULT_ALL_ENUMS, false);
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MISS_CASE_ID);
		checkNoErrorsOfKind(MISS_DEFAULT_ID);
	}

	//enum FRUIT {
	//	APPLE, PEAR, BANANA
	//};
	//int main() {
	//	FRUIT p = APPLE;
	//	switch (p) {
	//	case APPLE:
	//	case PEAR:
	//	default:
	//		break;
	//	}
	//	return 0;
	//}
	public void testSwitchDefaultClausePresent() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(MISS_CASE_ID);
		checkNoErrorsOfKind(MISS_DEFAULT_ID);
	}
}
