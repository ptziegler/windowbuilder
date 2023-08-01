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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.junit.Test;

import org.eclipse.wb.internal.rcp.model.widgets.DropTargetInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.assertj.core.api.Assertions;

/**
 * Test for {@link DropTargetInfo}.
 *
 * @author scheglov_ke
 */
public class DropTargetTest extends RcpModelTest {
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
						"    new DropTarget(this, DND.DROP_MOVE);",
						"  }",
						"}");
		shell.refresh();
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/new DropTarget(this, DND.DROP_MOVE)/}",
				"  {implicit-layout: absolute} {implicit-layout} {}",
				"  {new: org.eclipse.swt.dnd.DropTarget} {empty} {/new DropTarget(this, DND.DROP_MOVE)/}");
		Assertions.assertThat(shell.getChildren(DropTargetInfo.class)).hasSize(1);
	}
}