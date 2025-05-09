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
package org.eclipse.wb.internal.core.editor.palette.dialogs;

import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.palette.PluginPalettePreferences;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.FontDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.LayoutDialogFieldGroup;
import org.eclipse.wb.internal.core.utils.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.gef.ui.palette.PaletteViewerPreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

/**
 * Dialog for modifying palette settings.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public final class PalettePreferencesDialog extends AbstractPaletteDialog {
	private final PluginPalettePreferences m_preferences;
	private final BidiMap<Integer, Integer> m_index2layout;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PalettePreferencesDialog(Shell parentShell, PluginPalettePreferences preferences) {
		super(parentShell,
				Messages.PalettePreferencesDialog_shellTitle,
				Messages.PalettePreferencesDialog_title,
				null,
				Messages.PalettePreferencesDialog_message);
		m_preferences = preferences;
		m_index2layout = new DualHashBidiMap<>();
		m_index2layout.put(0, PaletteViewerPreferences.LAYOUT_COLUMNS);
		m_index2layout.put(1, PaletteViewerPreferences.LAYOUT_LIST);
		m_index2layout.put(2, PaletteViewerPreferences.LAYOUT_ICONS);
		m_index2layout.put(3, PaletteViewerPreferences.LAYOUT_DETAILS);
		setShellStyle(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Commits settings from dialog to the preferences.
	 */
	public void commit() {
		m_preferences.setMinColumns(1 + m_minColumnsField.getSelection()[0]);
		m_preferences.setCategoryFont(m_categoryFontField.getFontDataArray());
		m_preferences.setEntryFont(m_entryFontField.getFontDataArray());
		m_preferences.setLayoutSetting(m_index2layout.get(m_layoutDialogField.getSelection()[0]));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	private LayoutDialogFieldGroup m_layoutDialogField;
	private SelectionButtonDialogFieldGroup m_minColumnsField;
	private FontDialogField m_categoryFontField;
	private FontDialogField m_entryFontField;

	@Override
	protected void createControls(Composite container) {
		m_fieldsContainer = container;
		GridLayoutFactory.create(container).columns(2);
		// layout
		{
			m_layoutDialogField = new LayoutDialogFieldGroup(SWT.RADIO,
					new String[]{
							Messages.PalettePreferencesDialog_layouts_1,
							Messages.PalettePreferencesDialog_layouts_2,
							Messages.PalettePreferencesDialog_layouts_3,
							Messages.PalettePreferencesDialog_layouts_4},
					4,
					SWT.SHADOW_ETCHED_IN);
			doCreateField(m_layoutDialogField, "layout");
		}
		// min columns
		{
			m_minColumnsField =
					new SelectionButtonDialogFieldGroup(SWT.RADIO, new String[]{
							Messages.PalettePreferencesDialog_columns_1,
							Messages.PalettePreferencesDialog_columns_2,
							Messages.PalettePreferencesDialog_columns_3,
							Messages.PalettePreferencesDialog_columns_4,
							Messages.PalettePreferencesDialog_columns_5}, 5, SWT.SHADOW_ETCHED_IN);
			doCreateField(m_minColumnsField, Messages.PalettePreferencesDialog_minColumnsNumber);
		}
		// category font
		{
			m_categoryFontField = new FontDialogField();
			doCreateField(m_categoryFontField, Messages.PalettePreferencesDialog_categoryFontLabel);
			m_categoryFontField.setChooseButtonText(Messages.PalettePreferencesDialog_categoryFontChoose);
			m_categoryFontField.setDefaultButtonText(Messages.PalettePreferencesDialog_categoryFontSystem);
		}
		// entry font
		{
			m_entryFontField = new FontDialogField();
			doCreateField(m_entryFontField, Messages.PalettePreferencesDialog_entryFontLabel);
			m_entryFontField.setChooseButtonText(Messages.PalettePreferencesDialog_entryFontChoose);
			m_entryFontField.setDefaultButtonText(Messages.PalettePreferencesDialog_entryFontSystem);
		}
		// initialize fields
		m_minColumnsField.setSelection(new int[]{m_preferences.getMinColumns() - 1});
		m_categoryFontField.setFontDataArray(m_preferences.getCategoryFontDescriptor().getFontData());
		m_entryFontField.setFontDataArray(m_preferences.getEntryFontDescriptor().getFontData());
		m_layoutDialogField.setSelection(new int[] { m_index2layout.getKey(m_preferences.getLayoutType()) });
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private Composite m_fieldsContainer;

	/**
	 * Configures given {@link DialogField} for specific of this dialog.
	 */
	protected final void doCreateField(DialogField dialogField, String labelText) {
		dialogField.setLabelText(labelText);
		dialogField.setDialogFieldListener(m_validateListener);
		DialogFieldUtils.fillControls(m_fieldsContainer, dialogField, 2, 60);
	}
}
