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
package org.eclipse.wb.tests.designer.XML.editor;

import org.junit.Test;

import org.eclipse.wb.internal.core.xml.editor.actions.RefreshAction;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Test for {@link RefreshAction}.
 *
 * @author scheglov_ke
 */
public class RefreshActionTest extends XwtGefTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_run() throws Exception {
		openEditor("<Shell/>");
		Object oldObject = m_lastObject;
		//
		m_designPageActions.getRefreshAction().run();
		fetchContentFields();
		assertNotSame(oldObject, m_lastObject);
	}
}
