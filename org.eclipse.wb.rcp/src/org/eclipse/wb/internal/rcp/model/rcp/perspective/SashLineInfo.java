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
package org.eclipse.wb.internal.rcp.model.rcp.perspective;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Model for "sash" between {@link AbstractPartInfo}'s in {@link PageLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class SashLineInfo {
	public static final int SASH_SIZE = 3;
	private final AbstractPartInfo m_part;
	private final Rectangle m_bounds;
	private final Rectangle m_partBounds;
	private final Rectangle m_refBounds;
	private final int m_position;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SashLineInfo(AbstractPartInfo part,
			Rectangle partBounds,
			Rectangle refBounds,
			int position,
			Rectangle bounds) {
		m_part = part;
		m_position = position;
		m_bounds = bounds;
		m_partBounds = partBounds;
		m_refBounds = refBounds;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		String s_1 = m_part.getId() + "," + isHorizontal();
		return "(" + s_1 + "," + m_bounds + "," + m_partBounds + "," + m_refBounds + ")";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the host {@link AbstractPartInfo}.
	 */
	public AbstractPartInfo getPart() {
		return m_part;
	}

	/**
	 * @return the position relative to {@link #getPart()}, one of the
	 *         {@link PositionConstants#NORTH}, {@link PositionConstants#SOUTH},
	 *         {@link PositionConstants#WEST} or {@link PositionConstants#EAST}.
	 */
	public int getPosition() {
		return m_position;
	}

	/**
	 * @return <code>true</code> if this {@link AbstractPartInfo} references its "reference" in
	 *         horizontal direction, i.e. left/right.
	 */
	public boolean isHorizontal() {
		return m_position == PositionConstants.WEST || m_position == PositionConstants.EAST;
	}

	/**
	 * @return the bounds of this {@link SashLineInfo}.
	 */
	public Rectangle getBounds() {
		return m_bounds;
	}

	/**
	 * @return the bounds of {@link #getPart()}.
	 */
	public Rectangle getPartBounds() {
		return m_partBounds;
	}

	/**
	 * @return the bounds of "part" referenced by {@link #getPart()}.
	 */
	public Rectangle getRefBounds() {
		return m_refBounds;
	}
}