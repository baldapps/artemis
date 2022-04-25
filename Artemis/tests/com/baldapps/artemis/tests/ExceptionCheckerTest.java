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

import com.baldapps.artemis.checkers.ExceptionChecker;

/**
 * Test for {@link ExceptionChecker} class
 */
public class ExceptionCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = ExceptionChecker.AVOID_THROWS_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//class A {
	//public:
	//	void foo() throw();
	//};
	//void A::foo() throw() {
	//}
	public void testEmptyThrow() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, ERR_ID);
	}

	//class Exc {};
	//class A {
	//public:
	//	void foo() throw(Exc);
	//};
	//void A::foo() throw(Exc) {
	//}
	public void testThrow() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6, ERR_ID);
	}

	//class Exc {};
	//class A {
	//public:
	//	void foo();
	//};
	//void A::foo() {
	//}
	public void testNoThrow() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//class Exc {};
	//class A {
	//public:
	//	void foo() noexcept;
	//};
	//void A::foo() noexcept {
	//}
	public void testNoexcept() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//class Exc {};
	//class A {
	//public:
	//	void foo() noexcept(false);
	//};
	//void A::foo() noexcept(false) {
	//}
	public void testNoexceotFalse() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
