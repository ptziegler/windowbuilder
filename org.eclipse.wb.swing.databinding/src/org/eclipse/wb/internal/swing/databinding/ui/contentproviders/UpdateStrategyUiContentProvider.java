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

import org.eclipse.wb.internal.core.databinding.ui.editor.DialogFieldUiContentProvider;
import org.eclipse.wb.internal.core.utils.dialogfields.ComboDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.swing.databinding.Messages;
import org.eclipse.wb.internal.swing.databinding.model.bindings.UpdateStrategyInfo;

import org.eclipse.swt.SWT;

/**
 * Content provider for edit (choose strategy type over combo) {@link UpdateStrategyInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class UpdateStrategyUiContentProvider extends DialogFieldUiContentProvider {
	private final UpdateStrategyInfo m_strategyInfo;
	private final ComboDialogField m_dialogField;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public UpdateStrategyUiContentProvider(UpdateStrategyInfo strategyInfo) {
		m_strategyInfo = strategyInfo;
		m_dialogField = new ComboDialogField(SWT.BORDER | SWT.READ_ONLY);
		m_dialogField.setLabelText(Messages.UpdateStrategyUiContentProvider_label);
		m_dialogField.setItems(UpdateStrategyInfo.VALUES);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractUIContentProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public DialogField getDialogField() {
		return m_dialogField;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
		m_dialogField.selectItem(m_strategyInfo.getStrategyValue());
	}

	@Override
	public void saveToObject() throws Exception {
		m_strategyInfo.setStrategyValue(m_dialogField.getText());
	}
}