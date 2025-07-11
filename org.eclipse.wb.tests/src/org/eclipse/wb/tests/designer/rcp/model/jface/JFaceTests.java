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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for RCP JFace models.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
		TableViewerTest.class,
		TableViewerColumnTest.class,
		TreeViewerColumnTest.class,
		ComboViewerTest.class,
		AbstractColumnLayoutTest.class,
		WindowTopBoundsSupportTest.class,
		DialogTest.class,
		TitleAreaDialogTest.class,
		PopupDialogTest.class,
		DialogPageTest.class,
		ApplicationWindowTest.class,
		ApplicationWindowGefTest.class,
		ActionTest.class,
		MenuManagerTest.class,
		MenuManagerGefTest.class,
		CoolBarManagerTest.class,
		WizardPageTest.class,
		WizardTest.class,
		PreferencePageTest.class,
		FieldEditorPreferencePageTest.class,
		FieldEditorLabelsConstantsPropertyEditorTest.class,
		DoubleFieldEditorEntryInfoTest.class,
		FieldLayoutPreferencePageTest.class,
		ControlDecorationTest.class,
		FieldEditorPreferencePageGefTest.class,
		CellEditorTest.class,
		NoJFaceInClasspathTest.class,
		GridLayoutFactoryTest.class
})
public class JFaceTests {
}