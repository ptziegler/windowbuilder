/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.tests.designer.core.model.description;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for {@link ComponentDescription}, {@link ComponentDescriptionHelper}, etc.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
		ToolkitDescriptionTest.class,
		LayoutDescriptionTest.class,
		DescriptionProcessorTest.class,
		ComponentDescriptionKeyTest.class,
		ComponentDescriptionTest.class,
		ComponentDescriptionIbmTest.class,
		CreationDescriptionTest.class,
		CreationDescriptionLoadingTest.class,
		MorphingTargetDescriptionTest.class,
		DescriptionVersionsProvidersTest.class,
		ComponentDescriptionHelperTest.class,
		GenericPropertyDescriptionTest.class,
		BeanPropertyTagsTest.class,
		MethodPropertyRuleTest.class
})
public class DescriptionTests {
}
