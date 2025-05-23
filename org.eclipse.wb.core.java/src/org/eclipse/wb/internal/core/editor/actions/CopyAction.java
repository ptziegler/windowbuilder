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
package org.eclipse.wb.internal.core.editor.actions;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMementoTransfer;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link Action} for {@link ActionFactory#COPY}.
 *
 * @author scheglov_ke
 * @coverage core.editor.action
 */
public class CopyAction extends Action {
	private final IEditPartViewer m_viewer;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CopyAction(IEditPartViewer viewer) {
		m_viewer = viewer;
		m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				firePropertyChange(ENABLED, null, isEnabled() ? Boolean.TRUE : Boolean.FALSE);
			}
		});
		// copy presentation
		ActionUtils.copyPresentation(this, ActionFactory.COPY);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Action
	//
	////////////////////////////////////////////////////////////////////////////
	private List<JavaInfoMemento> m_mementos;

	@Override
	public void run() {
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				List<? extends EditPart> editParts = m_viewer.getSelectedEditParts();
				m_mementos = getMementos(editParts);
				doCopy(m_mementos);
			}
		});
	}

	@Override
	public boolean isEnabled() {
		List<? extends EditPart> editParts = m_viewer.getSelectedEditParts();
		return hasMementos(editParts);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Memento
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Copies {@link JavaInfoMemento}'s into {@link Clipboard}.
	 */
	static void doCopy(List<JavaInfoMemento> mementos) {
		if (mementos != null) {
			Clipboard clipboard = new Clipboard(Display.getCurrent());
			try {
				clipboard.setContents(
						new Object[]{mementos},
						new Transfer[]{JavaInfoMementoTransfer.getInstance()});
			} finally {
				clipboard.dispose();
			}
		}
	}

	/**
	 * @return <code>true</code> if given {@link JavaInfo}'s can be copy/pasted.
	 */
	static boolean hasMementos(final List<? extends EditPart> editParts) {
		return ExecutionUtils.runObjectLog(() -> {
			// selection required
			if (editParts.isEmpty()) {
				return false;
			}
			// check that JavaInfoMemento's can be created
			for (EditPart editPart : editParts) {
				// prepare model
				JavaInfo javaInfo;
				{
					Object model = editPart.getModel();
					if (model instanceof JavaInfo) {
						javaInfo = (JavaInfo) model;
					} else {
						return false;
					}
				}
				// check for memento
				if (!JavaInfoMemento.hasMemento(javaInfo)) {
					return false;
				}
			}
			// OK
			return true;
		}, false);
	}

	/**
	 * @return the {@link JavaInfoMemento}'s with copy/paste information for given {@link JavaInfo}'s.
	 */
	static List<JavaInfoMemento> getMementos(final List<? extends EditPart> editParts) {
		return ExecutionUtils.runObjectLog(() -> getMemento0(editParts), null);
	}

	private static List<JavaInfoMemento> getMemento0(List<? extends EditPart> editParts) throws Exception {
		// prepare objects
		List<JavaInfo> objects = new ArrayList<>();
		for (EditPart editPart : editParts) {
			// prepare object
			JavaInfo object;
			{
				Object model = editPart.getModel();
				if (!(model instanceof JavaInfo)) {
					return null;
				}
				object = (JavaInfo) model;
			}
			// add object
			objects.add(object);
		}
		// don't copy child, if we copy its parent
		for (Iterator<JavaInfo> I = objects.iterator(); I.hasNext();) {
			JavaInfo object = I.next();
			if (object.getParent(objects) != null) {
				I.remove();
			}
		}
		// prepare mementos
		List<JavaInfoMemento> mementos = new ArrayList<>();
		for (JavaInfo object : objects) {
			JavaInfoMemento memento = JavaInfoMemento.createMemento(object);
			mementos.add(memento);
		}
		// OK, final result
		return mementos;
	}
}
