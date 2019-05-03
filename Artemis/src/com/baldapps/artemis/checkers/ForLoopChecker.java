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

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVariableReadWriteFlags;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;

import com.baldapps.artemis.utils.IndexUtils;

@SuppressWarnings("restriction")
public class ForLoopChecker extends AbstractIndexAstChecker {
	public static final String FLOAT_COUNTER_ID = "com.baldapps.artemis.checkers.FloatCounterProblem"; //$NON-NLS-1$
	public static final String BREAK_IN_LOOP_ID = "com.baldapps.artemis.checkers.BreakInLoopProblem"; //$NON-NLS-1$
	public static final String CNT_MODIFICATION_ID = "com.baldapps.artemis.checkers.CounterModificationProblem"; //$NON-NLS-1$

	private class ForVisitor extends ASTVisitor {
		private final IBinding binding;

		public ForVisitor(IBinding b) {
			shouldVisitNames = true;
			binding = b;
		}

		@Override
		public int visit(IASTName name) {
			IBinding currentBinding = name.resolveBinding();
			if (currentBinding != null && !(currentBinding instanceof IProblemBinding) && IndexUtils
					.areEquivalentBindings(currentBinding, binding, name.getTranslationUnit().getIndex())) {
				if ((CPPVariableReadWriteFlags.getReadWriteFlags(name) & PDOMName.WRITE_ACCESS) != 0) {
					reportProblem(CNT_MODIFICATION_ID, name, ASTStringUtil.getSimpleName(name));
					return PROCESS_ABORT;
				}
			}
			return PROCESS_CONTINUE;
		}
	}

	private class FindCounterVisitor extends ASTVisitor {

		private IASTName name;

		public FindCounterVisitor() {
			shouldVisitNames = true;
			name = null;
		}

		public IASTName getName() {
			return name;
		}

		@Override
		public int visit(IASTName name) {
			IBinding currentBinding = name.resolveBinding();
			if (currentBinding != null && !(currentBinding instanceof IProblemBinding)) {
				if ((CPPVariableReadWriteFlags.getReadWriteFlags(name) & PDOMName.WRITE_ACCESS) != 0) {
					this.name = name;
					return PROCESS_ABORT;
				}
			}
			return PROCESS_CONTINUE;
		}
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			private boolean isEnclosedInFor(IASTNode node) {
				while (node != null && !IASTForStatement.class.isInstance(node)) {
					node = node.getParent();
					if (IASTSwitchStatement.class.isInstance(node) || IASTWhileStatement.class.isInstance(node)
							|| IASTDoStatement.class.isInstance(node))
						break;
				}
				return IASTForStatement.class.isInstance(node);
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTForStatement) {
					IASTExpression iteration = ((IASTForStatement) statement).getIterationExpression();
					if (iteration != null) {
						IType type = iteration.getExpressionType();
						if (isFloat(type)) {
							reportProblem(FLOAT_COUNTER_ID, statement);
						}
					}
					FindCounterVisitor counterVisitor = new FindCounterVisitor();
					iteration.accept(counterVisitor);
					IASTName n = counterVisitor.getName();
					if (n != null) {
						IBinding binding = n.resolveBinding();
						if (binding != null && !(binding instanceof IProblemBinding)) {
							ForVisitor v = new ForVisitor(binding);
							IASTStatement body = ((IASTForStatement) statement).getBody();
							if (body != null)
								body.accept(v);
						}
					}
				} else if (statement instanceof IASTBreakStatement) {
					if (isEnclosedInFor(statement)) {
						reportProblem(BREAK_IN_LOOP_ID, statement);
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	private boolean isFloat(IType type) {
		type = SemanticUtil.getUltimateType(type, true);
		if (!(type instanceof IBasicType)) {
			return false;
		}
		IBasicType.Kind k = ((IBasicType) type).getKind();
		switch (k) {
		case eFloat:
		case eDouble:
		case eFloat128:
			return true;
		default:
			return false;
		}
	}
}
