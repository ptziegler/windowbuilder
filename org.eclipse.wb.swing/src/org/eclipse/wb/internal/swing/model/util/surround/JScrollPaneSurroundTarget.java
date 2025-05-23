/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.swing.model.util.surround;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.util.surround.ISurroundTarget;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JScrollPaneInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;

import org.eclipse.jface.resource.ImageDescriptor;

import java.util.List;

import javax.swing.JScrollPane;

/**
 * {@link ISurroundTarget} that uses {@link JScrollPane} as target container.
 *
 * @author scheglov_ke
 * @coverage swing.model.util
 */
public final class JScrollPaneSurroundTarget
extends
ISurroundTarget<JScrollPaneInfo, ComponentInfo> {
	private static final String CLASS_NAME = "javax.swing.JScrollPane";

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getIcon(AstEditor editor) throws Exception {
		return ComponentDescriptionHelper.getDescription(editor, CLASS_NAME).getIcon();
	}

	@Override
	public String getText(AstEditor editor) throws Exception {
		return CLASS_NAME;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public JScrollPaneInfo createContainer(AstEditor editor) throws Exception {
		return (JScrollPaneInfo) JavaInfoUtils.createJavaInfo(
				editor,
				CLASS_NAME,
				new ConstructorCreationSupport());
	}

	@Override
	public void afterContainerAdd(JScrollPaneInfo container, List<ComponentInfo> components)
			throws Exception {
		super.afterContainerAdd(container, components);
		if (components.size() != 1) {
			ContainerInfo panel =
					(ContainerInfo) JavaInfoUtils.createJavaInfo(
							container.getEditor(),
							"javax.swing.JPanel",
							new ConstructorCreationSupport());
			container.command_CREATE(panel, "setViewportView");
		}
	}

	@Override
	public void move(JScrollPaneInfo container, ComponentInfo component) throws Exception {
		if (container.getChildrenComponents().isEmpty()) {
			container.command_ADD(component, "setViewportView");
		} else {
			ContainerInfo panel = (ContainerInfo) container.getChildrenComponents().get(0);
			FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
			flowLayout.move(component, null);
		}
	}
}
