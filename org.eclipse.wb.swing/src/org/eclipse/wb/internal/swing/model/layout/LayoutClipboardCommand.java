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
package org.eclipse.wb.internal.swing.model.layout;

import org.eclipse.wb.internal.core.model.clipboard.ComponentClipboardCommand;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

/**
 * Command for adding {@link ComponentInfo} to {@link LayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public abstract class LayoutClipboardCommand<L extends LayoutInfo>
extends
ComponentClipboardCommand<ContainerInfo> {
	private static final long serialVersionUID = 0L;
	private final JavaInfoMemento m_memento;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutClipboardCommand(ComponentInfo component) throws Exception {
		m_memento = JavaInfoMemento.createMemento(component);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Execute
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@SuppressWarnings("unchecked")
	protected void execute(ContainerInfo container) throws Exception {
		ComponentInfo component = (ComponentInfo) m_memento.create(container);
		add((L) container.getLayout(), component);
		m_memento.apply();
	}

	/**
	 * Adds given {@link ComponentInfo} to {@link ContainerInfo} using layout specific way.
	 */
	protected abstract void add(L layout, ComponentInfo component) throws Exception;
}
