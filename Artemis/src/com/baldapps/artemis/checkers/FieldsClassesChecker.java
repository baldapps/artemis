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
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.core.runtime.CoreException;

import com.baldapps.artemis.utils.ClassUtils;
import com.baldapps.artemis.utils.IndexUtils;

@SuppressWarnings("restriction")
public class FieldsClassesChecker extends AbstractIndexAstChecker {
	public static final String HIDING_FIELD = "com.baldapps.artemis.checkers.HidingFieldProblem"; //$NON-NLS-1$

	private IIndex index;

	@Override
	public void processAst(IASTTranslationUnit ast) {
		index = ast.getIndex();
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
							ICPPField[] fields = classType.getDeclaredFields();
							ICPPClassType[] bases = ClassUtils.getAllVisibleBases(classType);
							for (ICPPField f : fields) {
								outer: for (ICPPClassType b : bases) {
									ICPPField[] fieldsFromBase = ClassTypeHelper.getFields(b);
									for (ICPPField ff : fieldsFromBase) {
										if ((ff.getVisibility() != ICPPMember.v_private)
												&& f.getName().equals(ff.getName())) {
											reportProblem(HIDING_FIELD, f, element, f.getName(), b.getName());
											break outer;
										}
									}
								}
							}
						}
					}
				}
			} catch (InterruptedException | CoreException e) {
				ArtemisCoreActivator.log(e);
			}
			return PROCESS_CONTINUE;
		}
	}
}
