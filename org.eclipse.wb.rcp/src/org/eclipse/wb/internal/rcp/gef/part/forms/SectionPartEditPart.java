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
package org.eclipse.wb.internal.rcp.gef.part.forms;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.rcp.model.forms.SectionPartInfo;

/**
 * {@link EditPart} for {@link SectionPartInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class SectionPartEditPart extends AbstractComponentEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SectionPartEditPart(SectionPartInfo composite) {
		super(composite);
	}
}
