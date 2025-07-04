/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.MigLayout.gef.header.selection;

import org.eclipse.wb.gef.core.requests.KeyRequest;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.SideResizeHandle;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.edit.ColumnHeaderEditPart;
import org.eclipse.wb.internal.swing.MigLayout.model.MigColumnInfo;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.Request;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link ColumnHeaderEditPart}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.header
 */
public final class ColumnSelectionEditPolicy extends DimensionSelectionEditPolicy<MigColumnInfo> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ColumnSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
		super(mainPolicy);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resize
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Handle createResizeHandle() {
		Handle handle = new SideResizeHandle(getHost(), PositionConstants.RIGHT, 7, true);
		handle.setDragTracker(new ResizeTracker(getHost(), PositionConstants.EAST, REQ_RESIZE));
		return handle;
	}

	@Override
	protected Point getTextFeedbackLocation(Point mouseLocation) {
		return new Point(mouseLocation.x + 10, 10);
	}

	@Override
	protected int getPixelSize(Dimension resizeDelta) {
		return getHostFigure().getSize().width + resizeDelta.width;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Keyboard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void performRequest(Request request) {
		super.performRequest(request);
		if (request instanceof KeyRequest keyRequest) {
			if (keyRequest.isPressed()) {
				char c = keyRequest.getCharacter();
				// horizontal
				if (c == 'd') {
					setAlignment(MigColumnInfo.Alignment.DEFAULT);
				} else if (c == 'l') {
					setAlignment(MigColumnInfo.Alignment.LEFT);
				} else if (c == 'c') {
					setAlignment(MigColumnInfo.Alignment.CENTER);
				} else if (c == 'r') {
					setAlignment(MigColumnInfo.Alignment.RIGHT);
				} else if (c == 'f') {
					setAlignment(MigColumnInfo.Alignment.FILL);
				} else if (c == 'q') {
					setAlignment(MigColumnInfo.Alignment.LEADING);
				} else if (c == 'w') {
					setAlignment(MigColumnInfo.Alignment.TRAILING);
				}
			}
		}
	}

	/**
	 * Sets the alignment for {@link MigColumnInfo}.
	 */
	private void setAlignment(final MigColumnInfo.Alignment alignment) {
		final MigLayoutInfo layout = getLayout();
		ExecutionUtils.run(layout, new RunnableEx() {
			@Override
			public void run() throws Exception {
				getDimension().setAlignment(alignment);
				layout.writeDimensions();
			}
		});
	}
}
