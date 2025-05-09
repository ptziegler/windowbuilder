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
package org.eclipse.wb.internal.core.editor.palette.command;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;

import org.xml.sax.Attributes;

/**
 * Implementation of {@link Command} that removes {@link CategoryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette
 */
public final class CategoryRemoveCommand extends Command {
	public static final String ID = "removeCategory";
	private final String m_id;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public CategoryRemoveCommand(CategoryInfo category) {
		m_id = category.getId();
	}

	public CategoryRemoveCommand(Attributes attributes) {
		m_id = attributes.getValue("id");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void execute(PaletteInfo palette) {
		CategoryInfo category = palette.getCategory(m_id);
		if (category != null) {
			palette.getCategories().remove(category);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addAttributes() {
		addAttribute("id", m_id);
	}
}
