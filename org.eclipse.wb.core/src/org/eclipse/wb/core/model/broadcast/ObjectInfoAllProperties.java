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
package org.eclipse.wb.core.model.broadcast;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.Property;

import java.util.List;

/**
 * Listener for {@link ObjectInfo} events.
 * <p>
 * Used to get notified when all other methods for adding properties were already used so the
 * properties are gathered and {@link ObjectInfo#getProperties()} ready to sort and return result.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface ObjectInfoAllProperties {
	void invoke(ObjectInfo object, List<Property> properties) throws Exception;
}