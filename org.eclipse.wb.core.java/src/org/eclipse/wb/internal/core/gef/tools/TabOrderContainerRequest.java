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
package org.eclipse.wb.internal.core.gef.tools;

import org.eclipse.wb.core.model.AbstractComponentInfo;

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.List;

/**
 * Special {@link Request} for information about container child ordering.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage core.gef
 */
public final class TabOrderContainerRequest extends Request {
	private List<AbstractComponentInfo> m_possibleChildren;
	private List<AbstractComponentInfo> m_children;
	private Object m_selectedChild;
	private Command m_command;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TabOrderContainerRequest(Object type) {
		super(type);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return children that can be selected or not in list of tab order.
	 */
	public List<AbstractComponentInfo> getPossibleChildren() {
		return m_possibleChildren;
	}

	/**
	 * Sets children that can be selected or not in list of tab order.
	 */
	public void setPossibleChildren(List<AbstractComponentInfo> possibleChildren) {
		m_possibleChildren = possibleChildren;
	}

	/**
	 * @return selected children that should be reordered.
	 */
	public List<AbstractComponentInfo> getChildren() {
		return m_children;
	}

	/**
	 * Sets selected children that should be reordered.
	 */
	public void setChildren(List<AbstractComponentInfo> children) {
		m_children = children;
	}

	/**
	 * @return the current selection for change ordering.
	 */
	public Object getSelectedChild() {
		return m_selectedChild;
	}

	/**
	 * Sets current selection for change ordering.
	 */
	public void setSelectedChild(Object selectedChild) {
		m_selectedChild = selectedChild;
	}

	/**
	 * @return {@link Command} for execute reordering.
	 */
	public Command getCommand() {
		return m_command;
	}

	/**
	 * Sets {@link Command} for execute reordering.
	 */
	public void setCommand(Command command) {
		m_command = command;
	}
}