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
package org.eclipse.wb.tests.designer.core.model.description;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.MorphingTargetDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Tests for {@link MorphingTargetDescription}.
 *
 * @author scheglov_ke
 */
public class MorphingTargetDescriptionTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		parseContainer(
				"// filler filler filler",
				"public class Test extends JPanel {",
				"  public Test() {",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for loading {@link MorphingTargetDescription}'s from "*.wbp-component.xml" files.
	 */
	@Test
	public void test_loadFromDescriptions() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public class MyButton extends JComponent {",
						"  public MyButton() {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <morphTargets>",
						"    <morphTarget class='javax.swing.JButton'/>",
						"    <morphTarget class='javax.swing.JTextField' creationId='someId'/>",
						"  </morphTargets>",
						"</component>"));
		waitForAutoBuild();
		// prepare morphing targets
		List<MorphingTargetDescription> morphingTargets;
		{
			ComponentDescription description =
					ComponentDescriptionHelper.getDescription(
							m_lastEditor,
							m_lastLoader.loadClass("test.MyButton"));
			morphingTargets = description.getMorphingTargets();
		}
		// check targets
		assertEquals(2, morphingTargets.size());
		{
			MorphingTargetDescription morphingTarget = morphingTargets.get(0);
			assertEquals("javax.swing.JButton", morphingTarget.getComponentClass().getName());
			assertNull(morphingTarget.getCreationId());
		}
		{
			MorphingTargetDescription morphingTarget = morphingTargets.get(1);
			assertEquals("javax.swing.JTextField", morphingTarget.getComponentClass().getName());
			assertEquals("someId", morphingTarget.getCreationId());
		}
	}

	/**
	 * We should ignore invalid target classes.
	 */
	@Test
	public void test_noTargetClass() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public class MyButton extends JComponent {",
						"  public MyButton() {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <morphTargets>",
						"    <morphTarget class='no.such.Class'/>",
						"    <morphTarget class='javax.swing.JButton'/>",
						"  </morphTargets>",
						"</component>"));
		waitForAutoBuild();
		// prepare morphing targets
		List<MorphingTargetDescription> morphingTargets;
		{
			ComponentDescription description =
					ComponentDescriptionHelper.getDescription(
							m_lastEditor,
							m_lastLoader.loadClass("test.MyButton"));
			morphingTargets = description.getMorphingTargets();
		}
		// check targets
		Assertions.assertThat(morphingTargets).hasSize(1);
		{
			MorphingTargetDescription morphingTarget = morphingTargets.get(0);
			assertEquals("javax.swing.JButton", morphingTarget.getComponentClass().getName());
			assertNull(morphingTarget.getCreationId());
		}
	}

	/**
	 * We should clear targets on "noInhetit=true"
	 *
	 * @throws Exception
	 */
	@Test
	public void test_noInherit() throws Exception {
		// MyBaseButton
		{
			setFileContentSrc(
					"test/MyBaseButton.java",
					getTestSource(
							"public class MyBaseButton extends JComponent {",
							"  public MyBaseButton() {",
							"  }",
							"}"));
			setFileContentSrc(
					"test/MyBaseButton.wbp-component.xml",
					getSourceDQ(
							"<?xml version='1.0' encoding='UTF-8'?>",
							"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
							"  <morphTargets>",
							"    <morphTarget class='javax.swing.JButton'/>",
							"  </morphTargets>",
							"</component>"));
		}
		// MyButton1
		{
			setFileContentSrc(
					"test/MyButton1.java",
					getTestSource(
							"public class MyButton1 extends MyBaseButton {",
							"  public MyButton1() {",
							"  }",
							"}"));
			setFileContentSrc(
					"test/MyButton1.wbp-component.xml",
					getSourceDQ(
							"<?xml version='1.0' encoding='UTF-8'?>",
							"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
							"  <morphTargets>",
							"    <morphTarget class='javax.swing.JPanel'/>",
							"  </morphTargets>",
							"</component>"));
		}
		// MyButton2
		{
			setFileContentSrc(
					"test/MyButton2.java",
					getTestSource(
							"public class MyButton2 extends MyBaseButton {",
							"  public MyButton2() {",
							"  }",
							"}"));
			setFileContentSrc(
					"test/MyButton2.wbp-component.xml",
					getSourceDQ(
							"<?xml version='1.0' encoding='UTF-8'?>",
							"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
							"  <morphTargets>",
							"    <noInherit/>",
							"    <morphTarget class='javax.swing.JLabel'/>",
							"  </morphTargets>",
							"</component>"));
		}
		waitForAutoBuild();
		// check description for MyButton1
		{
			ComponentDescription description =
					ComponentDescriptionHelper.getDescription(
							m_lastEditor,
							m_lastLoader.loadClass("test.MyButton1"));
			List<MorphingTargetDescription> morphingTargets = description.getMorphingTargets();
			Assertions.assertThat(morphingTargets).hasSize(2);
			// check targets
			{
				MorphingTargetDescription morphingTarget = morphingTargets.get(0);
				assertEquals("javax.swing.JButton", morphingTarget.getComponentClass().getName());
				assertNull(morphingTarget.getCreationId());
			}
			{
				MorphingTargetDescription morphingTarget = morphingTargets.get(1);
				assertEquals("javax.swing.JPanel", morphingTarget.getComponentClass().getName());
				assertNull(morphingTarget.getCreationId());
			}
		}
		// check description for MyButton2
		{
			ComponentDescription description =
					ComponentDescriptionHelper.getDescription(
							m_lastEditor,
							m_lastLoader.loadClass("test.MyButton2"));
			List<MorphingTargetDescription> morphingTargets = description.getMorphingTargets();
			// check targets
			Assertions.assertThat(morphingTargets).hasSize(1); // no target JButton
			{
				MorphingTargetDescription morphingTarget = morphingTargets.get(0);
				assertEquals("javax.swing.JLabel", morphingTarget.getComponentClass().getName());
				assertNull(morphingTarget.getCreationId());
			}
		}
	}
}
