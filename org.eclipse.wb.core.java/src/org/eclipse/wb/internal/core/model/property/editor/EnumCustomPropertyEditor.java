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
package org.eclipse.wb.internal.core.model.property.editor;

import org.eclipse.wb.internal.core.model.property.Property;

/**
 * The customized {@link PropertyEditor} for selecting single value of type {@link Enum}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage core.model.property.editor
 */
public final class EnumCustomPropertyEditor extends AbstractEnumPropertyEditor {
	private Class<?> m_class;
	private Enum<?>[] m_enumElements;

	////////////////////////////////////////////////////////////////////////////
	//
	// Combo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Enum<?>[] getElements(Property property) {
		return m_enumElements;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Configuring
	//
	////////////////////////////////////////////////////////////////////////////
	public void configure(Class<?> enumClass) {
		m_class = enumClass;
		m_enumElements = (Enum<?>[]) m_class.getEnumConstants();
	}

	public <T extends Enum<?>> void configure(T[] enumElements) {
		m_class = enumElements[0].getDeclaringClass();
		m_enumElements = enumElements;
	}
}