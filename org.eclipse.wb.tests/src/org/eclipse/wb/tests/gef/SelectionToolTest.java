/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.DragPermissionRequest;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.tools.SelectionTool;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.requests.SelectionRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

/**
 * @author lobas_av
 *
 */
public class SelectionToolTest extends RequestTestCase {
	private SelectionTool m_tool;
	private SelectionRequest m_request;

	////////////////////////////////////////////////////////////////////////////
	//
	// SetUp
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		// configure
		m_tool = new SelectionTool();
		m_domain.setActiveTool(m_tool);
		//
		m_request = new SelectionRequest();
		m_request.setType(RequestConstants.REQ_SELECTION);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////

	@Test
	public void test_Move() throws Exception {
		RequestsLogger actualLogger = new RequestsLogger();
		//
		RequestTestCaseEditPart editPart =
				addEditPart(m_viewer.getRootEditPart(), "ParentEditPart", actualLogger, 50, 50, 400, 300);
		//
		RequestTestCaseEditPart childEditPart1 =
				addEditPart(editPart, "ChildEditPart1", actualLogger, 50, 50, 70, 50);
		//
		RequestTestCaseEditPart childEditPart2 =
				addEditPart(editPart, "ChildEditPart2", actualLogger, 250, 25, 100, 155);
		//
		RequestsLogger expectedLogger = new RequestsLogger();
		//
		// move outside of any EditPart
		{
			m_sender.moveTo(10, 10);
			actualLogger.assertEmpty();
		}
		// move over "ParentEditPart"
		{
			m_sender.moveTo(60, 60);
			//
			m_request.setLocation(new Point(60, 60));
			//
			expectedLogger.log(editPart, new String[]{
					"getTargetEditPart",
					"showTargetFeedback",
			"showTargetFeedback"}, m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// move over "ParentEditPart"
		{
			m_sender.moveTo(70, 70);
			//
			m_request.setLocation(new Point(70, 70));
			//
			expectedLogger.log(
					editPart,
					new String[]{"getTargetEditPart", "showTargetFeedback"},
					m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// move over "ChildEditPart1"
		{
			m_sender.moveTo(120, 120);
			//
			m_request.setLocation(new Point(120, 120));
			//
			expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
			expectedLogger.log(editPart, "eraseTargetFeedback", m_request);
			expectedLogger.log(
					childEditPart1,
					new String[]{"showTargetFeedback", "showTargetFeedback"},
					m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// move over "ChildEditPart2"
		{
			m_sender.moveTo(320, 90);
			//
			m_request.setLocation(new Point(320, 90));
			//
			expectedLogger.log(childEditPart2, "getTargetEditPart", m_request);
			expectedLogger.log(childEditPart1, "eraseTargetFeedback", m_request);
			expectedLogger.log(
					childEditPart2,
					new String[]{"showTargetFeedback", "showTargetFeedback"},
					m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
	}

	@Test
	public void test_DragTracker() throws Exception {
		RequestsLogger actualLogger = new RequestsLogger();
		//
		RequestTestCaseEditPart editPart =
				addEditPart(m_viewer.getRootEditPart(), "ParentEditPart", actualLogger, 50, 50, 400, 300);
		//
		RequestTestCaseEditPart childEditPart =
				addEditPart(editPart, "ChildEditPart", actualLogger, 50, 50, 70, 50);
		//
		RequestsLogger expectedLogger = new RequestsLogger();
		//
		assertNull(getDragTracker(m_tool));
		Tool tracker = null;
		//
		// start drag "ChildEditPart"
		{
			m_sender.startDrag(135, 125, 1);
			//
			tracker = getDragTracker(m_tool);
			assertNotNull(tracker);
			assertTrue(tracker.isActive());
			//
			m_request.setLocation(new Point(135, 125));
			m_request.setLastButtonPressed(1);
			//
			expectedLogger.log(
					childEditPart,
					new String[]{"getTargetEditPart", "showTargetFeedback"},
					m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// drag "ChildEditPart"
		{
			m_sender.dragTo(150, 150);
			//
			assertSame(tracker, getDragTracker(m_tool));
			//
			ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
			//
			request.setEditParts(childEditPart);
			//
			expectedLogger.log(childEditPart, "performRequest", new DragPermissionRequest());
			//
			request.setLocation(new Point(150, 150));
			request.setMoveDelta(new Point(15, 25));
			//
			expectedLogger.log(editPart, new String[]{"getTargetEditPart", "getTargetEditPart"}, request);
			//
			request.setType(RequestConstants.REQ_MOVE);
			//
			expectedLogger.log(editPart, "showTargetFeedback", request);
			//
			expectedLogger.log(editPart, new String[]{
					"getTargetEditPart",
					"getTargetEditPart",
					"showTargetFeedback",
			"getCommand"}, request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// end drag "ChildEditPart"
		{
			m_sender.endDrag();
			//
			assertNull(getDragTracker(m_tool));
			assertFalse(tracker.isActive());
			//
			ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
			request.setEditParts(childEditPart);
			request.setLocation(new Point(150, 150));
			request.setMoveDelta(new Point(15, 25));
			//
			expectedLogger.log(editPart, "eraseTargetFeedback", request);
			//
			m_request.setLastButtonPressed(0);
			//
			expectedLogger.log(editPart, "getTargetEditPart", m_request);
			expectedLogger.log(childEditPart, "eraseTargetFeedback", m_request);
			//
			m_request.setLocation(new Point(150, 150));
			//
			expectedLogger.log(editPart, "showTargetFeedback", m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
	}

	@Test
	public void test_DragTracker_MoveHandle() throws Exception {
		RequestsLogger actualLogger = new RequestsLogger();
		//
		RequestTestCaseEditPart editPart =
				addEditPart(m_viewer.getRootEditPart(), "ParentEditPart", actualLogger, 50, 50, 400, 300);
		//
		RequestTestCaseEditPart childEditPart =
				addEditPart(editPart, "ChildEditPart", actualLogger, 50, 50, 70, 50);
		//
		MoveHandle handle = new MoveHandle(childEditPart);
		LayerManager.Helper.find(m_viewer).getLayer(IEditPartViewer.HANDLE_LAYER).add(handle);
		//
		RequestsLogger expectedLogger = new RequestsLogger();
		//
		assertNull(getDragTracker(m_tool));
		//
		// start drag "ChildEditPart"
		{
			m_sender.startDrag(100, 100, 1);
			//
			Tool tracker = getDragTracker(m_tool);
			assertNotNull(tracker);
			assertTrue(tracker.isActive());
			assertSame(handle.getDragTracker(), tracker);
			//
			actualLogger.assertEmpty();
		}
		// drag "ChildEditPart"
		{
			m_sender.dragTo(150, 150);
			//
			assertSame(handle.getDragTracker(), getDragTracker(m_tool));
			//
			ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
			//
			request.setEditParts(childEditPart);
			//
			expectedLogger.log(childEditPart, "performRequest", new DragPermissionRequest());
			//
			request.setLocation(new Point(150, 150));
			request.setMoveDelta(new Point(50, 50));
			//
			expectedLogger.log(editPart, new String[]{"getTargetEditPart", "getTargetEditPart"}, request);
			//
			request.setType(RequestConstants.REQ_MOVE);
			//
			expectedLogger.log(editPart, "showTargetFeedback", request);
			//
			expectedLogger.log(editPart, new String[]{
					"getTargetEditPart",
					"getTargetEditPart",
					"showTargetFeedback",
			"getCommand"}, request);
			assertLoggers(expectedLogger, actualLogger);
		}
		// end drag "ChildEditPart"
		{
			m_sender.endDrag();
			//
			assertNull(getDragTracker(m_tool));
			assertFalse(handle.getDragTracker().isActive());
			//
			ChangeBoundsRequest request = new ChangeBoundsRequest(RequestConstants.REQ_MOVE);
			request.setEditParts(childEditPart);
			request.setLocation(new Point(150, 150));
			request.setMoveDelta(new Point(50, 50));
			//
			expectedLogger.log(editPart, "eraseTargetFeedback", request);
			//
			m_request.setLastButtonPressed(0);
			//
			expectedLogger.log(editPart, "getTargetEditPart", m_request);
			//
			m_request.setLocation(new Point(150, 150));
			//
			expectedLogger.log(editPart, "showTargetFeedback", m_request);
			assertLoggers(expectedLogger, actualLogger);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static final Tool getDragTracker(SelectionTool tool) throws Exception {
		Field field = SelectionTool.class.getDeclaredField("m_dragTracker");
		field.setAccessible(true);
		return (Tool) field.get(tool);
	}
}