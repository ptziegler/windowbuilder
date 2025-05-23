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
package org.eclipse.wb.internal.swing.model.layout.gbl;

import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

/**
 * Visitor for {@link ComponentInfo} and its {@link AbstractGridBagConstraintsInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
interface IComponentVisitor {
	/**
	 * Visits {@link ComponentInfo} and its {@link AbstractGridBagConstraintsInfo}.
	 */
	void visit(ComponentInfo component, AbstractGridBagConstraintsInfo constraints) throws Exception;
}
