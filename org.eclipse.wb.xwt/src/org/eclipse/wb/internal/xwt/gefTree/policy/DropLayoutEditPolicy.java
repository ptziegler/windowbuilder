/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.xwt.gefTree.policy;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;

import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;

/**
 * {@link LayoutEditPolicy} for dropping {@link LayoutInfo} on {@link CompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage XWT.gefTree.policy
 */
public final class DropLayoutEditPolicy extends LayoutEditPolicy {
	private final CompositeInfo m_composite;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DropLayoutEditPolicy(CompositeInfo composite) {
		m_composite = composite;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Routing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isRequestCondition(Request request) {
		// we understand only Layout_Info drop
		if (request.getType() == RequestConstants.REQ_CREATE) {
			CreateRequest createRequest = (CreateRequest) request;
			return createRequest.getNewObject() instanceof LayoutInfo;
		}
		return false;
	}

	@Override
	protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Command
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command getCreateCommand(Object newObject, Object referenceObject) {
		final LayoutInfo newLayout = (LayoutInfo) newObject;
		return new EditCommand(m_composite) {
			@Override
			protected void executeEdit() throws Exception {
				m_composite.setLayout(newLayout);
			}
		};
	}
}