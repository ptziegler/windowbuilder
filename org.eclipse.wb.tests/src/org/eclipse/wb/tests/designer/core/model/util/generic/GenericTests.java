/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model.util.generic;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test for generic, description driven features.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		CopyPropertyTopChildTest.class,
		CopyPropertyTopTest.class,
		ModelMethodPropertyTest.class,
		ModelMethodPropertyChildTest.class
})
public class GenericTests {
}
