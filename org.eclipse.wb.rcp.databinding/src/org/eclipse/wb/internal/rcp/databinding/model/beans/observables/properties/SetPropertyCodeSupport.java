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
package org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailSetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.SetBeanObservableInfo;

import java.util.List;

/**
 * Model for observable object {@code BeanProperties.set(...)}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public class SetPropertyCodeSupport extends BeanPropertiesCodeSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getObservableType() {
		return "org.eclipse.core.databinding.property.set.ISetProperty";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ObservableInfo createObservable(BeanBindableInfo bindableObject,
			BeanPropertyBindableInfo bindableProperty) throws Exception {
		return new SetBeanObservableInfo(bindableObject, bindableProperty);
	}

	@Override
	protected ObservableInfo createDetailObservable(ObservableInfo masterObservable) throws Exception {
		Assert.isNotNull(m_parserPropertyReference);
		Assert.isNotNull(m_parserPropertyType);
		DetailSetBeanObservableInfo observable =
				new DetailSetBeanObservableInfo(masterObservable,
						m_parserBeanType,
						m_parserPropertyReference,
						m_parserPropertyType);
		observable.setPojoBindable(parserIsPojo());
		observable.setCodeSupport(new SetPropertyDetailCodeSupport());
		return observable;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addSourceCode(ObservableInfo observable,
			List<String> lines,
			CodeGenerationSupport generationSupport) throws Exception {
		// prepare variable
		if (observable.getVariableIdentifier() == null) {
			observable.setVariableIdentifier(generationSupport.generateLocalName(
					observable.getBindableProperty().getReference(),
					observable.getBindableObject().getReference(),
					"ObserveSet"));
		}
		//
		String sourceCode =
				observable.isPojoBindable()
				? "org.eclipse.core.databinding.beans.typed.PojoProperties"
						: "org.eclipse.core.databinding.beans.typed.BeanProperties";
		sourceCode += ".set(" + observable.getBindableProperty().getReference() + ")";
		if (getVariableIdentifier() != null) {
			if (generationSupport.addModel(this)) {
				lines.add("org.eclipse.core.databinding.beans.IBeanSetProperty "
						+ getVariableIdentifier()
						+ " = "
						+ sourceCode
						+ ";");
			}
			sourceCode = getVariableIdentifier();
		}
		// add code
		lines.add("org.eclipse.core.databinding.observable.set.IObservableSet "
				+ observable.getVariableIdentifier()
				+ " = "
				+ sourceCode
				+ ".observe("
				+ observable.getBindableObject().getReference()
				+ ");");
	}

	@Override
	public String getDetailSourceCode(DetailBeanObservableInfo detailObservable,
			List<String> lines,
			CodeGenerationSupport generationSupport) throws Exception {
		String sourceCode =
				m_parserIsPojo
				? "org.eclipse.core.databinding.beans.typed.PojoProperties"
						: "org.eclipse.core.databinding.beans.typed.BeanProperties";
		sourceCode +=
				".set("
						+ detailObservable.getDetailPropertyReference()
						+ ", "
						+ CoreUtils.getClassName(detailObservable.getDetailPropertyType())
						+ ".class)";
		if (getVariableIdentifier() == null) {
			return sourceCode;
		}
		if (generationSupport.addModel(this)) {
			lines.add("org.eclipse.core.databinding.observable.set.IObservableSet "
					+ getVariableIdentifier()
					+ " = "
					+ sourceCode
					+ ";");
		}
		return getVariableIdentifier();
	}
}