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
package org.eclipse.wb.tests.designer.core.model.property;

import org.eclipse.wb.internal.core.model.property.EmptyProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.IntegerPropertyEditor;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link EmptyProperty}.
 *
 * @author scheglov_ke
 */
public class EmptyPropertyTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_defaultEditor() throws Exception {
		Property property = new EmptyProperty();
		assertSame(BooleanPropertyEditor.INSTANCE, property.getEditor());
		assertOtherFeatures(property);
	}

	@Test
	public void test_myEditor() throws Exception {
		Property property = new EmptyProperty(IntegerPropertyEditor.INSTANCE);
		assertSame(IntegerPropertyEditor.INSTANCE, property.getEditor());
		assertOtherFeatures(property);
	}

	private static void assertOtherFeatures(Property property) throws Exception {
		assertNull(property.getTitle());
		// no value
		assertFalse(property.isModified());
		assertSame(Property.UNKNOWN_VALUE, property.getValue());
		// setValue() ignored
		property.setValue(new Object());
		assertFalse(property.isModified());
		assertSame(Property.UNKNOWN_VALUE, property.getValue());
	}
}
