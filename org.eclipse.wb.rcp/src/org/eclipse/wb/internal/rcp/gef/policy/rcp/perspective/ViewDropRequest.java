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
package org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective;

import org.eclipse.wb.gef.core.requests.AbstractCreateRequest;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewInfo;

import org.eclipse.gef.Request;

/**
 * {@link Request} to drop new view.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class ViewDropRequest extends AbstractCreateRequest {
	public static final String TYPE = "drop View";
	private final ViewInfo m_view;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewDropRequest(ViewInfo view) {
		super(TYPE);
		m_view = view;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link ViewInfo} information about view to drop.
	 */
	public ViewInfo getView() {
		return m_view;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Component
	//
	////////////////////////////////////////////////////////////////////////////
	private Object m_component;

	/**
	 * @return the component to select after drop finished.
	 */
	public Object getComponent() {
		return m_component;
	}

	/**
	 * Sets the component to select after drop finished.
	 */
	public void setComponent(Object component) {
		m_component = component;
	}
}
