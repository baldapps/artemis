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

import com.baldapps.artemis.checkers.ExplicitChecker;

/**
 * Test for {@link ExplicitChecker} class
 */
public class ExplicitCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = ExplicitChecker.USE_EXPLICIT_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	public boolean isHeader() {
		return true;
	}

	//class A {
	//public:
	//	A(int a);
	//};
	public void testCtorSinglePar() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//class A {
	//public:
	//	A();
	//};
	public void testCtorZeroPar() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//struct B {
	//};
	//class A {
	//public:
	//	A(struct B b);
	//};
	public void testCtorOneComplexPar() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}

	//struct B {
	//	B(int a, int b);
	//};
	//class A {
	//public:
	//	A(struct B b);
	//};
	public void testCtorOneSimplePar() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4, ERR_ID);
	}
}
