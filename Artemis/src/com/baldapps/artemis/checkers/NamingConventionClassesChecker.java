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

import java.util.Stack;
import java.util.regex.Pattern;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

@SuppressWarnings("restriction")
public class NamingConventionClassesChecker extends AbstractIndexAstChecker {
	public static final String CLASS_NAME_ID = "com.baldapps.artemis.checkers.ClassNameProblem"; //$NON-NLS-1$
	public static final String LABELS_ID = "com.baldapps.artemis.checkers.LabelPositionProblem"; //$NON-NLS-1$
	public static final String FIELDS_ID = "com.baldapps.artemis.checkers.FieldsVisibilityProblem"; //$NON-NLS-1$
	public static final String PARAM_CLASS_PATTERN = "patternClass"; //$NON-NLS-1$
	public static final String PARAM_INTERFACE_PATTERN = "patternInterface"; //$NON-NLS-1$
	public static final String PARAM_EXCEPT_ARG_LIST = "exceptions"; //$NON-NLS-1$
	public static final String PARAM_ALLOW_PROTECTED = "allowProtected"; //$NON-NLS-1$

	private Pattern patternClass;
	private String parameterClass;
	private Pattern patternInterface;
	private String parameterInterface;
	private boolean allowProtected;

	@Override
	public void processAst(IASTTranslationUnit ast) {
		if (!ast.isHeaderUnit())
			return;
		IProblem pt = getProblemById(CLASS_NAME_ID, getFile());
		parameterClass = (String) getPreference(pt, PARAM_CLASS_PATTERN);
		patternClass = Pattern.compile(parameterClass);
		parameterInterface = (String) getPreference(pt, PARAM_INTERFACE_PATTERN);
		patternInterface = Pattern.compile(parameterInterface);
		pt = getProblemById(FIELDS_ID, getFile());
		allowProtected = (Boolean) getPreference(pt, PARAM_ALLOW_PROTECTED);
		ast.accept(new NodeVisitor());
	}

	private static class LabelOrder {
		public static final int NO_VALID = -1;
		private int latestKind = NO_VALID;
	}

	private class NodeVisitor extends ASTVisitor {

		Stack<LabelOrder> classStack = new Stack<>();

		public NodeVisitor() {
			shouldVisitDeclarations = true;
			shouldVisitDeclSpecifiers = true;
		}

		@Override
		public int visit(IASTDeclSpecifier element) {
			if (element instanceof ICPPASTCompositeTypeSpecifier) {
				ICPPASTCompositeTypeSpecifier clazz = (ICPPASTCompositeTypeSpecifier) element;
				if (clazz.getKey() == ICPPASTCompositeTypeSpecifier.k_class) {
					classStack.push(new LabelOrder());
					String name = new String(clazz.getName().getSimpleID());
					IBinding binding = clazz.getName().resolveBinding();
					boolean isInterface = false;
					boolean hasFieldsNotPrivate = false;
					if (binding instanceof ICPPClassType) {
						ICPPMethod[] methods = ((ICPPClassType) binding).getDeclaredMethods();
						ICPPField[] fields = ((ICPPClassType) binding).getDeclaredFields();
						for (ICPPMethod m : methods) {
							if (m.isPureVirtual()) {
								isInterface = true;
								break;
							}
						}
						for (ICPPField f : fields) {
							if (f.getVisibility() != ICPPMember.v_private && !f.isConstexpr() && !f.isExtern()
									&& !f.isExternC()) {
								if (f.getVisibility() == ICPPMember.v_protected && allowProtected)
									continue;
								if (!f.isStatic()) {
									hasFieldsNotPrivate = true;
									break;
								} else {
									IType type = f.getType();
									if (!SemanticUtil.isConst(type)) {
										hasFieldsNotPrivate = true;
										break;
									}
								}
							}
						}
					}
					final IProblem pt = getProblemById(CLASS_NAME_ID, getFile());
					if (isInterface) {
						if (!patternInterface.matcher(name).find() && !isFilteredArg(name, pt)) {
							reportProblem(pt, clazz.getName(), name, parameterInterface);
						}
					} else {
						if (!patternClass.matcher(name).find() && !isFilteredArg(name, pt)) {
							reportProblem(pt, clazz.getName(), name, parameterClass);
						}
					}
					if (hasFieldsNotPrivate) {
						reportProblem(FIELDS_ID, clazz.getName(), name);
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTDeclaration element) {
			if (element instanceof ICPPASTVisibilityLabel) {
				ICPPASTVisibilityLabel curLabel = (ICPPASTVisibilityLabel) element;
				if (classStack.isEmpty())
					return PROCESS_CONTINUE;
				LabelOrder labels = classStack.peek();
				if (curLabel.getVisibility() == ICPPASTVisibilityLabel.v_protected
						&& labels.latestKind == ICPPASTVisibilityLabel.v_private) {
					reportProblem(LABELS_ID, element);
					return PROCESS_CONTINUE;
				} else if (curLabel.getVisibility() == ICPPASTVisibilityLabel.v_public
						&& labels.latestKind != ICPPASTVisibilityLabel.v_public
						&& labels.latestKind != LabelOrder.NO_VALID) {
					reportProblem(LABELS_ID, element);
					return PROCESS_CONTINUE;
				}
				labels.latestKind = curLabel.getVisibility();
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTDeclSpecifier element) {
			if (element instanceof ICPPASTCompositeTypeSpecifier && !classStack.isEmpty()) {
				classStack.pop();
			}
			return PROCESS_CONTINUE;
		}
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		if (problem.getId().equals(CLASS_NAME_ID)) {
			addPreference(problem, PARAM_CLASS_PATTERN,
					CheckersMessages.NamingConventionClassesChecker_ClassLabelNamePattern, "^[A-Z]"); //$NON-NLS-1$
			addPreference(problem, PARAM_INTERFACE_PATTERN,
					CheckersMessages.NamingConventionClassesChecker_InterfaceLabelNamePattern, "^I[A-Z]"); //$NON-NLS-1$
			addListPreference(problem, PARAM_EXCEPT_ARG_LIST, CheckersMessages.GenericParameter_ParameterExceptions,
					CheckersMessages.GenericParameter_ParameterExceptionsItem);
		} else if (problem.getId().equals(FIELDS_ID)) {
			addPreference(problem, PARAM_ALLOW_PROTECTED,
					CheckersMessages.NamingConventionClassesChecker_AllowProtected, Boolean.TRUE); //$NON-NLS-1$
		}
	}

	public boolean isFilteredArg(String arg, IProblem pt) {
		return isFilteredArg(arg, pt, PARAM_EXCEPT_ARG_LIST);
	}
}
