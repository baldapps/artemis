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
package com.baldapps.artemis.checkers;

import org.eclipse.osgi.util.NLS;

/**
 * Messages
 */
public class CheckersMessages extends NLS {
	public static String GenericParameter_ParameterExceptions;
	public static String GenericParameter_ParameterExceptionsItem;
	public static String MagicNumberChecker_ParameterArray;
	public static String MagicNumberChecker_ParameterOperatorParen;
	public static String SwitchCaseChecker_ParameterDefaultAllEnums;
	public static String Copyright_regex;
	public static String ShallowCopyChecker_OnlyNew;
	public static String NamingConventionClassesChecker_ClassLabelNamePattern;
	public static String NamingConventionClassesChecker_InterfaceLabelNamePattern;
	public static String NamingConventionClassesChecker_AllowProtected;
	public static String BlacklistChecker_list;
	public static String BlacklistChecker_list_item;
	static {
		NLS.initializeMessages(CheckersMessages.class.getName(), CheckersMessages.class);
	}

	// Do not instantiate
	private CheckersMessages() {
	}
}
