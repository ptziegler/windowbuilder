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
package org.eclipse.wb.internal.rcp.databinding.ui.contentproviders;

import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.AbstractViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.EditingSupportInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.VirtualEditingSupportInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for {@link ViewerColumnsUiContentProvider}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class ViewerColumnsConfiguration {
	private final List<VirtualEditingSupportInfo> m_editingSupports = new ArrayList<>();
	private final AbstractViewerInputBindingInfo m_viewerBinding;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewerColumnsConfiguration(AbstractViewerInputBindingInfo viewerBinding,
			VirtualEditingSupportInfo.IElementTypeProvider elementTypeProvider,
			List<WidgetBindableInfo> viewerColumns,
			List<EditingSupportInfo> editingSupports) throws Exception {
		m_viewerBinding = viewerBinding;
		for (WidgetBindableInfo vieweColumn : viewerColumns) {
			boolean newColumn = true;
			// find exist support
			for (EditingSupportInfo editingSupport : editingSupports) {
				if (editingSupport.getViewerColumn() == vieweColumn) {
					newColumn = false;
					m_editingSupports.add(new VirtualEditingSupportInfo(elementTypeProvider, editingSupport));
					break;
				}
			}
			if (newColumn) {
				// create new support
				m_editingSupports.add(new VirtualEditingSupportInfo(elementTypeProvider,
						viewerBinding,
						vieweColumn));
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public List<VirtualEditingSupportInfo> getEditingSupports() {
		return m_editingSupports;
	}

	public void saveObjects() throws Exception {
		// clear old supports
		List<EditingSupportInfo> editingSupports = m_viewerBinding.getEditingSupports();
		editingSupports.clear();
		// add new supports
		for (VirtualEditingSupportInfo editingSupport : m_editingSupports) {
			if (!editingSupport.isEmpty()) {
				editingSupports.add(editingSupport.createOrUpdateEditingSupport());
			}
		}
	}
}