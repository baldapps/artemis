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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.cdt.codan.core.model.AbstractCheckerWithProblemPreferences;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class TrigraphsChecker extends AbstractCheckerWithProblemPreferences {
	public final static String ID = "com.baldapps.artemis.checkers.TrigraphsProblemError";

	public static final String[] trigraphs = { "??=", "??/", "??^", "??(", "??)", "??!", "??<", "??>", "??-" };

	@Override
	public boolean runInEditor() {
		return false;
	}

	@Override
	public synchronized boolean processResource(IResource resource) {
		if (!shouldProduceProblems(resource))
			return false;
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			processFile(file);
			return false;
		}
		return true;
	}

	void processFile(IFile file) {
		Collection<IProblem> refProblems = getRuntime().getCheckersRegistry().getRefProblems(this);
		for (Iterator<IProblem> iterator = refProblems.iterator(); iterator.hasNext();) {
			IProblem checkerProblem = iterator.next();
			IProblem problem = getProblemById(checkerProblem.getId(), file);
			if (shouldProduceProblem(problem, file.getFullPath())) {
				externalRun(file, problem);
			}
		}
	}

	/**
	 * @param file
	 * @param checkerProblem
	 * @param values
	 * @param problem
	 * @return
	 */
	private void externalRun(IFile file, IProblem problem) {
		try {
			InputStream is = file.getContents();
			BufferedReader bis = new BufferedReader(new InputStreamReader(is));
			String line;
			int iline = 0;
			while ((line = bis.readLine()) != null) {
				iline++;
				for (int i = 0; i < trigraphs.length; i++) {
					if (line.contains(trigraphs[i])) {
						reportProblem(problem.getId(), file, iline, trigraphs[i]);
					}
				}
			}
			bis.close();
		} catch (IOException | CoreException e) {
			ArtemisCoreActivator.log(e);
		}
	}
}
