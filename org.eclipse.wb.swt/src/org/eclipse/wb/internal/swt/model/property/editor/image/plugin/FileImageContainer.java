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
package org.eclipse.wb.internal.swt.model.property.editor.image.plugin;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.AbstractBrowseImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IHasChildren;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageElement;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation {@link IImageContainer} for file image resources into workspace plugin project.
 *
 * @author lobas_av
 * @coverage swt.property.editor.plugin
 */
public class FileImageContainer extends ImageContainer implements IHasChildren {
	private final IContainer m_container;
	private final String m_symbolicName;
	private IImageElement[] m_resources;
	private boolean m_calculateHasChildren = true;
	private boolean m_hasChildren;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FileImageContainer(IContainer container, String symbolicName) {
		m_container = container;
		m_symbolicName = symbolicName;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Elements
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Lazy loading resources for this container.
	 */
	private void ensureResources() {
		if (m_resources == null) {
			List<IImageElement> resources = new ArrayList<>();
			try {
				for (IResource resource : m_container.members()) {
					if (resource instanceof IContainer container) {
						// add not empty container
						if (isContainsResources(container)) {
							resources.add(new FileImageContainer(container, m_symbolicName));
						}
					} else if (resource instanceof IFile) {
						String extension = resource.getLocation().getFileExtension();
						// add image resource
						if (AbstractBrowseImagePage.isImageExtension(extension)) {
							resources.add(new FileImageResource((IFile) resource, m_symbolicName));
						}
					}
				}
			} catch (Throwable e) {
			}
			m_resources = resources.toArray(new IImageElement[resources.size()]);
		}
	}

	/**
	 * @return <code>true</code> if given {@link IContainer} contains image resources and
	 *         <code>false</code> otherwise.
	 */
	private static boolean isContainsResources(IContainer container) throws Exception {
		List<IContainer> subContainers = new ArrayList<>();
		// handle only file resources
		for (IResource resource : container.members()) {
			if (resource instanceof IContainer) {
				subContainers.add((IContainer) resource);
			} else if (resource instanceof IFile) {
				String extension = resource.getLocation().getFileExtension();
				if (AbstractBrowseImagePage.isImageExtension(extension)) {
					return true;
				}
			}
		}
		// handle child containers
		for (IContainer subContainer : subContainers) {
			if (isContainsResources(subContainer)) {
				return true;
			}
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IHasChildren
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean hasChildren() {
		try {
			if (m_calculateHasChildren) {
				m_calculateHasChildren = false;
				m_hasChildren = isContainsResources(m_container);
			}
			return m_hasChildren;
		} catch (Throwable e) {
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageContainer
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final IImageElement[] elements() {
		ensureResources();
		return m_resources;
	}

	@Override
	protected IImageElement[] directElements() {
		return m_resources;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageElement
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Image getImage() {
		return CoreImages.getSharedImage(CoreImages.FOLDER_OPEN);
	}

	@Override
	public final String getName() {
		return m_container.getName();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal
	//
	////////////////////////////////////////////////////////////////////////////
	protected final boolean findResource(List<Object> paths, String imagePath) {
		paths.add(this);
		for (IImageElement element : elements()) {
			if (element instanceof FileImageContainer container) {
				if (container.findResource(paths, imagePath)) {
					return true;
				}
			} else {
				FileImageResource resource = (FileImageResource) element;
				if (resource.getPath().equals(imagePath)) {
					paths.add(resource);
					return true;
				}
			}
		}
		paths.remove(this);
		return false;
	}

	@Override
	public Object[] findResource(String symbolicName, String imagePath) {
		return null;
	}
}