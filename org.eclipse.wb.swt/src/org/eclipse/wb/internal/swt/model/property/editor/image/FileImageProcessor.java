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

import org.eclipse.wb.core.editor.icon.AbstractFileImageProcessor;
import org.eclipse.wb.core.model.IGenericProperty;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Default implementation for handling SWT {@link Image}s via the file system.
 */
public class FileImageProcessor extends AbstractFileImageProcessor {
	@Override
	public boolean process(IGenericProperty property, String[] value) {
		Expression expression = property.getExpression();
		// new Image(...)
		if (expression instanceof ClassInstanceCreation creation) {
			ITypeBinding creationBinding = AstNodeUtils.getTypeBinding(creation);
			if (AstNodeUtils.getFullyQualifiedName(creationBinding, false).equals("org.eclipse.swt.graphics.Image")) {
				String constructorSignature = AstNodeUtils.getCreationSignature(creation);
				// absolute path
				if ("<init>(org.eclipse.swt.graphics.Device,java.lang.String)".equals(constructorSignature)) {
					Expression stringExpression = DomGenerics.arguments(creation).get(1);
					value[0] = prefix + JavaInfoEvaluationHelper.getValue(stringExpression);
					return true;
				}
			}
		}
		// Only here for backwards compatibility
		// SWTResourceManager.getImage(String path)
		if (AstNodeUtils.isMethodInvocation(expression, "org.eclipse.wb.swt.SWTResourceManager",
				"getImage(java.lang.String)")) {
			MethodInvocation invocation = (MethodInvocation) expression;
			Expression stringExpression = DomGenerics.arguments(invocation).get(0);
			value[0] = prefix + JavaInfoEvaluationHelper.getValue(stringExpression);
			return true;
		}
		// LocalResourceManager.create(ImageDescriptor.createFrom(Class, String))
		if (AstNodeUtils.isMethodInvocation(expression, "org.eclipse.jface.resource.ResourceManager",
				"create(org.eclipse.jface.resource.DeviceResourceDescriptor)")) {
			MethodInvocation managerInvocation = (MethodInvocation) expression;
			Expression managerExpression = DomGenerics.arguments(managerInvocation).get(0);
			if (AstNodeUtils.isMethodInvocation(managerExpression, "org.eclipse.jface.resource.ImageDescriptor",
					"createFromFile(java.lang.Class,java.lang.String)")) {
				MethodInvocation invocation = (MethodInvocation) managerExpression;
				Object clazz = JavaInfoEvaluationHelper.getValue(DomGenerics.arguments(invocation).get(0));
				if (clazz == null) {
					Object path = JavaInfoEvaluationHelper.getValue(DomGenerics.arguments(invocation).get(1));
					value[0] = prefix + path;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean postOpen(IGenericProperty property, String path, String[] value) {
		JavaInfo javaInfo = property.getJavaInfo();
		String pathSource = StringConverter.INSTANCE.toJavaSource(javaInfo, path);
		if (ImagePropertyEditor.useResourceManager(javaInfo)) {
			ExecutionUtils
					.runRethrow(() -> value[0] = ImagePropertyEditor.getInvocationSource(javaInfo, null, pathSource));
		} else {
			value[0] = "new org.eclipse.swt.graphics.Image(null, " + pathSource + ")";
		}
		return true;
	}
}
