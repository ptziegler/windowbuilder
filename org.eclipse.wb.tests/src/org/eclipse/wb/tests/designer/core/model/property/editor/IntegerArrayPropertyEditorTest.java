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
package org.eclipse.wb.tests.designer.core.model.property.editor;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.IntegerArrayPropertyEditor;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.junit.Test;

/**
 * Test for {@link IntegerArrayPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class IntegerArrayPropertyEditorTest extends AbstractTextPropertyEditorTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link IntegerArrayPropertyEditor#getText(Property)}.
	 */
	@Test
	public void test_getText() throws Exception {
		assert_getText(null, IntegerArrayPropertyEditor.INSTANCE, Property.UNKNOWN_VALUE);
		assert_getText("1 2 3", IntegerArrayPropertyEditor.INSTANCE, new int[]{1, 2, 3});
	}

	/**
	 * Test for {@link IntegerArrayPropertyEditor#getEditorText(Property)}.
	 */
	@Test
	public void test_getEditorText() throws Exception {
		assert_getEditorText(null, IntegerArrayPropertyEditor.INSTANCE, Property.UNKNOWN_VALUE);
		assert_getEditorText("1 2 3", IntegerArrayPropertyEditor.INSTANCE, new int[]{1, 2, 3});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setEditorText()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link IntegerArrayPropertyEditor#setEditorText(Property, String)}.
	 */
	@Test
	public void test_setEditorText_setValue() throws Exception {
		prepareIntegerPanel();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		panel.refresh();
		//
		Property property = panel.getPropertyByTitle("foo");
		setTextEditorText(property, "1 2 3");
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"    setFoo(new int[] {1, 2, 3});",
				"  }",
				"}");
	}

	/**
	 * Test for {@link IntegerArrayPropertyEditor#setEditorText(Property, String)}.
	 */
	@Test
	public void test_setEditorText_removeValue_emptyString() throws Exception {
		prepareIntegerPanel();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setFoo(1, 2, 3);",
						"  }",
						"}");
		panel.refresh();
		//
		Property property = panel.getPropertyByTitle("foo");
		setTextEditorText(property, "");
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"  }",
				"}");
	}

	/**
	 * Test for {@link IntegerArrayPropertyEditor#setEditorText(Property, String)}.
	 */
	@Test
	public void test_setEditorText_removeValue_whitespaceString() throws Exception {
		prepareIntegerPanel();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"    setFoo(1, 2, 3);",
						"  }",
						"}");
		panel.refresh();
		//
		Property property = panel.getPropertyByTitle("foo");
		setTextEditorText(property, " ");
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"  }",
				"}");
	}

	/**
	 * Test for {@link IntegerArrayPropertyEditor#setEditorText(Property, String)}.
	 */
	@Test
	public void test_setEditorText_invalidValue() throws Exception {
		prepareIntegerPanel();
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends MyPanel {",
						"  public Test() {",
						"  }",
						"}");
		panel.refresh();
		//
		final Property property = panel.getPropertyByTitle("foo");
		new UiContext().executeAndCheck(new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				setTextEditorText(property, "notInteger");
			}
		}, new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				context.useShell("foo");
				context.clickButton("OK");
			}
		});
		assertEditor(
				"// filler filler filler",
				"public class Test extends MyPanel {",
				"  public Test() {",
				"  }",
				"}");
	}

	private void prepareIntegerPanel() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public void setFoo(int... foo) {",
						"  }",
						"}"));
		waitForAutoBuild();
	}
}
