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
package org.eclipse.wb.internal.rcp.model.jface.action;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.broadcast.DisplayEventListener;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.JavaMenuMenuObject;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.menu.MenuVisualData;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.rcp.model.ModelMessages;
import org.eclipse.wb.internal.swt.support.ToolkitSupport;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model for {@link IMenuManager}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class MenuManagerInfo extends ContributionManagerInfo implements IContributionItemInfo, IAdaptable {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuManagerInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	private MenuVisualData m_visualData;

	@Override
	protected void refresh_afterCreate() throws Exception {
		// force creation for all MenuManager's
		if (!(getParent() instanceof MenuManagerInfo)) {
			getManager().updateAll(true);
		}
		// reset 'setRemoveAllWhenShown', because it causes empty menu.
		getManager().setRemoveAllWhenShown(false);
		// process Menu widget
		{
			Menu menu = getManager().getMenu();
			// if no any items, create one
			if (menu.getItemCount() == 0) {
				new MenuItem(menu, SWT.NONE).setText(ModelMessages.MenuManagerInfo_emptyMessage);
			}
			// OK, remember as component
			setComponentObject(menu);
		}
		// process children
		super.refresh_afterCreate();
	}

	@Override
	protected void refresh_fetch() throws Exception {
		Menu menu = getManager().getMenu();
		// fetch menu visual data
		DisplayEventListener displayListener = getBroadcast(DisplayEventListener.class);
		try {
			displayListener.beforeMessagesLoop();
			m_visualData = ToolkitSupport.fetchMenuVisualData(menu);
		} finally {
			displayListener.afterMessagesLoop();
		}
		// process children
		super.refresh_fetch();
		// set child items bounds
		MenuItem[] menuItems = menu.getItems();
		for (AbstractComponentInfo contributionItem : getItems()) {
			Object contributionItemObject = contributionItem.getObject();
			for (int i = 0; i < menuItems.length; i++) {
				MenuItem menuItem = menuItems[i];
				if (menuItem.getData() == contributionItemObject) {
					contributionItem.setModelBounds(m_visualData.m_itemBounds.get(i));
					break;
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this {@link MenuManagerInfo} is represented by {@link Menu}
	 *         widgets with {@link SWT#BAR} style.
	 */
	public boolean isBar() {
		return ExecutionUtils.runObject(() -> {
			Menu menu = getManager().getMenu();
			return (menu.getStyle() & SWT.BAR) != 0;
		});
	}

	/**
	 * Convenience method for accessing the {@link MenuManager} contained by this
	 * {@link MenuManagerInfo}.
	 */
	public MenuManager getManager() {
		return (MenuManager) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IAdaptable
	//
	////////////////////////////////////////////////////////////////////////////
	private final IMenuItemInfo m_itemImpl = new MenuItemImpl();
	private final IMenuInfo m_menuImpl = new MenuImpl();

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isAssignableFrom(IMenuItemInfo.class)) {
			return adapter.cast(m_itemImpl);
		}
		if (adapter.isAssignableFrom(IMenuInfo.class)) {
			return adapter.cast(m_menuImpl);
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractMenuImpl
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Abstract superclass for {@link IMenuObjectInfo} implementations.
	 *
	 * @author scheglov_ke
	 */
	private abstract class MenuAbstractImpl extends JavaMenuMenuObject {
		public MenuAbstractImpl() {
			super(MenuManagerInfo.this);
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// IMenuItemInfo for "this"
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation of {@link IMenuItemInfo} for "this" {@link MenuManagerInfo}.
	 *
	 * @author scheglov_ke
	 */
	private final class MenuItemImpl extends AbstractMenuObject implements IMenuItemInfo {
		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public MenuItemImpl() {
			super(MenuManagerInfo.this);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Model
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Object getModel() {
			return MenuManagerInfo.this;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Presentation
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public Rectangle getBounds() {
			return MenuManagerInfo.this.getBounds();
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// IMenuItemInfo
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public IMenuInfo getMenu() {
			return m_menuImpl;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Policy
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public IMenuPolicy getPolicy() {
			return IMenuPolicy.NOOP;
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// IMenuInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation of {@link IMenuInfo}.
	 *
	 * @author scheglov_ke
	 */
	private final class MenuImpl extends MenuAbstractImpl implements IMenuInfo, IMenuPolicy {
		////////////////////////////////////////////////////////////////////////////
		//
		// Model
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Object getModel() {
			return this;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Presentation
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public ImageDescriptor getImageDescriptor() {
			if (m_visualData == null || m_visualData.m_menuImage == null) {
				return null;
			}
			return ImageDescriptor.createFromImage(m_visualData.m_menuImage);
		}

		@Override
		public Rectangle getBounds() {
			return m_visualData.m_menuBounds;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public boolean isHorizontal() {
			return isBar();
		}

		@Override
		public List<IMenuItemInfo> getItems() {
			List<IMenuItemInfo> items = new ArrayList<>();
			for (AbstractComponentInfo item : MenuManagerInfo.this.getItems()) {
				IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(item);
				items.add(itemObject);
			}
			return items;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Policy
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public IMenuPolicy getPolicy() {
			return this;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Validation
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public boolean validateCreate(Object newObject) {
			return newObject instanceof ActionInfo || newObject instanceof IContributionItemInfo;
		}

		@Override
		public boolean validatePaste(final Object mementoObject) {
			return false;
		}

		@Override
		public boolean validateMove(Object object) {
			if (object instanceof IContributionItemInfo) {
				AbstractComponentInfo item = (AbstractComponentInfo) object;
				// don't move item on its child menu
				return !item.isParentOf(MenuManagerInfo.this);
			}
			//
			return false;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Operations
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public void commandCreate(Object newObject, Object nextObject) throws Exception {
			AbstractComponentInfo nextItem = (AbstractComponentInfo) nextObject;
			AbstractComponentInfo newItem;
			if (newObject instanceof ActionInfo action) {
				newItem = command_CREATE(action, nextItem);
			} else {
				newItem = (AbstractComponentInfo) newObject;
				command_CREATE(newItem, nextItem);
			}
			// schedule selection
			MenuObjectInfoUtils.setSelectingObject(newItem);
		}

		@Override
		public List<?> commandPaste(Object mementoObject, Object nextObject) throws Exception {
			return Collections.emptyList();
		}

		@Override
		public void commandMove(Object object, Object nextObject) throws Exception {
			AbstractComponentInfo item = (AbstractComponentInfo) object;
			AbstractComponentInfo nextItem = (AbstractComponentInfo) nextObject;
			command_MOVE(item, nextItem);
			// schedule selection
			MenuObjectInfoUtils.setSelectingObject(item);
		}
	}
}
