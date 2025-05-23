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
package org.eclipse.wb.internal.rcp.gef.policy.forms;

import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.GefMessages;
import org.eclipse.wb.internal.rcp.gef.policy.AbstractPositionCompositeLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.forms.ExpandableCompositeInfo;

import org.eclipse.draw2d.geometry.Insets;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link ExpandableCompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class ExpandableCompositeLayoutEditPolicy
extends
AbstractPositionCompositeLayoutEditPolicy {
	////////////////////////////////////////////////////////////////////////////
	//
	// Composite
	//
	////////////////////////////////////////////////////////////////////////////
	public ExpandableCompositeLayoutEditPolicy(ExpandableCompositeInfo composite) {
		super(composite);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Positions
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addFeedbacks() throws Exception {
		addFeedback2(
				0.8,
				0.0,
				1.0,
				0.2,
				new Insets(0, 0, 1, 0),
				GefMessages.ExpandableCompositeLayoutEditPolicy_textClientHint,
				"setTextClient");
		addFeedback2(
				0.0,
				0.2,
				1.0,
				1.0,
				new Insets(0, 0, 0, 0),
				GefMessages.ExpandableCompositeLayoutEditPolicy_clientHint,
				"setClient");
	}
}
