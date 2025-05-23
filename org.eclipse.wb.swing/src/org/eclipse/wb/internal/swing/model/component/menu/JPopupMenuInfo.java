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
package org.eclipse.wb.internal.swing.model.component.menu;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.JavaMenuMenuObject;
import org.eclipse.wb.internal.core.model.menu.MenuVisualData;
import org.eclipse.wb.internal.core.model.order.ComponentOrder;
import org.eclipse.wb.internal.core.model.order.ComponentOrderFirst;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.utils.SwingImageUtils;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/**
 * Model for {@link JPopupMenu}.
 *
 * @author scheglov_ke
 * @coverage swing.model.menu
 */
public final class JPopupMenuInfo extends ContainerInfo implements IAdaptable {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JPopupMenuInfo(AstEditor editor,
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
		JPopupMenu menu = (JPopupMenu) getObject();
		// add text, if no "real" items
		{
			if (menu.getComponentCount() == 0) {
				menu.add(new JMenuItem(IMenuInfo.NO_ITEMS_TEXT));
			}
		}
		// add a popup menu tracking listener to get the menu working in 'Test/Preview' mode.
		{
			ComponentInfo parent = (ComponentInfo) getParent();
			if (parent != null) {
				Component parentComponent = parent.getComponent();
				addPopup(parentComponent, menu);
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

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds new {@link JPopupMenuInfo} on given {@link ComponentInfo}.
	 */
	public void command_CREATE(ComponentInfo component) throws Exception {
		JavaInfoUtils.addFirst(this, getAssociation_(), component);
	}

	/**
	 * Moves this {@link JPopupMenuInfo} on given {@link ComponentInfo}.
	 */
	public void command_MOVE(ComponentInfo component) throws Exception {
		ComponentOrder order = ComponentOrderFirst.INSTANCE;
		JavaInfo nextComponent = order.getNextComponent_whenLast(this, component);
		JavaInfoUtils.move(this, getAssociation_(), component, nextComponent);
	}

	private static AssociationObject getAssociation_() {
		Association association = new JPopupMenuAssociation();
		return new AssociationObject(association, true);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IAdaptable
	//
	////////////////////////////////////////////////////////////////////////////
	private final IMenuPopupInfo m_popupImpl = new MenuPopupImpl();
	private final IMenuInfo m_menuImpl = new MenuImpl();
	private final IMenuPolicy m_menuPolicyImpl = new JMenuPolicyImpl(this);

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isAssignableFrom(IMenuPopupInfo.class)) {
			return adapter.cast(m_popupImpl);
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Popup menu tracking
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds a special popup tracking listener to get the menu working in 'Test/Preview' mode.
	 */
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
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
			super(JPopupMenuInfo.this);
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// IMenuPopupInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Implementation of {@link IMenuPopupInfo}.
	 *
	 * @author scheglov_ke
	 */
	private final class MenuPopupImpl extends MenuAbstractImpl implements IMenuPopupInfo {
		////////////////////////////////////////////////////////////////////////////
		//
		// Model
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Object getModel() {
			return JPopupMenuInfo.this;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Presentation
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public ImageDescriptor getImageDescriptor() {
			return ExecutionUtils.runObjectLog(() -> getPresentation().getIcon(), getDescription().getIcon());
		}

		@Override
		public Rectangle getBounds() {
			ImageData imageData = getImageDescriptor().getImageData(100);
			return new Rectangle(0, 0, imageData.width, imageData.height);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// IMenuPopupInfo
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
			return MenuUtils.getItems(JPopupMenuInfo.this);
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
