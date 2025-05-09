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
package org.eclipse.wb.internal.swt.gef.policy.layout.form;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplClassic;
import org.eclipse.wb.internal.swt.model.layout.form.IFormLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Insets;

import java.util.List;

/**
 * @author scheglov_ke
 * @author mitin_aa
 */
public final class FormUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Private constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private FormUtils() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	public static <C extends IControlInfo> List<C> getAttachableControls(IFormLayoutInfo<C> layout,
			C firstControl,
			boolean horizontal) throws Exception {
		int side = horizontal ? PositionConstants.LEFT : PositionConstants.TOP;
		FormLayoutInfoImplClassic<C> impl = (FormLayoutInfoImplClassic<C>) layout.getImpl();
		return impl.getAlignControlInfos(firstControl, side);
	}

	public static <C extends IControlInfo> List<C> getAttachableControls(IFormLayoutInfo<C> layout) {
		return layout.getControls();
	}

	public static <C extends IControlInfo> String getVariableName(final C child) {
		return ExecutionUtils.runObjectIgnore(() -> child.getPresentation().getText(), "<unknown>");
	}

	/**
	 * Check whether x is in interval [a, b]
	 */
	public static boolean between(int x, int a, int b) {
		return x >= a && x <= b;
	}

	public static int snapGrid(int value, int snap) {
		return applyGrid(value, snap);
	}

	/**
	 * Simple math round to nearest integer, based on grid step.
	 */
	private static int applyGrid(int value, int step) {
		return value / step * step;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils returning the layout margin according to side
	//
	////////////////////////////////////////////////////////////////////////////
	public static <C extends IControlInfo> int getLayoutMarginLeft(IFormLayoutInfo<C> layout) {
		return getLayoutMargin(layout, "marginLeft") + getLayoutMargin(layout, "marginWidth");
	}

	public static <C extends IControlInfo> int getLayoutMarginRight(IFormLayoutInfo<C> layout) {
		return getLayoutMargin(layout, "marginRight") + getLayoutMargin(layout, "marginWidth");
	}

	public static <C extends IControlInfo> int getLayoutMarginTop(IFormLayoutInfo<C> layout) {
		return getLayoutMargin(layout, "marginTop") + getLayoutMargin(layout, "marginHeight");
	}

	public static <C extends IControlInfo> int getLayoutMarginBottom(IFormLayoutInfo<C> layout) {
		return getLayoutMargin(layout, "marginBottom") + getLayoutMargin(layout, "marginHeight");
	}

	public static <C extends IControlInfo> Insets getLayoutMargins(IFormLayoutInfo<C> layout) {
		return new Insets(getLayoutMarginTop(layout),
				getLayoutMarginLeft(layout),
				getLayoutMarginBottom(layout),
				getLayoutMarginRight(layout));
	}

	private static <C extends IControlInfo> int getLayoutMargin(IFormLayoutInfo<C> layout,
			String marginSide) {
		try {
			return (Integer) layout.getPropertyByTitle(marginSide).getValue();
		} catch (Throwable e) {
			ReflectionUtils.propagate(e);
		}
		return 0;
	}
}
