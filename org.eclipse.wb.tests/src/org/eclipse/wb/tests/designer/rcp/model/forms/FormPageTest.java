/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.rcp.model.forms;

import org.eclipse.wb.internal.rcp.model.forms.FormPageInfo;
import org.eclipse.wb.internal.rcp.model.forms.ManagedFormInfo;
import org.eclipse.wb.internal.rcp.model.forms.ScrolledFormInfo;

import org.eclipse.ui.forms.editor.FormEditor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link FormPageInfo}.
 *
 * @author scheglov_ke
 */
public class FormPageTest extends AbstractFormsTest {
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
	@Test
	public void test_0() throws Exception {
		FormPageInfo page =
				parseJavaInfo(
						"import org.eclipse.ui.forms.editor.*;",
						"public class Test extends FormPage {",
						"  /**",
						"  * @wbp.eval.method.parameter id 'Some id'",
						"  * @wbp.eval.method.parameter title 'Some title'",
						"  */",
						"  public Test(FormEditor editor, String id, String title) {",
						"    super(editor, id, title);",
						"  }",
						"  protected void createFormContent(IManagedForm managedForm) {",
						"    FormToolkit toolkit = managedForm.getToolkit();",
						"    ScrolledForm form = managedForm.getForm();",
						"    form.setText('Empty FormPage');",
						"  }",
						"}");
		// check hierarchy
		assertHierarchy(
				"{this: org.eclipse.ui.forms.editor.FormPage} {this} {}",
				"  {parameter} {managedForm} {/managedForm.getToolkit()/ /managedForm.getForm()/}",
				"    {method: public org.eclipse.ui.forms.widgets.ScrolledForm org.eclipse.ui.forms.ManagedForm.getForm()} {property} {/managedForm.getForm()/ /form.setText('Empty FormPage')/}",
				"      {method: public org.eclipse.swt.widgets.Composite org.eclipse.ui.forms.widgets.ScrolledForm.getBody()} {property} {}",
				"        {implicit-layout: absolute} {implicit-layout} {}",
				"    {method: public org.eclipse.ui.forms.widgets.FormToolkit org.eclipse.ui.forms.ManagedForm.getToolkit()} {property} {/managedForm.getToolkit()/}",
				"  {instance factory container}",
				"    {method: public org.eclipse.ui.forms.widgets.FormToolkit org.eclipse.ui.forms.ManagedForm.getToolkit()} {property} {/managedForm.getToolkit()/}");
		ManagedFormInfo managedForm = (ManagedFormInfo) page.getChildrenJava().get(0);
		ScrolledFormInfo scrolledForm = (ScrolledFormInfo) managedForm.getChildrenJava().get(0);
		// refresh
		page.refresh();
		assertEquals(page.getBounds().width, 600);
		assertEquals(page.getBounds().height, 500);
		Assertions.assertThat(scrolledForm.getBounds().width).isGreaterThanOrEqualTo(590);
		Assertions.assertThat(scrolledForm.getBounds().height).isGreaterThanOrEqualTo(450);
	}

	@Test
	public void test_severalConstructors() throws Exception {
		useStrictEvaluationMode(false);
		FormPageInfo page =
				parseJavaInfo(
						"import org.eclipse.ui.forms.editor.*;",
						"public class Test extends FormPage {",
						"  public Test(String id, String title) {",
						"    super(id, title);",
						"  }",
						"  public Test(FormEditor editor, String id, String title) {",
						"    super(editor, id, title);",
						"  }",
						"  protected void createFormContent(IManagedForm managedForm) {",
						"    ScrolledForm form = managedForm.getForm();",
						"  }",
						"}");
		page.refresh();
		assertNoErrors(page);
	}

	/**
	 * We should try to support {@link FormEditor} subclass.
	 */
	@Test
	public void test_subclassFormEditor() throws Exception {
		setFileContentSrc(
				"test/MyFormEditor.java",
				getTestSource(
						"import org.eclipse.ui.forms.editor.*;",
						"public abstract class MyFormEditor extends FormEditor {",
						"  public MyFormEditor() {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyFormPage.java",
				getTestSource(
						"import org.eclipse.ui.forms.editor.*;",
						"public abstract class MyFormPage extends FormPage {",
						"  public MyFormPage(MyFormEditor editor) {",
						"    super(editor, 'id', 'title');",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		FormPageInfo page =
				parseJavaInfo(
						"import org.eclipse.ui.forms.editor.*;",
						"public class Test extends MyFormPage {",
						"  public Test(MyFormEditor editor) {",
						"    super(editor);",
						"  }",
						"  protected void createFormContent(IManagedForm managedForm) {",
						"    ScrolledForm form = managedForm.getForm();",
						"  }",
						"}");
		page.refresh();
		assertNoErrors(page);
	}
}