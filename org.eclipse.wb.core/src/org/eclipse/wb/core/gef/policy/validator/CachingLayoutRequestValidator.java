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
package org.eclipse.wb.core.gef.policy.validator;

import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link ILayoutRequestValidator} which caches results of validation.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class CachingLayoutRequestValidator implements ILayoutRequestValidator {
	private final ILayoutRequestValidator m_validator;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CachingLayoutRequestValidator(ILayoutRequestValidator validator) {
		m_validator = validator;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ILayoutRequestValidator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean validateCreateRequest(EditPart host, CreateRequest request) {
		Map<EditPart, Boolean> cache = getCache(request);
		Boolean cachedResult = cache.get(host);
		if (cachedResult == null) {
			cachedResult = m_validator.validateCreateRequest(host, request);
			cache.put(host, cachedResult);
		}
		return cachedResult;
	}

	@Override
	public boolean validatePasteRequest(final EditPart host, final PasteRequest request) {
		Map<EditPart, Boolean> cache = getCache(request);
		Boolean cachedResult = cache.get(host);
		if (cachedResult == null) {
			cachedResult = m_validator.validatePasteRequest(host, request);
			cache.put(host, cachedResult);
		}
		return cachedResult;
	}

	@Override
	public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
		Map<EditPart, Boolean> cache = getCache(request);
		Boolean cachedResult = cache.get(host);
		if (cachedResult == null) {
			cachedResult = m_validator.validateMoveRequest(host, request);
			cache.put(host, cachedResult);
		}
		return cachedResult;
	}

	@Override
	public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
		Map<EditPart, Boolean> cache = getCache(request);
		Boolean cachedResult = cache.get(host);
		if (cachedResult == null) {
			cachedResult = m_validator.validateAddRequest(host, request);
			cache.put(host, cachedResult);
		}
		return cachedResult;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the cache for validation results.
	 */
	@SuppressWarnings("unchecked")
	private Map<EditPart, Boolean> getCache(Request request) {
		Map<EditPart, Boolean> cache = (Map<EditPart, Boolean>) request.getExtendedData().get(this);
		if (cache == null) {
			cache = new HashMap<>();
			request.getExtendedData().put(this, cache);
		}
		return cache;
	}
}
