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
package org.eclipse.wb.internal.core.model.menu;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;

/**
 * Abstract base for {@link IMenuInfo} implementation.
 *
 * @author scheglov_ke
 * @coverage core.model.menu
 */
public abstract class AbstractMenuMenuObject extends AbstractMenuObject {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractMenuMenuObject(ObjectInfo component) {
		super(component);
		m_component.addBroadcastListener(new ObjectEventListener() {
			@Override
			public void refreshed() throws Exception {
				if (isRoot()) {
					fireRefreshListeners();
				}
			}

			@Override
			public void refreshed2() throws Exception {
				if (isRoot()) {
					MenuObjectInfoUtils.m_selectingObject = null;
				}
			}

			@Override
			public void selecting(ObjectInfo object, boolean[] refreshFlag) throws Exception {
				if (isRootFor(object)) {
					IMenuObjectInfo menuObject = null;
					for (; menuObject == null && object != null; object = object.getParent()) {
						menuObject = MenuObjectInfoUtils.getMenuObjectInfo(object);
					}
					// if found some IMenuObjectInfo in parent hierarchy, do refresh() to show
					if (menuObject != null) {
						MenuObjectInfoUtils.m_selectingObject = menuObject;
						fireRefreshListeners();
						MenuObjectInfoUtils.m_selectingObject = null;
					}
				}
			}
		});
	}
}
