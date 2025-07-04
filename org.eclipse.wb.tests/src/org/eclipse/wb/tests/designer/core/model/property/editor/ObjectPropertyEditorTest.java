/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.core.model.property.editor;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.ObjectPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.utils.TableCollection;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableRunnable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Tests for {@link ObjectPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class ObjectPropertyEditorTest extends SwingModelTest {
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
	 * {@link ObjectPropertyEditor} can be used to select object of any type, as non-visual bean.
	 */
	@Test
	public void test_nonVisualBean() throws Exception {
		setJavaContentSrc("test", "MyObject", new String[]{
				"public class MyObject {",
				"  // filler filler filler filler filler",
		"}"}, null);
		setJavaContentSrc("test", "MyComponent", new String[]{
				"public class MyComponent extends JLabel {",
				"  public void setValue(MyObject object) {",
				"  }",
		"}"}, null);
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private final MyComponent component = new MyComponent();",
						"  /**",
						"  * @wbp.nonvisual location=0,0",
						"  */",
						"  private final MyObject object_1 = new MyObject();",
						"  /**",
						"  * @wbp.nonvisual location=0,0",
						"  */",
						"  private final Object object_2 = new Object();",
						"  public Test() {",
						"    add(component);",
						"  }",
						"}");
		panel.refresh();
		// no "Object" properties for standard Swing components
		{
			assertNull(panel.getPropertyByTitle("dropTarget"));
		}
		// hierarchy
		assertHierarchy(
				"{this: javax.swing.JPanel} {this} {/add(component)/}",
				"  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
				"  {new: test.MyComponent} {field-initializer: component} {/new MyComponent()/ /add(component)/}",
				"  {NonVisualBeans}",
				"    {new: test.MyObject} {field-initializer: object_1} {/new MyObject()/}",
				"    {new: java.lang.Object} {field-initializer: object_2} {/new Object()/}");
		ComponentInfo component = panel.getChildrenComponents().get(0);
		// prepare property
		final Property property = component.getPropertyByTitle("value");
		assertNotNull(property);
		assertFalse(property.isModified());
		assertSame(PropertyCategory.ADVANCED, property.getCategory());
		// prepare editor
		final PropertyEditor propertyEditor = property.getEditor();
		assertSame(propertyEditor, ObjectPropertyEditor.INSTANCE);
		// animate
		new UiContext().executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() throws Exception {
				openPropertyDialog(property);
			}
		}, new FailableConsumer<>() {
			@Override
			public void accept(SWTBot bot) {
				SWTBot shell = bot.shell("value").bot();
				SWTBotTreeItem panelItem = shell.tree().expandNode("(javax.swing.JPanel)");
				SWTBotButton okButton = shell.button("OK");
				// initially "panel" selected, so invalid
				assertFalse(okButton.isEnabled());
				// prepare "non-visual beans" item
				SWTBotTreeItem beansContainer;
				{
					SWTBotTreeItem[] childItems = panelItem.getItems();
					Assertions.assertThat(childItems).hasSize(1);
					assertEquals("(non-visual beans)", childItems[0].getText());
					beansContainer = childItems[0];
				}
				// prepare "object_1"
				SWTBotTreeItem myObjectItem;
				{
					SWTBotTreeItem[] beanItems = beansContainer.getItems();
					Assertions.assertThat(beanItems).hasSize(1);
					assertEquals("object_1", beanItems[0].getText());
					myObjectItem = beanItems[0];
				}
				// container - invalid
				beansContainer.select();
				assertFalse(okButton.isEnabled());
				// "object_1" - valid
				myObjectItem.select();
				assertTrue(okButton.isEnabled());
				// click OK
				okButton.click();
			}
		});
		// check
		assertEditor(
				"public class Test extends JPanel {",
				"  private final MyComponent component = new MyComponent();",
				"  /**",
				"  * @wbp.nonvisual location=0,0",
				"  */",
				"  private final MyObject object_1 = new MyObject();",
				"  /**",
				"  * @wbp.nonvisual location=0,0",
				"  */",
				"  private final Object object_2 = new Object();",
				"  public Test() {",
				"    component.setValue(object_1);",
				"    add(component);",
				"  }",
				"}");
		assertNoErrors(panel);
		assertEquals("object_1", getPropertyText(property));
	}

	/**
	 * {@link ObjectPropertyEditor} can be used to select subclass {@link Component}.
	 */
	@Test
	public void test_subclassOfComponent() throws Exception {
		setJavaContentSrc("test", "MyComponent", new String[]{
				"public class MyComponent extends JLabel {",
				"  public void setButton(JButton button) {",
				"  }",
		"}"}, null);
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private final MyComponent component = new MyComponent();",
						"  private final JButton button = new JButton();",
						"  private final JTextField textField = new JTextField();",
						"  public Test() {",
						"    add(component);",
						"    add(button);",
						"    add(textField);",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo component = panel.getChildrenComponents().get(0);
		// prepare property
		final Property property = component.getPropertyByTitle("button");
		assertNotNull(property);
		assertFalse(property.isModified());
		// prepare editor
		final PropertyEditor propertyEditor = property.getEditor();
		assertSame(propertyEditor, ObjectPropertyEditor.INSTANCE);
		// animate
		new UiContext().executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() throws Exception {
				openPropertyDialog(property);
			}
		}, new FailableConsumer<>() {
			@Override
			public void accept(SWTBot bot) {
				SWTBot shell = bot.shell("button").bot();
				SWTBotTreeItem panelItem = shell.tree().expandNode("(javax.swing.JPanel)");
				SWTBotButton okButton = shell.button("OK");
				// initially "panel" selected, so invalid
				assertFalse(okButton.isEnabled());
				// prepare items
				SWTBotTreeItem[] childItems = panelItem.getItems();
				Assertions.assertThat(childItems).hasSize(1);
				assertEquals("button", childItems[0].getText());
				// JButton - valid
				childItems[0].click();
				assertTrue(okButton.isEnabled());
				// click OK
				okButton.click();
			}
		});
		// check
		assertEditor(
				"public class Test extends JPanel {",
				"  private final MyComponent component = new MyComponent();",
				"  private final JButton button = new JButton();",
				"  private final JTextField textField = new JTextField();",
				"  public Test() {",
				"    component.setButton(button);",
				"    add(component);",
				"    add(button);",
				"    add(textField);",
				"  }",
				"}");
		assertNoErrors(panel);
		assertEquals("button", getPropertyText(property));
	}

	/**
	 * {@link ObjectPropertyEditor} should select current value in dialog.
	 */
	@Test
	public void test_initialSelection() throws Exception {
		setJavaContentSrc("test", "MyComponent", new String[]{
				"public class MyComponent extends JLabel {",
				"  public void setButton(JButton button) {",
				"  }",
		"}"}, null);
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private final MyComponent component = new MyComponent();",
						"  private final JButton button_1 = new JButton();",
						"  private final JButton button_2 = new JButton();",
						"  public Test() {",
						"    component.setButton(button_2);",
						"    add(component);",
						"    add(button_1);",
						"    add(button_2);",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo component = getJavaInfoByName("component");
		// prepare property
		final Property property = component.getPropertyByTitle("button");
		assertNotNull(property);
		// prepare editor
		final PropertyEditor propertyEditor = property.getEditor();
		assertSame(propertyEditor, ObjectPropertyEditor.INSTANCE);
		// animate
		new UiContext().executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() throws Exception {
				openPropertyDialog(property);
			}
		}, new FailableConsumer<>() {
			@Override
			public void accept(SWTBot bot) {
				SWTBot shell = bot.shell("button").bot();
				// "button_2" is selected
				TableCollection selection = shell.tree().selection();
				assertEquals(selection.rowCount(), 1);
				assertEquals(selection.get(0, 0), "button_2");
				shell.button("Cancel").click();
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// JLabel.setLabelFor(java.awt.Component)
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * No invocation for this method.
	 */
	@Test
	public void test_getText_noInvocation() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JLabel label = new JLabel();",
						"      add(label);",
						"    }",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo label = panel.getChildrenComponents().get(0);
		// check "labelFor" property
		Property labelForProperty = label.getPropertyByTitle("labelFor");
		assertNotNull(labelForProperty);
		assertSame(PropertyCategory.PREFERRED, labelForProperty.getCategory());
		// no "setLabelFor()" invocation, so no text
		assertFalse(labelForProperty.isModified());
		assertNull(getPropertyText(labelForProperty));
		// check sub-properties
		{
			ObjectPropertyEditor opEditor = (ObjectPropertyEditor) labelForProperty.getEditor();
			assertEquals(opEditor.getProperties(labelForProperty).length, 0);
		}
	}

	/**
	 * We have invocation for this method.
	 */
	@Test
	public void test_getText_hasInvocation() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private final JLabel label = new JLabel();",
						"  private final JTextField textField = new JTextField();",
						"  public Test() {",
						"    add(label);",
						"    label.setLabelFor(textField);",
						"    add(textField);",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo label = panel.getChildrenComponents().get(0);
		// check "labelFor" property
		Property labelForProperty = label.getPropertyByTitle("labelFor");
		assertTrue(labelForProperty.isModified());
		assertEquals("textField", getPropertyText(labelForProperty));
		// check sub-properties
		{
			ObjectPropertyEditor opEditor = (ObjectPropertyEditor) labelForProperty.getEditor();
			Assertions.assertThat(opEditor.getProperties(labelForProperty).length).isGreaterThan(0);
		}
	}

	/**
	 * Absolute layout has <code>null</code> component class, should be handled correctly.
	 */
	@Test
	public void test_withAbsoluteLayout() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    setLayout(null);",
						"    add(new JLabel());",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo label = panel.getChildrenComponents().get(0);
		// prepare property
		final Property property = label.getPropertyByTitle("labelFor");
		final PropertyEditor propertyEditor = property.getEditor();
		assertSame(propertyEditor, ObjectPropertyEditor.INSTANCE);
		// animate - just open and ensure that dialog opened (no exception during this)
		new UiContext().executeAndCheck(new FailableRunnable<>() {
			@Override
			public void run() throws Exception {
				openPropertyDialog(property);
			}
		}, new FailableConsumer<>() {
			@Override
			public void accept(SWTBot bot) {
				SWTBot shell = bot.shell("labelFor").bot();
				shell.button("Cancel").click();
			}
		});
	}

	/**
	 * {@link JLabel} is before {@link JTextField}.
	 */
	@Test
	public void test_setComponent_labelBefore() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JLabel label = new JLabel();",
						"      add(label);",
						"    }",
						"    {",
						"      JTextField textField = new JTextField();",
						"      add(textField);",
						"    }",
						"  }",
						"}");
		ComponentInfo label = panel.getChildrenComponents().get(0);
		ComponentInfo textField = panel.getChildrenComponents().get(1);
		// set "labelFor" property
		Property labelForProperty = label.getPropertyByTitle("labelFor");
		setComponent(labelForProperty, textField);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JLabel label;",
				"  public Test() {",
				"    {",
				"      label = new JLabel();",
				"      add(label);",
				"    }",
				"    {",
				"      JTextField textField = new JTextField();",
				"      label.setLabelFor(textField);",
				"      add(textField);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * {@link JLabel} is after {@link JTextField}.
	 */
	@Test
	public void test_setComponent_labelAfter() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    {",
						"      JTextField textField = new JTextField();",
						"      add(textField);",
						"    }",
						"    {",
						"      JLabel label = new JLabel();",
						"      add(label);",
						"    }",
						"  }",
						"}");
		ComponentInfo textField = panel.getChildrenComponents().get(0);
		ComponentInfo label = panel.getChildrenComponents().get(1);
		// set "labelFor" property
		Property labelForProperty = label.getPropertyByTitle("labelFor");
		setComponent(labelForProperty, textField);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JTextField textField;",
				"  public Test() {",
				"    {",
				"      textField = new JTextField();",
				"      add(textField);",
				"    }",
				"    {",
				"      JLabel label = new JLabel();",
				"      label.setLabelFor(textField);",
				"      add(label);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * {@link LazyVariableSupport} for {@link JLabel} and {@link JTextField}. We can put
	 * {@link JLabel#setLabelFor(Component)} in any place, but prefer {@link JLabel} method.
	 */
	@Test
	public void test_setComponent_lazy() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private JLabel m_label;",
						"  private JTextField m_textField;",
						"  public Test() {",
						"    add(getLabel());",
						"    add(getTextField());",
						"  }",
						"  private JLabel getLabel() {",
						"    if (m_label == null) {",
						"      m_label = new JLabel();",
						"    }",
						"    return m_label;",
						"  }",
						"  private JTextField getTextField() {",
						"    if (m_textField == null) {",
						"      m_textField = new JTextField();",
						"    }",
						"    return m_textField;",
						"  }",
						"}");
		ComponentInfo label = panel.getChildrenComponents().get(0);
		ComponentInfo textField = panel.getChildrenComponents().get(1);
		// set "labelFor" property
		Property labelForProperty = label.getPropertyByTitle("labelFor");
		setComponent(labelForProperty, textField);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JLabel m_label;",
				"  private JTextField m_textField;",
				"  public Test() {",
				"    add(getLabel());",
				"    add(getTextField());",
				"  }",
				"  private JLabel getLabel() {",
				"    if (m_label == null) {",
				"      m_label = new JLabel();",
				"      m_label.setLabelFor(getTextField());",
				"    }",
				"    return m_label;",
				"  }",
				"  private JTextField getTextField() {",
				"    if (m_textField == null) {",
				"      m_textField = new JTextField();",
				"    }",
				"    return m_textField;",
				"  }",
				"}");
	}

	/**
	 * Set to different {@link JTextField}.
	 */
	@Test
	public void test_setComponent_newComponent() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private JLabel label;",
						"  public Test() {",
						"    {",
						"      label = new JLabel();",
						"      add(label);",
						"    }",
						"    {",
						"      JTextField textField_1 = new JTextField();",
						"      label.setLabelFor(textField_1);",
						"      add(textField_1);",
						"    }",
						"    {",
						"      JTextField textField_2 = new JTextField();",
						"      add(textField_2);",
						"    }",
						"  }",
						"}");
		ComponentInfo label = panel.getChildrenComponents().get(0);
		ComponentInfo textField_2 = panel.getChildrenComponents().get(2);
		// set "labelFor" property
		Property labelForProperty = label.getPropertyByTitle("labelFor");
		setComponent(labelForProperty, textField_2);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JLabel label;",
				"  public Test() {",
				"    {",
				"      label = new JLabel();",
				"      add(label);",
				"    }",
				"    {",
				"      JTextField textField_1 = new JTextField();",
				"      add(textField_1);",
				"    }",
				"    {",
				"      JTextField textField_2 = new JTextField();",
				"      label.setLabelFor(textField_2);",
				"      add(textField_2);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Use <code>null</code> to remove existing value.
	 */
	@Test
	public void test_setComponent_noComponent() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  private JLabel label;",
						"  public Test() {",
						"    {",
						"      label = new JLabel();",
						"      add(label);",
						"    }",
						"    {",
						"      JTextField textField_1 = new JTextField();",
						"      label.setLabelFor(textField_1);",
						"      add(textField_1);",
						"    }",
						"  }",
						"}");
		ComponentInfo label = panel.getChildrenComponents().get(0);
		// set "labelFor" property
		Property labelForProperty = label.getPropertyByTitle("labelFor");
		setComponent(labelForProperty, null);
		assertEditor(
				"public class Test extends JPanel {",
				"  private JLabel label;",
				"  public Test() {",
				"    {",
				"      label = new JLabel();",
				"      add(label);",
				"    }",
				"    {",
				"      JTextField textField_1 = new JTextField();",
				"      add(textField_1);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Use {@link ObjectPropertyEditor} for constructor argument.
	 */
	@Test
	public void test_setComponent_constructor() throws Exception {
		setFileContentSrc(
				"test/MyPanel.java",
				getTestSource(
						"public class MyPanel extends JPanel {",
						"  public MyPanel(JButton button) {",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		parseContainer(
				"public class Test extends JPanel {",
				"  private final JButton button_1 = new JButton();",
				"  private final JButton button_2 = new JButton();",
				"  private final MyPanel myPanel = new MyPanel(button_1);",
				"  public Test() {",
				"    add(button_1);",
				"    add(button_2);",
				"    add(myPanel);",
				"  }",
				"}");
		refresh();
		ContainerInfo myPanel = getJavaInfoByName("myPanel");
		ComponentInfo button_2 = getJavaInfoByName("button_2");
		// prepare Property
		Property property = PropertyUtils.getByPath(myPanel, "Constructor/button");
		assertNotNull(property);
		// initially "button_1"
		assertTrue(property.isModified());
		assertEquals("button_1", getPropertyText(property));
		// set "button_2"
		setComponent(property, button_2);
		assertEditor(
				"public class Test extends JPanel {",
				"  private final JButton button_1 = new JButton();",
				"  private final JButton button_2 = new JButton();",
				"  private final MyPanel myPanel = new MyPanel(button_2);",
				"  public Test() {",
				"    add(button_1);",
				"    add(button_2);",
				"    add(myPanel);",
				"  }",
				"}");
	}

	private static void setComponent(Property property, JavaInfo component) throws Exception {
		ReflectionUtils.invokeMethod2(
				property.getEditor(),
				"setComponent",
				GenericProperty.class,
				JavaInfo.class,
				property,
				component);
	}
}
