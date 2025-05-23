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
package org.eclipse.wb.internal.core.model.property.order;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;

import org.eclipse.jface.window.Window;

/**
 * Implementation of {@link PropertyEditor} for {@link TabOrderProperty}.
 *
 * @author lobas_av
 * @coverage core.model.property.order
 */
public final class TabOrderPropertyEditor extends TextDialogPropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new TabOrderPropertyEditor();

	private TabOrderPropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getText(Property property) throws Exception {
		TabOrderProperty orderProperty = (TabOrderProperty) property;
		return orderProperty.getDisplayText();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void openDialog(Property property) throws Exception {
		TabOrderInfo orderInfo = (TabOrderInfo) property.getValue();
		ReorderDialog dialog = new ReorderDialog(DesignerPlugin.getShell(), orderInfo);
		if (dialog.open() == Window.OK) {
			property.setValue(orderInfo);
		}
	}
}