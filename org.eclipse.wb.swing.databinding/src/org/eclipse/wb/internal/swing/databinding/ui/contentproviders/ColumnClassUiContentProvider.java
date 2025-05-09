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
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassUiContentProvider;
import org.eclipse.wb.internal.swing.databinding.model.bindings.ColumnBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

/**
 * {@link ColumnBindingInfo} {@code class} attribute editor.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public class ColumnClassUiContentProvider extends ChooseClassUiContentProvider {
	private final ColumnBindingInfo m_binding;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnClassUiContentProvider(ChooseClassConfiguration configuration,
			ColumnBindingInfo binding) {
		super(configuration);
		m_binding = binding;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
		IGenericType columnType = m_binding.getColumnType();
		setClassName(columnType == null ? "N/S" : columnType.getFullTypeName());
	}

	@Override
	public void saveToObject() throws Exception {
		String className = getClassName();
		// check set or clear value
		if ("N/S".equals(className)) {
			m_binding.setColumnType(null);
		} else {
			m_binding.setColumnType(new ClassGenericType(null, className, "???"));
		}
	}
}