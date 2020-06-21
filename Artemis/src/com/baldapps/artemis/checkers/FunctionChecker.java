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

import java.util.Arrays;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

@SuppressWarnings("restriction")
public class FunctionChecker extends AbstractIndexAstChecker {
	public static final String PAR_BY_COPY_ID = "com.baldapps.artemis.checkers.ParByCopyProblem"; //$NON-NLS-1$
	public static final String PARAM_WHITELIST = "whitelist"; //$NON-NLS-1$

	private Object[] list;

	@Override
	public void processAst(IASTTranslationUnit ast) {
		list = (Object[]) getPreference(getProblemById(PAR_BY_COPY_ID, getFile()), PARAM_WHITELIST);
		if (list == null)
			return;
		Arrays.sort(list);
		ast.accept(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration element) {
				if (element instanceof IASTFunctionDefinition) {
					processFunction((IASTFunctionDefinition) element);
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addListPreference(problem, PARAM_WHITELIST, CheckersMessages.FunctionChecker_list,
				CheckersMessages.FunctionChecker_list_item);
	}

	private String getQualifiedName(IASTName name) {
		if (name instanceof ICPPASTQualifiedName) {
			ICPPASTQualifiedName qual = (ICPPASTQualifiedName) name;
			StringBuffer buf = new StringBuffer();
			ICPPASTNameSpecifier[] quals = qual.getQualifier();
			for (int i = 0; i < quals.length; ++i) {
				if (i > 0) {
					buf.append(Keywords.cpCOLONCOLON);
				}
				buf.append(quals[i]);
			}
			buf.append(Keywords.cpCOLONCOLON);
			buf.append(qual.getLastName().getSimpleID());
			return buf.toString();
		} else {
			IBinding binding = name.resolveBinding();
			if (binding instanceof ICPPBinding) {
				try {
					return String.join("::", ((ICPPBinding) binding).getQualifiedName());
				} catch (DOMException e) {
					ArtemisCoreActivator.log(e);
				}
			}
		}
		return null;
	}

	private void processFunction(IASTFunctionDefinition func) {
		IASTFunctionDeclarator declarator = func.getDeclarator();
		if (declarator instanceof IASTStandardFunctionDeclarator) {
			IASTParameterDeclaration[] parans = ((IASTStandardFunctionDeclarator) declarator).getParameters();
			for (IASTParameterDeclaration p : parans) {
				IBinding binding = p.getDeclarator().getName().resolveBinding();
				if (binding instanceof IProblemBinding)
					continue;
				IType type = SemanticUtil.getNestedType(((IParameter) binding).getType(), SemanticUtil.TDEF);
				if (!(type instanceof ICPPReferenceType) && !(type instanceof IPointerType)
						&& !(type instanceof IProblemType)) {
					type = SemanticUtil.getNestedType(type, SemanticUtil.REF | SemanticUtil.PTR | SemanticUtil.ALLCVQ);
					if (!(type instanceof IBasicType) && !(type instanceof IProblemType)) {
						IASTDeclSpecifier declSpecifier = p.getDeclSpecifier();
						if (declSpecifier instanceof ICPPASTNamedTypeSpecifier) {
							ICPPASTNamedTypeSpecifier t = (ICPPASTNamedTypeSpecifier) declSpecifier;
							IASTName name = t.getName();
							String fully = getQualifiedName(name);
							if (fully != null && Arrays.binarySearch(list, fully) < 0) {
								reportProblem(PAR_BY_COPY_ID, p, ASTStringUtil.getSignatureString(p.getDeclarator()));
							}
						}
					}
				}
			}
		}
	}
}
