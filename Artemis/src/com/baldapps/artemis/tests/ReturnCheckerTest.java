/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package com.baldapps.artemis.tests;

import com.baldapps.artemis.checkers.ReturnChecker;

/**
 * Test for {@see ReturnChecker} class
 */
public class ReturnCheckerTest extends ArtemisCheckerTestCase {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ReturnChecker.RET_LOCAL_ID, ReturnChecker.RET_PRIVATE_FIELD_ID, ReturnChecker.NO_RET_THIS_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//	int& bar() {
	//		int a = 0;
	//		return a; //error here
	//	}
	public void testReturnByRef() throws Exception {
		checkSampleAboveCpp();
	}

	//	int* bar() {
	//		int a = 0;
	//		return &a; //error here
	//	}
	public void testReturnByPtr() throws Exception {
		checkSampleAboveCpp();
	}

	//	int& bar() {
	//		int a = 0;
	//		return reinterpret_cast<int&>(a); //error here
	//	}
	public void testReturnByCastRef() throws Exception {
		checkSampleAboveCpp();
	}

	//	int* bar() {
	//		int a = 0;
	//		return reinterpret_cast<int*>(a);
	//	}
	public void testReturnByCastPtr() throws Exception {
		checkSampleAboveCpp();
	}

	//	int* bar() {
	//		int a = 0, b = 0;
	//		bool cond = true;
	//		return cond ? &a : b; //error here
	//	}
	public void testReturnByTernary() throws Exception {
		checkSampleAboveCpp();
	}

	//	struct S { int a; }
	//	int& bar() {
	//		struct S s;
	//		return s.a; //error here
	//	}
	public void testReturnLocalStructField() throws Exception {
		checkSampleAboveCpp();
	}

	//	class Test {
	//	private:
	//		int field;
	//	public:
	//		int& bar() {
	//			return field;
	//		}
	//	}
	public void testReturnClassField() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6, ReturnChecker.RET_PRIVATE_FIELD_ID);
		checkNoErrorsOfKind(ReturnChecker.RET_LOCAL_ID);
	}

	//	class Test {
	//	private:
	//		int field;
	//	public:
	//		void foo(double*);
	//		void (Test::*op_func)(double*) bar() {
	//			return foo;
	//		}
	//	}
	public void testReturnClassMethod() throws Exception {
		checkSampleAboveCpp();
	}

	//int& foo() {
	//	int* a = new int;
	//	return *a;
	//}
	public void testReturnRefUsingDerefPtr() throws Exception {
		checkSampleAboveCpp();
	}

	//	class Test {
	//	private:
	//		int field;
	//	public:
	//		int operator=(const Test& t) {
	//			return 1; //error here
	//		}
	//	}
	public void testReturnOpAssign() throws Exception {
		checkSampleAboveCpp();
	}
}
