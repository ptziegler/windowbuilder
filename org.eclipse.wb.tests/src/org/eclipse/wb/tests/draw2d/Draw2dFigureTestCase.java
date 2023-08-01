/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.draw2d;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.ICursorConstants;

import org.eclipse.swt.widgets.Display;

import org.junit.Assert;
import org.junit.Before;



/**
 * @author lobas_av
 *
 */
public abstract class Draw2dFigureTestCase extends Assert
implements
IColorConstants,
ICursorConstants {

	@Before
	public void setUp() throws Exception {
		// check create display for initialize figure's colors
		Display.getDefault();
	}
}