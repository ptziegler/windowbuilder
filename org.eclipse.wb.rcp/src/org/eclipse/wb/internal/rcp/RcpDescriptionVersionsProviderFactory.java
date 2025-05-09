/*******************************************************************************
 * Copyright (c) 2011, 2015 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    Lars Vogel <Lars.Vogel@vogella.com> - Bug 481842
 *******************************************************************************/
package org.eclipse.wb.internal.rcp;

import org.eclipse.wb.internal.core.model.description.resource.FromListDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProviderFactory;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.SWT;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link IDescriptionVersionsProviderFactory} for RCP.
 *
 * @author scheglov_ke
 * @coverage rcp
 */
public final class RcpDescriptionVersionsProviderFactory
implements
IDescriptionVersionsProviderFactory {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final IDescriptionVersionsProviderFactory INSTANCE =
			new RcpDescriptionVersionsProviderFactory();

	private RcpDescriptionVersionsProviderFactory() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IDescriptionVersionsProviderFactory
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Map<String, Object> getVersions(IJavaProject javaProject, ClassLoader classLoader)
			throws Exception {
		if (!isRCP(javaProject)) {
			return Collections.emptyMap();
		}
		// OK, RCP project
		String version = getSWTVersion();
		return Map.of("rcp_version", version);
	}

	@Override
	public IDescriptionVersionsProvider getProvider(IJavaProject javaProject, ClassLoader classLoader)
			throws Exception {
		if (!isRCP(javaProject)) {
			return null;
		}
		// OK, RCP project
		String version = getSWTVersion();
		List<String> allVersions = List.of(
				"3.7",
				"3.8",
				"4.2",
				"4.3",
				"4.4",
				"4.5",
				"4.6",
				"4.7",
				"4.8",
				"4.9",
				"4.10",
				"4.11",
				"4.12",
				"4.13",
				"4.14",
				"4.15",
				"4.16",
				"4.17",
				"4.18",
				"4.19",
				"4.20",
				"4.21",
				"4.22",
				"4.23",
				"4.24",
				"4.25",
				"4.26",
				"4.27",
				"4.28",
				"4.29");
		return new FromListDescriptionVersionsProvider(allVersions, version) {
			@Override
			protected boolean validate(Class<?> componentClass) throws Exception {
				String className = componentClass.getName();
				return className.startsWith("org.eclipse.swt.")
						|| className.startsWith("org.eclipse.jface.")
						|| className.startsWith("org.eclipse.ui.");
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean isRCP(IJavaProject javaProject) throws JavaModelException {
		return javaProject.findType("org.eclipse.swt.custom.CTabFolder") != null;
	}

	private static String getSWTVersion() {
		int version = SWT.getVersion();
		int major = version / 1000;
		int minor = (version - major * 1000) / 100;
		return major + "." + minor;
	}
}
