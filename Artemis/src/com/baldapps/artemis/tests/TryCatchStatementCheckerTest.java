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

import com.baldapps.artemis.checkers.TryCatchStatementChecker;

/**
 * Test for {@link TryCatchStatementChecker} class
 */
public class TryCatchStatementCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = TryCatchStatementChecker.CATCH_ALL_ID;
	public static final String CATCH_ALL_ORDER_ID = TryCatchStatementChecker.CATCH_ALL_ORDER_ID;
	public static final String CATCH_EMPTY_ID = TryCatchStatementChecker.CATCH_EMPTY_ID;
	public static final String EMPTY_THROW_ID = TryCatchStatementChecker.EMPTY_THROW_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID, CATCH_ALL_ORDER_ID, CATCH_EMPTY_ID, EMPTY_THROW_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	// class Foo {
	// public:
	// Foo();
	// ~Foo() {
	// try {
	// throw 1;
	// } catch(...) {}
	// }
	// };
	public void testTryCatchAlone() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(7, ERR_ID);
	}

	// class Foo {
	// public:
	// Foo();
	// ~Foo() {
	// try {
	// throw 1;
	// } catch(int&) {}
	// } catch(...) {}
	// }
	// };
	public void testTryCatchMultiple() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//class Foo {
	//public:
	//	Foo();
	//	~Foo() {
	//		try {
	//			throw 1;
	//		} catch(...) {
	//			int a = 0;
	//		} catch(int&) {
	//		}
	//	}
	// };
	public void testCatchWrongOrder() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(7, CATCH_ALL_ORDER_ID);
	}

	//class Foo {
	//public:
	//	Foo();
	//	~Foo() {
	//		try {
	//			throw 1;
	//		} catch(int&) {
	//			int a = 0;
	//		} catch(...) {
	//		}
	//	}
	// };
	public void testCatchEmpty() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(9, CATCH_EMPTY_ID);
	}

	//class Foo {
	//public:
	//	Foo();
	//	~Foo() {
	//		throw;
	//	}
	// };
	public void testEmptyThrow() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, EMPTY_THROW_ID);
	}

	//class Foo {
	//public:
	//	Foo();
	//	~Foo() {
	//		throw 1;
	//	}
	// };
	public void testNoEmptyThrow() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(EMPTY_THROW_ID);
	}

	//class Foo {
	//public:
	//	Foo();
	//	~Foo() {
	//		try {
	//			throw 1;
	//		} catch(int&) {
	//			throw;
	//		}
	//	}
	//};
	public void testEmptyThrowInCatch() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(EMPTY_THROW_ID);
	}
}
