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
package org.eclipse.wb.internal.core.gefTree.part.menu;

import org.eclipse.wb.core.gefTree.part.ObjectEditPart;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.gefTree.policy.menu.MenuLayoutEditPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;

/**
 * {@link TreeEditPart} for {@link IMenuInfo}.
 *
 * @author scheglov_ke
 * @coverage core.gefTree.menu
 */
public final class MenuEditPart extends ObjectEditPart {
	private final ObjectInfo m_menuInfo;
	private final IMenuInfo m_menu;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuEditPart(ObjectInfo menuInfo, IMenuInfo menu) {
		super(menuInfo);
		m_menuInfo = menuInfo;
		m_menu = menu;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link IMenuInfo} model.
	 */
	public IMenuInfo getMenu() {
		return m_menu;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(new MenuLayoutEditPolicy(m_menuInfo, m_menu));
	}
}
