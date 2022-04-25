/*******************************************************************************
 * Copyright (c) 2020 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.baldapps.artemis.tests;

import com.baldapps.artemis.checkers.CtorTemplateChecker;

/**
 * Test for {@link CtorTemplateChecker} class
 */
public class CtorTemplateTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = CtorTemplateChecker.CTOR_TEMPLATE_ID;

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
		checkNoErrors();
	}

	//template<class T>
	//class A {
	//public:
	//	A(T&& t) {}
	//};
	public void testCtorClassTemplateRValue() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//class A {
	//public:
	//	template<class T>
	//	A(T&& t) {}
	//};
	public void testCtorTemplateRValue() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//class A {
	//public:
	//	template<class T>
	//	A(T& t) {}
	//};
	public void testCtorTemplateLValue() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}
	
	//template<class U>
	//class foo {};
	//class A {
	//public:
	//	template<class T>
	//	A(foo<T>&& t) {}
	//};
	public void testCtorTemplateRValueWithTemplate() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}
}
