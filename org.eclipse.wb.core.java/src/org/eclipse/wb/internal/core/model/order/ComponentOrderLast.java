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
package org.eclipse.wb.internal.core.model.order;

import org.eclipse.wb.core.model.JavaInfo;

/**
 * {@link MethodOrder} for component that should be added last, after all other children.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ComponentOrderLast extends ComponentOrder {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final ComponentOrder INSTANCE = new ComponentOrderLast();

	private ComponentOrderLast() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ComponentOrder
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canBeBefore(JavaInfo otherComponent) {
		return false;
	}
}
