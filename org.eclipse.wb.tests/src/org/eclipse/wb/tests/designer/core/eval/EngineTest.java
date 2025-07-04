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
package org.eclipse.wb.tests.designer.core.eval;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ScrollPaneConstants;

/**
 * @author scheglov_ke
 */
public class EngineTest extends AbstractEngineTest {
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
	// Fail
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_fail() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				public class Test {
					int foo(int value) {
						return value;
					}
				}""");
		try {
			evaluateSingleMethod(typeDeclaration, "foo(int)");
			fail();
		} catch (Throwable e_) {
			DesignerException e = DesignerExceptionUtils.getDesignerException(e_);
			assertEquals(ICoreExceptionConstants.EVAL_NO_METHOD_INVOCATION, e.getCode());
		}
	}

	/**
	 * Test that {@link EvaluationContext#evaluationFailed(Expression, Throwable)} is notified.
	 */
	@Test
	public void test_EvaluationContext_evaluationFailed_noResult() throws Exception {
		setFileContentSrc(
				"test/MyObject.java",
				getSource(
						"package test;",
						"public class MyObject {",
						"  public static int getValue() {",
						"    throw new IllegalStateException();",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				public class Test {
					public void root() {
						MyObject.getValue();
					}
				}""");
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		Expression expression =
				(Expression) m_lastEditor.getEnclosingNode("MyObject.getValue()").getParent();
		// prepare context
		EvaluationContext context;
		final AtomicBoolean evaluationFailed = new AtomicBoolean();
		{
			ClassLoader projectClassLoader =
					CodeUtils.getProjectClassLoader(m_lastEditor.getModelUnit().getJavaProject());
			ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(methodDeclaration);
			context = new EvaluationContext(projectClassLoader, flowDescription) {
				@Override
				public Object evaluationFailed(Expression expression_, Throwable e) throws Exception {
					evaluationFailed.set(true);
					return AstEvaluationEngine.UNKNOWN;
				}
			};
		}
		// evaluate, we don't return value, so evaluation failed
		try {
			AstEvaluationEngine.evaluate(context, expression);
			fail();
		} catch (Throwable e) {
		}
		assertTrue(evaluationFailed.get());
	}

	/**
	 * Test that {@link EvaluationContext#evaluationFailed(Expression, Throwable)} is notified.
	 */
	@Test
	public void test_EvaluationContext_evaluationFailed_returnResult() throws Exception {
		setFileContentSrc(
				"test/MyObject.java",
				getSource(
						"package test;",
						"public class MyObject {",
						"  public static int getValue() {",
						"    throw new IllegalStateException();",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				public class Test {
					public void root() {
						MyObject.getValue();
					}
				}""");
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		Expression expression =
				(Expression) m_lastEditor.getEnclosingNode("MyObject.getValue()").getParent();
		// prepare context
		EvaluationContext context;
		{
			ClassLoader projectClassLoader =
					CodeUtils.getProjectClassLoader(m_lastEditor.getModelUnit().getJavaProject());
			ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(methodDeclaration);
			context = new EvaluationContext(projectClassLoader, flowDescription) {
				@Override
				public Object evaluationFailed(Expression expression_, Throwable e) throws Exception {
					return 123;
				}
			};
		}
		// evaluate, special result returned
		assertEquals(123, AstEvaluationEngine.evaluate(context, expression));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Simple tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_null() throws Exception {
		assertEquals(null, evaluateExpression("null", "java.lang.Object"));
	}

	@Test
	public void test_parenthesis() throws Exception {
		assertEquals(null, evaluateExpression("(null)", "java.lang.Object"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SimpleName
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_SimpleName_notFound() throws Exception {
		createTypeDeclaration(
				"test",
				"BaseClass.java",
				getSourceDQ(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"package test;",
						"public class BaseClass {",
						"  protected int m_value;",
						"}"));
		TypeDeclaration typeDeclaration =
				createTypeDeclaration(
						"test",
						"BaseSubClass.java",
						getSourceDQ(
								"package test;",
								"public class BaseSubClass extends BaseClass {",
								"  public int getValue() {",
								"    return m_value;",
								"  }",
								"}"));
		waitForAutoBuild();
		try {
			evaluateSingleMethod(typeDeclaration, "getValue()");
			fail();
		} catch (Throwable e_) {
			DesignerException e = DesignerExceptionUtils.getDesignerException(e_);
			assertEquals(ICoreExceptionConstants.EVAL_NO_SIMPLE_NAME_FOUND, e.getCode());
		}
	}

	@Test
	public void test_SimpleName_local() throws Exception {
		TypeDeclaration typeDeclaration =
				createTypeDeclaration(
						"test",
						"Test.java",
						getSourceDQ(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"package test;",
								"public class Test {",
								"  public String use_simpleName_local() {",
								"    String s = '12345';",
								"    return s;",
								"  }",
								"}"));
		Object actual = evaluateSingleMethod(typeDeclaration, "use_simpleName_local()");
		assertEquals("12345", actual);
	}

	@Test
	public void test_SimpleName_field() throws Exception {
		TypeDeclaration typeDeclaration =
				createTypeDeclaration(
						"test",
						"Test.java",
						getSourceDQ(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"package test;",
								"public class Test {",
								"  private final String m_fieldString = '12345';",
								"  public String use_simpleName_field() {",
								"    return m_fieldString;",
								"  }",
								"}"));
		Object actual = evaluateSingleMethod(typeDeclaration, "use_simpleName_field()");
		assertEquals("12345", actual);
	}

	@Test
	public void test_SimpleName_assignment() throws Exception {
		TypeDeclaration typeDeclaration =
				createTypeDeclaration(
						"test",
						"Test.java",
						getSourceDQ(
								"// filler filler filler filler filler",
								"// filler filler filler filler filler",
								"package test;",
								"public class Test {",
								"  public String use_simpleName_assignment() {",
								"    String s = '23';",
								"    s = '12345';",
								"    return s;",
								"  }",
								"}"));
		Object actual = evaluateSingleMethod(typeDeclaration, "use_simpleName_assignment()");
		assertEquals("12345", actual);
	}

	@Test
	public void test_SimpleName_recursiveAssignment() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				public class Test {
					public int root() {
						int value = 4;
						value = value + 1;
						return value;
					}
				}""");
		Object actual = evaluateSingleMethod(typeDeclaration, "root()");
		assertEquals(5, actual);
	}

	@Test
	public void test_SimpleName_inv_good() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				public class Test {
					public void root() {
						foo("12345");
					}
					public String foo(String s) {
						return s;
					}
				}""");
		Object actual = evaluateSingleMethod(typeDeclaration, "root()", "foo(java.lang.String)");
		assertEquals("12345", actual);
	}

	@Test
	public void test_SimpleName_inv_noInvocation() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				public class Test {
					public int foo(int value) {
						return value;
					}
				}""");
		try {
			evaluateSingleMethod(typeDeclaration, "foo(int)");
			fail();
		} catch (Throwable e_) {
			DesignerException e = DesignerExceptionUtils.getDesignerException(e_);
			assertEquals(ICoreExceptionConstants.EVAL_NO_METHOD_INVOCATION, e.getCode());
		}
	}

	/**
	 * If there are two invocations of method, so two variants of value for parameter, choose first
	 * one, don't just fail.
	 */
	@Test
	public void test_SimpleName_inv_twoInvocation() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				public class Test {
					public void root() {
						foo("111");
						foo("222");
					}
					public String foo(String s) {
						return s;
					}
				}""");
		Object actual = evaluateSingleMethod(typeDeclaration, "root()", "foo(java.lang.String)");
		assertEquals("111", actual);
	}

	@Test
	public void test_SimpleName_inheritedConstant() throws Exception {
		setFileContentSrc(
				"test/SuperClass.java",
				getSourceDQ(
						"package test;",
						"public class SuperClass {",
						"  public static int FOO = 555;",
						"}"));
		waitForAutoBuild();
		// parse
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				public class Test extends SuperClass {
					public int foo() {
						return FOO;
					}
				}""");
		waitForAutoBuild();
		// evaluate
		assertEquals(555, evaluateSingleMethod(typeDeclaration, "foo()"));
	}

	@Test
	public void test_SimpleName_localConstant() throws Exception {
		waitForAutoBuild();
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				public class Test {
					public static int FOO = 222;
						public int foo() {
						return FOO;
					}
				}""");
		// evaluate
		assertEquals(222, evaluateSingleMethod(typeDeclaration, "foo()"));
	}

	@Test
	public void test_SimpleName_fieldWithoutInitializer() throws Exception {
		waitForAutoBuild();
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				public class Test {
					public Object field;
					public Object foo() {
						return field;
					}
				}""");
		// evaluate
		assertEquals(null, evaluateSingleMethod(typeDeclaration, "foo()"));
	}

	@Test
	public void test_SimpleName_interfaceConstant() throws Exception {
		setFileContentSrc(
				"test/IConstants.java",
				getSourceDQ("package test;", "public interface IConstants {", "  int FOO = 555;", "}"));
		waitForAutoBuild();
		// parse
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				public class Test implements IConstants {
					public int foo() {
						return FOO;
					}
				}""");
		waitForAutoBuild();
		// evaluate
		assertEquals(555, evaluateSingleMethod(typeDeclaration, "foo()"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// QualifiedName
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for usual {@link QualifiedName}.
	 */
	@Test
	public void test_QualifiedName_1() throws Exception {
		assertEquals(
				Collections.EMPTY_LIST,
				evaluateExpression("java.util.Collections.EMPTY_LIST", "java.lang.Object"));
	}

	/**
	 * Test for {@link QualifiedName} for field implemented in interface.
	 */
	@Test
	public void test_QualifiedName_2() throws Exception {
		Integer actualValue =
				(Integer) evaluateExpression("javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS", "int");
		assertEquals(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS, actualValue.intValue());
	}

	/**
	 * Test for array.length
	 */
	@Test
	public void test_QualifiedName_3() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				class Test {
					public int foo() {
						int[] ints = new int[]{1,2,3};
						return ints.length;
					}
				}""");
		waitForAutoBuild();
		Object actual = evaluateSingleMethod(typeDeclaration, "foo()");
		assertEquals(3, actual);
	}

	/**
	 * Test for java.awt.Dimension.x
	 */
	@Test
	public void test_QualifiedName_4() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				import java.awt.Dimension;
				class Test {
					public int foo() {
						Dimension dimension = new Dimension(1, 2);
						return dimension.width;
					}
				}""");
		Object actual = evaluateSingleMethod(typeDeclaration, "foo()");
		assertEquals(1, actual);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FieldAccess
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_FieldAccess() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				class Test {
					public int use_fieldAccess() {
						return new Foo(12345).m_value;
					}
					public static class Foo {
						public final int m_value;
						public Foo(int value) {
						m_value = value;
						}
					}
				}""");
		waitForAutoBuild();
		Object actual = evaluateSingleMethod(typeDeclaration, "use_fieldAccess()");
		assertEquals(12345, actual);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Assignment
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_assignment() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				class Test {
					public void foo() {
						int a;
						int b;
						a = b = 5;
					}
				}""");
		waitForAutoBuild();
		//
		MethodDeclaration methodDeclaration =
				AstNodeUtils.getMethodBySignature(typeDeclaration, "foo()");
		List<Statement> statements = DomGenerics.statements(methodDeclaration.getBody());
		//
		ClassLoader projectClassLoader =
				CodeUtils.getProjectClassLoader(m_lastEditor.getModelUnit().getJavaProject());
		ExecutionFlowDescription flowDescription = new ExecutionFlowDescription(methodDeclaration);
		EvaluationContext context = new EvaluationContext(projectClassLoader, flowDescription);
		//
		ExpressionStatement statement = (ExpressionStatement) statements.get(2);
		Object result = AstEvaluationEngine.evaluate(context, statement.getExpression());
		assertEquals(5, result);
	}

	/**
	 * Checks when array is initialized with values.
	 */
	@Test
	public void test_assignment_array1() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				class Test {
				  public int[] foo() {
				    int[] a = new int[] { 5 };
				    return a;
				  }
				}""");
		waitForAutoBuild();
		//
		int[] result = (int[]) evaluateSingleMethod(typeDeclaration, "foo()");
		assertArrayEquals(new int[] { 5 }, result);
	}

	/**
	 * Checks when array is initialized with values, but later updated.
	 */
	@Test
	public void test_assignment_array2() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				class Test {
				  public int[] foo() {
				    int[] a = new int[] { 1 };
				    a[0] = 5;
				    return a;
				  }
				}""");
		waitForAutoBuild();
		//
		int[] result = (int[]) evaluateSingleMethod(typeDeclaration, "foo()");
		assertArrayEquals(new int[] { 5 }, result);
	}

	/**
	 * Checks when array is initialized but values set later.
	 */
	@Test
	public void test_assignment_array3() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				class Test {
				  public int[] foo() {
				    int[] a = new int[1];
				    a[0] = 5;
				    return a;
				  }
				}""");
		waitForAutoBuild();
		//
		int[] result = (int[]) evaluateSingleMethod(typeDeclaration, "foo()");
		assertArrayEquals(new int[] { 5 }, result);
	}

	/**
	 * Checks when values are assigned multiple times, the last is used.
	 */
	@Test
	public void test_assignment_array4() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				class Test {
				  public int[] foo() {
				    int[] a = new int[1];
				    a[0] = 1;
				    a = new int[1];
				    a[0] = 5;
				    return a;
				  }
				}""");
		waitForAutoBuild();
		//
		int[] result = (int[]) evaluateSingleMethod(typeDeclaration, "foo()");
		assertArrayEquals(new int[] { 5 }, result);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ConditionalExpression
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_ConditionalExpression_1() throws Exception {
		assertEquals(1, evaluateExpression("true ? 1 : 2", "int"));
	}

	@Test
	public void test_ConditionalExpression_2() throws Exception {
		assertEquals(2, evaluateExpression("false? 1 : 2", "int"));
	}
}
