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

import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;

public class QuickFixPointerToZero extends AbstractQuickFixPointerToZero {

	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixPointerToZero_add_assignment_to_zero;
	}

	@Override
	protected String getNullString() {
		return "0";
	}

	@Override
	protected int getNullKind() {
		return IASTLiteralExpression.lk_integer_constant;
	}

}
