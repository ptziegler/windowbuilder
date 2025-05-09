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
package org.eclipse.wb.internal.core.utils.binding.editors;

import org.eclipse.wb.internal.core.utils.binding.IDataEditor;
import org.eclipse.wb.internal.core.utils.binding.ValueUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.SelectionButtonDialogFieldGroup;

/**
 * @author lobas_av
 *
 */
public class SelectionButtonGroupEditor implements IDataEditor {
	private final SelectionButtonDialogFieldGroup m_field;
	private final int m_buttonCount;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SelectionButtonGroupEditor(SelectionButtonDialogFieldGroup field, int buttonCount) {
		m_field = field;
		m_buttonCount = buttonCount;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDataEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getValue() {
		boolean[] values = new boolean[m_buttonCount];
		for (int i = 0; i < m_buttonCount; i++) {
			values[i] = m_field.isSelected(i);
		}
		return values;
	}

	@Override
	public void setValue(Object value) {
		// prepare boolean array
		boolean[] values = ValueUtils.objectToBooleanArray(value);
		// set values
		if (values != null && values.length == m_buttonCount) {
			for (int i = 0; i < m_buttonCount; i++) {
				m_field.setSelection(i, values[i]);
			}
		} else {
			for (int i = 0; i < m_buttonCount; i++) {
				m_field.setSelection(i, false);
			}
		}
	}
}