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
package org.eclipse.wb.core.eval;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Implementations of this interface are used during evaluating of {@link Expression} by
 * {@link AstEvaluationEngine}.
 *
 * They are contributed using extension point, so it is possible to extend set of supported
 * expressions.
 *
 * @author scheglov_ke
 */
public interface IExpressionEvaluator {
	/**
	 * @return value of given {@link Expression} or {@link AstEvaluationEngine#UNKNOWN}.
	 */
	Object evaluate(EvaluationContext context,
			Expression expression,
			ITypeBinding typeBinding,
			String typeQualifiedName) throws Exception;
}
