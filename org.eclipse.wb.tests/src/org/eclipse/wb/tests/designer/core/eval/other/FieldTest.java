/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.core.eval.other;

import org.eclipse.wb.internal.core.eval.evaluators.FieldAccessEvaluator;
import org.eclipse.wb.tests.designer.core.eval.AbstractEngineTest;

import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link FieldAccessEvaluator}.
 *
 * @author lobas_av
 */
public class FieldTest extends AbstractEngineTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		if (m_testProject == null) {
			do_projectCreate();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Exit zone :-) XXX
	//
	////////////////////////////////////////////////////////////////////////////
	public void _test_exit() throws Exception {
		System.exit(0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Field
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_beanFieldValue() throws Exception {
		setFileContentSrc(
				"test/TestBean.java",
				getSourceDQ("package test;", "class TestBean {", "  public int value = 5;", "}"));
		waitForAutoBuild();
		assertEquals(5, evaluateExpression("new TestBean().value", "int"));
	}

	@Test
	public void test_localFieldValue_thisQualifier() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				class Test {
					private int value = 5;
					int foo() {
						return this.value;
					}
				}""");
		assertEquals(5, evaluateSingleMethod(typeDeclaration, "foo()"));
	}

	@Test
	public void test_localFieldValue_defaultValue_false() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				class Test {
					private boolean value;
					boolean foo() {
						return this.value;
					}
				}""");
		assertEquals(false, evaluateSingleMethod(typeDeclaration, "foo()"));
	}

	@Test
	public void test_localFieldValue_defaultValue_zero() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				class Test {
					private int value;
					int foo() {
						return this.value;
					}
				}""");
		assertEquals(0, evaluateSingleMethod(typeDeclaration, "foo()"));
	}

	@Test
	public void test_localFieldValue_defaultValue_null() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				class Test {
					private Object value;
					Object foo() {
						return this.value;
					}
				}""");
		assertEquals(null, evaluateSingleMethod(typeDeclaration, "foo()"));
	}

	@Test
	public void test_localFieldValue_noQualifier() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				class Test {
					private int value = 5;
					int foo() {
						return value;
					}
				}""");
		assertEquals(5, evaluateSingleMethod(typeDeclaration, "foo()"));
	}
}