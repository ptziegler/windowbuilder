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
package org.eclipse.wb.internal.rcp.model.forms.layout.table;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutAssistantSupport;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import java.util.List;

/**
 * Assistant pages provider for {@link TableWrapLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class TableWrapLayoutAssistant extends LayoutAssistantSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TableWrapLayoutAssistant(LayoutInfo layout) {
		super(layout);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Pages
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected AbstractAssistantPage createLayoutPage(Composite parent) {
		return new TableWrapLayoutAssistantPage(parent, m_layout);
	}

	@Override
	protected AbstractAssistantPage createConstraintsPage(Composite parent, List<ObjectInfo> objects) {
		List<LayoutDataInfo> dataList = getDataList(objects);
		return new TableWrapLayoutDataAssistantPage(parent, dataList);
	}
}
