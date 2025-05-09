/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.component;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.broadcast.EvaluationEventListener;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

import javax.swing.JTable;

/**
 * Model for {@link JTable}.
 *
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class JTableInfo extends ContainerInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JTableInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		evaluateColumnModelInvocations();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Support for getColumnModel() invocations
	//
	////////////////////////////////////////////////////////////////////////////
	private void evaluateColumnModelInvocations() {
		addBroadcastListener(new EvaluationEventListener() {
			@Override
			public void evaluateAfter(EvaluationContext context, ASTNode node) throws Exception {
				if (node instanceof ExpressionStatement expressionStatement) {
					if (AstNodeUtils.isMethodInvocation(
							expressionStatement.getExpression(),
							"javax.swing.JTable",
							"setModel(javax.swing.table.TableModel)")) {
						evaluateColumnModelInvocations(context);
					}
				}
			}
		});
	}

	private void evaluateColumnModelInvocations(EvaluationContext context) throws Exception {
		for (MethodInvocation invocationPart : getMethodInvocations("getColumnModel()")) {
			Statement statement = AstNodeUtils.getEnclosingStatement(invocationPart);
			if (statement instanceof ExpressionStatement expressionStatement) {
				if (expressionStatement.getExpression() instanceof MethodInvocation) {
					MethodInvocation invocation = (MethodInvocation) expressionStatement.getExpression();
					AstEvaluationEngine.evaluate(context, invocation);
				}
			}
		}
	}
}
