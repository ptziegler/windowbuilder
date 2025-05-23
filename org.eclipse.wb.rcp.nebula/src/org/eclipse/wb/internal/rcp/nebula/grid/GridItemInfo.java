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
package org.eclipse.wb.internal.rcp.nebula.grid;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridItem;

import java.util.List;

/**
 * Model {@link GridItem}.
 *
 * @author sablin_aa
 * @coverage nebula.model
 */
public final class GridItemInfo extends ItemInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridItemInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public GridItem getWidget() {
		return (GridItem) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		{
			Rectangle bounds = getComponentBounds();
			if (getParent() instanceof GridItemInfo) {
				GridItemInfo parent = (GridItemInfo) getParent();
				Rectangle parentBounds = parent.getComponentBounds();
				if (parent.getComponentExpanded()) {
					bounds.performTranslate(-parentBounds.x, -parentBounds.y);
				} else {
					bounds = new Rectangle(parentBounds.x, parentBounds.height, parentBounds.width, 0);
				}
			}
			setModelBounds(bounds);
		}
		// continue in super()
		super.refresh_fetch();
	}

	private Rectangle getComponentBounds() throws Exception {
		Rectangle bounds = getComponentCellsBounds();
		if (getComponentExpanded()) {
			List<GridItemInfo> childItems = getChildren(GridItemInfo.class);
			for (GridItemInfo chilsItem : childItems) {
				bounds.union(chilsItem.getComponentBounds());
			}
		}
		return bounds;
	}

	private Rectangle getComponentCellsBounds() throws Exception {
		Grid grid = getWidget().getParent();
		int columnCount = grid.getColumnCount();
		Rectangle bounds = null;
		for (int i = 0; i < columnCount; i++) {
			if (bounds == null) {
				bounds = new Rectangle(getWidget().getBounds(i));
			} else {
				bounds.union(new Rectangle(getWidget().getBounds(i)));
			}
		}
		return bounds;
	}

	private Boolean getComponentExpanded() throws Exception {
		return (Boolean) ReflectionUtils.invokeMethod(getObject(), "isExpanded()");
	}
}
