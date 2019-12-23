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

import org.eclipse.cdt.codan.checkers.CodanCheckersActivator;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.SemanticQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

@SuppressWarnings("restriction")
public class VariablesChecker extends AbstractIndexAstChecker {
	public static final String VAR_MISS_INIT_ID = "com.baldapps.artemis.checkers.MissedInitializationProblem"; //$NON-NLS-1$
	public static final String STATIC_VAR_MISS_INIT_ID = "com.baldapps.artemis.checkers.StaticMissedInitializationProblem"; //$NON-NLS-1$
	public static final String AVOID_GLOBALS_ID = "com.baldapps.artemis.checkers.AvoidGlobalVarsProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simple = (IASTSimpleDeclaration) declaration;
					if (simple.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_auto
							|| simple.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_unspecified
							|| simple.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_register) {
						if (simple.getParent() instanceof ICPPASTCatchHandler)
							return PROCESS_CONTINUE;
						IASTDeclarator decls[] = ((IASTSimpleDeclaration) declaration).getDeclarators();
						if (decls.length == 0)
							return PROCESS_CONTINUE;
						for (IASTDeclarator d : decls) {
							IBinding binding = d.getName().resolveBinding();
							if (binding == null || !(binding instanceof IVariable) || binding instanceof ICPPParameter)
								continue;
							EScopeKind scopeKind = EScopeKind.eLocal;
							String errorId = VAR_MISS_INIT_ID;
							if (binding instanceof IField) {
								//special case: static field variables declared outside classes
								if (!((IField) binding).isStatic())
									continue;
								scopeKind = EScopeKind.eClassType;
								errorId = STATIC_VAR_MISS_INIT_ID;
							}
							try {
								IScope scope = binding.getScope();
								if (scope == null) {
									continue;
								}
								if (scope.getKind() == EScopeKind.eGlobal || scope.getKind() == EScopeKind.eNamespace) {
									reportProblem(AVOID_GLOBALS_ID, declaration, d.getName());
								}
								if (scope.getKind() != scopeKind) {
									continue;
								}
							} catch (DOMException e) {
								CodanCheckersActivator.log(e);
								continue;
							}
							IType type = SemanticUtil.getUltimateType(((IVariable) binding).getType(), true);
							if (binding.getLinkage().getLinkageID() == ILinkage.CPP_LINKAGE_ID
									&& type instanceof ICompositeType) {
								ICompositeType comp = (ICompositeType) type;
								if ((comp.getKey() == ICompositeType.k_struct
										|| comp.getKey() == ICompositeType.k_union) && d.getInitializer() == null) {
									ICPPConstructor[] ctors = ((ICPPClassType) comp).getConstructors();
									boolean found = false;
									for (ICPPConstructor c : ctors) {
										if (!c.isImplicit() && !SemanticQueries.isCopyOrMoveConstructor(c)) {
											found = true;
											break;
										}
									}
									if (!found)
										reportProblem(errorId, declaration, d.getName());
								}
							} else if (d.getInitializer() == null)
								reportProblem(errorId, declaration, d.getName());
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
