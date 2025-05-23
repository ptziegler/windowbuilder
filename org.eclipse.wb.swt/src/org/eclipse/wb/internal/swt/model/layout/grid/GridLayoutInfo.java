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
package org.eclipse.wb.internal.swt.model.layout.grid;

import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildGraphical;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildTree;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData;
import org.eclipse.wb.internal.core.model.util.grid.GridAlignmentHelper;
import org.eclipse.wb.internal.core.model.util.grid.GridAlignmentHelper.IAlignmentProcessor;
import org.eclipse.wb.internal.core.model.variable.EmptyPureVariableSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.model.layout.LayoutClipboardCommand;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataClipboardCommand;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.grid.actions.SelectionActionsSupport;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;
import org.eclipse.wb.internal.swt.support.GridLayoutSupport;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.bidimap.UnmodifiableBidiMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Model for SWT {@link GridLayout}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public class GridLayoutInfo extends LayoutInfo
implements
IPreferenceConstants,
IGridLayoutInfo<ControlInfo> {
	private boolean m_useObjectColumnCount;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridLayoutInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		new GridLayoutAssistant(this);
		new SelectionActionsSupport<>(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void onSet() throws Exception {
		super.onSet();
		GridLayoutConverter.convert(getComposite(), this);
	}

	@Override
	protected void onDelete() throws Exception {
		// delete filler's
		for (ControlInfo control : getControls()) {
			if (isFiller(control)) {
				control.delete();
			}
		}
		// delete other
		super.onDelete();
	}

	@Override
	protected void initialize() throws Exception {
		super.initialize();
		// add listeners
		addBroadcastListener(new ObjectInfoChildTree() {
			@Override
			public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
				if (object instanceof ControlInfo) {
					visible[0] &= !isFiller((ControlInfo) object);
				}
			}
		});
		addBroadcastListener(new ObjectInfoChildGraphical() {
			@Override
			public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
				if (object instanceof ControlInfo) {
					visible[0] &= !isFiller((ControlInfo) object);
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isManagedObject(Object object) {
		if (!super.isManagedObject(object)) {
			return false;
		}
		if (object instanceof ControlInfo control) {
			if (getGridData(control).getExclude()) {
				return false;
			}
		}
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	private final Set<Control> m_controlsImplicit = new HashSet<>();

	@Override
	public void refresh_dispose() throws Exception {
		m_gridInfo = null;
		super.refresh_dispose();
	}

	@Override
	protected void refresh_afterCreate() throws Exception {
		super.refresh_afterCreate();
		prepareImplicitControls();
		fixEmptyColumns();
	}

	/**
	 * Prepares {@link #m_controlsImplicit} - {@link Set} of {@link Control} which exist on
	 * {@link Composite}, but we don't have models for them. Usually these controls are implicit ones,
	 * created in super constructor.
	 */
	private void prepareImplicitControls() {
		m_controlsImplicit.clear();
		// with model
		Set<Control> controlsWithModel = new HashSet<>();
		for (ControlInfo control : getControls()) {
			controlsWithModel.add(control.getWidget());
		}
		// process all and remember Control-s without model
		Composite compositeObject = getComposite().getWidget();
		Control[] controlsAll = compositeObject.getChildren();
		for (Control control : controlsAll) {
			if (!controlsWithModel.contains(control)) {
				m_controlsImplicit.add(control);
			}
		}
	}

	/**
	 * Ensures that empty columns (only with fillers) have reasonable width.
	 */
	private void fixEmptyColumns() throws Exception {
		fetchGridDataValues();
		ControlInfo[][] grid = getControlsGrid();
		int columnCount = grid.length != 0 ? grid[0].length : 0;
		m_columnWidths = GridLayoutSupport.getColumnWidths(getObject());
		// set empty text for fillers in empty column
		for (int column = 0; column < columnCount; column++) {
			int width = m_columnWidths[column];
			if (width == 0) {
				for (int row = 0; row < grid.length; row++) {
					ControlInfo control = grid[row][column];
					if (control != null && isFiller(control)) {
						((Label) control.getObject()).setText("      ");
					}
				}
			}
		}
	}

	/**
	 * When {@link GridLayout} was already created and rendered, we can fetch its location/span
	 * information. We need this, because we need actual grid information when check for empty
	 * columns.
	 */
	private void fetchGridDataValues() throws Exception {
		for (ControlInfo control : getControls()) {
			GridDataInfo gridData = getGridData(control);
			gridData.refresh_fetch();
		}
	}

	@Override
	protected void refresh_fetch() throws Exception {
		super.refresh_fetch();
		Object layoutObject = getObject();
		// prepare dimensions information
		m_columnOrigins = GridLayoutSupport.getColumnOrigins(layoutObject);
		m_rowOrigins = GridLayoutSupport.getRowOrigins(layoutObject);
		m_columnWidths = GridLayoutSupport.getColumnWidths(layoutObject);
		m_rowHeights = GridLayoutSupport.getRowHeights(layoutObject);
	}

	@Override
	protected void refresh_finish() throws Exception {
		super.refresh_finish();
		// fix grid
		try {
			m_useObjectColumnCount = true;
			fixGrid();
		} finally {
			m_useObjectColumnCount = false;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Components/constraints
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean m_replaceWithFillers = true;
	private boolean m_removeEmptyColumnsRows = true;
	private GridDataInfo m_removingGridData;

	/**
	 * Specifies if empty columns/rows should be removed when {@link ControlInfo} is removed (deleted
	 * or moved). We need to disable this for example when we want to surround controls with some
	 * container, so that it is placed in same cells.
	 */
	void setRemoveEmptyColumnsRows(boolean removeEmptyColumnsRows) {
		m_removeEmptyColumnsRows = removeEmptyColumnsRows;
	}

	@Override
	protected Object getDefaultVirtualDataObject() throws Exception {
		return GridLayoutSupport.createGridData();
	}

	@Override
	protected void onControlRemoveBefore(ControlInfo control) throws Exception {
		// remember GridDataInfo for using later in "remove after"
		m_removingGridData = getGridData(control);
		// continue
		super.onControlRemoveBefore(control);
	}

	@Override
	protected void onControlRemoveAfter(ControlInfo control) throws Exception {
		// replace control with fillers
		if (m_replaceWithFillers && !isFiller(control)) {
			// replace with fillers
			{
				GridDataInfo gridData = m_removingGridData;
				for (int x = gridData.x; x < gridData.x + gridData.width; x++) {
					for (int y = gridData.y; y < gridData.y + gridData.height; y++) {
						addFiller(x, y);
					}
				}
			}
			// delete empty columns/rows
			if (m_removeEmptyColumnsRows) {
				deleteEmptyColumnsRows(m_removingGridData);
			}
			m_removingGridData = null;
		}
		// continue
		super.onControlRemoveAfter(control);
	}

	@Override
	public IGridDataInfo getGridData2(ControlInfo control) {
		return getGridData(control);
	}

	/**
	 * Static version of {@link #getGridData2(IControlInfo)}.
	 */
	public static GridDataInfo getGridData(ControlInfo control) {
		return (GridDataInfo) getLayoutData(control);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dimensions operations
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Removes empty (only with fillers) columns/rows.
	 */
	void deleteEmptyColumnsRows(GridDataInfo removingData) throws Exception {
		deleteEmptyColumns(removingData);
		deleteEmptyRows(removingData);
	}

	/**
	 * Deletes empty (only with fillers) columns.
	 */
	private void deleteEmptyColumns(GridDataInfo removingData) throws Exception {
		ControlInfo[][] grid = getControlsGrid();
		// check columns
		boolean deleteOnlyIfIsRemovingColumn = false;
		for (int column = grid[0].length - 1; column >= 0; column--) {
			boolean isRemovingColumn = removingData != null
					&& removingData.x <= column
					&& column < removingData.x + removingData.width;
			// check if empty
			boolean isEmpty = true;
			for (int row = 0; row < grid.length; row++) {
				ControlInfo control = grid[row][column];
				isEmpty &= isFiller(control);
			}
			// delete if empty
			if (isEmpty && (!deleteOnlyIfIsRemovingColumn || isRemovingColumn)) {
				command_deleteColumn(column, false);
			} else {
				deleteOnlyIfIsRemovingColumn = true;
			}
		}
	}

	/**
	 * Deletes empty (only with fillers) rows.
	 */
	private void deleteEmptyRows(GridDataInfo removingData) throws Exception {
		ControlInfo[][] grid = getControlsGrid();
		boolean deleteOnlyIfIsRemovingRow = false;
		for (int row = grid.length - 1; row >= 0; row--) {
			boolean isRemovingRow = removingData != null
					&& removingData.y <= row
					&& row < removingData.y + removingData.height;
			// check if empty
			boolean isEmpty = true;
			for (int column = 0; column < grid[row].length; column++) {
				ControlInfo control = grid[row][column];
				isEmpty &= isFiller(control);
			}
			// delete if empty
			if (isEmpty && (!deleteOnlyIfIsRemovingRow || isRemovingRow)) {
				command_deleteRow(row, false);
			} else {
				deleteOnlyIfIsRemovingRow = true;
			}
		}
	}

	@Override
	public void command_deleteColumn(int column, boolean deleteEmptyDims) throws Exception {
		int columnCount = getControlsGridSize().width;
		// update GridData, delete controls
		m_replaceWithFillers = false;
		try {
			for (ControlInfo control : getControls()) {
				GridDataInfo gridData = getGridData(control);
				//
				if (gridData.x == column) {
					control.delete();
				} else if (gridData.x > column) {
					gridData.x--;
				} else if (gridData.x + gridData.width > column) {
					gridData.setHorizontalSpan(gridData.width - 1);
				}
			}
		} finally {
			m_replaceWithFillers = true;
		}
		// update count
		if (columnCount >= 2) {
			getPropertyByTitle("numColumns").setValue(columnCount - 1);
		}
		// it is possible, that we have now empty rows, so delete them too
		if (deleteEmptyDims) {
			deleteEmptyColumnsRows(null);
		}
	}

	@Override
	public void command_deleteRow(int row, boolean deleteEmptyDims) throws Exception {
		// update GridData, delete controls
		m_replaceWithFillers = false;
		try {
			for (ControlInfo control : getControls()) {
				GridDataInfo gridData = getGridData(control);
				//
				if (gridData.y == row) {
					control.delete();
				} else if (gridData.y > row) {
					gridData.y--;
				} else if (gridData.y + gridData.height > row) {
					gridData.setVerticalSpan(gridData.height - 1);
				}
			}
		} finally {
			m_replaceWithFillers = true;
		}
		// it is possible, that we have now empty columns, so delete them too
		if (deleteEmptyDims) {
			deleteEmptyColumnsRows(null);
		}
	}

	@Override
	public void command_MOVE_COLUMN(int fromIndex, int toIndex) throws Exception {
		// move column in columns list
		{
			getColumns(); // kick to initialize columns
			GridColumnInfo<ControlInfo> column = m_columns.remove(fromIndex);
			if (fromIndex < toIndex) {
				m_columns.add(toIndex - 1, column);
			} else {
				m_columns.add(toIndex, column);
			}
			// update "index"
			for (int i = 0; i < m_columns.size(); i++) {
				m_columns.get(i).setIndex(i);
			}
		}
		// prepare new column
		prepareCell(toIndex, true, -1, false);
		if (toIndex < fromIndex) {
			fromIndex++;
		}
		// move children
		for (ControlInfo control : getControls()) {
			if (!isFiller(control)) {
				GridDataInfo gridData = getGridData(control);
				if (gridData.x == fromIndex) {
					command_setCells(control, new Rectangle(toIndex, gridData.y, 1, gridData.height), true);
				}
			}
		}
		// delete old column
		command_deleteColumn(fromIndex, false);
		deleteEmptyColumnsRows(null);
	}

	@Override
	public void command_MOVE_ROW(int fromIndex, int toIndex) throws Exception {
		// move row in rows list
		{
			getRows(); // kick to initialize rows
			GridRowInfo<ControlInfo> row = m_rows.remove(fromIndex);
			if (fromIndex < toIndex) {
				m_rows.add(toIndex - 1, row);
			} else {
				m_rows.add(toIndex, row);
			}
			// update "index"
			for (int i = 0; i < m_rows.size(); i++) {
				m_rows.get(i).setIndex(i);
			}
		}
		// prepare new row
		prepareCell(-1, false, toIndex, true);
		if (toIndex < fromIndex) {
			fromIndex++;
		}
		// move children
		for (ControlInfo control : getControls()) {
			if (!isFiller(control)) {
				GridDataInfo gridData = getGridData(control);
				if (gridData.y == fromIndex) {
					command_setCells(control, new Rectangle(gridData.x, toIndex, gridData.width, 1), true);
				}
			}
		}
		// delete old row
		command_deleteRow(fromIndex, false);
		deleteEmptyColumnsRows(null);
	}

	@Override
	public void command_normalizeSpanning() throws Exception {
		ExecutionUtils.run(this, new RunnableEx() {
			@Override
			public void run() throws Exception {
				command_normalizeSpanning0();
			}
		});
	}

	private void command_normalizeSpanning0() throws Exception {
		Dimension gridSize = getControlsGridSize();
		boolean[] filledColumns = new boolean[gridSize.width];
		boolean[] filledRows = new boolean[gridSize.height];
		for (ControlInfo control : getControls()) {
			if (!isFiller(control)) {
				GridDataInfo gridData = getGridData(control);
				filledColumns[gridData.x] = true;
				filledRows[gridData.y] = true;
			}
		}
		// remove empty columns
		for (int column = filledColumns.length - 1; column >= 0; column--) {
			if (!filledColumns[column]) {
				command_deleteColumn(column, false);
			}
		}
		// remove empty rows
		for (int row = filledRows.length - 1; row >= 0; row--) {
			if (!filledRows[row]) {
				command_deleteRow(row, false);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dimensions access
	//
	////////////////////////////////////////////////////////////////////////////
	private final List<GridColumnInfo<ControlInfo>> m_columns = new ArrayList<>();
	private final List<GridRowInfo<ControlInfo>> m_rows = new ArrayList<>();

	/**
	 * @return the "numColumns" property value.
	 */
	int getNumColumns() {
		if (getObject() == null) {
			return 1;
		} else {
			return GridLayoutSupport.getNumColumns(getObject());
		}
	}

	@Override
	public List<GridColumnInfo<ControlInfo>> getColumns() {
		Dimension size = getControlsGridSize();
		if (m_columns.size() != size.width) {
			m_columns.clear();
			for (int i = 0; i < size.width; i++) {
				GridColumnInfo<ControlInfo> column = new GridColumnInfo<>(this);
				column.setIndex(i);
				m_columns.add(column);
			}
		}
		return m_columns;
	}

	@Override
	public List<GridRowInfo<ControlInfo>> getRows() {
		Dimension size = getControlsGridSize();
		if (m_rows.size() != size.height) {
			m_rows.clear();
			for (int i = 0; i < size.height; i++) {
				GridRowInfo<ControlInfo> row = new GridRowInfo<>(this);
				row.setIndex(i);
				m_rows.add(row);
			}
		}
		return m_rows;
	}

	@Override
	public boolean canChangeDimensions() {
		if (getCreationSupport() instanceof ConstructorCreationSupport) {
			return true;
		}
		if (m_controlsImplicit.isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isExplicitRow(int row) {
		return row >= getImplicitGridSize().height;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void command_CREATE(ControlInfo newControl,
			int column,
			boolean insertColumn,
			int row,
			boolean insertRow) throws Exception {
		startEdit();
		try {
			command_CREATE(newControl, null);
			// move to required cell
			prepareCell(column, insertColumn, row, insertRow);
			command_setCells(newControl, new Rectangle(column, row, 1, 1), false);
			// perform automatic alignment
			doAutomaticAlignment(newControl);
		} finally {
			endEdit();
		}
	}

	@Override
	public void command_MOVE(ControlInfo control,
			int column,
			boolean insertColumn,
			int row,
			boolean insertRow) throws Exception {
		startEdit();
		try {
			prepareCell(column, insertColumn, row, insertRow);
			command_setCells(control, new Rectangle(column, row, 1, 1), true);
			deleteEmptyColumnsRows(null);
		} finally {
			endEdit();
		}
	}

	@Override
	public void command_ADD(ControlInfo control,
			int column,
			boolean insertColumn,
			int row,
			boolean insertRow) throws Exception {
		startEdit();
		try {
			command_MOVE(control, null);
			// move to required cell
			prepareCell(column, insertColumn, row, insertRow);
			command_setCells(control, new Rectangle(column, row, 1, 1), false);
		} finally {
			endEdit();
		}
	}

	/**
	 * Prepares cell with given column/row - inserts/appends columns/rows if necessary.
	 */
	void prepareCell(int column, boolean insertColumn, int row, boolean insertRow) throws Exception {
		// prepare count of columns/rows
		int columnCount;
		int rowCount;
		{
			Dimension gridSize = getControlsGridSize();
			columnCount = gridSize.width;
			rowCount = gridSize.height;
		}
		// append
		{
			int newColumnCount = Math.max(columnCount, 1 + column);
			int newRowCount = Math.max(rowCount, 1 + row);
			// append rows
			for (int newRow = rowCount; newRow <= row; newRow++) {
				for (int columnIndex = 0; columnIndex < newColumnCount; columnIndex++) {
					addFiller(columnIndex, newRow);
				}
			}
			// append columns
			getPropertyByTitle("numColumns").setValue(newColumnCount);
			for (int newColumn = columnCount; newColumn <= column; newColumn++) {
				for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
					addFiller(newColumn, rowIndex);
				}
			}
			// set new count of columns/rows
			columnCount = newColumnCount;
			rowCount = newRowCount;
		}
		// insert row
		if (insertRow) {
			rowCount++;
			// update GridData for all controls
			boolean[] columnsToIgnore = new boolean[columnCount];
			for (ControlInfo control : getControls()) {
				GridDataInfo gridData = getGridData(control);
				//
				if (gridData.y >= row) {
					gridData.y++;
				} else if (gridData.y + gridData.height > row) {
					gridData.setVerticalSpan(gridData.height + 1);
					for (int i = gridData.x; i < gridData.x + gridData.width; i++) {
						columnsToIgnore[i] = true;
					}
				}
			}
			// add fillers for new row
			for (int i = 0; i < columnCount; i++) {
				if (!columnsToIgnore[i]) {
					addFiller(i, row);
				}
			}
		}
		// insert column
		if (insertColumn) {
			// update GridData for all controls
			boolean[] rowsToIgnore = new boolean[rowCount];
			for (ControlInfo control : getControls()) {
				GridDataInfo gridData = getGridData(control);
				//
				if (gridData.x >= column) {
					gridData.x++;
				} else if (gridData.x + gridData.width > column) {
					gridData.setHorizontalSpan(gridData.width + 1);
					for (int i = gridData.y; i < gridData.y + gridData.height; i++) {
						rowsToIgnore[i] = true;
					}
				}
			}
			// insert fillers for new column
			getPropertyByTitle("numColumns").setValue(columnCount + 1);
			for (int i = 0; i < rowCount; i++) {
				if (!rowsToIgnore[i]) {
					addFiller(column, i);
				}
			}
		}
	}

	@Override
	public void command_setCells(ControlInfo control, Rectangle cells, boolean forMove)
			throws Exception {
		GridDataInfo gridData = getGridData(control);
		// prepare grid
		ControlInfo[][] grid = getControlsGrid();
		Set<Point> cellsToAddFillers = new HashSet<>();
		Set<Point> cellsToRemoveFillers = new HashSet<>();
		// replace control with fillers
		if (forMove) {
			for (int x = gridData.x; x < gridData.x + gridData.width; x++) {
				for (int y = gridData.y; y < gridData.y + gridData.height; y++) {
					Point cell = new Point(x, y);
					cellsToAddFillers.add(cell);
				}
			}
		}
		// remove fillers from occupied cells
		for (int x = cells.x; x < cells.right(); x++) {
			for (int y = cells.y; y < cells.bottom(); y++) {
				Point cell = new Point(x, y);
				cellsToAddFillers.remove(cell);
				if (isFiller(grid[y][x])) {
					cellsToRemoveFillers.add(cell);
				}
			}
		}
		// do edit operations
		startEdit();
		try {
			// move
			if (gridData.x != cells.x || gridData.y != cells.y) {
				// update GridData
				{
					gridData.x = cells.x;
					gridData.y = cells.y;
				}
				// move model
				{
					ControlInfo reference = getReferenceControl(cells.y, cells.x, control);
					command_MOVE(control, reference);
				}
			}
			// set span
			{
				gridData.setHorizontalSpan(cells.width);
				gridData.setVerticalSpan(cells.height);
			}
			// remove fillers
			for (Point cell : cellsToRemoveFillers) {
				ControlInfo filler = grid[cell.y][cell.x];
				filler.delete();
			}
			// add fillers
			for (Point cell : cellsToAddFillers) {
				addFiller(cell.x, cell.y);
			}
		} finally {
			endEdit();
		}
	}

	@Override
	public void command_setSizeHint(ControlInfo control, boolean horizontal, Dimension size)
			throws Exception {
		startEdit();
		try {
			GridDataInfo gridData = getGridData(control);
			if (horizontal) {
				gridData.setWidthHint(size.width);
			} else {
				gridData.setHeightHint(size.height);
			}
		} finally {
			endEdit();
		}
	}

	/**
	 * @return the {@link Point} with size of controls grid.
	 */
	private Dimension getControlsGridSize() {
		int columnCount = 0;
		int rowCount = 0;
		// when we just refreshed, we can use "numColumns" from GridLayout object
		if (m_useObjectColumnCount) {
			columnCount = getNumColumns();
		}
		// use implicit controls
		{
			Dimension implicitGridSize = getImplicitGridSize();
			columnCount = Math.max(columnCount, implicitGridSize.width);
			rowCount = implicitGridSize.height;
		}
		// use explicit controls
		for (ControlInfo control : getControls()) {
			GridDataInfo gridData = getGridData(control);
			columnCount = Math.max(columnCount, gridData.x + gridData.width);
			rowCount = Math.max(rowCount, gridData.y + gridData.height);
		}
		// OK, we have grid
		return new Dimension(columnCount, rowCount);
	}

	/**
	 * @return the size of {@link #m_controlsImplicit} grid.
	 */
	private Dimension getImplicitGridSize() {
		int columnCount = 0;
		int rowCount = 0;
		for (Control control : m_controlsImplicit) {
			Rectangle cells = getImplicitControlCells(control);
			columnCount = Math.max(columnCount, cells.right());
			rowCount = Math.max(rowCount, cells.bottom());
		}
		return new Dimension(columnCount, rowCount);
	}

	/**
	 * @return the cells of given {@link Control}.
	 */
	private Rectangle getImplicitControlCells(Control control) {
		Object layout = getObject();
		Point xy = GridLayoutSupport.getXY(layout, control);
		// may be excluded
		if (xy == null) {
			return new Rectangle();
		}
		// continue
		Dimension wh = GridLayoutSupport.getWH(layout, control);
		return new Rectangle(xy.x, xy.y, wh.width, wh.height);
	}

	/**
	 * @return the double array of {@link ControlInfo} where each element contains {@link ControlInfo}
	 *         that occupies this cell.
	 */
	private ControlInfo[][] getControlsGrid() throws Exception {
		Dimension gridSize = getControlsGridSize();
		// prepare empty grid
		ControlInfo[][] grid;
		{
			grid = new ControlInfo[gridSize.height][];
			for (int rowIndex = 0; rowIndex < grid.length; rowIndex++) {
				grid[rowIndex] = new ControlInfo[gridSize.width];
			}
		}
		// fill grid
		for (ControlInfo control : getControls()) {
			// prepare cells
			Rectangle cells;
			{
				GridDataInfo gridData = getGridData(control);
				cells = new Rectangle(gridData.x, gridData.y, gridData.width, gridData.height);
			}
			// fill grid cells
			for (int x = cells.x; x < cells.right(); x++) {
				for (int y = cells.y; y < cells.bottom(); y++) {
					// ignore newly added controls without real cell
					if (x != -1 && y != -1) {
						grid[y][x] = control;
					}
				}
			}
		}
		// OK, we have grid
		return grid;
	}

	/**
	 * "Fixes" grid, i.e. ensures that all cells are filled (at least with fillers), even if this is
	 * not strongly required by layout itself for final cells. We do this to avoid checks for
	 * <code>null</code> in many places.
	 */
	@Override
	public void fixGrid() throws Exception {
		if (JavaInfoUtils.isImplicitlyCreated(this)) {
			return;
		}
		ControlInfo[][] grid = getControlsGrid();
		for (int row = 0; row < grid.length; row++) {
			for (int column = 0; column < grid[row].length; column++) {
				if (isEmptyCell(grid, row, column)) {
					addFiller(column, row);
				}
			}
		}
	}

	private boolean isEmptyCell(ControlInfo[][] grid, int row, int column) {
		{
			Object layoutObject = getObject();
			for (Control control : m_controlsImplicit) {
				Point point = GridLayoutSupport.getXY(layoutObject, control);
				if (point != null && point.x == column && point.y == row) {
					return false;
				}
			}
		}
		return grid[row][column] == null;
	}

	/**
	 * @return the {@link ControlInfo} that should be used as reference of adding into specified cell.
	 *
	 * @param exclude
	 *          the {@link ControlInfo} that should not be checked, for example because we move it now
	 */
	private ControlInfo getReferenceControl(int row, int column, ControlInfo exclude)
			throws Exception {
		for (ControlInfo control : getControls()) {
			if (control != exclude) {
				GridDataInfo gridData = getGridData(control);
				if (gridData.y > row || gridData.y == row && gridData.x >= column) {
					return control;
				}
			}
		}
		// no reference
		return null;
	}

	/**
	 * Adds filler {@link ControlInfo} into given cell.
	 */
	private void addFiller(int column, int row) throws Exception {
		// prepare creation support
		ConstructorCreationSupport creationSupport = new ConstructorCreationSupport(null, false);
		// prepare filler
		ControlInfo filler = (ControlInfo) JavaInfoUtils.createJavaInfo(
				getEditor(),
				Label.class,
				creationSupport);
		// add filler
		ControlInfo reference = getReferenceControl(row, column, null);
		JavaInfoUtils.add(
				filler,
				new EmptyPureVariableSupport(filler),
				PureFlatStatementGenerator.INSTANCE,
				null,
				getComposite(),
				reference);
		// set x/y for new filler
		GridDataInfo gridData = getGridData(filler);
		gridData.x = column;
		gridData.y = row;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Automatic alignment
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Performs automatic alignment, such as grab/fill for Text/Table/etc, right alignment for Label.
	 */
	private void doAutomaticAlignment(ControlInfo control) throws Exception {
		final IPreferenceStore preferences = getDescription().getToolkit().getPreferences();
		GridAlignmentHelper.doAutomaticAlignment(control, new IAlignmentProcessor<ControlInfo>() {
			@Override
			public boolean grabEnabled() {
				return preferences.getBoolean(P_ENABLE_GRAB);
			}

			@Override
			public boolean rightEnabled() {
				return preferences.getBoolean(P_ENABLE_RIGHT_ALIGNMENT);
			}

			@Override
			public ControlInfo getComponentAtLeft(ControlInfo component) {
				GridDataInfo gridData = getGridData(component);
				return getControlAt(gridData.x - 1, gridData.y);
			}

			@Override
			public ControlInfo getComponentAtRight(ControlInfo component) {
				GridDataInfo gridData = getGridData(component);
				return getControlAt(gridData.x + 1, gridData.y);
			}

			@Override
			public void setGrabFill(ControlInfo component, boolean horizontal) throws Exception {
				GridDataInfo gridData = getGridData(component);
				if (horizontal) {
					gridData.setHorizontalGrab(true);
					gridData.setHorizontalAlignment(SWT.FILL);
				} else {
					gridData.setVerticalGrab(true);
					gridData.setVerticalAlignment(SWT.FILL);
				}
			}

			@Override
			public void setRightAlignment(ControlInfo component) throws Exception {
				GridDataInfo gridData = getGridData(component);
				gridData.setHorizontalAlignment(SWT.RIGHT);
			}
		});
	}

	/**
	 * @return the {@link ControlInfo} with given top-left cell, may be <code>null</code>.
	 */
	private ControlInfo getControlAt(int x, int y) {
		for (ControlInfo control : getControls()) {
			if (!isFiller(control)) {
				GridDataInfo gridData = getGridData(control);
				if (gridData.x == x && gridData.y == y) {
					return control;
				}
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IGridInfo support
	//
	////////////////////////////////////////////////////////////////////////////
	public static final int VIRTUAL_SIZE = 25;
	public static final int VIRTUAL_GAP = 5;
	private IGridInfo m_gridInfo;
	private int[] m_columnOrigins;
	private int[] m_rowOrigins;
	private int[] m_columnWidths;
	private int[] m_rowHeights;

	@Override
	public IGridInfo getGridInfo() {
		if (m_gridInfo == null) {
			ExecutionUtils.runRethrow(new RunnableEx() {
				@Override
				public void run() throws Exception {
					createGridInfo();
				}
			});
		}
		return m_gridInfo;
	}

	/**
	 * @return the origins of the columns.
	 */
	public int[] getColumnOrigins() {
		return m_columnOrigins;
	}

	/**
	 * @return the origins of the rows.
	 */
	public int[] getRowOrigins() {
		return m_rowOrigins;
	}

	/**
	 * Initializes {@link #m_gridInfo}.
	 */
	private void createGridInfo() throws Exception {
		// prepare intervals
		final Interval[] columnIntervals = getIntervals(m_columnOrigins, m_columnWidths);
		final Interval[] rowIntervals = getIntervals(m_rowOrigins, m_rowHeights);
		// prepare cells
		final Map<ControlInfo, Rectangle> componentToCells = new HashMap<>();
		final Map<Point, ControlInfo> occupiedCells = new HashMap<>();
		{
			for (ControlInfo control : getControls()) {
				// ignore filler
				if (isFiller(control)) {
					continue;
				}
				// prepare cells
				Rectangle cells;
				{
					GridDataInfo gridData = getGridData(control);
					cells = new Rectangle(gridData.x, gridData.y, gridData.width, gridData.height);
				}
				// fill map: ControlInfo -> cells Rectangle
				componentToCells.put(control, cells);
				// fill occupied cells map: cell -> ControlInfo
				fillOccupiedCells(occupiedCells, control, cells);
			}
			for (Control control : m_controlsImplicit) {
				Rectangle cells = getImplicitControlCells(control);
				fillOccupiedCells(occupiedCells, getComposite(), cells);
			}
		}
		// create IGridInfo instance
		m_gridInfo = new IGridInfo() {
			////////////////////////////////////////////////////////////////////////////
			//
			// Dimensions
			//
			////////////////////////////////////////////////////////////////////////////
			@Override
			public int getColumnCount() {
				return columnIntervals.length;
			}

			@Override
			public int getRowCount() {
				return rowIntervals.length;
			}

			////////////////////////////////////////////////////////////////////////////
			//
			// Intervals
			//
			////////////////////////////////////////////////////////////////////////////
			@Override
			public Interval[] getColumnIntervals() {
				return columnIntervals;
			}

			@Override
			public Interval[] getRowIntervals() {
				return rowIntervals;
			}

			////////////////////////////////////////////////////////////////////////////
			//
			// Cells
			//
			////////////////////////////////////////////////////////////////////////////
			@Override
			public Rectangle getComponentCells(IAbstractComponentInfo component) {
				Assert.instanceOf(ControlInfo.class, component);
				return componentToCells.get(component);
			}

			@Override
			public Rectangle getCellsRectangle(Rectangle cells) {
				int x = columnIntervals[cells.x].begin();
				int y = rowIntervals[cells.y].begin();
				int w = columnIntervals[cells.right() - 1].end() - x;
				int h = rowIntervals[cells.bottom() - 1].end() - y;
				return new Rectangle(x, y, w, h);
			}

			////////////////////////////////////////////////////////////////////////////
			//
			// Feedback
			//
			////////////////////////////////////////////////////////////////////////////
			@Override
			public boolean isRTL() {
				return getComposite().isRTL();
			}

			@Override
			public Insets getInsets() {
				return getComposite().getClientAreaInsets2();
			}

			////////////////////////////////////////////////////////////////////////////
			//
			// Virtual columns
			//
			////////////////////////////////////////////////////////////////////////////
			@Override
			public boolean hasVirtualColumns() {
				return true;
			}

			@Override
			public int getVirtualColumnSize() {
				return VIRTUAL_SIZE;
			}

			@Override
			public int getVirtualColumnGap() {
				return VIRTUAL_GAP;
			}

			////////////////////////////////////////////////////////////////////////////
			//
			// Virtual columns
			//
			////////////////////////////////////////////////////////////////////////////
			@Override
			public boolean hasVirtualRows() {
				return true;
			}

			@Override
			public int getVirtualRowSize() {
				return VIRTUAL_SIZE;
			}

			@Override
			public int getVirtualRowGap() {
				return VIRTUAL_GAP;
			}

			////////////////////////////////////////////////////////////////////////////
			//
			// Checks
			//
			////////////////////////////////////////////////////////////////////////////
			@Override
			public IAbstractComponentInfo getOccupied(int column, int row) {
				return occupiedCells.get(new Point(column, row));
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the array of {@link Interval}'s for given arrays of origins/sizes.
	 */
	private static Interval[] getIntervals(int[] origins, int[] sizes) {
		if (origins == null) {
			return new Interval[]{};
		}
		Interval[] intervals = new Interval[origins.length];
		for (int i = 0; i < intervals.length; i++) {
			intervals[i] = new Interval(origins[i], sizes[i]);
		}
		return intervals;
	}

	private void fillOccupiedCells(Map<Point, ControlInfo> occupiedCells,
			ControlInfo control,
			Rectangle cells) {
		for (int x = cells.x; x < cells.right(); x++) {
			for (int y = cells.y; y < cells.bottom(); y++) {
				occupiedCells.put(new Point(x, y), control);
			}
		}
	}

	@Override
	public boolean isFiller(ControlInfo control) {
		return control != null
				&& isLabel(control)
				&& control.getCreationSupport() instanceof ConstructorCreationSupport
				&& control.getVariableSupport() instanceof EmptyVariableSupport
				&& control.getMethodInvocations().isEmpty();
	}

	private static boolean isLabel(ControlInfo control) {
		Class<?> componentClass = control.getDescription().getComponentClass();
		return componentClass.getName().equals("org.eclipse.swt.widgets.Label");
	}

	//////////////////////////////////////////////////////////////////////////
	//
	// Clipboard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void clipboardCopy_addControlCommands(ControlInfo control,
			List<ClipboardCommand> commands) throws Exception {
		if (!isFiller(control)) {
			GridDataInfo gridData = getGridData(control);
			final int column = gridData.x;
			final int row = gridData.y;
			// command for adding Control
			commands.add(new LayoutClipboardCommand<GridLayoutInfo>(control) {
				private static final long serialVersionUID = 0L;

				@Override
				protected void add(GridLayoutInfo layout, ControlInfo control) throws Exception {
					layout.command_CREATE(control, column, false, row, false);
				}
			});
			// command for GridData
			commands.add(new LayoutDataClipboardCommand(this, control));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Manage general layout data
	//
	////////////////////////////////////////////////////////////////////////////
	static final BidiMap<GeneralLayoutData.HorizontalAlignment, Integer> m_horizontalAlignmentMap;
	static {
		BidiMap<GeneralLayoutData.HorizontalAlignment, Integer> horizontalAlignmentMap = new DualHashBidiMap<>();
		horizontalAlignmentMap.put(GeneralLayoutData.HorizontalAlignment.LEFT, SWT.LEFT);
		horizontalAlignmentMap.put(GeneralLayoutData.HorizontalAlignment.CENTER, SWT.CENTER);
		horizontalAlignmentMap.put(GeneralLayoutData.HorizontalAlignment.RIGHT, SWT.RIGHT);
		horizontalAlignmentMap.put(GeneralLayoutData.HorizontalAlignment.FILL, SWT.FILL);
		m_horizontalAlignmentMap = UnmodifiableBidiMap.unmodifiableBidiMap(horizontalAlignmentMap);
	}
	static final BidiMap<GeneralLayoutData.VerticalAlignment, Integer> m_verticalAlignmentMap;
	static {
		BidiMap<GeneralLayoutData.VerticalAlignment, Integer> verticalAlignmentMap = new DualHashBidiMap<>();
		verticalAlignmentMap.put(GeneralLayoutData.VerticalAlignment.TOP, SWT.TOP);
		verticalAlignmentMap.put(GeneralLayoutData.VerticalAlignment.CENTER, SWT.CENTER);
		verticalAlignmentMap.put(GeneralLayoutData.VerticalAlignment.BOTTOM, SWT.BOTTOM);
		verticalAlignmentMap.put(GeneralLayoutData.VerticalAlignment.FILL, SWT.FILL);
		m_verticalAlignmentMap = UnmodifiableBidiMap.unmodifiableBidiMap(verticalAlignmentMap);
	}

	@Override
	protected void storeLayoutData(ControlInfo control, LayoutDataInfo layoutData) throws Exception {
		GridDataInfo gridLayoutData = (GridDataInfo) layoutData;
		GeneralLayoutData generalLayoutData = new GeneralLayoutData();
		generalLayoutData.gridX = gridLayoutData.x;
		generalLayoutData.gridY = gridLayoutData.y;
		generalLayoutData.spanX =
				(Integer) GeneralLayoutData.getLayoutPropertyValue(gridLayoutData, "horizontalSpan");
		generalLayoutData.spanY =
				(Integer) GeneralLayoutData.getLayoutPropertyValue(gridLayoutData, "verticalSpan");
		generalLayoutData.horizontalGrab = (Boolean) GeneralLayoutData.getLayoutPropertyValue(
				gridLayoutData,
				"grabExcessHorizontalSpace");
		generalLayoutData.verticalGrab = (Boolean) GeneralLayoutData.getLayoutPropertyValue(
				gridLayoutData,
				"grabExcessVerticalSpace");
		// alignments
		generalLayoutData.horizontalAlignment = GeneralLayoutData.getGeneralValue(
				m_horizontalAlignmentMap,
				(Integer) GeneralLayoutData.getLayoutPropertyValue(gridLayoutData, "horizontalAlignment"));
		generalLayoutData.verticalAlignment = GeneralLayoutData.getGeneralValue(
				m_verticalAlignmentMap,
				(Integer) GeneralLayoutData.getLayoutPropertyValue(gridLayoutData, "verticalAlignment"));
		generalLayoutData.putToInfo(control);
	}
}