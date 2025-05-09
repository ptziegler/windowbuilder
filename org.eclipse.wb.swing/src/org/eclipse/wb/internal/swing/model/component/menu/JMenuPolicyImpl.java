/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.model.component.menu;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.VoidInvocationCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.model.variable.VoidInvocationVariableSupport;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo;
import org.eclipse.wb.internal.swing.model.bean.ActionInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Implementation of {@link IMenuPolicy} for {@link JMenu}, plain or wrapped by {@link JPopupMenu}.
 *
 * @author scheglov_ke
 * @coverage swing.model.menu
 */
final class JMenuPolicyImpl implements IMenuPolicy {
	private final JavaInfo m_menu;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	JMenuPolicyImpl(JavaInfo menu_Info) {
		m_menu = menu_Info;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean validateCreate(Object newObject) {
		return isValidObjectType(newObject);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean validatePaste(final Object mementoObject) {
		return ExecutionUtils.runObjectLog(() -> {
			List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) mementoObject;
			for (JavaInfoMemento memento : mementos) {
				JavaInfo component = memento.create(m_menu);
				if (!isValidObjectType(component)) {
					return false;
				}
			}
			return true;
		}, false);
	}

	@Override
	public boolean validateMove(Object object) {
		if (isValidObjectType(object)) {
			ComponentInfo component = (ComponentInfo) object;
			// don't move item on its child menu
			return !component.isItOrParentOf(m_menu);
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operations
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void commandCreate(Object newObject, Object nextObject) throws Exception {
		// prepare ComponentInfo to add and its Association
		ComponentInfo newComponent;
		AssociationObject association;
		if (newObject instanceof ActionInfo action) {
			// ensure that ActionInfo is already added
			if (action.getParent() == null) {
				ActionContainerInfo.add(m_menu.getRootJava(), action);
			}
			// prepare CreationSupport
			CreationSupport creationSupport;
			{
				String signature = "add(javax.swing.Action)";
				String source = TemplateUtils.format("add({0})", action);
				creationSupport = new ImplicitFactoryCreationSupport(signature, source);
			}
			// use JMenuItem as component
			newComponent =
					(ComponentInfo) JavaInfoUtils.createJavaInfo(
							m_menu.getEditor(),
							JMenuItem.class,
							creationSupport);
			m_menu.getBroadcastObject().select(List.of(newComponent));
			// association is done implicitly during creation
			association = AssociationObjects.invocationVoid();
		} else {
			newComponent = (ComponentInfo) newObject;
			association = getNewAssociation();
		}
		// add ComponentInfo
		ComponentInfo nextComponent = (ComponentInfo) nextObject;
		if (newComponent.getCreationSupport() instanceof VoidInvocationCreationSupport) {
			JavaInfoUtils.add(
					newComponent,
					new VoidInvocationVariableSupport(newComponent),
					PureFlatStatementGenerator.INSTANCE,
					AssociationObjects.invocationVoid(),
					m_menu,
					nextComponent);
		} else {
			JavaInfoUtils.add(newComponent, association, m_menu, nextComponent);
		}
		// schedule selection
		MenuUtils.setSelectingItem(newComponent);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<?> commandPaste(Object mementoObject, final Object nextObject) throws Exception {
		List<ComponentInfo> pastedObjects = new ArrayList<>();
		List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) mementoObject;
		for (JavaInfoMemento memento : mementos) {
			ComponentInfo newComponent = (ComponentInfo) memento.create(m_menu);
			commandCreate(newComponent, nextObject);
			memento.apply();
			pastedObjects.add(newComponent);
		}
		return pastedObjects;
	}

	@Override
	public void commandMove(Object object, Object nextObject) throws Exception {
		ComponentInfo component = (ComponentInfo) object;
		ComponentInfo nextComponent = (ComponentInfo) nextObject;
		JavaInfoUtils.move(component, getNewAssociation(), m_menu, nextComponent);
		// schedule selection
		MenuUtils.setSelectingItem(component);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if object has valid type, i.e. {@link ComponentInfo} and not
	 *         {@link JPopupMenuInfo}.
	 */
	private static boolean isValidObjectType(Object object) {
		// don't allow JPopupMenu
		if (object instanceof JPopupMenuInfo) {
			return false;
		}
		// allow Action
		if (object instanceof ActionInfo) {
			return true;
		}
		// add any component
		return object instanceof ComponentInfo;
	}

	/**
	 * @return the {@link AssociationObject} for new {@link JMenuItemInfo}.
	 */
	private AssociationObject getNewAssociation() {
		return AssociationObjects.invocationChild("%parent%.add(%child%)", false);
	}
}