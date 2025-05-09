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
package org.eclipse.wb.internal.core.editor.multi;

import org.eclipse.wb.core.editor.IDesignerEditor;
import org.eclipse.wb.core.editor.IEditorPage;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.HashMap;
import java.util.Map;

/**
 * "Source" page of {@link DesignerEditor}.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public final class SourcePage implements IEditorPage {
	private final DesignerEditor m_editor;
	private final Map<String, IAction> m_idToTextEditorAction = new HashMap<>();
	private Composite m_composite;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SourcePage(DesignerEditor editor) {
		m_editor = editor;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void initialize(IDesignerEditor designerPage) {
	}

	@Override
	public void dispose() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Activation
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean m_active = false;

	@Override
	public void handleActiveState(boolean activate) {
		if (m_active == activate) {
			return;
		}
		m_active = activate;
		//
		updateSourceActions(m_active);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Control createControl(Composite parent) {
		m_composite = new Composite(parent, SWT.NONE);
		m_composite.setLayout(new FillLayout());
		m_editor.super_createPartControl(m_composite);
		return m_composite;
	}

	@Override
	public Control getControl() {
		return m_composite;
	}

	@Override
	public void setFocus() {
		getTextWidget().setFocus();
	}

	private StyledText getTextWidget() {
		return m_editor.super_getSourceViewer().getTextWidget();
	}

	/**
	 * @return <code>true</code> if page is active.
	 */
	public boolean isActive() {
		return getTextWidget().isFocusControl();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getName() {
		return Messages.SourcePage_name;
	}

	@Override
	public Image getImage() {
		return DesignerPlugin.getImage("editor_source_page.png");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void setAction(String actionID, IAction action) {
		if (action != null
				&& !"save".equals(actionID)
				&& !"undo".equals(actionID)
				&& !"redo".equals(actionID)) {
			m_idToTextEditorAction.put(actionID, action);
		}
	}

	/**
	 * Installs/removes source editor actions depending on given flag. We need this to prevent
	 * activation of source editor actions on Design page, such as auto-completion (Ctrl+Space).
	 */
	private void updateSourceActions(boolean install) {
		for (Map.Entry<String, IAction> entry : m_idToTextEditorAction.entrySet()) {
			String id = entry.getKey();
			IAction action = entry.getValue();
			action.setEnabled(install);
			if (install) {
				m_editor.super_setAction(id, action);
			} else {
				m_editor.super_setAction(id, null);
			}
		}
	}
}