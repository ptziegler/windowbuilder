/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.gef.tree.policies;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.gef.tree.TreeViewer;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Implementation of {@link EditPolicy} for {@link TreeViewer} and {@link TreeEditPart}'s, that
 * automatically expands {@link TreeItem}'s, when {@link Request#REQ_CREATE} or
 * {@link Request#REQ_PASTE} are received.
 *
 * @author scheglov_ke
 * @coverage gef.tree
 */
public final class AutoExpandEditPolicy extends EditPolicy {
	@Override
	public boolean understandsRequest(Request request) {
		return request.getType() == RequestConstants.REQ_CREATE || request.getType() == PasteRequest.REQ_PASTE;
	}

	@Override
	public EditPart getTargetEditPart(Request request) {
		// prepare host widget
		final TreeEditPart host = (TreeEditPart) getHost();
		final TreeItem hostWidget = host.getWidget();
		final Tree tree = hostWidget.getParent();
		// prepare target widget
		TreeItem targetWidget;
		{
			DropRequest dropRequest = (DropRequest) request;
			Point location = dropRequest.getLocation();
			targetWidget = tree.getItem(location.getSWTPoint());
		}
		// if mouse cursor is above our "host", expand it
		if (targetWidget == hostWidget && !hostWidget.getExpanded() && hostWidget.getItemCount() != 0) {
			tree.getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!hostWidget.isDisposed()) {
						hostWidget.setExpanded(true);
					}
				}
			});
		}
		// we don't perform any real checks
		return null;
	}
}