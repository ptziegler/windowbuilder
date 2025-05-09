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
package org.eclipse.wb.internal.rcp.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateStrategyInfo;

/**
 * Content provider for edit (choose strategy class over dialog and combo)
 * {@link UpdateStrategyInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class UpdateStrategyUiContentProvider extends ChooseClassUiContentProvider {
	private final UpdateStrategyInfo m_strategy;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public UpdateStrategyUiContentProvider(ChooseClassConfiguration configuration,
			UpdateStrategyInfo strategy) {
		super(configuration);
		m_strategy = strategy;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void updateFromObject() {
		setClassName(m_strategy.getStringValue());
	}

	@Override
	public void saveToObject() {
		m_strategy.setStringValue(getClassName());
	}
}