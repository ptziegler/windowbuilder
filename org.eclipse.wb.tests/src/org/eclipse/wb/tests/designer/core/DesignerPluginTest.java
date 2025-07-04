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
package org.eclipse.wb.tests.designer.core;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * Test for {@link DesignerPlugin}.
 *
 * @author scheglov_ke
 */
public class DesignerPluginTest extends DesignerTestCase {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getDefault() {
		assertNotNull(DesignerPlugin.getDefault());
	}

	@Test
	public void test_log_message() {
		final String message = "Information message";
		ILog log = DesignerPlugin.getDefault().getLog();
		ILogListener logListener = new ILogListener() {
			@Override
			public void logging(IStatus status, String plugin) {
				assertEquals(IStatus.INFO, status.getSeverity());
				assertEquals(DesignerPlugin.PLUGIN_ID, status.getPlugin());
				assertEquals(IStatus.INFO, status.getCode());
				assertEquals(message, status.getMessage());
				assertNull(status.getException());
			}
		};
		//
		try {
			log.addLogListener(logListener);
			DesignerPlugin.log(message);
		} finally {
			log.removeLogListener(logListener);
		}
	}

	@Test
	public void test_log_Exception() {
		final Exception exception = new Exception();
		ILog log = DesignerPlugin.getDefault().getLog();
		ILogListener logListener = new ILogListener() {
			@Override
			public void logging(IStatus status, String plugin) {
				assertEquals(IStatus.ERROR, status.getSeverity());
				assertEquals(DesignerPlugin.PLUGIN_ID, status.getPlugin());
				assertEquals(IStatus.ERROR, status.getCode());
				assertSame(exception, status.getException());
			}
		};
		//
		try {
			log.addLogListener(logListener);
			DesignerPlugin.setDisplayExceptionOnConsole(false);
			DesignerPlugin.log(exception);
		} finally {
			log.removeLogListener(logListener);
			DesignerPlugin.setDisplayExceptionOnConsole(true);
		}
	}

	/**
	 * However we should allow {@link JFrame#setDefaultCloseOperation(int)} with
	 * {@link JFrame#EXIT_ON_CLOSE}.
	 */
	@Test
	public void test_preventExit_JFrame() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
}
