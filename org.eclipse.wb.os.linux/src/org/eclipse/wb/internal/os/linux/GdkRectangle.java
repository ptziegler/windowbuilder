/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.os.linux;

/**
 * A GdkRectangle data type for representing rectangles.
 *
 * GdkRectangle is identical to cairo_rectangle_t. Together with Cairo’s
 * cairo_region_t data type, these are the central types for representing sets
 * of pixels.
 *
 * The intersection of two rectangles can be computed with
 * gdk_rectangle_intersect(); to find the union of two rectangles use
 * gdk_rectangle_union().
 *
 * The cairo_region_t type provided by Cairo is usually used for managing
 * non-rectangular clipping of graphical operations.
 *
 * The Graphene library has a number of other data types for regions and volumes
 * in 2D and 3D.
 */
public sealed class GdkRectangle permits GtkAllocation {
	public int x;
	public int y;
	public int width;
	public int height;
}
