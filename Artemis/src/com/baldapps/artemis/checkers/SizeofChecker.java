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

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

@SuppressWarnings("restriction")
public class SizeofChecker extends AbstractIndexAstChecker {
	public static final String SIZEOF_ARRAY_ID = "com.baldapps.artemis.checkers.SizeofArrayProblem"; //$NON-NLS-1$
	public static final String SIZEOF_NESTED_ID = "com.baldapps.artemis.checkers.SizeofNestedProblem"; //$NON-NLS-1$
	public static final String SIZEOF_VOID_ID = "com.baldapps.artemis.checkers.SizeofVoidProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			private boolean areEquivalentBindings(IBinding binding1, IBinding binding2, IIndex index) {
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

			private IASTExpression unwrapUnaryExpression(IASTUnaryExpression expression) {
				if (expression.getOperand() instanceof IASTUnaryExpression) {
					if (((IASTUnaryExpression) expression.getOperand())
							.getOperator() == IASTUnaryExpression.op_sizeof) {
						reportProblem(SIZEOF_NESTED_ID, expression);
						return null;
					}
					return unwrapUnaryExpression((IASTUnaryExpression) expression.getOperand());
				}
				return expression.getOperand();
			}

			private boolean isVoid(IType type) {
				type = SemanticUtil.getUltimateType(type, true);
				if (!(type instanceof IBasicType)) {
					return false;
				}
				IBasicType.Kind k = ((IBasicType) type).getKind();
				switch (k) {
				case eVoid:
					return true;
				default:
					return false;
				}
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTUnaryExpression
						&& ((IASTUnaryExpression) expression).getOperator() == IASTUnaryExpression.op_sizeof) {
					IASTExpression operand = unwrapUnaryExpression((IASTUnaryExpression) expression);
					if (operand == null)
						return PROCESS_SKIP;
					if (!(operand instanceof IASTIdExpression))
						return PROCESS_SKIP;
					if (isVoid(operand.getExpressionType())) {
						reportProblem(SIZEOF_VOID_ID, expression);
						return PROCESS_SKIP;
					}
					IASTName sizeofParName = ((IASTIdExpression) operand).getName();
					IBinding sizeofParNameBinding = sizeofParName.resolveBinding();
					IASTFunctionDefinition func = CxxAstUtils.getEnclosingFunction(operand);
					if (func == null)
						return PROCESS_SKIP;
					IASTFunctionDeclarator declarator = func.getDeclarator();
					if (!(declarator instanceof IASTStandardFunctionDeclarator))
						return PROCESS_SKIP;
					IASTParameterDeclaration[] parameters = ((IASTStandardFunctionDeclarator) declarator)
							.getParameters();
					for (IASTParameterDeclaration p : parameters) {
						IASTDeclarator parDecl = p.getDeclarator();
						if (parDecl instanceof IASTArrayDeclarator) {
							IBinding parNameBinding = parDecl.getName().resolveBinding();
							if (areEquivalentBindings(parNameBinding, sizeofParNameBinding,
									sizeofParName.getTranslationUnit().getIndex())) {
								reportProblem(SIZEOF_ARRAY_ID, expression, new String(sizeofParName.getSimpleID()));
							}
						}
					}
				} else if (expression instanceof IASTTypeIdExpression) {
					if (((IASTTypeIdExpression) expression).getOperator() == IASTTypeIdExpression.op_sizeof) {
						IASTDeclSpecifier declSpec = ((IASTTypeIdExpression) expression).getTypeId().getDeclSpecifier();
						if (declSpec instanceof IASTSimpleDeclSpecifier
								&& ((IASTSimpleDeclSpecifier) declSpec).getType() == IASTSimpleDeclSpecifier.t_void)
							reportProblem(SIZEOF_VOID_ID, expression);
						return PROCESS_SKIP;
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
