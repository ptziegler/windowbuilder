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
package org.eclipse.wb.internal.swt.support;

import org.eclipse.wb.internal.core.model.menu.MenuVisualData;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;

import java.lang.reflect.Constructor;

/**
 * Toolkit specific utilities for RCP.
 *
 * @author lobas_av
 * @coverage swt.support
 */
public class ToolkitSupport extends AbstractSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// ScreenShot
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Marks given {@link Control} instance as needed screen shot {@link Image}.
	 */
	public static void markAsNeededImage(Control control) throws Exception {
		control.setData(OSSupport.WBP_NEED_IMAGE, Boolean.TRUE);
	}

	/**
	 * @return the screen shot {@link Image} from given {@link Control}.
	 */
	public static Image getShotImage(Control control) throws Exception {
		return (Image) control.getData(OSSupport.WBP_IMAGE);
	}

	/**
	 * Creates screen shots for all {@link Control}'s in hierarchy marked with
	 * <code>WBP_NEED_IMAGE</code>. Created {@link Image}'s are located in <code>WBP_IMAGE</code>
	 * data.
	 */
	public static void makeShots(Control control) throws Exception {
		getImpl().makeShots(control);
		makeShotsHierarchy(control);
	}

	/**
	 * Creates screen shots for all {@link Control}'s in hierarchy marked with
	 * <code>WBP_NEED_IMAGE</code>. Created {@link Image}'s are located in <code>WBP_IMAGE</code>
	 * data.
	 */
	private static void makeShotsHierarchy(Control control) throws Exception {
		if (control.getData(OSSupport.WBP_NEED_IMAGE) != null) {
			control.setData(OSSupport.WBP_IMAGE, getImpl().getShotImage(control));
			// set images for children
			if (control instanceof Composite composite) {
				for (Control child : composite.getChildren()) {
					makeShotsHierarchy(child);
				}
			}
		}
	}

	/**
	 * Prepares the process of taking screen shot.
	 */
	public static void beginShot(Control control) throws Exception {
		getImpl().beginShot(control);
	}

	/**
	 * Finalizes the process of taking screen shot.
	 */
	public static void endShot(Control control) throws Exception {
		getImpl().endShot(control);
	}

	/**
	 * @return {@link MenuVisualData} for given menu.
	 */
	public static MenuVisualData fetchMenuVisualData(Menu menu) throws Exception {
		return getImpl().fetchMenuVisualData(menu);
	}

	/**
	 * @return the default height of single-line menu bar according to system metrics (if available).
	 */
	public static int getDefaultMenuBarHeight() throws Exception {
		return getImpl().getDefaultMenuBarHeight();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Shell
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Shows given {@link Shell} object to user. On close {@link Shell} will be hidden, not disposed.
	 */
	public static void showShell(Shell shell) throws Exception {
		getImpl().showShell(shell);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Font
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the array of font families registered in system.
	 */
	public static String[] getFontFamilies(boolean scalable) throws Exception {
		return getImpl().getFontFamilies(scalable);
	}

	/**
	 * @return {@link Image} with preview for given {@link Font}.
	 */
	public static Image getFontPreview(Font font) throws Exception {
		return getImpl().getFontPreview(font);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private static IToolkitSupport m_impl_RCP;

	/**
	 * @return the toolkit dependent implementation of {@link IToolkitSupport}.
	 */
	private static IToolkitSupport getImpl() throws Exception {
		if (m_impl_RCP == null) {
			m_impl_RCP = createImpl();
		}
		return m_impl_RCP;
	}

	/**
	 * @return the new instance of toolkit dependent implementation of {@link IToolkitSupport}.
	 */
	private static IToolkitSupport createImpl() throws Exception {
		// prepare "impl" class
		Class<?> implClass;
		{
			Bundle bundle = GlobalState.getToolkit().getBundle();
			String implClassName = bundle.getSymbolicName() + ".support.ToolkitSupportImpl";
			implClassName = StringUtils.replace(implClassName, ".wb.", ".wb.internal.");
			implClass = bundle.loadClass(implClassName);
		}
		// create instance
		ClassLoader editorLoader = GlobalState.getClassLoader();
		Constructor<?> constructor = implClass.getConstructor(ClassLoader.class);
		return (IToolkitSupport) constructor.newInstance(editorLoader);
	}
}