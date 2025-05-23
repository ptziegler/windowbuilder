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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties;

import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailValueBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ValuePropertyCodeSupport;

/**
 * Model for detail observable object <code>IValueProperty.value(IValueProperty)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class ValueValuePropertyCodeSupport extends DetailPropertyCodeSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ValueValuePropertyCodeSupport(ViewerPropertySingleSelectionCodeSupport selectionProperty,
			ValuePropertyCodeSupport detailProperty) {
		super("org.eclipse.core.databinding.property.value.IValueProperty",
				"org.eclipse.core.databinding.observable.value.IObservableValue",
				"value",
				selectionProperty,
				detailProperty);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected DetailBeanObservableInfo createDetailObservable() {
		DetailValueBeanObservableInfo detailObservable =
				new DetailValueBeanObservableInfo(m_masterObservable,
						null,
						m_detailProperty.getParserPropertyReference(),
						m_detailProperty.getParserPropertyType());
		detailObservable.setPojoBindable(m_masterObservable.isPojoBindable());
		return detailObservable;
	}
}