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

import com.baldapps.artemis.checkers.NamingConventionClassesChecker;

/**
 * Test for {@link NamingConventionClassesChecker} class
 */
public class NamingConventionClassesCheckerTest extends ArtemisCheckerTestCase {

	public static final String CLASS_ID = NamingConventionClassesChecker.CLASS_NAME_ID;
	public static final String LABELS_ID = NamingConventionClassesChecker.LABELS_ID;
	public static final String FIELDS_ID = NamingConventionClassesChecker.FIELDS_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(CLASS_ID, LABELS_ID, FIELDS_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public boolean isHeader() {
		return true;
	}

	//class foo {
	//public:
	//}
	public void testWrongName() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, CLASS_ID);
	}

	//class Foo {
	//public:
	//}
	public void testRightName() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(CLASS_ID);
	}

	//class IFoo {
	//public:
	//	virtual void test() = 0;
	//}
	public void testIntRightName() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(CLASS_ID);
	}

	//class ifoo {
	//public:
	//	virtual void test() = 0;
	//}
	public void testIntWrongName() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, CLASS_ID);
	}

	//class Foo {
	//public:
	//	int pp;
	//}
	public void testPublicField() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, FIELDS_ID);
	}

	//class Foo {
	//private:
	//	int pp;
	//}
	public void testPrivateField() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(FIELDS_ID);
	}

	//class Foo {
	//protected:
	//	int pp;
	//}
	public void testProtectedAllowedField() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(FIELDS_ID);
	}

	//class Foo {
	//protected:
	//	int pp;
	//}
	public void testProtectedNotAllowedField() throws Exception {
		setPreferenceValue(FIELDS_ID, NamingConventionClassesChecker.PARAM_ALLOW_PROTECTED, false);
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, FIELDS_ID);
	}
}
