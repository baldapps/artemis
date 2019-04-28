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

import com.baldapps.artemis.checkers.AutoPtrChecker;

/**
 * Test for {@link AutoPtrChecker} class
 */
public class AutoPtrCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = AutoPtrChecker.ERR_ID;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ERR_ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	//namespace std {
	//	template<class T>
	//	class auto_ptr;
	//}
	//int foo(int arr[]) {
	//	std::auto_ptr<int> aa;
	//}
	public void testUsingTrigraphs() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(6, ERR_ID);
	}

	//namespace std {
	//	template<class T>
	//	class auto_ptr {
	//	public:
	//		auto_ptr() {};
	//	};
	//}
	//using namespace std;
	//int foo() {
	//	auto_ptr<int> aa;
	//}
	public void testNoUsingTrigraphs() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(10, ERR_ID);
	}
}
