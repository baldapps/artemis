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

import com.baldapps.artemis.checkers.SizeofChecker;

/**
 * Test for {@link SizeofChecker} class
 */
public class SizeofCheckerTest extends ArtemisCheckerTestCase {

	public static final String SIZEOF_ARRAY_ID = SizeofChecker.SIZEOF_ARRAY_ID;
	public static final String SIZEOF_NESTED_ID = SizeofChecker.SIZEOF_NESTED_ID;
	public static final String SIZEOF_VOID_ID = SizeofChecker.SIZEOF_VOID_ID;
	public static final String SIZEOF_NO_PAREN_ID = SizeofChecker.SIZEOF_NO_PAREN_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(SIZEOF_ARRAY_ID, SIZEOF_NESTED_ID, SIZEOF_VOID_ID, SIZEOF_NO_PAREN_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//int foo(int arr[]) {
	//	sizeof(arr);
	//}
	public void testSizeofArray() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, SIZEOF_ARRAY_ID);
	}

	//int foo(int* arr) {
	//	sizeof(arr);
	//}
	public void testSizeofPtr() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(SIZEOF_ARRAY_ID);
	}

	//int foo(int* arr) {
	//	sizeof(sizeof(arr));
	//}
	public void testSizeofNested() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, SIZEOF_NESTED_ID);
	}

	//int foo(void* arr) {
	//	sizeof(arr);
	//}
	public void testSizeofVoidPtr() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(SIZEOF_VOID_ID);
	}

	//int foo() {
	//	sizeof(void);
	//}
	public void testSizeofVoid() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(2, SIZEOF_VOID_ID);
	}

	//int foo() {
	//	int a = 0;
	//	sizeof a;
	//}
	public void testSizeofNoParen() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, SIZEOF_NO_PAREN_ID);
	}
}
