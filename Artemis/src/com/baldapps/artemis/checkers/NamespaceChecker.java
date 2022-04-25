/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.baldapps.artemis.checkers;

import java.util.regex.Pattern;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

public class NamespaceChecker extends AbstractIndexAstChecker {
	public static final String STD_NAMESPACE_ID = "com.baldapps.artemis.checkers.StdNamespaceProblem"; //$NON-NLS-1$
	public static final String NAMESPACE_NAME_ID = "com.baldapps.artemis.checkers.NamespaceNameProblem"; //$NON-NLS-1$
	public static final String PARAM_EXCEPT_ARG_LIST = "exceptions"; //$NON-NLS-1$
	public static final String PARAM_NAMESPACE_PATTERN = "patternNamespace"; //$NON-NLS-1$

	private Pattern namespacePattern;
	private String parameterNamespace;

	@Override
	public void processAst(IASTTranslationUnit ast) {
		IProblem pt = getProblemById(NAMESPACE_NAME_ID, getFile());
		parameterNamespace = (String) getPreference(pt, PARAM_NAMESPACE_PATTERN);
		namespacePattern = Pattern.compile(parameterNamespace);
		ast.accept(new ASTVisitor() {
			{
				shouldVisitNamespaces = true;
			}

			@Override
			public int visit(ICPPASTNamespaceDefinition declaration) {
				//verify naming convention
				IASTName name = declaration.getName();
				final IProblem pt = getProblemById(NAMESPACE_NAME_ID, getFile());
				String stringName = new String(declaration.getName().getSimpleID());
				if (!namespacePattern.matcher(stringName).find() && !isFilteredArg(stringName, pt)) {
					reportProblem(pt, declaration.getName(), name, parameterNamespace);
				}
				//verify hook std namespace
				if (CharArrayUtils.equals(name.getSimpleID(), "std".toCharArray())) {
					IASTDeclaration[] decls = declaration.getDeclarations();
					if (decls == null || decls.length == 0) {
						return PROCESS_CONTINUE;
					}
					for (IASTDeclaration d : decls) {
						if (d instanceof ICPPASTTemplateSpecialization || d instanceof IASTProblemDeclaration) {
							return PROCESS_CONTINUE;
						}
						if (d instanceof ICPPASTTemplateDeclaration) {
							IASTDeclaration decl = ((ICPPASTTemplateDeclaration) d).getDeclaration();
							if (decl instanceof IASTSimpleDeclaration) {
								IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration) decl).getDeclSpecifier();
								if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
									IASTName n = ((ICPPASTCompositeTypeSpecifier) declSpec).getName();
									if (!(n instanceof ICPPASTTemplateId)) {
										reportProblem(STD_NAMESPACE_ID, declaration);
										return PROCESS_CONTINUE;
									}
								} else {
									reportProblem(STD_NAMESPACE_ID, declaration);
									return PROCESS_CONTINUE;
								}
							} else {
								reportProblem(STD_NAMESPACE_ID, declaration);
								return PROCESS_CONTINUE;
							}
						} else {
							reportProblem(STD_NAMESPACE_ID, declaration);
							return PROCESS_CONTINUE;
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		if (problem.getId().equals(NAMESPACE_NAME_ID)) {
			addPreference(problem, PARAM_NAMESPACE_PATTERN,
					CheckersMessages.NamingConventionClassesChecker_NamespaceLabelNamePattern, "[a-z]"); //$NON-NLS-1$
			addListPreference(problem, PARAM_EXCEPT_ARG_LIST, CheckersMessages.GenericParameter_ParameterExceptions,
					CheckersMessages.GenericParameter_ParameterExceptionsItem);
		}
	}

	public boolean isFilteredArg(String arg, IProblem pt) {
		return isFilteredArg(arg, pt, PARAM_EXCEPT_ARG_LIST);
	}
}
