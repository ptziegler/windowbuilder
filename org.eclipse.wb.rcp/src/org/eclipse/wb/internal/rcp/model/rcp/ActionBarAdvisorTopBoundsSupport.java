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
package org.eclipse.wb.internal.rcp.model.rcp;

import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.swt.model.widgets.CompositeTopBoundsSupport;

import org.eclipse.draw2d.geometry.Dimension;

/**
 * Implementation of {@link TopBoundsSupport} for {@link ActionBarAdvisorInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class ActionBarAdvisorTopBoundsSupport extends TopBoundsSupport {
	private final ActionBarAdvisorInfo m_advisor;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ActionBarAdvisorTopBoundsSupport(ActionBarAdvisorInfo advisor) {
		super(advisor);
		m_advisor = advisor;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TopBoundsSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void apply() throws Exception {
		// set size from resource properties (or default)
		{
			Dimension size = getResourceSize();
			m_advisor.getShell().setSize(size.width, size.height);
		}
	}

	@Override
	public void setSize(int width, int height) throws Exception {
		// remember size in resource properties
		setResourceSize(width, height);
	}

	@Override
	protected Dimension getDefaultSize() {
		return new Dimension(600, 500);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Show
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean show() throws Exception {
		CompositeTopBoundsSupport.show(m_advisor, m_advisor.getShell());
		return true;
	}
}