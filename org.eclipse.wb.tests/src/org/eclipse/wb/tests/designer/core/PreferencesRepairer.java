/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.tests.designer.core;

import org.eclipse.jface.preference.IPreferenceStore;

import java.util.Map;
import java.util.TreeMap;

/**
 * Helper for temporary modifying/restoring values in {@link IPreferenceStore}.
 *
 * @author scheglov_ke
 */
public final class PreferencesRepairer {
	private final IPreferenceStore m_preferences;
	private final Map<String, Object> m_nameToValue = new TreeMap<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PreferencesRepairer(IPreferenceStore preferences) {
		m_preferences = preferences;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the <code>boolean</code> value.
	 */
	public void setValue(String name, boolean value) {
		save(name, Boolean.valueOf(m_preferences.getBoolean(name)));
		m_preferences.setValue(name, value);
	}

	/**
	 * Sets the <code>int</code> value.
	 */
	public void setValue(String name, int value) {
		save(name, m_preferences.getInt(name));
		m_preferences.setValue(name, value);
	}

	/**
	 * Sets the <code>String</code> value.
	 */
	public void setValue(String name, String value) {
		save(name, m_preferences.getString(name));
		m_preferences.setValue(name, value);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Restore
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * If preference with given name is not saved yet, save it.
	 */
	private void save(String name, Object value) {
		if (!m_nameToValue.containsKey(name)) {
			m_nameToValue.put(name, value);
		}
	}

	/**
	 * Restores all preferences in {@link IPreferenceStore} in their initial values.
	 */
	public void restore() {
		for (Map.Entry<String, Object> entry : m_nameToValue.entrySet()) {
			String name = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof Boolean) {
				m_preferences.setValue(name, ((Boolean) value).booleanValue());
			} else if (value instanceof Integer) {
				m_preferences.setValue(name, ((Integer) value).intValue());
			} else if (value instanceof String) {
				m_preferences.setValue(name, (String) value);
			}
		}
	}
}
