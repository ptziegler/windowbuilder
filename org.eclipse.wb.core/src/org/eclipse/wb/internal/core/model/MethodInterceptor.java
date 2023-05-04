/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model;

import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ImplementationDefinition;
import net.bytebuddy.implementation.Implementation;

/**
 * General-purpose Byte-Buddy aware method interceptor. Instances of this class
 * may be used as an argument for
 * {@link ImplementationDefinition#intercept(Implementation)}, in order to
 * instrument the method invocations of the generated class.
 */
public interface MethodInterceptor {
}