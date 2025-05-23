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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables;

import org.eclipse.wb.internal.core.databinding.model.IObserveDecoration;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.presentation.SimpleObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.TypeImageProvider;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;

import java.util.Collections;
import java.util.List;

/**
 * {@link BindableInfo} model for <code>SWT</code> widget property.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class WidgetPropertyBindableInfo extends BindableInfo implements IObserveDecoration {
	private final IObservableFactory m_observableFactory;
	private final IObservePresentation m_presentation;
	private final IObserveDecorator m_decorator;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public WidgetPropertyBindableInfo(Class<?> objectType,
			IReferenceProvider referenceProvider,
			IObservableFactory observableFactory,
			IObservePresentation presentation,
			IObserveDecorator decorator) {
		super(objectType, referenceProvider);
		setBindingDecoration(IDecoration.TOP_LEFT);
		m_observableFactory = observableFactory;
		m_presentation = presentation;
		m_decorator = decorator;
	}

	public WidgetPropertyBindableInfo(String text,
			ImageDescriptor image,
			Class<?> objectType,
			String reference,
			IObservableFactory observableFactory,
			IObserveDecorator decorator) {
		this(objectType,
				new StringReferenceProvider(reference),
				observableFactory,
				new SimpleObservePresentation(text, image),
				decorator);
	}

	public WidgetPropertyBindableInfo(String text,
			Class<?> objectType,
			String reference,
			IObservableFactory observableFactory,
			IObserveDecorator decorator) {
		this(text,
				TypeImageProvider.getImageDescriptor(objectType),
				objectType,
				reference,
				observableFactory,
				decorator);
	}

	public WidgetPropertyBindableInfo(String text,
			Class<?> objectType,
			String reference,
			IObserveDecorator decorator) {
		this(text, objectType, reference, SwtObservableFactory.SWT, decorator);
	}

	public WidgetPropertyBindableInfo(WidgetPropertyBindableInfo bindable) {
		this(bindable.getObjectType(),
				bindable.getReferenceProvider(),
				bindable.m_observableFactory,
				bindable.m_presentation,
				bindable.m_decorator);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// BindableInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<BindableInfo> getChildren() {
		return Collections.emptyList();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Hierarchy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IObserveInfo getParent() {
		return null;
	}

	@Override
	public List<IObserveInfo> getChildren(ChildrenContext context) {
		return Collections.emptyList();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IObservePresentation getPresentation() {
		return m_presentation;
	}

	@Override
	public IObserveDecorator getDecorator() {
		return m_decorator;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IObservableFactory getObservableFactory() throws Exception {
		return m_observableFactory;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObserveType
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ObserveType getType() {
		return ObserveType.WIDGETS;
	}
}