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
package org.eclipse.wb.internal.rcp.wizards.forms.details;

import org.eclipse.wb.internal.core.wizards.AbstractDesignWizardPage;
import org.eclipse.wb.internal.rcp.wizards.RcpWizard;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.forms.IDetailsPage;

/**
 * {@link Wizard} that creates new Forms {@link IDetailsPage}.
 *
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public final class DetailsPageWizard extends RcpWizard {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DetailsPageWizard() {
		setWindowTitle(WizardsMessages.DetailsPageWizard_title);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Wizard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected AbstractDesignWizardPage createMainPage() {
		return new DetailsPageWizardPage();
	}
}