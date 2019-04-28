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

import com.baldapps.artemis.checkers.AllocationChecker;

/**
 * Test for {@link AllocationChecker} class
 */
public class AllocationCheckerTest extends ArtemisCheckerTestCase {

	public static final String DELETE_ERR_ID = AllocationChecker.DELETE_THIS_ID;
	public static final String DYNAMIC_ARRAY_ID = AllocationChecker.DYNAMIC_ARRAY_ID;
	public static final String POINTER_RESET_ID = AllocationChecker.POINTER_RESET_ID;
	public static final String DELETE_VOID_ID = AllocationChecker.DELETE_VOID_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(DELETE_ERR_ID, DYNAMIC_ARRAY_ID, POINTER_RESET_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	// class Foo {
	// public:
	// Foo();
	// ~Foo() {
	// delete this;
	// }
	// };
	public void testDeleteThis() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, DELETE_ERR_ID);
	}

	// void foo() {
	// int* a = new int[5];
	// };
	public void testDynArray() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, DYNAMIC_ARRAY_ID);
	}

	// void foo() {
	// int* a = new int;
	// };
	public void testDynAlloc() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo() {
	// int* a = new int;
	// delete a;
	// };
	public void testPointerAfterDelete() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, POINTER_RESET_ID);
	}

	// void foo() {
	// int* a = new int;
	// delete a;
	// a = nullptr;
	// };
	public void testPointerAfterDelete2() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo() {
	// int* a = new int;
	// int* b = new int;
	// delete a;
	// a = nullptr;
	// delete b;
	// b = nullptr;
	// };
	public void testMultiplePointerAfterDelete() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	// void foo() {
	// int* a = new int;
	// int* b = new int;
	// delete a;
	// delete b;
	// a = nullptr;
	// b = nullptr;
	// };
	public void testMultiplePointerAfterDeleteNoMatch() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4, 5);
	}

	// void foo() {
	// int* a = new int;
	// int* b = new int;
	// delete a;
	// a = 1;
	// };
	public void testPointerAfterDeleteNoZero() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4);
	}

	// void foo() {
	// int* a = new int;
	// void* b = a;
	// delete b;
	// };
	public void testDeleteUsingVoid() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLines(4);
	}
}
