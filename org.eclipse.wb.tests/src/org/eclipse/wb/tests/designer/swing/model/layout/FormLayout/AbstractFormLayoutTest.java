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
package org.eclipse.wb.tests.designer.swing.model.layout.FormLayout;

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.swing.laf.LafSupport;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.util.DefaultUnitConverter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.awt.Font;

/**
 * Abstract test for {@link FormLayout}.
 *
 * @author scheglov_ke
 */
public abstract class AbstractFormLayoutTest extends AbstractLayoutTest {
	private static Font m_font;
	protected boolean m_useFormsImports = true;

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////

	@BeforeAll
	public static void setUpAll() {
		m_font = DefaultUnitConverter.getInstance().getDefaultDialogFont();
		Font newFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		DefaultUnitConverter.getInstance().setDefaultDialogFont(newFont);
	}

	@AfterAll
	public static void tearDownAll() {
		DefaultUnitConverter.getInstance().setDefaultDialogFont(m_font);
		m_font = null;
	}

	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		LafSupport.applySelectedLAF(LafSupport.getDefaultLAF());
	}

	@Override
	protected void configureNewProject() throws Exception {
		super.configureNewProject();
		do_configureNewProject();
	}

	static void do_configureNewProject() throws Exception {
		m_testProject.addPlugin("com.jgoodies.common");
		m_testProject.addPlugin("com.jgoodies.forms");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTestSource(String... lines) {
		if (m_useFormsImports) {
			lines =
					CodeUtils.join(new String[]{
							"import com.jgoodies.forms.layout.*;",
					"import com.jgoodies.forms.factories.*;"}, lines);
		}
		return super.getTestSource(lines);
	}
}
