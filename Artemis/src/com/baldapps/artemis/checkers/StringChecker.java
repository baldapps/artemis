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

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;

@SuppressWarnings("restriction")
public class StringChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "com.baldapps.artemis.checkers.CstrProblem"; //$NON-NLS-1$

	private static final String[] C_STR = { "std", "basic_string", "c_str" };
	private static final String[] C_STR_C11 = { "std", "__cxx11", "basic_string", "c_str" };

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof ICPPASTFunctionCallExpression) {
					ICPPASTFunctionCallExpression fCall = (ICPPASTFunctionCallExpression) expression;
					IASTExpression fNameExp = fCall.getFunctionNameExpression();
					if (fCall.getParent() instanceof ICPPASTFunctionCallExpression)
						return PROCESS_SKIP;
					IBinding fBinding = null;
					if (fNameExp instanceof IASTFieldReference) {
						IASTFieldReference fName = (IASTFieldReference) fNameExp;
						fBinding = fName.getFieldName().resolveBinding();
					}
					if (fBinding != null && fBinding instanceof ICPPMethod) {
						ICPPMethod method = (ICPPMethod) fBinding;
						try {
							if (Arrays.equals(method.getQualifiedName(), C_STR)
									|| Arrays.equals(method.getQualifiedName(), C_STR_C11)) {
								fCall.getValueCategory();
								if (!fCall.isLValue() && fCall.getImplicitDestructorNames().length > 0
										|| ((IASTFieldReference) fNameExp)
												.getFieldOwner() instanceof ICPPASTFunctionCallExpression) {
									IASTBinaryExpression binary = ASTQueries.findAncestorWithType(expression,
											IASTBinaryExpression.class);
									if (binary != null && binary.getOperator() == IASTBinaryExpression.op_assign)
										reportProblem(ERR_ID, expression);
									else {
										IASTEqualsInitializer eqInit = ASTQueries.findAncestorWithType(expression,
												IASTEqualsInitializer.class);
										if (eqInit != null)
											reportProblem(ERR_ID, expression);
									}
								}
							}
						} catch (DOMException e) {
							ArtemisCoreActivator.log(e);
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
