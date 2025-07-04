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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.eclipse.wb.internal.rcp.model.widgets.DragSourceInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link DragSourceInfo}.
 *
 * @author scheglov_ke
 */
public class DragSourceTest extends RcpModelTest {
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
		CompositeInfo shell =
				parseComposite(
						"import org.eclipse.swt.dnd.*;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    new DragSource(this, DND.DROP_MOVE);",
						"  }",
						"}");
		shell.refresh();
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/new DragSource(this, DND.DROP_MOVE)/}",
				"  {implicit-layout: absolute} {implicit-layout} {}",
				"  {new: org.eclipse.swt.dnd.DragSource} {empty} {/new DragSource(this, DND.DROP_MOVE)/}");
		Assertions.assertThat(shell.getChildren(DragSourceInfo.class)).hasSize(1);
	}
}