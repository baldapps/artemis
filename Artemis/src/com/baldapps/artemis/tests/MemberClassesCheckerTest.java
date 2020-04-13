/*******************************************************************************
 * Copyright (c) 2010, 2019 Gil Barash
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Gil Barash  - Initial implementation
 *    Marco Stornelli - Improvements
 *******************************************************************************/
package com.baldapps.artemis.tests;

import com.baldapps.artemis.checkers.MemberClassesChecker;

/**
 * Test for {@link#MemberClassesChecker} class
 */
public class MemberClassesCheckerTest extends ArtemisCheckerTestCase {

	public static final String CTOR_DTOR_INLINE_ID = MemberClassesChecker.CTOR_DTOR_INLINE;
	public static final String IMPLICIT_VIRTUAL_ID = MemberClassesChecker.IMPLICIT_VIRTUAL;
	public static final String VIRTUAL_INLINE_ID = MemberClassesChecker.VIRTUAL_INLINE;
	public static final String AVOID_OVERLOAD_ID = MemberClassesChecker.AVOID_OVERLOADS;
	public static final String USER_CTOR = MemberClassesChecker.USER_CTOR;
	public static final String ABSTRACT_NO_COPY = MemberClassesChecker.ABSTRACT_NO_COPY;
	public static final String MULTIPLE_INHERITANCE = MemberClassesChecker.MULTIPLE_INHERITANCE;
	public static final String COPY_CTOR_ONLY = MemberClassesChecker.COPY_CTOR_ONLY;
	public static final String ASSIGN_OP_ONLY = MemberClassesChecker.ASSIGN_OP_ONLY;
	public static final String VIRTUAL_NO_OVERRIDE = MemberClassesChecker.VIRTUAL_NO_OVERRIDE;
	public static final String PROTECTED_FIELDS = MemberClassesChecker.PROTECTED_FIELDS;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(CTOR_DTOR_INLINE_ID, IMPLICIT_VIRTUAL_ID, VIRTUAL_INLINE_ID, AVOID_OVERLOAD_ID, USER_CTOR,
				ABSTRACT_NO_COPY, MULTIPLE_INHERITANCE, COPY_CTOR_ONLY, ASSIGN_OP_ONLY, VIRTUAL_NO_OVERRIDE,
				PROTECTED_FIELDS);
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
	//	inline A() {}
	//};
	public void testCtorInline() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, CTOR_DTOR_INLINE_ID);
	}

	//class A {
	//private:
	//	int a;
	//public:
	//	A() { a = 1; }
	//};
	public void testCtorImplicitInline() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, CTOR_DTOR_INLINE_ID);
	}

	//class A {
	//public:
	//	A();
	//	inline ~A() {}
	//};
	public void testDtorInline() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, CTOR_DTOR_INLINE_ID);
	}

	//class A {
	//private:
	//	int* a;
	//public:
	//	A();
	//	~A() { delete a; }
	//};
	public void testDtorImplicitInline() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, CTOR_DTOR_INLINE_ID);
	}

	//class B {
	//public:
	//	virtual void tt();
	//}
	//class A: public B {
	//public:
	//	void tt();
	//};
	public void testImplicitVirtual() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5, IMPLICIT_VIRTUAL_ID);
		checkErrorLine(5, VIRTUAL_NO_OVERRIDE);
	}

	//class B {
	//public:
	//	virtual void tt();
	//}
	//class A: public B {
	//public:
	//	virtual void tt() override;
	//};
	public void testVirtualOverride() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(IMPLICIT_VIRTUAL_ID);
		checkNoErrorsOfKind(VIRTUAL_NO_OVERRIDE);
	}

	//class B {
	//public:
	//	virtual void tt();
	//}
	//class A: public B {
	//public:
	//	virtual void tt();
	//};
	public void testExplicitVirtual() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(IMPLICIT_VIRTUAL_ID);
	}

	//class A{
	//public:
	//	virtual void tt() {}
	//};
	public void testImplicitVirtualInline() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, VIRTUAL_INLINE_ID);
	}

	//class A{
	//public:
	//	inline virtual void tt() {}
	//};
	public void testExplicitVirtualInline() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, VIRTUAL_INLINE_ID);
	}

	//class A {
	//public:
	//	bool operator&&();
	//};
	public void testOverloadLogicAnd() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, AVOID_OVERLOAD_ID);
	}

	//class A {
	//public:
	//	bool operator,();
	//};
	public void testOverloadComma() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, AVOID_OVERLOAD_ID);
	}

	//class A {
	//public:
	//	bool operator||();
	//};
	public void testOverloadLogicOr() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, AVOID_OVERLOAD_ID);
	}

	//class A {
	//public:
	//	~A();
	//};
	public void testNoUserCtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, USER_CTOR);
	}

	//class A {
	//public:
	//	A() = delete;
	//	~A();
	//};
	public void testUserCtorDeleted() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(USER_CTOR);
	}

	//class A {
	//public:
	//	A() = default;
	//	~A();
	//};
	public void testUserCtorDefault() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(USER_CTOR);
	}

	//class A {
	//public:
	//	A();
	//	virtual void test() = 0;
	//};
	public void testAbstractCopyable1() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ABSTRACT_NO_COPY);
	}

	//class A {
	//private:
	//	A(const A& a);
	//public:
	//	A();
	//	virtual void test() = 0;
	//};
	public void testAbstractCopyable2() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ABSTRACT_NO_COPY);
	}

	//class A {
	//private:
	//	A(const A& a);
	//	A& operator=(const A& a);
	//public:
	//	A();
	//	virtual void test() = 0;
	//};
	public void testAbstractCopyable3() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ABSTRACT_NO_COPY);
	}

	//class A {
	//public:
	//	A();
	//	A(const A& a) = delete;
	//	A& operator=(const A& a) = delete;
	//	virtual void test() = 0;
	//};
	public void testAbstractCopyable4() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ABSTRACT_NO_COPY);
	}

	//class A {
	//public:
	//	A();
	//	A(const A& a);
	//	A& operator=(const A& a);
	//	virtual void test() = 0;
	//};
	public void testAbstractCopyable5() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ABSTRACT_NO_COPY);
	}

	//class F {};
	//class B {};
	//class A: public B, public F {
	//public:
	//	A();
	//	A(const A& a);
	//	A& operator=(const A& a);
	//};
	public void testMultipleInheritance() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, MULTIPLE_INHERITANCE);
	}

	//class A {
	//public:
	//	A();
	//	A(const A& a);
	//};
	public void testOnlyCopyCtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, COPY_CTOR_ONLY);
	}

	//class A {
	//public:
	//	A();
	//	A& operator=(const A& a);
	//};
	public void testAssignOp() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, ASSIGN_OP_ONLY);
	}

	//class A {
	//public:
	//	A();
	//	~A();
	//};
	public void testCtorAndDtor() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkNoErrorsOfKind(ASSIGN_OP_ONLY);
		checkNoErrorsOfKind(COPY_CTOR_ONLY);
	}

	//class A {
	//public:
	//	A();
	//protected:
	//	int foo;
	//};
	public void testProtectedField() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, PROTECTED_FIELDS);
	}

	//class Base {
	//protected:
	//	int foo;
	//};
	//class A: public Base {
	//public:
	//	A();
	//};
	public void testProtectedFieldChild() throws Exception {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(1, PROTECTED_FIELDS);
	}
}
