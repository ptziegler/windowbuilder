/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.property.editor.presentation;

import org.eclipse.wb.internal.core.EnvironmentUtils;

import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.swt.graphics.Image;

/**
 * Property editor presentation realized as a {@link Button}.
 */
public class PresentationButton extends Button {
	private final PropertyEditorPresentation m_presentation;

	public PresentationButton(PropertyEditorPresentation presentation, Image image) {
		m_presentation = presentation;
		if (useFlatButton()) {
			setContents(new CFlatButton(getModel(), image));
			setBorder(null);
		} else {
			setContents(new Label(image));
		}
	}

	@Override
	protected void paintClientArea(Graphics graphics) {
		if (useFlatButton() && (getModel().isArmed() || getModel().isSelected())) {
			graphics.translate(-1, -1);
		}
		super.paintClientArea(graphics);
		if (useFlatButton() && (getModel().isArmed() || getModel().isSelected())) {
			graphics.translate(1, 1);
		}
	}

	private boolean useFlatButton() {
		return EnvironmentUtils.IS_MAC;
	}

	public final PropertyEditorPresentation getPresentation() {
		return m_presentation;
	}
}
