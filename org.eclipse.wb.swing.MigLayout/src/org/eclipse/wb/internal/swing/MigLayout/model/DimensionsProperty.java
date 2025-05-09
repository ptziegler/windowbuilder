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
package org.eclipse.wb.internal.swing.MigLayout.model;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.swing.MigLayout.model.ui.ColumnsDialog;
import org.eclipse.wb.internal.swing.MigLayout.model.ui.RowsDialog;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Property} to display/edit {@link List} of {@link MigDimensionInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.model
 */
public final class DimensionsProperty extends Property {
	private final boolean m_horizontal;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DimensionsProperty(MigLayoutInfo layout, boolean horizontal) {
		super(new Editor(layout, horizontal));
		setCategory(PropertyCategory.PREFERRED);
		m_horizontal = horizontal;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Property
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTitle() {
		return m_horizontal ? "columnSpecs" : "rowSpecs";
	}

	@Override
	public Object getValue() throws Exception {
		return UNKNOWN_VALUE;
	}

	@Override
	public boolean isModified() throws Exception {
		return true;
	}

	@Override
	public void setValue(Object value) throws Exception {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editor
	//
	////////////////////////////////////////////////////////////////////////////
	private static class Editor extends TextDialogPropertyEditor {
		private final MigLayoutInfo m_layout;
		private final boolean m_horizontal;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public Editor(MigLayoutInfo layout, boolean horizontal) {
			m_layout = layout;
			m_horizontal = horizontal;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Presentation
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected String getText(Property property) throws Exception {
			List<String> titles = new ArrayList<>();
			List<? extends MigDimensionInfo> dimensions =
					m_horizontal ? m_layout.getColumns() : m_layout.getRows();
			for (MigDimensionInfo dimension : dimensions) {
				titles.add(dimension.getString(true));
			}
			return StringUtils.join(titles.iterator(), "");
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Editing
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void openDialog(Property property) throws Exception {
			if (m_horizontal) {
				new ColumnsDialog(DesignerPlugin.getShell(), m_layout).open();
			} else {
				new RowsDialog(DesignerPlugin.getShell(), m_layout).open();
			}
		}
	}
}
