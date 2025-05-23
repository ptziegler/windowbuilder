/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.model.property.editor.image;

import org.eclipse.wb.core.editor.icon.AbstractClasspathImageProcessor;
import org.eclipse.wb.core.model.IGenericProperty;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Default implementation for handling JFace {@link ImageDescriptor}s via the
 * classpath.
 */
public class ClasspathImageDescriptorProcessor extends AbstractClasspathImageProcessor {
	@Override
	public boolean process(IGenericProperty property, String[] value) {
		Expression expression = property.getExpression();
		// ResourceManager.getImageDescriptor(String path)
		if (AstNodeUtils.isMethodInvocation(expression, "org.eclipse.jface.resource.ImageDescriptor",
				"createFromFile(java.lang.Class,java.lang.String)")) {
			MethodInvocation invocation = (MethodInvocation) expression;
			Expression locationExpression = DomGenerics.arguments(invocation).get(0);
			if (!(locationExpression instanceof NullLiteral)) {
				Expression pathExpression = DomGenerics.arguments(invocation).get(1);
				Object path = JavaInfoEvaluationHelper.getValue(pathExpression);
				value[0] = prefix + path;
				return true;
			}
		}
		// ResourceManager.getImageDescriptor(Class class, String path)
		if (AstNodeUtils.isMethodInvocation(expression, "org.eclipse.wb.swt.ResourceManager",
				"getImageDescriptor(java.lang.Class,java.lang.String)")) {
			MethodInvocation invocation = (MethodInvocation) expression;
			Expression stringExpression = DomGenerics.arguments(invocation).get(1);
			value[0] = prefix + JavaInfoEvaluationHelper.getValue(stringExpression);
			return true;
		}
		return false;
	}

	@Override
	public boolean postOpen(IGenericProperty property, String path, String[] value) {
		String pathSource = StringConverter.INSTANCE.toJavaSource(property.getJavaInfo(), path);
		value[0] = ImageDescriptorPropertyEditor.getInvocationSource("{wbp_classTop}", pathSource);
		return true;
	}
}
