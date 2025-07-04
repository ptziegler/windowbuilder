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

import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.rcp.IExceptionConstants;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.junit.jupiter.api.Test;

/**
 * Test for <code>MultiPageEditorPart</code> support (rather not support).
 *
 * @author scheglov_ke
 */
public class MultiPageEditorPartTest extends RcpModelTest {
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
	public void test_parsingException() throws Exception {
		try {
			parseJavaInfo(
					"import org.eclipse.core.runtime.IProgressMonitor;",
					"import org.eclipse.ui.part.MultiPageEditorPart;",
					"public abstract class Test extends MultiPageEditorPart {",
					"  public Test() {",
					"  }",
					"  protected void createPages() {",
					"  }",
					"  public boolean isSaveAsAllowed() {",
					"    return false;",
					"  }",
					"  public void doSave(IProgressMonitor monitor) {",
					"  }",
					"  public void doSaveAs() {	",
					"  }",
					"}");
			fail();
		} catch (DesignerException e) {
			assertEquals(IExceptionConstants.NO_DESIGN_MP_EDITOR, e.getCode());
			assertTrue(DesignerExceptionUtils.isWarning(e));
		}
	}
}