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
package org.eclipse.wb.internal.core.editor.errors;

import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.apache.commons.lang3.ArrayUtils;

/**
 * {@link JavaWarningComposite} for {@link ICoreExceptionConstants#PARSER_NOT_GUI}.
 *
 * @author scheglov_ke
 * @coverage core.editor.errors
 */
public final class NotUiJavaWarningComposite extends JavaWarningComposite {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public NotUiJavaWarningComposite(Composite parent, int style) {
		super(parent, style);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// UI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected int getNumButtons() {
		return 3;
	}

	@Override
	protected void createButtons(Composite buttonsComposite) {
		{
			Button toolkitsButton = new Button(buttonsComposite, SWT.NONE);
			GridDataFactory.create(toolkitsButton).fill();
			toolkitsButton.setText("Open UI Toolkits");
			toolkitsButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					PreferencesUtil.createPreferenceDialogOn(
							getShell(),
							"org.eclipse.wb.internal.discovery.ui.preferences.ToolkitsPreferencePage",
							ArrayUtils.EMPTY_STRING_ARRAY,
							null).open();
				}
			});
		}
		super.createButtons(buttonsComposite);
	}
}
