/*******************************************************************************
 * Copyright (c) 2020 Marco Stornelli
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
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.SemanticQueries;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.CoreException;

import com.baldapps.artemis.utils.IndexUtils;

@SuppressWarnings("restriction")
public class CtorTemplateChecker extends AbstractIndexAstChecker {
	public static final String CTOR_TEMPLATE_ID = "com.baldapps.artemis.checkers.CtorTemplateProblem"; //$NON-NLS-1$

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

		private boolean isOneParameter(ICPPFunctionTemplate m) {
			return m.getParameters().length == 1 && ((ICPPFunctionTemplate) m).getTemplateParameters().length == 1;
		}

		private boolean isTwoParameter(ICPPFunctionTemplate m) {
			ICPPTemplateParameter[] parameters = ((ICPPFunctionTemplate) m).getTemplateParameters();
			return parameters.length == 2 && parameters[1].isParameterPack();
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
								if (m instanceof ICPPFunctionTemplate && !m.isImplicit()
										&& (isOneParameter((ICPPFunctionTemplate) m)
												|| isTwoParameter((ICPPFunctionTemplate) m))
										&& !SemanticQueries.isCopyOrMoveConstructor(m)) {
									ICPPParameter[] parameters = m.getParameters();
									IType t = parameters[0].getType();
									IType refType = SemanticUtil.getNestedType(t, SemanticUtil.TDEF);
									if (refType instanceof ICPPReferenceType
											&& ((ICPPReferenceType) refType).isRValueReference()) {
										IType base = SemanticUtil.getNestedType(t, SemanticUtil.REF);
										if (base instanceof ICPPTemplateTypeParameter && (parameters.length == 1
												|| (parameters.length == 2 && parameters[1].isParameterPack()))) {
											reportProblem(CTOR_TEMPLATE_ID, m, element);
										}
									}
								}
							}
						}
					}
				}
			} catch (InterruptedException |

					CoreException e) {
				ArtemisCoreActivator.log(e);
			}
			return PROCESS_CONTINUE;
		}
	}
}
