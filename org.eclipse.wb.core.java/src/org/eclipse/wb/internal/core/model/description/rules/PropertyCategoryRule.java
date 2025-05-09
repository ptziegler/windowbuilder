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
package org.eclipse.wb.internal.core.model.description.rules;

import org.eclipse.wb.core.databinding.xsd.component.PropertyConfiguration;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;

import org.apache.commons.lang3.function.FailableBiConsumer;

/**
 * The {@link FailableBiConsumer} that sets {@link PropertyCategory} of current
 * {@link GenericPropertyDescription} .
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class PropertyCategoryRule
		implements FailableBiConsumer<GenericPropertyDescription, PropertyConfiguration.Category, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(GenericPropertyDescription propertyDescription, PropertyConfiguration.Category category)
			throws Exception {
		// prepare category
		PropertyCategory propertyCategory;
		{
			String categoryTitle = category.getValue().value();
			if ("preferred".equals(categoryTitle)) {
				propertyCategory = PropertyCategory.PREFERRED;
			} else if ("normal".equals(categoryTitle)) {
				propertyCategory = PropertyCategory.NORMAL;
			} else if ("advanced".equals(categoryTitle)) {
				propertyCategory = PropertyCategory.ADVANCED;
			} else if ("hidden".equals(categoryTitle)) {
				propertyCategory = PropertyCategory.HIDDEN;
			} else {
				throw new IllegalArgumentException("Unknown category " + categoryTitle);
			}
		}
		// set category
		propertyDescription.setCategory(propertyCategory);
	}
}
