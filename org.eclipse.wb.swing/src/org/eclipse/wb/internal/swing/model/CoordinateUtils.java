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
package org.eclipse.wb.internal.swing.model;

/**
 * Utilities for converting AWT coordinate objects into draw2d objects.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class CoordinateUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private CoordinateUtils() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AWT -> draw2d
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the draw2d {@link org.eclipse.wb.draw2d.geometry.Point} for given AWT
	 *         {@link java.awt.Point}.
	 */
	public static org.eclipse.draw2d.geometry.Point get(java.awt.Point o) {
		return new org.eclipse.draw2d.geometry.Point(o.x, o.y);
	}

	/**
	 * @return the draw2d {@link org.eclipse.draw2d.geometry.Rectangle} for given AWT
	 *         {@link java.awt.Rectangle}.
	 */
	public static org.eclipse.draw2d.geometry.Rectangle get(java.awt.Rectangle o) {
		return new org.eclipse.draw2d.geometry.Rectangle(o.x, o.y, o.width, o.height);
	}

	/**
	 * @return the draw2d {@link org.eclipse.draw2d.geometry.Dimension} for given AWT
	 *         {@link java.awt.Dimension}.
	 */
	public static org.eclipse.draw2d.geometry.Dimension get(java.awt.Dimension o) {
		return new org.eclipse.draw2d.geometry.Dimension(o.width, o.height);
	}

	/**
	 * @return the draw2d {@link org.eclipse.draw2d.geometry.Insets} for given AWT
	 *         {@link java.awt.Insets}.
	 */
	public static org.eclipse.draw2d.geometry.Insets get(java.awt.Insets o) {
		return new org.eclipse.draw2d.geometry.Insets(o.top, o.left, o.bottom, o.right);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// draw2d -> AWT
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the AWT {@link java.awt.Insets} for given draw2d
	 *         {@link org.eclipse.draw2d.geometry.Insets}.
	 */
	public static java.awt.Insets get(org.eclipse.draw2d.geometry.Insets o) {
		return new java.awt.Insets(o.top, o.left, o.bottom, o.right);
	}

	public static java.awt.Rectangle get(org.eclipse.draw2d.geometry.Rectangle o) {
		return new java.awt.Rectangle(o.x, o.y, o.width, o.height);
	}
}
