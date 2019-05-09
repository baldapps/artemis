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

import com.baldapps.artemis.checkers.FieldsClassesChecker;

/**
 * Test for {@link FieldsClassesChecker} class
 */
public class FieldsClassesCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = FieldsClassesChecker.HIDING_FIELD;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//class Base {
	//public:
	//	int a;
	//};
	//class Child: public Base {
	//private:
	//	double a;
	//};
	public void testHidingNamePublic() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, ERR_ID);
	}

	//class Base {
	//public:
	//	int a;
	//};
	//class Child: private Base {
	//private:
	//	double a;
	//};
	public void testHidingNamePrivate() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//class Base {
	//public:
	//	int a;
	//};
	//class Child: protected Base {
	//private:
	//	double a;
	//};
	public void testHidingNameProtected() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, ERR_ID);
	}
}
