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
package org.eclipse.wb.tests.designer.XML;

import org.junit.Test;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.xml.Activator;
import org.eclipse.wb.internal.core.xml.IExceptionConstants;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;

import java.io.InputStream;

/**
 * Test for {@link Activator}.
 *
 * @author scheglov_ke
 */
public class ActivatorTest extends DesignerTestCase {
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
	 * Test for {@link Activator#getDefault()}.
	 */
	@Test
	public void test_getDefault() throws Exception {
		assertNotNull(Activator.getDefault());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getFile()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link Activator#getFile(String)}.
	 */
	@Test
	public void test_getFile() throws Exception {
		InputStream file = Activator.getFile("plugin.xml");
		assertNotNull(file);
		try {
			String s = IOUtils2.readString(file);
			Assertions.assertThat(s.length()).isGreaterThan(1024);
		} finally {
			IOUtils.closeQuietly(file);
		}
	}

	/**
	 * Test for {@link Activator#getFile(String)}.
	 */
	@Test
	public void test_getFile_bad() throws Exception {
		try {
			Activator.getFile("noSuch.file");
		} catch (Throwable e) {
			String msg = e.getMessage();
			Assertions.assertThat(msg).contains("noSuch.file").contains("org.eclipse.wb.core.xml");
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getImage()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link Activator#getImage(String)}.
	 */
	@Test
	public void test_getImage_good() throws Exception {
		Image image = Activator.getImage("editor_page_xml.png");
		assertNotNull(image);
	}

	/**
	 * Test for {@link Activator#getImage(String)}.
	 */
	@Test
	public void test_getImage_bad() throws Exception {
		try {
			Activator.getImage("noSuch.png");
		} catch (Throwable e) {
			String msg = e.getMessage();
			Assertions.assertThat(msg).contains("noSuch.png").contains("org.eclipse.wb.core.xml");
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getImageDescriptor()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link Activator#getImageDescriptor(String)}.
	 */
	@Test
	public void test_getImageDescription_good() throws Exception {
		ImageDescriptor imageDescriptor = Activator.getImageDescriptor("editor_page_xml.png");
		assertNotNull(imageDescriptor);
	}

	/**
	 * Test for {@link Activator#getImageDescriptor(String)}.
	 */
	@Test
	public void test_getImageDescription_bad() throws Exception {
		try {
			Activator.getImageDescriptor("noSuch.png");
		} catch (Throwable e) {
			String msg = e.getMessage();
			Assertions.assertThat(msg).contains("noSuch.png").contains("org.eclipse.wb.core.xml");
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IExceptionConstants
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Just kick {@link IExceptionConstants} to force its coverage.
	 */
	@Test
	public void test_IExceptionConstants() throws Exception {
		Assertions.assertThat(IExceptionConstants.__FORCE_EXECUTION).isZero();
	}
}