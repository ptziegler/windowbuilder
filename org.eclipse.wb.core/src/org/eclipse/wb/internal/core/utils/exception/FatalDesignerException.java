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
package org.eclipse.wb.internal.core.utils.exception;

/**
 * {@link DesignerException} that should be handled as fatal, which terminates current operation.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public class FatalDesignerException extends DesignerException {
	private static final long serialVersionUID = 0L;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public FatalDesignerException(int code, String... parameters) {
		super(code, parameters);
	}

	public FatalDesignerException(int code, Throwable cause, String... parameters) {
		super(code, cause, parameters);
	}
}
