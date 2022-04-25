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

import com.baldapps.artemis.checkers.CtorDtorChecker;

/**
 * Test for {@link CtorDtorChecker} class
 */
public class CtorDtorCheckerTest extends ArtemisCheckerTestCase {

	private static final String ERR_THROW_ID = CtorDtorChecker.THROW_ID;
	private static final String ERR_GLOBALS_ID = CtorDtorChecker.GLOBALS_ID;
	private static final String ERR_CALL_SUPER = CtorDtorChecker.CALL_SUPER_CTOR_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_THROW_ID, ERR_GLOBALS_ID, ERR_CALL_SUPER);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//int a = 1;
	//class Foo {
	//public:
	//	Foo() { a = 2; }
	//	~Foo();
	//};
	public void testGlobalsInCtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, ERR_GLOBALS_ID);
	}

	//struct S {
	//	int a;
	//};
	//struct S global;
	//class Foo {
	//public:
	//	Foo() { global.a = 2; }
	//	~Foo();
	//};
	public void testGlobalStructInCtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(7, ERR_GLOBALS_ID);
	}

	//class Foo {
	//public:
	//	Foo() {}
	//	~Foo() {
	//		throw 1;
	//	}
	//};
	public void testThrowInDtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, ERR_THROW_ID);
	}

	//class Foo {
	//public:
	//	Foo() {}
	//	~Foo() {
	//		try {
	//			throw 1;
	//		} catch(...) {
	//		}
	//	}
	//};
	public void testThrowInDtorWithCatchAll() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_THROW_ID);
	}

	//class A {
	//public:
	//	A();
	//};
	//class B {
	//public:
	//	B();
	//};
	//class C: public A, public B {
	//public:
	//	C() {  }
	//};
	public void testCallSuperWrong1() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(11, ERR_CALL_SUPER);
	}

	//class A {
	//public:
	//	A();
	//};
	//class B {
	//public:
	//	B();
	//};
	//class C: public A, public B {
	//public:
	//	C() : A() {  }
	//};
	public void testCallSuperWrong2() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(11, ERR_CALL_SUPER);
	}

	//class A {
	//public:
	//	A();
	//};
	//class B {
	//public:
	//	B();
	//};
	//class C: public A, public B {
	//public:
	//	C() : A(), B() {  }
	//};
	public void testCallSuperRight() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_CALL_SUPER);
	}
}
