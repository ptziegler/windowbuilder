/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer;

import com.google.common.base.Strings;

import org.eclipse.wb.tests.designer.XML.XmlTests;
import org.eclipse.wb.tests.designer.XWT.XwtTests;
import org.eclipse.wb.tests.designer.core.CoreTests;
import org.eclipse.wb.tests.designer.editor.EditorTests;
import org.eclipse.wb.tests.designer.rcp.RcpTests;
import org.eclipse.wb.tests.designer.swing.SwingTests;
import org.eclipse.wb.tests.designer.swt.SwtTests;
import org.eclipse.wb.tests.draw2d.Draw2dTests;
import org.eclipse.wb.tests.gef.GefTests;
import org.eclipse.wb.tests.utils.CommonTests;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * All WindowBuilder tests.
 *
 * @author scheglov_ke
 */

@RunWith(Suite.class)
@SuiteClasses({
	Draw2dTests.class, //
	GefTests.class, //
	SwtTests.class,
	CommonTests.class,
	CoreTests.class,
	EditorTests.class,
	SwingTests.class,
	RcpTests.class,
	// not yet ready to run, need work to run successfully
	XmlTests.class,
	XwtTests.class

})

public class WindowBuilderTests {
	private static List<Class<?>> TEST_ANNOTATIONS = List.of( //
			Test.class, //
			Ignore.class, //
			Before.class, //
			After.class, //
			BeforeClass.class, //
			AfterClass.class);

	public static void main(String[] args) {
		System.out.println(new File("src").getAbsolutePath());
		// listJUnitClasses(new IndentPrinter(), WindowBuilderTests.class);
	}

	public static void listJUnitClasses(IndentPrinter printer, Class<?> clazz) {
		printer.println(clazz.getName());
		SuiteClasses suiteClasses = clazz.getAnnotation(SuiteClasses.class);
		if (suiteClasses != null) {
			for (Class<?> suiteClass : suiteClasses.value()) {
				printer.depth++;
				listJUnitClasses(printer, suiteClass);
				printer.depth--;
			}
		} else {
			printer.depth++;
			// listJUnitMethods(printer, clazz);
			printer.depth--;
		}
	}

	public static void listJUnitMethods(IndentPrinter printer, Class<?> clazz) {
		// Only public methods can be executed. Include inherited tests as well
		for (Method method : clazz.getMethods()) {
			if (isTestMethod(method)) {
				printer.println(method.getName());
			}
		}
	}

	private static boolean isTestMethod(Method method) {
		for (Annotation annotation : method.getAnnotations()) {
			if (TEST_ANNOTATIONS.contains(annotation.annotationType())) {
				return true;
			}
		}
		return false;
	}

	private static class IndentPrinter {
		private int depth;

		public void println(String message) {
			System.out.println(Strings.repeat("\t", depth) + message);
		}
	}
}
