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

import java.util.Arrays;
import java.util.List;

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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.SemanticQueries;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;
import org.eclipse.core.runtime.CoreException;

import com.baldapps.artemis.utils.IndexUtils;
import com.baldapps.artemis.utils.SemanticUtils;

@SuppressWarnings("restriction")
public class MemberClassesChecker extends AbstractIndexAstChecker {
	public static final String CTOR_DTOR_INLINE = "com.baldapps.artemis.checkers.CtorDtorInlineProblem"; //$NON-NLS-1$
	public static final String VIRTUAL_INLINE = "com.baldapps.artemis.checkers.VirtualInlineProblem"; //$NON-NLS-1$
	public static final String IMPLICIT_VIRTUAL = "com.baldapps.artemis.checkers.ImplicitVirtualProblem"; //$NON-NLS-1$
	public static final String AVOID_OVERLOADS = "com.baldapps.artemis.checkers.AvoidOverloadsProblem"; //$NON-NLS-1$
	public static final String USER_CTOR = "com.baldapps.artemis.checkers.UserCtorProblem"; //$NON-NLS-1$
	public static final String ABSTRACT_NO_COPY = "com.baldapps.artemis.checkers.AbstractNoCopyProblem"; //$NON-NLS-1$
	public static final String MULTIPLE_INHERITANCE = "com.baldapps.artemis.checkers.AvoidMultipleInheritanceProblem"; //$NON-NLS-1$
	public static final String COPY_CTOR_ONLY = "com.baldapps.artemis.checkers.CopyCtorOnlyProblem"; //$NON-NLS-1$
	public static final String ASSIGN_OP_ONLY = "com.baldapps.artemis.checkers.AssignOpOnlyProblem"; //$NON-NLS-1$

	private static final List<String> illOverloads = Arrays.asList("operator &&", "operator ||", "operator ,");

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
							if (classType.getBases().length >= 2) {
								reportProblem(MULTIPLE_INHERITANCE, element,
										ASTStringUtil.getSimpleName(clazz.getName()));
							}
							ObjectSet<ICPPMethod> methods = ClassTypeHelper.getOwnMethods(classType);
							boolean isAbstract = false;
							boolean hasAccessibleCopyCtor = false;
							boolean hasAccessibleAssignOp = false;
							boolean hasUserDtor = false;
							boolean hasUserCopyCtor = false;
							boolean hasUserOpAssign = false;
							for (ICPPMethod m : methods) {
								if (m.isDestructor() && !m.isImplicit()) {
									hasUserDtor = true;
								}
								if (m.isDestructor() && m.isInline() && !SemanticUtils.isTemplate(classType)) {
									reportProblem(CTOR_DTOR_INLINE, m, element,
											ASTStringUtil.getSimpleName(clazz.getName()));
								}
								boolean isVirtual = ClassTypeHelper.isVirtual(m);
								if (!m.isVirtual() && isVirtual)
									reportProblem(IMPLICIT_VIRTUAL, m, element, m.getName());
								if (isVirtual && m.isInline() && !m.isFinal() && !SemanticUtils.isTemplate(classType))
									reportProblem(VIRTUAL_INLINE, m, element, m.getName());
								if (illOverloads.contains(m.getName()))
									reportProblem(AVOID_OVERLOADS, m, element, m.getName());
								if (m.isPureVirtual())
									isAbstract = true;
								if (!m.isDeleted() && SemanticUtils.isCopyAssignmentOperator(m)) {
									if (m.getVisibility() == ICPPMember.v_public)
										hasAccessibleAssignOp = true;
									if (!m.isImplicit())
										hasUserOpAssign = true;
								}
							}
							boolean hasAtLeastOneUserCtor = false;
							ICPPConstructor[] ctors = classType.getConstructors();
							for (ICPPConstructor m : ctors) {
								boolean isMoveCtor = false;
								boolean isCopyCtor = false;
								if (!m.isImplicit() && m.isInline() && !SemanticUtils.isTemplate(classType)) {
									reportProblem(CTOR_DTOR_INLINE, m, element,
											ASTStringUtil.getSimpleName(clazz.getName()));
								}
								isMoveCtor = SemanticQueries.isMoveConstructor(m);
								isCopyCtor = SemanticQueries.isCopyConstructor(m);
								if (!m.isImplicit() && !isMoveCtor && !isCopyCtor) {
									hasAtLeastOneUserCtor = true;
								}
								if (isCopyCtor && !m.isDeleted() && m.getVisibility() == ICPPMember.v_public) {
									hasAccessibleCopyCtor = true;
								}
								if (isCopyCtor && !m.isImplicit() && !m.isDeleted()) {
									hasUserCopyCtor = true;
								}
							}
							if (!hasAtLeastOneUserCtor) {
								reportProblem(USER_CTOR, element, ASTStringUtil.getSimpleName(clazz.getName()));
							}
							if ((hasAccessibleAssignOp || hasAccessibleCopyCtor) && isAbstract) {
								reportProblem(ABSTRACT_NO_COPY, element, ASTStringUtil.getSimpleName(clazz.getName()));
							}
							if (hasUserCopyCtor && (!hasUserDtor || !hasUserOpAssign)) {
								reportProblem(COPY_CTOR_ONLY, element, ASTStringUtil.getSimpleName(clazz.getName()));
							}
							if (hasUserOpAssign && (!hasUserDtor || !hasUserCopyCtor)) {
								reportProblem(ASSIGN_OP_ONLY, element, ASTStringUtil.getSimpleName(clazz.getName()));
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
