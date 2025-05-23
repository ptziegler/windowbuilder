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
package org.eclipse.wb.internal.swt.gefTree.part;

import org.eclipse.wb.core.gefTree.part.JavaEditPart;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;

/**
 * {@link EditPart} for {@link ItemInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gefTree.part
 */
public class ItemEditPart extends JavaEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ItemEditPart(ItemInfo item) {
		super(item);
	}
}