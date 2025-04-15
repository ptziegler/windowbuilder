/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.ast;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

/**
 * Support for using {@link AnonymousClassDeclaration} as {@link TypeDeclaration}.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 * @deprecated Don't use a custom JDT type and work on the {@link AnonymousClassDeclaration directly.
 */
@Deprecated
public class AnonymousTypeDeclaration {
	private static final String ANONYMOUS_TYPE_DECLARATION_PATH = "AnonymousTypeDeclaration2.clazz";
	private static final String KEY = "AnonymousTypeDeclaration";
	private static Class<?> m_class;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private AnonymousTypeDeclaration() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public static boolean is(TypeDeclaration node) {
		return node != null && node.getClass().getName().endsWith("AnonymousTypeDeclaration2");
	}

	public static TypeDeclaration get(ASTNode node) {
		return (TypeDeclaration) node.getProperty(KEY);
	}

	public static TypeDeclaration create(AnonymousClassDeclaration acd) {
		ensureClass();
		try {
			Constructor<?> constructor = m_class.getConstructor(AnonymousClassDeclaration.class);
			return (TypeDeclaration) constructor.newInstance(acd);
		} catch (Throwable e) {
			throw ReflectionUtils.propagate(e);
		}
	}

	private static void ensureClass() {
		if (m_class == null) {
			try (InputStream stream = AnonymousTypeDeclaration.class
					.getResourceAsStream(ANONYMOUS_TYPE_DECLARATION_PATH)) {
				ReflectionUtils.defineClass(TypeDeclaration.class.getClassLoader(), IOUtils.toByteArray(stream));
			} catch (IOException e) {
				ReflectionUtils.propagate(e);
			}
		}
	}
}
