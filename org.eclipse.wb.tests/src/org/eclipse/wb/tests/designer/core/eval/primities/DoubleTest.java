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
package org.eclipse.wb.tests.designer.core.eval.primities;

import org.eclipse.wb.tests.designer.core.eval.AbstractEngineTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author scheglov_ke
 */
public class DoubleTest extends AbstractEngineTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Project creation
	//
	////////////////////////////////////////////////////////////////////////////
	@BeforeAll
	public static void setUpClass() throws Exception {
		do_projectCreate();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// double
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_double_value1() throws Exception {
		check_double("1D", 1D);
	}

	@Test
	public void test_double_value2() throws Exception {
		check_double("2d", 2d);
	}

	@Test
	public void test_double_positive_value() throws Exception {
		check_double("+3d", +3d);
	}

	@Test
	public void test_double_negative_value() throws Exception {
		check_double("-3d", -3d);
	}

	@Test
	public void test_double_plus() throws Exception {
		check_double("1D + 2D", 1D + 2D);
	}

	@Test
	public void test_double_plus3() throws Exception {
		check_double("1D + 2D + 3D", 1D + 2D + 3D);
	}

	@Test
	public void test_double_minus() throws Exception {
		check_double("5D - 1D", 5D - 1D);
	}

	@Test
	public void test_double_mul() throws Exception {
		check_double("2D * 3D", 2D * 3D);
	}

	@Test
	public void test_double_div() throws Exception {
		check_double("6D / 2D", 6D / 2D);
	}

	@Test
	public void test_double_div2() throws Exception {
		check_double("5D / 2D", 5D / 2D);
	}

	@Test
	public void test_double_mod() throws Exception {
		check_double("5D % 2D", 5D % 2D);
	}

	@Test
	public void test_double_mod2() throws Exception {
		check_double("-5D % 3D", -5D % 3D);
	}

	@Test
	public void test_double_mix_int() throws Exception {
		check_double("1D + 2", 1D + 2);
	}

	@Test
	public void test_double_mix_char() throws Exception {
		check_double("1D + '0'", 1D + '0');
	}

	@Test
	public void test_double_cast_to() throws Exception {
		check_double("((double)1) + 2", (double) 1 + 2);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void check_double(String expression, double expected) throws Exception {
		assertEquals(Double.valueOf(expected), evaluateExpression(expression, "double"));
	}
}
