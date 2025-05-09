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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.ICompleteListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;

import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link IUiContentProvider} which is a container for other {@link IUiContentProvider}'s.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public abstract class UIContentContainer<T> implements IUiContentProvider {
	protected final T m_binding;
	private final String m_errorPrefix;
	protected final List<IUiContentProvider> m_providers = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public UIContentContainer(T binding, String errorPrefix) {
		m_binding = binding;
		m_errorPrefix = errorPrefix;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public T getBinding() {
		return m_binding;
	}

	public List<IUiContentProvider> getProviders() {
		return m_providers;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Complete
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setCompleteListener(ICompleteListener listener) {
		for (IUiContentProvider provider : m_providers) {
			provider.setCompleteListener(listener);
		}
	}

	@Override
	public String getErrorMessage() {
		for (IUiContentProvider provider : m_providers) {
			String errorMessage = provider.getErrorMessage();
			if (errorMessage != null) {
				return m_errorPrefix + errorMessage;
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int getNumberOfControls() {
		int columns = 0;
		for (IUiContentProvider provider : m_providers) {
			columns = Math.max(columns, provider.getNumberOfControls());
		}
		return columns;
	}

	@Override
	public void createContent(Composite parent, int columns) {
		for (IUiContentProvider provider : m_providers) {
			provider.createContent(parent, columns);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() throws Exception {
		for (IUiContentProvider provider : m_providers) {
			provider.updateFromObject();
		}
	}

	@Override
	public void saveToObject() throws Exception {
		for (IUiContentProvider provider : m_providers) {
			provider.saveToObject();
		}
	}
}