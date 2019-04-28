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

import com.baldapps.artemis.checkers.NamespaceChecker;

/**
 * Test for {@link NamespaceChecker} class
 */
public class NamespaceCheckerTest extends ArtemisCheckerTestCase {

	public static final String ERR_ID = NamespaceChecker.STD_NAMESPACE_ID;

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
	//	int a;
	//}
	public void testAddToStd() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//using namespace std;
	//namespace custom {
	//	int a;
	//}
	public void testAddToOther() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
}
