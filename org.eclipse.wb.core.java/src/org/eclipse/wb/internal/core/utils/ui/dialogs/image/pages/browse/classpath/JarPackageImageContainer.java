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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.classpath;

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageElement;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link IImageContainer} for package in jar.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
final class JarPackageImageContainer extends AbstractJarImageElement implements IImageContainer {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JarPackageImageContainer(JarImageContainer jarContainer, IPath entryPath) {
		super(jarContainer, entryPath);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageContainer
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IImageElement[] elements() {
		return m_imageEntryList.toArray(new IImageElement[m_imageEntryList.size()]);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageElement
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Image getImage() {
		return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
	}

	@Override
	public String getName() {
		return m_entryPath.toString().replace('/', '.');
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Image resources
	//
	////////////////////////////////////////////////////////////////////////////
	private final List<JarImageResource> m_imageEntryList = new ArrayList<>();

	/**
	 * Adds given {@link JarImageResource} to this package.
	 */
	void addImageEntry(JarImageResource resource) {
		m_imageEntryList.add(resource);
	}
}