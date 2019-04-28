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
package com.baldapps.artemis.tests;

import java.io.IOException;

import org.eclipse.cdt.codan.core.tests.CheckerTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;

import com.baldapps.artemis.checkers.ArtemisCoreActivator;

public class ArtemisCheckerTestCase extends CheckerTestCase {
	@Override
	protected StringBuilder[] getContents(int sections) {
		try {
			return TestSourceReader.getContentsForTest(ArtemisCoreActivator.getDefault().getBundle(), getSourcePrefix(),
					getClass(), getName(), sections);
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}
}
