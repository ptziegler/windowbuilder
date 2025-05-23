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
package org.eclipse.wb.internal.rcp.gef.policy.forms;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2.IPasteProcessor;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.forms.FormInfo;
import org.eclipse.wb.internal.swt.gef.ControlsLayoutRequestValidator;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.List;

/**
 * {@link LayoutEditPolicy} for dropping {@link ControlInfo} on {@link FormInfo#getHead()}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class FormHeadLayoutEditPolicy extends LayoutEditPolicy {
	private final FormInfo m_form;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormHeadLayoutEditPolicy(FormInfo form) {
		m_form = form;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Requests
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ILayoutRequestValidator getRequestValidator() {
		return ControlsLayoutRequestValidator.INSTANCE;
	}

	@Override
	protected void showLayoutTargetFeedback(Request request) {
		super.showLayoutTargetFeedback(request);
		PolicyUtils.showBorderTargetFeedback(this);
	}

	@Override
	protected void eraseLayoutTargetFeedback(Request request) {
		super.eraseLayoutTargetFeedback(request);
		PolicyUtils.eraseBorderTargetFeedback(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Command getCommand(Request request) {
		if (m_form.getHeadClient() != null) {
			return null;
		}
		return super.getCommand(request);
	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		final ControlInfo newControl = (ControlInfo) request.getNewObject();
		return new EditCommand(m_form) {
			@Override
			protected void executeEdit() throws Exception {
				m_form.setHeadClient(newControl);
			}
		};
	}

	@Override
	protected Command getPasteCommand(PasteRequest request) {
		return LayoutPolicyUtils2.getPasteCommand(
				m_form,
				request,
				ControlInfo.class,
				new IPasteProcessor<ControlInfo>() {
					@Override
					public void process(ControlInfo control) throws Exception {
						m_form.setHeadClient(control);
					}
				});
	}

	@Override
	protected Command getAddCommand(ChangeBoundsRequest request) {
		List<? extends EditPart> addParts = request.getEditParts();
		if (addParts.size() == 1) {
			final ControlInfo control = (ControlInfo) addParts.get(0).getModel();
			return new EditCommand(m_form) {
				@Override
				protected void executeEdit() throws Exception {
					m_form.setHeadClient(control);
				}
			};
		}
		return null;
	}
}