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
package org.eclipse.wb.internal.swing.databinding.model.components;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.variable.AbstractSimpleVariableSupport;
import org.eclipse.wb.internal.core.model.variable.ExposedFieldVariableSupport;
import org.eclipse.wb.internal.core.model.variable.ExposedPropertyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.ThisVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * {@link IReferenceProvider} for provider reference on {@link JavaInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.widgets
 */
public final class JavaInfoReferenceProvider implements IReferenceProvider {
	public static boolean LAZY_DETECTED;
	private JavaInfo m_javaInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JavaInfoReferenceProvider(JavaInfo javaInfo) {
		m_javaInfo = javaInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void setJavaInfo(JavaInfo javaInfo) {
		m_javaInfo = javaInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IReferenceProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getReference() throws Exception {
		return getReference(m_javaInfo);
	}

	public static String getReference(JavaInfo javaInfo) throws Exception {
		VariableSupport variableSupport = javaInfo.getVariableSupport();
		// handle lazy
		if (LAZY_DETECTED && variableSupport instanceof LazyVariableSupport lazyVariableSupport) {
			return lazyVariableSupport.getAccessorReferenceExpression();
		}
		// handle this
		if (variableSupport instanceof ThisVariableSupport) {
			return "this";
		}
		// handle named variable
		if (variableSupport instanceof AbstractSimpleVariableSupport && variableSupport.hasName()) {
			return variableSupport.getName();
		}
		// handle exposed
		if (variableSupport instanceof ExposedPropertyVariableSupport
				|| variableSupport instanceof ExposedFieldVariableSupport) {
			try {
				for (ASTNode node : javaInfo.getRelatedNodes()) {
					if (AstNodeUtils.isVariable(node)) {
						return CoreUtils.getNodeReference(node);
					}
				}
			} catch (Throwable e) {
			}
			String reference = getReference(javaInfo.getParentJava());
			if (reference != null) {
				return reference + "." + variableSupport.getTitle();
			}
		}
		return null;
	}
}