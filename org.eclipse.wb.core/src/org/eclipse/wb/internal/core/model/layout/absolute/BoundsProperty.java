/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.model.layout.absolute;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.IntegerPropertyEditor;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Property representing item of complex "Bounds" property.
 *
 * @author mitin_aa
 * @coverage core.model.layout.absolute
 */
public abstract class BoundsProperty<C extends IAbstractComponentInfo> extends Property {
	protected final C m_component;
	private final String m_title;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BoundsProperty(C component, String title) {
		super(IntegerPropertyEditor.INSTANCE);
		m_component = component;
		m_title = title;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Property
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTitle() {
		return m_title;
	}

	@Override
	public boolean isModified() throws Exception {
		return true;
	}

	@Override
	public Object getValue() throws Exception {
		return ScriptUtils.evaluate(m_title, m_component.getModelBounds());
	}

	@Override
	public final void setValue(final Object value) throws Exception {
		if (value != UNKNOWN_VALUE) {
			ExecutionUtils.run(m_component.getUnderlyingModel(), () -> setValue2((Integer) value, m_component.getModelBounds()));
		}
	}

	/**
	 * Utility method to be called from {@link BoundsProperty#setValue(Object)}, bounds updating is
	 * here.
	 */
	public abstract void setValue2(int value, Rectangle modelBounds) throws Exception;
}