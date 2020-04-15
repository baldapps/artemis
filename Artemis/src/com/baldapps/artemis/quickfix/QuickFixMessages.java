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
package com.baldapps.artemis.quickfix;

import org.eclipse.osgi.util.NLS;

public class QuickFixMessages extends NLS {
	public static String QuickFixPointerToZero_add_assignment_to_zero;
	public static String QuickFixPointerToZero_add_assignment_to_nullptr;
	public static String QuickFixAddDefaultSwitch_add_default_to_switch;
	public static String QuickFixAddCaseSwitch_add_cases_to_switch;
	public static String QuickFixAddVirtualKeyword_add_virtual;
	public static String QuickFixAddBlock_add_block;
	public static String QuickFixSizeOfParen_add_parens;
	public static String QuickFixCppCast_static_cast;
	public static String QuickFixCppCast_reinterpret_cast;
	public static String QuickFixCppCast_const_cast;
	public static String QuickFixCppCast_dynamic_cast;
	public static String QuickFixAddExplicit_add_explicit;
	public static String QuickFixValueConventionLiterals_to_uppercase;
	public static String QuickFixAddNoexcept_change_to_noexcept;
	public static String QuickFixAddOverrideKeyword_add_override;
	public static String QuickFixAddElse_add_else;

	static {
		NLS.initializeMessages(QuickFixMessages.class.getName(), QuickFixMessages.class);
	}

	private QuickFixMessages() {
	}
}
