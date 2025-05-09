/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.swing.model.component.menu;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.JavaMenuMenuObject;
import org.eclipse.wb.internal.core.model.menu.MenuVisualData;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.utils.SwingImageUtils;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;

import java.awt.Component;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

/**
 * Model for {@link JMenu}.
 *
 * @author scheglov_ke
 * @coverage swing.model.menu
 */
public final class JMenuInfo extends JMenuItemInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JMenuInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initialize() throws Exception {
		super.initialize();
		MenuUtils.copyPasteItems(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link JMenuItemInfo} children. Practically can be used only in tests because
	 *         {@link JMenu} may have not only {@link JMenuItem} children, but also {@link JSeparator}
	 *         or just any {@link Component}.
	 */
	public List<JMenuItemInfo> getChildrenItems() {
		return getChildren(JMenuItemInfo.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	private MenuVisualData m_visualData;

	@Override
	protected void refresh_afterCreate() throws Exception {
		// add text, if no "real" items
		{
			JMenu menu = (JMenu) getObject();
			if (menu.getItemCount() == 0) {
				menu.add(new JMenuItem(IMenuInfo.NO_ITEMS_TEXT));
			}
		}
		// continue
		super.refresh_afterCreate();
	}

	@Override
	protected void refresh_fetch() throws Exception {
		m_visualData = SwingImageUtils.fetchMenuVisualData(getContainer(), null);
		super.refresh_fetch();
		MenuUtils.setItemsBounds(m_visualData, getChildrenComponents());
	}

	@Override
	public void refresh_dispose() throws Exception {
		super.refresh_dispose();
		if (m_visualData != null) {
			m_visualData.m_menuImage.dispose();
			m_visualData = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IAdaptable
	//
	////////////////////////////////////////////////////////////////////////////
	private final IMenuItemInfo m_itemImpl = new MenuItemImpl();
	private final IMenuInfo m_menuImpl = new MenuImpl();
	private final IMenuPolicy m_menuPolicyImpl = new JMenuPolicyImpl(this);

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
			super(JMenuInfo.this);
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// IMenuItemInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation of {@link IMenuItemInfo}.
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
			super(JMenuInfo.this);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Model
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Object getModel() {
			return JMenuInfo.this;
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
			return JMenuInfo.this.getBounds();
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
			return m_menuPolicyImpl;
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
	private final class MenuImpl extends MenuAbstractImpl implements IMenuInfo {
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
			return false;
		}

		@Override
		public List<IMenuItemInfo> getItems() {
			return MenuUtils.getItems(JMenuInfo.this);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Policy
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public IMenuPolicy getPolicy() {
			return m_menuPolicyImpl;
		}
	}
}
