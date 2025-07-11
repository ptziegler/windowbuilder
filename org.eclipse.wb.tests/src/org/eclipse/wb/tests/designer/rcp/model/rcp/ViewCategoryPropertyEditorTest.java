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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.rcp.model.rcp.ViewCategoryPropertyEditor;
import org.eclipse.wb.internal.rcp.model.rcp.ViewPartInfo;
import org.eclipse.wb.tests.designer.core.PdeProjectConversionUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ViewCategoryPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class ViewCategoryPropertyEditorTest extends RcpModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@AfterEach
	public void tearDown() throws Exception {
		do_projectDispose();
		super.tearDown();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_existingAttribute() throws Exception {
		PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null, "testplugin.Activator");
		AbstractPdeTest.createPluginXML(new String[]{
				"<plugin>",
				"  <extension point='org.eclipse.ui.views'>",
				"    <view id='id_1' class='test.Test' category='category_1'/>",
				"  </extension>",
		"</plugin>"});
		// parse
		Property categoryProperty = parseAndGetCategoryProperty();
		assertTrue(categoryProperty.isModified());
		assertEquals("category_1", categoryProperty.getValue());
		// ViewCategory_PropertyEditor
		assertSame(ViewCategoryPropertyEditor.INSTANCE, categoryProperty.getEditor());
		assertEquals("category_1", getPropertyText(categoryProperty));
	}

	@Test
	public void test_noAttribute() throws Exception {
		PdeProjectConversionUtils.convertToPDE(m_testProject.getProject(), null, "testplugin.Activator");
		AbstractPdeTest.createPluginXML(new String[]{
				"<plugin>",
				"  <extension point='org.eclipse.ui.views'>",
				"    <view id='id_1' class='test.Test'/>",
				"  </extension>",
		"</plugin>"});
		// parse
		Property categoryProperty = parseAndGetCategoryProperty();
		assertFalse(categoryProperty.isModified());
		assertSame(Property.UNKNOWN_VALUE, categoryProperty.getValue());
		// ViewCategory_PropertyEditor
		assertSame(ViewCategoryPropertyEditor.INSTANCE, categoryProperty.getEditor());
		assertEquals(null, getPropertyText(categoryProperty));
	}

	private Property parseAndGetCategoryProperty() throws Exception {
		ViewPartInfo part =
				parseJavaInfo(
						"import org.eclipse.ui.part.*;",
						"public abstract class Test extends ViewPart {",
						"  public Test() {",
						"  }",
						"  public void createPartControl(Composite parent) {",
						"    Composite container = new Composite(parent, SWT.NULL);",
						"  }",
						"}");
		// "category" property
		Property extensionProperty = part.getPropertyByTitle("Extension");
		Property categoryProperty = getSubProperties(extensionProperty)[2];
		assertEquals("category", categoryProperty.getTitle());
		return categoryProperty;
	}
}