/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.core.model;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.BroadcastSupport;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;

import java.util.Objects;

import javax.swing.AbstractButton;

/**
 * Reference on {@link ObjectInfo}.
 * <p>
 * For {@link ButtonGroupInfo} we want to show its bounds {@link AbstractButton} models as children
 * in component tree, but we can not use {@link ComponentInfo} models itself, because in this case
 * element of tree will be registered with this {@link ComponentInfo} as model. So, when we click on
 * design canvas, instance of {@link ComponentInfo} in {@link ButtonGroupInfo} will be selected, not
 * on the place of "visual" presentation.
 * <p>
 * So, we need some proxy/reference to use instead.
 *
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class ObjectReferenceInfo extends ObjectInfo {
	private final ObjectInfo m_object;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ObjectReferenceInfo(ObjectInfo object) {
		Objects.requireNonNull(object);
		m_object = object;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ObjectReferenceInfo) {
			return ((ObjectReferenceInfo) obj).m_object == m_object;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return m_object.hashCode();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the referenced {@link ObjectInfo}.
	 */
	public ObjectInfo getObject() {
		return m_object;
	}

	@Override
	public BroadcastSupport getBroadcastSupport() {
		return m_object.getBroadcastSupport();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IObjectPresentation getPresentation() {
		return m_object.getPresentation();
	}
}
