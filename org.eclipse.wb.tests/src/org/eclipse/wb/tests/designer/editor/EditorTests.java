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
package org.eclipse.wb.tests.designer.editor;

import org.eclipse.wb.tests.designer.editor.actions.DesignerEditorTests;
import org.eclipse.wb.tests.designer.editor.validator.LayoutRequestValidatorTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
		//basic policy
		TopSelectionEditPolicyTest.class,
		// basic features
		UndoManagerTest.class,
		ContentDescriberTest.class,
		ReparseOnModificationTest.class,
		SelectSupportTest.class,
		ComponentsPropertiesPageTest.class,
		JavaPropertiesToolBarContributorTest.class,
		ComponentsTreePageTest.class,
		SplitModeTest.class,
		DesignerEditorTests.class,
		LayoutRequestValidatorTests.class
})

public class EditorTests {
}
