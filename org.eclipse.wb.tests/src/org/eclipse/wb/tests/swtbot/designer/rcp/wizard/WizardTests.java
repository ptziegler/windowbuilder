/*******************************************************************************
 * Copyright (c) 2023, 2024 Patrick Ziegler and others.
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
package org.eclipse.wb.tests.swtbot.designer.rcp.wizard;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * A collection of all SWTBot-based RCP Wizard tests.
 */
@Suite
@SelectClasses({
		Eclipse4WizardTest.class,
		JFaceWizardTest.class,
		RcpWizardTest.class,
		SwtWizardTest.class
})
public class WizardTests {
}
