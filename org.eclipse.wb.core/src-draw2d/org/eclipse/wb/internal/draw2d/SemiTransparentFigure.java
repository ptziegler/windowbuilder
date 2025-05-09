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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.wb.draw2d.Figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

/**
 * {@link Figure} that fill its client area with semi-transparent background color with given alpha.
 *
 * @author scheglov_ke
 * @coverage gef.draw2d
 */
public class SemiTransparentFigure extends Figure {
	private final int m_alpha;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SemiTransparentFigure(int alpha) {
		m_alpha = alpha;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void paintClientArea(Graphics graphics) {
		Rectangle clientArea = getClientArea();
		// prepare image 1x1
		Image image;
		{
			PaletteData palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			ImageData data = new ImageData(1, 1, 32, palette);
			int pixel = palette.getPixel(getBackgroundColor().getRGB());
			data.setPixel(0, 0, pixel);
			data.setAlpha(0, 0, m_alpha);
			//
			image = new Image(null, data);
		}
		// stretch image on client area
		graphics.drawImage(image, 0, 0, 1, 1, 0, 0, clientArea.width, clientArea.height);
		image.dispose();
	}
}
