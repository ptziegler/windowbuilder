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
package org.eclipse.wb.internal.rcp.model.layout.grid;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.layout.grid.GridDataInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Model for {@link GridLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.layout
 */
public final class GridLayoutInfo
extends
org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridLayoutInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_afterCreate() throws Exception {
		replaceGridLayout();
		super.refresh_afterCreate();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Replace
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Replaces standard {@link GridLayout} and {@link GridData} with our {@link GridLayout2} and
	 * {@link GridData2}.
	 */
	private void replaceGridLayout() throws Exception {
		Composite composite = getComposite().getWidget();
		// update GridLayout
		{
			GridLayout2 newGridLayout = GridLayout2.replaceGridLayout(composite);
			setObject(newGridLayout);
		}
		// force layout() to recalculate "design" fields
		composite.layout();
		// update GridDataInfo's
		for (ControlInfo controlInfo : getControls()) {
			Control control = controlInfo.getWidget();
			GridData2 gridDataObject = GridLayout2.getLayoutData2(control);
			if (gridDataObject != null) {
				GridDataInfo gridDataInfo = getGridData(controlInfo);
				gridDataInfo.setObject(gridDataObject);
			}
		}
	}
}
