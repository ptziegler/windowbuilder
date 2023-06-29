/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.xwt.gef.part;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.policies.TerminatorLayoutEditPolicy;
import org.eclipse.wb.internal.xwt.gef.policy.widgets.ViewFormLayoutEditPolicy;
import org.eclipse.wb.internal.xwt.model.widgets.ViewFormInfo;

/**
 * {@link EditPart} for {@link ViewFormInfo}.
 *
 * @author scheglov_ke
 * @coverage XWT.gef.part
 */
public final class ViewFormEditPart extends CompositeEditPart {
	private final ViewFormInfo m_composite;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewFormEditPart(ViewFormInfo composite) {
		super(composite);
		m_composite = composite;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refreshEditPolicies() {
		super.refreshEditPolicies();
		installEditPolicy(new ViewFormLayoutEditPolicy(m_composite));
		installEditPolicy(new TerminatorLayoutEditPolicy());
	}
}
