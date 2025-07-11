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
package org.eclipse.wb.tests.designer.swing.model.layout.gbl;

import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for {@link GridBagLayoutInfo}.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
		GridBagLayoutTest.class,
		GridBagDimensionTest.class,
		GridBagColumnTest.class,
		GridBagRowTest.class,
		GridBagConstraintsTest.class,
		GridBagLayoutParametersTest.class,
		GridBagLayoutConverterTest.class,
		GridBagLayoutSelectionActionsTest.class,
		GridBagLayoutSurroundSupportTest.class,
		GridBagLayoutGefTest.class
})
public class GridBagLayoutTests {
}
