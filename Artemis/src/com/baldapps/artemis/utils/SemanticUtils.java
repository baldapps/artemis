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
package com.baldapps.artemis.utils;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.CVTYPE;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

@SuppressWarnings("restriction")
public class SemanticUtils {
	private static final String OPERATOR_ASSIGN = "operator ="; //$NON-NLS-1$

	/**
	 * Check if the method is a copy assignment operator, i.e. an overload of "operator="
	 * with one parameter which is of the same class type.
	 * @param method The method to be checked
	 * @return True if the method is a copy assignment operator, false otherwise
	 * @since 6.7
	 */
	public static boolean isCopyAssignmentOperator(ICPPMethod method) {
		if (!OPERATOR_ASSIGN.equals(method.getName()))
			return false;
		if (!isCallableWithNumberOfArguments(method, 1))
			return false;
		IType firstArgumentType = method.getType().getParameterTypes()[0];
		firstArgumentType = SemanticUtil.getNestedType(firstArgumentType, TDEF);
		if (!(firstArgumentType instanceof ICPPReferenceType))
			return false;
		ICPPReferenceType firstArgReferenceType = (ICPPReferenceType) firstArgumentType;
		firstArgumentType = firstArgReferenceType.getType();
		firstArgumentType = SemanticUtil.getNestedType(firstArgumentType, CVTYPE);
		ICPPClassType classType = method.getClassOwner();
		if (classType instanceof ICPPClassTemplate)
			classType = CPPTemplates.createDeferredInstance((ICPPClassTemplate) classType);
		return firstArgumentType.isSameType(classType);
	}

	private static boolean isCallableWithNumberOfArguments(ICPPFunction function, int numArguments) {
		return function.getParameters().length >= numArguments && function.getRequiredArgumentCount() <= numArguments;
	}

	/**
	 * Checks whether expression references this (directly, by pointer or by reference)
	 */
	public static boolean referencesThis(IASTNode expr) {
		if (expr instanceof IASTLiteralExpression) {
			IASTLiteralExpression litArg = (IASTLiteralExpression) expr;
			if (litArg.getKind() == IASTLiteralExpression.lk_this) {
				return true;
			}
		} else if (expr instanceof ICPPASTUnaryExpression) {
			ICPPASTUnaryExpression unExpr = (ICPPASTUnaryExpression) expr;
			switch (unExpr.getOperator()) {
			case IASTUnaryExpression.op_amper:
			case IASTUnaryExpression.op_star:
			case IASTUnaryExpression.op_bracketedPrimary:
				return referencesThis(unExpr.getOperand());
			}
		}
		return false;
	}

	public static boolean areEquivalentBindings(IBinding binding1, IBinding binding2, IIndex index) {
		if (binding1.equals(binding2)) {
			return true;
		}
		if ((binding1 instanceof IIndexBinding) != (binding2 instanceof IIndexBinding) && index != null) {
			if (binding1 instanceof IIndexBinding) {
				binding2 = index.adaptBinding(binding2);
			} else {
				binding1 = index.adaptBinding(binding1);
			}
			if (binding1 == null || binding2 == null) {
				return false;
			}
			if (binding1.equals(binding2)) {
				return true;
			}
		}
		return false;
	}
}
