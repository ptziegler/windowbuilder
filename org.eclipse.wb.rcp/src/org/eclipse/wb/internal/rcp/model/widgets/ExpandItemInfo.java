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
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils.IMoveTargetProvider;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

import java.util.Collections;
import java.util.List;

/**
 * Model for {@link ExpandItem}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public final class ExpandItemInfo extends ItemInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ExpandItemInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		addBroadcastListener(new JavaEventListener() {
			private ControlInfo m_ourControl;

			@Override
			public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				// remove setControl() invocation when move ControlInfo in/from our ExpandBar
				if (oldParent == getParent() && child instanceof ControlInfo && getControl() == child) {
					removeMethodInvocations("setControl(org.eclipse.swt.widgets.Control)");
					removeMethodInvocations("setHeight(int)");
				}
				// when WE are moved, remove possible setControl() invocation
				if (child == ExpandItemInfo.this) {
					m_ourControl = getControl();
					removeMethodInvocations("setControl(org.eclipse.swt.widgets.Control)");
				}
			}

			@Override
			public void variable_addStatementsToMove(JavaInfo parent, List<JavaInfo> children)
					throws Exception {
				if (parent == ExpandItemInfo.this) {
					if (m_ourControl != null) {
						children.add(m_ourControl);
					}
				}
			}

			@Override
			public void moveAfter(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				// if WE were moved, move our ControlInfo
				if (child == ExpandItemInfo.this) {
					if (m_ourControl != null) {
						command_ADD(m_ourControl);
					}
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean isExpanded() throws Exception {
		return getWidget().getExpanded();
	}

	@Override
	public ExpandItem getWidget() {
		return (ExpandItem) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
		@Override
		public List<ObjectInfo> getChildrenTree() throws Exception {
			ControlInfo control = getControl();
			return GenericsUtils.singletonList(control);
		}

		@Override
		public List<ObjectInfo> getChildrenGraphical() throws Exception {
			if (!isExpanded()) {
				return Collections.emptyList();
			}
			return getChildrenTree();
		}
	};

	@Override
	public IObjectPresentation getPresentation() {
		return m_presentation;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		{
			ExpandItem object = getWidget();
			int headerHeight = object.getHeaderHeight();
			int width = ReflectionUtils.getFieldInt(object, "width");
			int height = object.getHeight();
			if (isExpanded()) {
				height += headerHeight;
			} else {
				height = headerHeight;
			}
			// x & y fields may not exist on every platform
			// but as far as I can tell, they are always initialized with 0
			setModelBounds(new Rectangle(0, 0, width, height));
		}
		super.refresh_fetch();
	}

	/**
	 * {@link Control} returns bounds on {@link ExpandBar}, but we show it as child of
	 * {@link ExpandItem}, so we should tweak {@link Control} bounds.
	 */
	void fixControlBounds() {
		ControlInfo control = getControl();
		if (control != null) {
			{
				Point offset = getModelBounds().getLocation().getNegated();
				control.getModelBounds().performTranslate(offset);
			}
			{
				Point offset = getBounds().getLocation().getNegated();
				control.getBounds().performTranslate(offset);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setControl() support
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link ControlInfo} set using
	 *         {@link ExpandItem#setControl(org.eclipse.swt.widgets.Control)}, may be
	 *         <code>null</code>.
	 */
	public ControlInfo getControl() {
		MethodInvocation invocation =
				getMethodInvocation("setControl(org.eclipse.swt.widgets.Control)");
		if (invocation != null) {
			Expression controlExpression = DomGenerics.arguments(invocation).get(0);
			return (ControlInfo) getParentJava().getChildRepresentedBy(controlExpression);
		}
		return null;
	}

	/**
	 * Adds new {@link ControlInfo} to {@link ExpandBarInfo} and associates with given
	 * {@link ExpandItemInfo} set using {@link ExpandItem#setControl(org.eclipse.swt.widgets.Control)}
	 * .
	 */
	public void command_CREATE(ControlInfo control) throws Exception {
		addInvocation_setHeight();
		// add to ExpandBar
		{
			StatementTarget target = JavaInfoUtils.getTarget(this, control, null);
			JavaInfoUtils.addTarget(control, null, getParentJava(), target);
		}
		// add setControl() invocation
		addInvocation_setControl(control);
	}

	/**
	 * After CREATE or ADD operation.
	 */
	public void command_TARGET_after(ControlInfo control) throws Exception {
		getPropertyByTitle("expanded").setValue(true);
	}

	/**
	 * Adds existing {@link ControlInfo} to {@link ExpandBarInfo} and associates with given
	 * {@link ExpandItemInfo} set using {@link ExpandItem#setControl(org.eclipse.swt.widgets.Control)}
	 * .
	 */
	public void command_ADD(final ControlInfo control) throws Exception {
		addInvocation_setHeight();
		// move to ExpandBar, but code inside of ExpandItem
		{
			final ExpandBarInfo expandBar = (ExpandBarInfo) getParentJava();
			final StatementTarget target = JavaInfoUtils.getTarget(this, control, null);
			IMoveTargetProvider targetProvider = new IMoveTargetProvider() {
				@Override
				public void add() throws Exception {
					expandBar.addChild(control, getNextJavaInfo());
				}

				@Override
				public void move() throws Exception {
					expandBar.moveChild(control, getNextJavaInfo());
				}

				@Override
				public StatementTarget getTarget() throws Exception {
					return target;
				}

				private JavaInfo getNextJavaInfo() {
					return GenericsUtils.getNextOrNull(expandBar.getChildrenJava(), ExpandItemInfo.this);
				}
			};
			JavaInfoUtils.moveProvider(control, null, expandBar, targetProvider);
		}
		// associate with this item
		addInvocation_setControl(control);
	}

	/**
	 * Ensure that there is {@link ExpandItem#setHeight(int)} invocation.
	 */
	private void addInvocation_setHeight() throws Exception {
		if (getMethodInvocation("setHeight(int)") == null) {
			String argument =
					TemplateUtils.format(
							"{0}.getControl().computeSize({1}, {1}).y",
							this,
							"org.eclipse.swt.SWT.DEFAULT");
			addMethodInvocation("setHeight(int)", argument);
		}
	}

	/**
	 * Adds {@link ExpandItem#setControl(org.eclipse.swt.widgets.Control)} invocation.
	 */
	private void addInvocation_setControl(ControlInfo control) throws Exception {
		String source = TemplateUtils.format("{0}.setControl({1})", this, control);
		Expression expression = control.addExpressionStatement(source);
		addRelatedNodes(expression);
	}
}
