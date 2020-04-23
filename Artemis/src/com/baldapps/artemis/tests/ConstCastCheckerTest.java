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

import com.baldapps.artemis.checkers.ConstCastChecker;

/**
 * Test for {@link ConstCastChecker} class
 */
public class ConstCastCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = ConstCastChecker.ERR_ID;

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
	//	const int a = 5;
	//	int b = const_cast<int>(a);
	//}
	public void testRemoveConst() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//void foo() {
	//	volatile int a = 5;
	//	int b = const_cast<int>(a);
	//}
	public void testRemoveVolatile() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//typedef int Int;
	//typedef const int CInt;
	//void foo() {
	//	CInt a = 5;
	//	int b = const_cast<Int>(a);
	//}
	public void testRemoveConstTypedef() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, ERR_ID);
	}

	//typedef volatile int VInt;
	//typedef int CInt;
	//void foo() {
	//	VInt a = 5;
	//	int b = const_cast<CInt>(a);
	//}
	public void testRemoveVolatileTypedef() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, ERR_ID);
	}

	//void foo() {
	//	int a = 5;
	//	const int b = const_cast<const int>(a);
	//}
	public void testAddConst() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//void foo() {
	//	int a = 5;
	//	volatile int b = const_cast<volatile int>(a);
	//}
	public void testAddVolatile() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//class Foo {
	//public:
	//	Foo* test() const {
	//		return const_cast<Foo*>(this);
	//	}
	//};
	public void testThisPtr() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, ERR_ID);
	}

	//class Foo {
	//public:
	//	Foo& test() const {
	//		const Foo& ref = *this;
	//		return const_cast<Foo&>(ref);
	//	}
	//};
	public void testThisRef() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, ERR_ID);
	}
}
