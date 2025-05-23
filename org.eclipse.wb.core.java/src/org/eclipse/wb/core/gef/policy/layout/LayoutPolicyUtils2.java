/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.core.gef.policy.layout;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.gef.commands.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains utilities for {@link LayoutEditPolicy}'s.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public class LayoutPolicyUtils2 {
	////////////////////////////////////////////////////////////////////////////
	//
	// Paste
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Command} that performs generic {@link JavaInfo} paste operation.
	 */
	public static <T extends JavaInfo> Command getPasteCommand(final JavaInfo existingHierarchyObject,
			final PasteRequest request,
			final Class<T> componentClass,
			final IPasteProcessor<T> processor) {
		@SuppressWarnings("unchecked")
		final List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) request.getMemento();
		return ExecutionUtils.runObjectLog(() -> {
			// prepare models
			final List<JavaInfo> components;
			{
				components = new ArrayList<>();
				for (JavaInfoMemento memento : mementos) {
					JavaInfo javaInfo = memento.create(existingHierarchyObject);
					if (componentClass.isAssignableFrom(javaInfo.getClass())) {
						components.add(javaInfo);
					} else {
						return null;
					}
				}
				// set objects for selection
				request.setObjects(components);
			}
			// create command
			return new EditCommand(existingHierarchyObject) {
				@Override
				@SuppressWarnings("unchecked")
				protected void executeEdit() throws Exception {
					for (int i = 0; i < components.size(); i++) {
						processor.process((T) components.get(i));
						mementos.get(i).apply();
					}
				}
			};
		}, null);
	}
	/**
	 * Performs some concrete operation during {@link JavaInfo} pasting.
	 */
	public interface IPasteProcessor<T extends JavaInfo> {
		/**
		 * Performs some action for given {@link JavaInfo} (in most case - adds given component).
		 */
		void process(T component) throws Exception;
	}
}
