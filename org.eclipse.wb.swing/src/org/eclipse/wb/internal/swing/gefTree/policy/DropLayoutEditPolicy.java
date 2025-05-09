/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.internal.swing.gefTree.policy;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;

/**
 * Implementation of {@link LayoutEditPolicy} for dropping {@link LayoutInfo} on
 * {@link ContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gefTree.policy
 */
public final class DropLayoutEditPolicy extends LayoutEditPolicy {
	private final ContainerInfo m_composite;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DropLayoutEditPolicy(ContainerInfo composite) {
		m_composite = composite;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Routing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isRequestCondition(Request request) {
		// we understand only LayoutInfo drop
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