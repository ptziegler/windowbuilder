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
package org.eclipse.wb.internal.core.nls.model;

import org.eclipse.swt.graphics.Image;

/**
 * Information about part of Locale - language or country.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class LocalePartInfo implements Comparable<LocalePartInfo> {
	private final String m_name;
	private final String m_displayName;
	private final Image m_flagImage;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LocalePartInfo(String name, String displayName, Image flagImage) {
		m_name = name;
		m_displayName = displayName;
		m_flagImage = flagImage;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public String getName() {
		return m_name;
	}

	public Image getFlagImage() {
		return m_flagImage;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		if (m_name.length() == 0) {
			return m_displayName;
		}
		return m_name + " - " + m_displayName;
	}

	@Override
	public int hashCode() {
		return m_name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof LocalePartInfo && m_name.equals(((LocalePartInfo) obj).m_name);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Comparable
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int compareTo(LocalePartInfo o) {
		return m_name.compareTo(o.m_name);
	}
}