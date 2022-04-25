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

	//namespace std {
	//	class a {};
	//}
	public void testAddClassToStd() throws Exception {
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

	//namespace std::chrono {
	//	int a;
	//}
	public void testNestedStd() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}

	//class MyType {};
	//namespace std {
	//  template <> 
	//  struct std::hash<MyType> {
	//     std::size_t operator()(const MyType& t) const {
	//        return 0; 
	//     }
	//  };
	//}
	public void testTemplateSpec() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}

	//template<class T>
	//class Myclass;
	//namespace std {
	//	template<class T>
	//	struct hash<Myclass<T>> {};
	//}
	public void testPartialSpecialization() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ERR_ID);
	}
	
	//namespace std {
	//	template<class T>
	//	struct foo {};
	//}
	public void testTemplateDeclaration() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ERR_ID);
	}
	
	//template<class T>
	//class Myclass;
	//namespace std {
	//	template<class T>
	//	Myclass<T> max(const Myclass<T>& a, const Myclass<T>& b) {
	//		return a;
	//	}
	//}
	public void testPartialSpecializationFunction() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ERR_ID);
	}
}
