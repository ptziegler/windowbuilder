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
package org.eclipse.wb.internal.rcp.model.forms.layout.table;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.StaticFieldPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.ModelMessages;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.actions.SetAlignmentAction;
import org.eclipse.wb.internal.rcp.model.forms.layout.table.actions.SetGrabAction;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.forms.widgets.TableWrapData;

import java.util.List;

/**
 * Model for {@link TableWrapData}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class TableWrapDataInfo extends LayoutDataInfo implements ITableWrapDataInfo {
	private boolean m_initialized;
	int x = -1;
	int y = -1;
	int width = 1;
	int height = 1;
	boolean horizontalGrab;
	boolean verticalGrab;
	int horizontalAlignment;
	int verticalAlignment;
	int heightHint;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TableWrapDataInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Initialize
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initialize() throws Exception {
		super.initialize();
		// events
		addBroadcastListener(new ObjectEventListener() {
			@Override
			public void addContextMenu(List<? extends ObjectInfo> objects,
					ObjectInfo object,
					IMenuManager manager) throws Exception {
				if (getParent() == object) {
					TableWrapDataInfo.this.addContextMenu(manager);
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		super.refresh_fetch();
		// prepare x/y
		getCurrentObjectCell(getLayout(), getControl());
		// prepare values from TableWrapData
		getCurrentObjectFields();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Initializes fields of this {@link TableWrapDataInfo} using object of given
	 * {@link TableWrapLayoutInfo} and {@link ControlInfo}.
	 */
	void initialize(TableWrapLayoutInfo layout, ControlInfo controlInfo) throws Exception {
		if (!m_initialized) {
			m_initialized = true;
			// prepare x/y
			getCurrentObjectCell(layout, controlInfo);
			// prepare values from TableWrapData
			getCurrentObjectFields();
		}
	}

	/**
	 * Gets cell using objects of given {@link TableWrapLayoutInfo} and {@link ControlInfo}.
	 */
	private void getCurrentObjectCell(TableWrapLayoutInfo layout, ControlInfo controlInfo)
			throws Exception {
		if (layout.getObject() != null && controlInfo.getObject() != null) {
			Point xyPoint = TableWrapLayoutSupport.getXY(layout.getObject(), controlInfo.getObject());
			if (xyPoint != null) {
				x = xyPoint.x;
				y = xyPoint.y;
			}
		}
	}

	/**
	 * Gets values from {@link TableWrapData} object to this {@link TableWrapDataInfo} fields.
	 */
	private void getCurrentObjectFields() throws Exception {
		Object object = getObject();
		width = ReflectionUtils.getFieldInt(object, "colspan");
		height = ReflectionUtils.getFieldInt(object, "rowspan");
		horizontalGrab = ReflectionUtils.getFieldBoolean(object, "grabHorizontal");
		verticalGrab = ReflectionUtils.getFieldBoolean(object, "grabVertical");
		horizontalAlignment = ReflectionUtils.getFieldInt(object, "align");
		verticalAlignment = ReflectionUtils.getFieldInt(object, "valign");
		heightHint = ReflectionUtils.getFieldInt(object, "heightHint");
//		TODO
//		https://github.com/eclipse-windowbuilder/windowbuilder/issues/963
//		TableWrapData object = getObject();
//		width = object.colspan;
//		height = object.rowspan;
//		horizontalGrab = object.grabHorizontal;
//		verticalGrab = object.grabVertical;
//		horizontalAlignment = object.align;
//		verticalAlignment = object.valign;
//		heightHint = object.heightHint;
	}

//	TODO
//	https://github.com/eclipse-windowbuilder/windowbuilder/issues/963
//	@Override
//	public TableWrapData getObject() {
//		return (TableWrapData) super.getObject();
//	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal access
	//
	////////////////////////////////////////////////////////////////////////////
	private ControlInfo getControl() {
		return (ControlInfo) getParent();
	}

	private TableWrapLayoutInfo getLayout() {
		CompositeInfo composite = (CompositeInfo) getControl().getParent();
		if (composite == null) {
			return null;
		}
		return (TableWrapLayoutInfo) composite.getLayout();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Location
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int getX() {
		return x;
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Span
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int getHorizontalSpan() {
		return width;
	}

	@Override
	public void setHorizontalSpan(int width) throws Exception {
		if (this.width != width) {
			this.width = width;
			getPropertyByTitle("colspan").setValue(width);
		}
	}

	@Override
	public int getVerticalSpan() {
		return height;
	}

	@Override
	public void setVerticalSpan(int height) throws Exception {
		if (this.height != height) {
			this.height = height;
			getPropertyByTitle("rowspan").setValue(height);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Grab
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean getHorizontalGrab() {
		return horizontalGrab;
	}

	@Override
	public void setHorizontalGrab(boolean grab) throws Exception {
		if (horizontalGrab != grab) {
			horizontalGrab = grab;
			materialize();
			// special case: convert constructor FILL into FILL_GRAB and vice-versa
			if (horizontalAlignment == TableWrapData.FILL && constructor_hasHorizontalAlignment()) {
				if (horizontalGrab) {
					constructor_setEnumProperty("align", TableWrapData.FILL_GRAB);
				} else {
					constructor_setEnumProperty("align", TableWrapData.FILL);
				}
				// don't continue
				return;
			}
			// just update property
			getPropertyByTitle("grabHorizontal").setValue(grab);
		}
	}

	@Override
	public boolean getVerticalGrab() {
		return verticalGrab;
	}

	@Override
	public void setVerticalGrab(boolean grab) throws Exception {
		if (verticalGrab != grab) {
			verticalGrab = grab;
			materialize();
			// special case: convert constructor FILL into FILL_GRAB and vice-versa
			if (verticalAlignment == TableWrapData.FILL && constructor_hasVerticalAlignment()) {
				if (verticalGrab) {
					constructor_setEnumProperty("valign", TableWrapData.FILL_GRAB);
				} else {
					constructor_setEnumProperty("valign", TableWrapData.FILL);
				}
				// don't continue
				return;
			}
			// just update property
			getPropertyByTitle("grabVertical").setValue(grab);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Alignment
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int getHorizontalAlignment() {
		return horizontalAlignment;
	}

	@Override
	public void setHorizontalAlignment(int alignment) throws Exception {
		if (horizontalAlignment != alignment) {
			materialize();
			// special case: convert constructor FILL_GRAB into simple grab
			if (constructor_hasHorizontalAlignment()
					&& horizontalAlignment == TableWrapData.FILL
					&& horizontalGrab) {
				setFieldSource("grabHorizontal", "true");
			}
			// set new alignment
			horizontalAlignment = alignment;
			if (constructor_hasHorizontalAlignment()) {
				// special case: convert constructor FILL into FILL_GRAB
				if (horizontalAlignment == TableWrapData.FILL && horizontalGrab) {
					constructor_setEnumProperty("align", TableWrapData.FILL_GRAB);
					getPropertyByTitle("grabHorizontal").setValue(false);
					return;
				}
				// just update constructor property
				constructor_setEnumProperty("align", alignment);
			} else {
				// just update property
				setEnumProperty("align", alignment);
			}
		}
	}

	@Override
	public int getVerticalAlignment() {
		return verticalAlignment;
	}

	@Override
	public void setVerticalAlignment(int alignment) throws Exception {
		if (verticalAlignment != alignment) {
			materialize();
			// special case: convert constructor FILL_GRAB into simple grab
			if (constructor_hasVerticalAlignment()
					&& verticalAlignment == TableWrapData.FILL
					&& verticalGrab) {
				setFieldSource("grabVertical", "true");
			}
			// set new alignment
			verticalAlignment = alignment;
			if (constructor_hasVerticalAlignment()) {
				// special case: convert constructor FILL into FILL_GRAB
				if (verticalAlignment == TableWrapData.FILL && verticalGrab) {
					constructor_setEnumProperty("valign", TableWrapData.FILL_GRAB);
					getPropertyByTitle("grabVertical").setValue(false);
					return;
				}
				// just update constructor property
				constructor_setEnumProperty("valign", alignment);
			} else {
				setEnumProperty("valign", alignment);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Hint
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return value of <code>heightHint</code> property.
	 */
	public int getHeightHint() {
		return heightHint;
	}

	/**
	 * Sets value of <code>heightHint</code> property.
	 */
	public void setHeightHint(int heightHint) throws Exception {
		if (this.heightHint != heightHint) {
			this.heightHint = heightHint;
			getPropertyByTitle("heightHint").setValue(heightHint);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * We need way to "force" assignment to grabHorizontal/grabVertical fields, when we know that its
	 * default value will prevent from doing this just by using {@link Property#setValue(Object)}.
	 */
	private void setFieldSource(String fieldName, String source) throws Exception {
		removeFieldAssignments(fieldName);
		addFieldAssignment(fieldName, source);
	}

	/**
	 * Sets value for {@link Property} with {@link StaticFieldPropertyEditor}.
	 */
	private void setEnumProperty(String propertyTitle, int value) throws Exception {
		Property property = getPropertyByTitle(propertyTitle);
		setEnumProperty(property, value);
	}

	/**
	 * Sets value for {@link Property} with {@link StaticFieldPropertyEditor}.
	 */
	private static void setEnumProperty(Property property, int value) throws Exception {
		StaticFieldPropertyEditor propertyEditor = (StaticFieldPropertyEditor) property.getEditor();
		propertyEditor.setValue(property, value);
	}

	/**
	 * @return <code>true</code> if constructor used to create {@link TableWrapData}, has argument
	 *         with horizontal alignment (as 0-th argument).
	 */
	private boolean constructor_hasHorizontalAlignment() {
		return constructor_getNumberArguments() >= 1;
	}

	/**
	 * @return <code>true</code> if constructor used to create {@link TableWrapData}, has argument
	 *         with vertical alignment (as 1-th argument).
	 */
	private boolean constructor_hasVerticalAlignment() {
		return constructor_getNumberArguments() >= 2;
	}

	/**
	 * Sets the "enum" value for {@link Property} of {@link ConstructorCreationSupport}.
	 */
	private void constructor_setEnumProperty(String propertyTitle, int value) throws Exception {
		Property constructorProperty = getPropertyByTitle("Constructor");
		if (constructorProperty != null) {
			IComplexPropertyEditor complexEditor =
					(IComplexPropertyEditor) constructorProperty.getEditor();
			for (Property property : complexEditor.getProperties(constructorProperty)) {
				if (property.getTitle().equals(propertyTitle)) {
					setEnumProperty(property, value);
				}
			}
		}
	}

	/**
	 * @return the number of constructor arguments used to create {@link TableWrapData} in source.
	 */
	private int constructor_getNumberArguments() {
		ConstructorCreationSupport creationSupport = (ConstructorCreationSupport) getCreationSupport();
		return creationSupport.getBinding().getParameterTypes().length;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Images
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getSmallAlignmentImageDescriptor(boolean horizontal) {
		if (horizontal) {
			switch (horizontalAlignment) {
			case TableWrapData.LEFT :
				return CoreImages.ALIGNMENT_H_SMALL_LEFT;
			case TableWrapData.CENTER :
				return CoreImages.ALIGNMENT_H_SMALL_CENTER;
			case TableWrapData.RIGHT :
				return CoreImages.ALIGNMENT_H_SMALL_RIGHT;
			default :
				Assert.isTrue(horizontalAlignment == TableWrapData.FILL);
				return CoreImages.ALIGNMENT_H_SMALL_FILL;
			}
		} else {
			switch (verticalAlignment) {
			case TableWrapData.TOP :
				return CoreImages.ALIGNMENT_V_SMALL_TOP;
			case TableWrapData.MIDDLE :
				return CoreImages.ALIGNMENT_V_SMALL_CENTER;
			case TableWrapData.BOTTOM :
				return CoreImages.ALIGNMENT_V_SMALL_BOTTOM;
			default :
				Assert.isTrue(verticalAlignment == TableWrapData.FILL);
				return CoreImages.ALIGNMENT_V_SMALL_FILL;
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds items to the context {@link IMenuManager}.
	 */
	public void addContextMenu(IMenuManager manager) {
		// horizontal
		{
			IMenuManager manager2 =
					new MenuManager(ModelMessages.TableWrapDataInfo_managerHorizontalAlignment);
			manager.appendToGroup(IContextMenuConstants.GROUP_TOP, manager2);
			//
			manager2.add(new SetGrabAction(this, ModelMessages.TableWrapDataInfo_haGrab,
					CoreImages.ALIGNMENT_H_MENU_GROW, true));
			manager2.add(new Separator());
			//
			fillHorizontalAlignmentMenu(manager2);
		}
		// vertical
		{
			IMenuManager manager2 =
					new MenuManager(ModelMessages.TableWrapDataInfo_managerVerticalAlignment);
			manager.appendToGroup(IContextMenuConstants.GROUP_TOP, manager2);
			//
			manager2.add(new SetGrabAction(this,
					ModelMessages.TableWrapDataInfo_vaGrab,
					CoreImages.ALIGNMENT_V_MENU_GROW,
					false));
			manager2.add(new Separator());
			//
			fillVerticalAlignmentMenu(manager2);
		}
	}

	@Override
	public void fillHorizontalAlignmentMenu(IMenuManager manager) {
		manager.add(new SetAlignmentAction(this,
				ModelMessages.TableWrapDataInfo_haLeft,
				CoreImages.ALIGNMENT_H_MENU_LEFT,
				true,
				TableWrapData.LEFT));
		manager.add(new SetAlignmentAction(this,
				ModelMessages.TableWrapDataInfo_haCenter,
				CoreImages.ALIGNMENT_H_MENU_CENTER,
				true,
				TableWrapData.CENTER));
		manager.add(new SetAlignmentAction(this,
				ModelMessages.TableWrapDataInfo_haRight,
				CoreImages.ALIGNMENT_H_MENU_RIGHT,
				true,
				TableWrapData.RIGHT));
		manager.add(new SetAlignmentAction(this,
				ModelMessages.TableWrapDataInfo_haFill,
				CoreImages.ALIGNMENT_H_MENU_FILL,
				true,
				TableWrapData.FILL));
	}

	@Override
	public void fillVerticalAlignmentMenu(IMenuManager manager) {
		manager.add(new SetAlignmentAction(this,
				ModelMessages.TableWrapDataInfo_vaTop,
				CoreImages.ALIGNMENT_V_MENU_TOP,
				false,
				TableWrapData.TOP));
		manager.add(new SetAlignmentAction(this,
				ModelMessages.TableWrapDataInfo_vaMiddle,
				CoreImages.ALIGNMENT_V_MENU_CENTER,
				false,
				TableWrapData.MIDDLE));
		manager.add(new SetAlignmentAction(this,
				ModelMessages.TableWrapDataInfo_vaBottom,
				CoreImages.ALIGNMENT_V_MENU_BOTTOM,
				false,
				TableWrapData.BOTTOM));
		manager.add(new SetAlignmentAction(this,
				ModelMessages.TableWrapDataInfo_vaGill,
				CoreImages.ALIGNMENT_V_MENU_FILL,
				false,
				TableWrapData.FILL));
	}
}