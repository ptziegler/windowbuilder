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
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends broadcast event that something happens with given {@link ObjectInfo}. For example we can
 * send events that some object was added/removed from its parent. We send this event as broadcast
 * because there are cases when we need to know globally that some new object was added (for example
 * in Forms API for adapting each Composite).
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public final class BroadcastSupport {
	/**
	 * {@link Map} for "listener class" -> "listener implementations".
	 */
	private final Map<Class<?>, List<Object>> m_classToListeners = new HashMap<>();
	/**
	 * {@link Map} for "listener target" -> "listener implementations".
	 */
	private final Map<ObjectInfo, List<Object>> m_targetToListeners = new HashMap<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds new listener with superclass.
	 */
	public void addListener(ObjectInfo target, Object listenerImpl) {
		Class<?> listenerClass = getListenerClass(listenerImpl);
		addListener(getClassListeners(listenerClass), listenerImpl);
		if (target != null) {
			addListener(getTargetListeners(target), listenerImpl);
		}
	}

	/**
	 * Removes listener with superclass.
	 */
	public void removeListener(ObjectInfo target, Object listenerImpl) {
		Class<?> listenerClass = getListenerClass(listenerImpl);
		getClassListeners(listenerClass).remove(listenerImpl);
		if (target != null) {
			getTargetListeners(target).remove(listenerImpl);
		}
	}

	/**
	 * Moves listeners from one target to another.
	 *
	 * @noreference
	 */
	public void targetListener(ObjectInfo oldTarget, ObjectInfo newTarget) {
		List<?> listeners = m_targetToListeners.remove(oldTarget);
		if (listeners != null) {
			getTargetListeners(newTarget).addAll(listeners);
		}
	}

	/**
	 * When we remove {@link ObjectInfo}'s from their parent, so exclude them from hierarchy, or these
	 * {@link ObjectInfo}'s are just not included into hierarchy during parsing; we should remove any
	 * broadcast listeners, registered by these {@link ObjectInfo}'s.
	 *
	 * @noreference
	 */
	public void cleanUpTargets(ObjectInfo root) {
		List<ObjectInfo> targets = new ArrayList<>(m_targetToListeners.keySet());
		for (ObjectInfo target : targets) {
			if (!root.isItOrParentOf(target)) {
				cleanUpTarget(target);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Class} of broadcast listener extended/implemented by given {@link Object}.
	 */
	private static Class<?> getListenerClass(Object listenerImpl) {
		Class<?> implClass = listenerImpl.getClass();
		// old case - extent listener class with many methods
		{
			Class<?> listenerClass = implClass.getSuperclass();
			if (listenerClass != Object.class) {
				while (listenerClass.getSuperclass() != Object.class) {
					listenerClass = listenerClass.getSuperclass();
				}
				return listenerClass;
			}
		}
		// new case - implement single listener interface
		{
			Class<?>[] interfaces = implClass.getInterfaces();
			if (EnvironmentUtils.DEVELOPER_HOST) {
				Assert.isTrue(
						interfaces.length == 1,
						"Only one listener interface expected, but %s found.",
						interfaces.length);
			}
			return interfaces[0];
		}
	}

	/**
	 * @return the {@link List} of listeners of given class. May return empty {@link List}, but not
	 *         <code>null</code>.
	 */
	private List<Object> getClassListeners(Class<?> listenerClass) {
		List<Object> listeners = m_classToListeners.get(listenerClass);
		if (listeners == null) {
			listeners = new ArrayList<>();
			m_classToListeners.put(listenerClass, listeners);
		}
		return listeners;
	}

	/**
	 * @return the {@link List} of listeners of given class. May return empty {@link List}, but not
	 *         <code>null</code>.
	 */
	private List<Object> getTargetListeners(ObjectInfo target) {
		List<Object> listeners = m_targetToListeners.get(target);
		if (listeners == null) {
			listeners = new ArrayList<>();
			m_targetToListeners.put(target, listeners);
		}
		return listeners;
	}

	/**
	 * Adds new listener into {@link List} only if there are no same listener yet.
	 */
	private static void addListener(List<Object> listeners, Object listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes listeners bound to the given target.
	 */
	private void cleanUpTarget(ObjectInfo target) {
		// remove separate listeners for target
		List<Object> listeners = new ArrayList<>(getTargetListeners(target));
		for (Object listenerImpl : listeners) {
			removeListener(target, listenerImpl);
		}
		// remove target
		m_targetToListeners.remove(target);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Sending
	//
	////////////////////////////////////////////////////////////////////////////
	private final Map<Class<?>, Object> m_listenerToMulticast = new HashMap<>();

	/**
	 * @return the implementation of given listener class (so it can be casted to it) that can be used
	 *         for sending events to all subscribers.
	 */
	public <T> T getListener(final Class<T> listenerClass) {
		Object listenerMulticast = m_listenerToMulticast.get(listenerClass);
		if (listenerMulticast == null) {
			try {
				// remember multi-cast
				listenerMulticast = new ByteBuddy()
						.subclass(listenerClass)
						.method(ElementMatchers.any())
						.intercept(InvocationHandlerAdapter.of((Object obj, Method method, Object[] args) -> {
							// Iterate over a local copy due to a potential ConcurrentModificationException
							for (Object listener : getClassListeners(listenerClass).toArray()) {
								try {
									if (args == null) {
										ReflectionUtils.invokeMethod(method, listener);
									} else {
										ReflectionUtils.invokeMethod(method, listener, args);
									}
								} catch (InvocationTargetException e) {
									throw e.getCause();
								}
							}
							// no result
							return null;
						}))
						.make()
						.load(listenerClass.getClassLoader())
						.getLoaded()
						.getConstructor()
						.newInstance();
				m_listenerToMulticast.put(listenerClass, listenerMulticast);
			} catch(ReflectiveOperationException  e) {
				DesignerPlugin.log(e.getMessage(), e);
			}
		}
		//
		@SuppressWarnings("unchecked")
		T casted_listenerMulticast = (T) listenerMulticast;
		return casted_listenerMulticast;
	}
}
