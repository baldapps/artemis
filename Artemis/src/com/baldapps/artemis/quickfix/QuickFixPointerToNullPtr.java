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

public class QuickFixPointerToNullPtr extends AbstractQuickFixPointerToZero {

	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixPointerToZero_add_assignment_to_nullptr;
	}

	@Override
	protected String getNullString() {
		return "nullptr";
	}

	@Override
	protected int getNullKind() {
		return IASTLiteralExpression.lk_nullptr;
	}

}
