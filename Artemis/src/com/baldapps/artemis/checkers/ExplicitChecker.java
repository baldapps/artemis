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

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemLocationFactory;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.SemanticQueries;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.CoreException;

import com.baldapps.artemis.utils.IndexUtils;

@SuppressWarnings("restriction")
public class ExplicitChecker extends AbstractIndexAstChecker {
	public static final String USE_EXPLICIT_ID = "com.baldapps.artemis.checkers.UseExplicitProblem"; //$NON-NLS-1$
	public static final String PARAM_CONV_OP = "paramConvOp"; //$NON-NLS-1$

	private IIndex index;
	private boolean checkConvOp;

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, PARAM_CONV_OP, CheckersMessages.ExplicitChecker_IncludeConversionOperators,
				Boolean.FALSE);
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		index = ast.getIndex();
		checkConvOp = (Boolean) getPreference(getProblemById(USE_EXPLICIT_ID, getFile()), PARAM_CONV_OP);
		ast.accept(new NodeVisitor());
	}

	private void reportProblem(String problemId, IBinding binding, IASTNode root, Object... args)
			throws InterruptedException, CoreException {
		IProblemLocation location = getProblemLocation(binding);
		if (location == null)
			reportProblem(problemId, root, args);
		else
			reportProblem(problemId, location, args);
	}

	private IProblemLocation getProblemLocation(IBinding binding) throws InterruptedException, CoreException {
		IASTFileLocation astLocation = IndexUtils.getLocation(index, binding);
		if (astLocation == null)
			return null;
		int line = astLocation.getStartingLineNumber();
		IProblemLocationFactory locFactory = getRuntime().getProblemLocationFactory();
		int start = astLocation.getNodeOffset();
		int end = start + astLocation.getNodeLength();
		return locFactory.createProblemLocation(getFile(), start, end, line);
	}

	private class NodeVisitor extends ASTVisitor {

		public NodeVisitor() {
			shouldVisitDeclSpecifiers = true;
		}

		@Override
		public int visit(IASTDeclSpecifier element) {
			try {
				if (element instanceof ICPPASTCompositeTypeSpecifier) {
					ICPPASTCompositeTypeSpecifier clazz = (ICPPASTCompositeTypeSpecifier) element;
					if (clazz.getKey() == ICPPASTCompositeTypeSpecifier.k_class) {
						IBinding binding = clazz.getName().resolveBinding();
						if (binding instanceof ICPPClassType) {
							ICPPClassType classType = (ICPPClassType) binding;
							ICPPConstructor[] ctors = classType.getConstructors();
							for (ICPPConstructor m : ctors) {
								if (!m.isExplicit() && !m.isImplicit() && m.getParameters().length == 1
										&& !SemanticQueries.isCopyOrMoveConstructor(m)) {
									reportProblem(USE_EXPLICIT_ID, m, element);
								}
							}
							if (checkConvOp) {
								ICPPMethod[] methods = SemanticUtil.getConversionOperators(classType);
								for (ICPPMethod m : methods) {
									if (!m.isExplicit())
										reportProblem(USE_EXPLICIT_ID, m, element);
								}
							}
						}
					}
				}
			} catch (InterruptedException | CoreException | DOMException e) {
				ArtemisCoreActivator.log(e);
			}
			return PROCESS_CONTINUE;
		}
	}
}
