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
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;

/**
 * {@link MethodOrder} for component that should be added before other children.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ComponentOrderFirst extends ComponentOrder {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final ComponentOrder INSTANCE = new ComponentOrderFirst();

	private ComponentOrderFirst() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ComponentOrder
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public JavaInfo getNextComponent_whenLast(JavaInfo component, JavaInfo container)
			throws Exception {
		for (JavaInfo child : container.getChildrenJava()) {
			boolean isImplicit = child.getCreationSupport() instanceof IImplicitCreationSupport;
			if (!isImplicit) {
				return child;
			}
		}
		return null;
	}
}
