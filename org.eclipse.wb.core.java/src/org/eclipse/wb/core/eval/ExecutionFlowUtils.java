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
package org.eclipse.wb.core.eval;

import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getBinding;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getConstructor;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getCreationBinding;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getFullyQualifiedName;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getLocalConstructorDeclaration;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getLocalMethodDeclaration;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getMethodBinding;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getMethodSignature;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.getVariableName;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.hasJavaDocTag;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.isDanglingNode;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.isStatic;
import static org.eclipse.wb.internal.core.utils.ast.AstNodeUtils.isVariable;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.eval.ExecutionFlowProvider;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupportUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.exception.MultipleConstructorsError;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.TypeCache;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The utility class for handling execution flow in AST.
 *
 * We use it in any place where we need visit AST - during parsing, components creation, etc.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class ExecutionFlowUtils {
	static final String KEY_FRAME_INVOCATION = "ExecutionFlowUtils.frameInvocation";

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private ExecutionFlowUtils() {
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// ExecutionFlowFrameVisitor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * The extension of {@link ASTVisitor} for visiting {@link TypeDeclaration},
	 * {@link MethodDeclaration} and {@link Block}, i.e. AST nodes that form frames.
	 *
	 * @author scheglov_ke
	 */
	public static class ExecutionFlowFrameVisitor extends ASTVisitor {
		public Statement m_currentStatement;

		////////////////////////////////////////////////////////////////////////////
		//
		// Limitations
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public boolean visit(AnonymousClassDeclaration node) {
			return shouldVisitAnonymousClassDeclaration(node);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Frames
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * Enter into frame.<br>
		 * Frame is {@link TypeDeclaration}, {@link MethodDeclaration} or {@link Block}.
		 *
		 * @return <code>true</code> if we should enter into given frame.
		 */
		public boolean enterFrame(ASTNode node) {
			return true;
		}

		/**
		 * Leaves frame entered by {@link #enterFrame(ASTNode)}.
		 */
		public void leaveFrame(ASTNode node) {
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting context
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Global, "visiting level" context.
	 */
	public static class VisitingContext {
		boolean classInitialized;
		boolean instanceInitialized;
		boolean useBinaryFlow;
		final Set<MethodDeclaration> visitedMethods = new HashSet<>();

		public VisitingContext(boolean useBinaryFlow) {
			this.useBinaryFlow = useBinaryFlow;
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String WBP_PARSER_CONSTRUCTOR = "@wbp.parser.constructor";

	/**
	 * Tracks execution flow along given {@link ExecutionFlowDescription}.
	 *
	 * @param flowDescription
	 *          the {@link ExecutionFlowDescription} with starting methods and binary flow
	 *          information.
	 * @param useBinaryFlow
	 *          is <code>true</code> if we should use binary flow information from
	 *          {@link ExecutionFlowDescription}.
	 * @param visitor
	 *          the {@link ExecutionFlowFrameVisitor} to inform about tracking execution flow.
	 */
	public static void visit(VisitingContext context,
			ExecutionFlowDescription flowDescription,
			ExecutionFlowFrameVisitor visitor) {
		visit(context, flowDescription, visitor, flowDescription.getStartMethods());
	}

	/**
	 * Visit nodes starting from given methods.
	 *
	 * @param flowDescription
	 *          the {@link ExecutionFlowDescription} with starting methods and binary flow
	 *          information.
	 * @param useBinaryFlow
	 *          is <code>true</code> if we should use binary flow information from
	 *          {@link ExecutionFlowDescription}.
	 * @param visitor
	 *          the {@link ExecutionFlowFrameVisitor} to inform about tracking execution flow.
	 */
	public static void visit(VisitingContext context,
			ExecutionFlowDescription flowDescription,
			ExecutionFlowFrameVisitor visitor,
			List<MethodDeclaration> methodsToVisit) {
		TypeDeclaration typeDeclaration = AstNodeUtils.getParentType(methodsToVisit.get(0));
		if (!context.instanceInitialized) {
			visitor.enterFrame(typeDeclaration);
		}
		for (MethodDeclaration method : methodsToVisit) {
			// before any method (static or instance), visit class initialization code
			if (!context.classInitialized) {
				visitFields(visitor, typeDeclaration, true);
				visitInitializers(context, flowDescription, visitor, typeDeclaration, true);
				context.classInitialized = true;
			}
			// before any instance method, visit instance initialization code
			if (!context.instanceInitialized && !isStatic(method)) {
				visitFields(visitor, typeDeclaration, false);
				visitInitializers(context, flowDescription, visitor, typeDeclaration, false);
				context.instanceInitialized = true;
			}
			// OK, visit current method
			visit(context, flowDescription, visitor, method);
		}
	}

	/**
	 * Visits single {@link MethodDeclaration}.
	 */
	private static void visit(VisitingContext context,
			ExecutionFlowDescription flowDescription,
			ExecutionFlowFrameVisitor visitor,
			MethodDeclaration method) {
		if (visitor.enterFrame(method)) {
			// parameters
			for (SingleVariableDeclaration parameter : DomGenerics.parameters(method)) {
				parameter.accept(visitor);
			}
			// body
			Block body = method.getBody();
			if (body != null) {
				visitStatement(context, flowDescription, body, visitor);
			}
			// leave method
			visitor.leaveFrame(method);
		}
	}

	/**
	 * Visits static or instance {@link FieldDeclaration}'s.
	 */
	private static void visitFields(ExecutionFlowFrameVisitor visitor,
			TypeDeclaration typeDeclaration,
			boolean onlyStatic) {
		FieldDeclaration[] fields = typeDeclaration.getFields();
		for (FieldDeclaration fieldDeclaration : fields) {
			boolean isStatic = isStatic(fieldDeclaration);
			if (onlyStatic && isStatic) {
				fieldDeclaration.accept(visitor);
			}
			if (!onlyStatic && !isStatic) {
				fieldDeclaration.accept(visitor);
			}
		}
	}

	/**
	 * Visits static or instance {@link Initializer}'s.
	 */
	private static void visitInitializers(VisitingContext context,
			ExecutionFlowDescription flowDescription,
			ExecutionFlowFrameVisitor visitor,
			TypeDeclaration typeDeclaration,
			boolean onlyStatic) {
		List<BodyDeclaration> bodyDeclarations = DomGenerics.bodyDeclarations(typeDeclaration);
		for (BodyDeclaration bodyDeclaration : bodyDeclarations) {
			boolean isStatic = isStatic(bodyDeclaration);
			if (bodyDeclaration instanceof Initializer initializer && context != null) {
				if (onlyStatic && isStatic) {
					visitStatement(context, flowDescription, initializer.getBody(), visitor);
				}
				if (!onlyStatic && !isStatic) {
					visitStatement(context, flowDescription, initializer.getBody(), visitor);
				}
			}
		}
	}

	/**
	 * Visit {@link ASTNode}'s "unconditionally accessible" starting from given {@link Statement}.
	 */
	private static void visitStatement(VisitingContext context,
			ExecutionFlowDescription flowDescription,
			Statement statement,
			ExecutionFlowFrameVisitor visitor) {
		// check if we already visited MethodDeclaration
		if (statement.getLocationInParent() == MethodDeclaration.BODY_PROPERTY) {
			MethodDeclaration methodDeclaration = (MethodDeclaration) statement.getParent();
			if (context.visitedMethods.contains(methodDeclaration)) {
				return;
			}
			context.visitedMethods.add(methodDeclaration);
		}
		// visit "binary flow" methods that should be visited BEFORE current Statement
		visitBinaryFlowMethods(true, context, flowDescription, statement, visitor);
		// visit current Statement
		flowDescription.enterStatement(statement);
		try {
			visitStatement0(context, flowDescription, statement, visitor);
		} finally {
			flowDescription.leaveStatement(statement);
		}
		// visit "binary flow" methods that should be visited AFTER current Statement
		visitBinaryFlowMethods(false, context, flowDescription, statement, visitor);
	}

	/**
	 * Visit {@link ASTNode}'s "unconditionally accessible" starting from given {@link Statement}.
	 */
	private static void visitStatement0(VisitingContext context,
			ExecutionFlowDescription flowDescription,
			Statement statement,
			ExecutionFlowFrameVisitor visitor) {
		visitor.m_currentStatement = statement;
		if (statement instanceof Block block) {
			if (visitor.enterFrame(block)) {
				//
				for (Statement childStatement : DomGenerics.statements(block)) {
					visitStatement(context, flowDescription, childStatement, visitor);
				}
				//
				visitor.leaveFrame(block);
			}
		} else if (statement instanceof TryStatement tryStatement) {
			visitStatement(context, flowDescription, tryStatement.getBody(), visitor);
		} else if (statement instanceof IfStatement ifStatement) {
			if (shouldVisit_IfStatement_Then(ifStatement)) {
				visitStatement(context, flowDescription, ifStatement.getThenStatement(), visitor);
			} else if (ifStatement.getElseStatement() != null
					&& shouldVisit_IfStatement_Else(ifStatement)) {
				visitStatement(context, flowDescription, ifStatement.getElseStatement(), visitor);
			}
		} else if (shouldVisitStatement(statement)) {
			ASTVisitor complexVisitor = getInterceptingVisitor(context, flowDescription, visitor);
			statement.accept(complexVisitor);
		}
	}

	/**
	 * Local storage for the enhanced {@link ASTVisitor} class. Classes are loaded
	 * with the {@link ExecutionFlowUtils} class-loader, meaning there should only
	 * exists a single instance of this class at a time. All instances are should be
	 * created from this class. We have to avoid creating a separate class for each
	 * enhanced visitor, as the {@link ClassLoader} is never discarded and thus, the
	 * classes stay in memory indefinitely. If left unchecked, this may lead to an
	 * {@link OutOfMemoryError}.
	 */
	private static final TypeCache<TypeCache.SimpleKey> PROXY_CACHE = new TypeCache.WithInlineExpunction<>(
			TypeCache.Sort.WEAK);

	private static ASTVisitor getInterceptingVisitor(final VisitingContext context,
			final ExecutionFlowDescription flowDescription, final ExecutionFlowFrameVisitor visitor) {

		DynamicType.Builder<VisitorStub> builder = new ByteBuddy() //
				.subclass(VisitorStub.class) //
				.method(ElementMatchers.any()) //
				.intercept(InvocationHandlerAdapter.of(new ExecutionFlowHandler()));

		ClassLoader proxyClassLoader = ExecutionFlowUtils.class.getClassLoader();
		TypeCache.SimpleKey proxyKey = new TypeCache.SimpleKey(VisitorStub.class);

		try {
			return (ASTVisitor) PROXY_CACHE
					.findOrInsert(proxyClassLoader, proxyKey, () -> builder.make().load(proxyClassLoader).getLoaded())
					.getConstructor(VisitingContext.class, ExecutionFlowDescription.class, ExecutionFlowFrameVisitor.class)
					.newInstance(context, flowDescription, visitor);
		} catch (ReflectiveOperationException e) {
			throw new DesignerException(ICoreExceptionConstants.EVAL_BYTEBUDDY, e);
		}
	}

	/**
	 * In general we should not visit anonymous classes, they are usually event handlers. However
	 * there are special cases, such as <code>EventQueue.invokeLater(Runnable)</code> in Swing.
	 */
	private static boolean shouldVisitAnonymousClassDeclaration(final AnonymousClassDeclaration anonymous) {
		return ExecutionUtils.runObjectLog(() -> {
			for (ExecutionFlowProvider provider : getExecutionFlowProviders()) {
				if (provider.shouldVisit(anonymous)) {
					return true;
				}
			}
			return false;
		}, false);
	}

	/**
	 * @return all {@link ExecutionFlowProvider}'s.
	 */
	private static List<ExecutionFlowProvider> getExecutionFlowProviders() {
		return ExternalFactoriesHelper.getElementsInstances(
				ExecutionFlowProvider.class,
				"org.eclipse.wb.core.java.executionFlowProviders",
				"provider");
	}

	/**
	 * Visits {@link MethodDeclaration} that should be visited as part of binary execution flow for
	 * given {@link Statement}.
	 *
	 * @param beforeStatement
	 *          is <code>true</code> if we should visit {@link MethodDeclaration}'s executed before
	 *          {@link Statement}, and <code>false</code>, if {@link MethodDeclaration}'s are visited
	 *          after (in reality - during) executing given {@link Statement}. Currently
	 *          <code>true</code> is used only for {@link MethodDeclaration}'s executed during
	 *          {@link SuperConstructorInvocation}.
	 */
	private static void visitBinaryFlowMethods(boolean beforeStatement,
			VisitingContext context,
			ExecutionFlowDescription flowDescription,
			Statement statement,
			ExecutionFlowFrameVisitor visitor) {
		if (context.useBinaryFlow) {
			List<MethodDeclaration> binaryFlowMethods =
					beforeStatement
					? flowDescription.getBinaryFlowMethodsBefore(statement)
							: flowDescription.getBinaryFlowMethodsAfter(statement);
			if (binaryFlowMethods != null) {
				for (MethodDeclaration method : binaryFlowMethods) {
					visit(context, flowDescription, visitor, method);
				}
			}
		}
	}

	/**
	 * @return <code>true</code> if given {@link Statement} should be visited.
	 */
	private static boolean shouldVisitStatement(Statement statement) {
		// usual statements
		if (statement instanceof ExpressionStatement
				|| statement instanceof VariableDeclarationStatement
				|| statement instanceof ConstructorInvocation
				|| statement instanceof SuperConstructorInvocation
				|| statement instanceof ReturnStatement) {
			return true;
		}
		// not supported statement
		return false;
	}

	/**
	 * @return <code>true</code> if given "then" statement of {@link IfStatement} should be visited.
	 */
	private static boolean shouldVisit_IfStatement_Then(IfStatement ifStatement) {
		Expression expression = ifStatement.getExpression();
		// if (true) {}
		if (expression instanceof BooleanLiteral literal) {
			return literal.booleanValue();
		}
		// if (Beans.isDesignTime()) {}
		if (AstNodeUtils.isMethodInvocation(expression, "isDesignTime()")) {
			return true;
		}
		// special case: lazy creation
		{
			Block block = (Block) ifStatement.getParent();
			if (block.getParent() instanceof MethodDeclaration) {
				MethodDeclaration method = (MethodDeclaration) block.getParent();
				if (LazyVariableSupportUtils.getInformation(method) != null) {
					return true;
				}
			}
		}
		// no
		return false;
	}

	/**
	 * @return <code>true</code> if given "else" statement of {@link IfStatement} should be visited.
	 */
	private static boolean shouldVisit_IfStatement_Else(IfStatement ifStatement) {
		Expression expression = ifStatement.getExpression();
		// if (false) {}
		if (expression instanceof BooleanLiteral literal) {
			return !literal.booleanValue();
		}
		// if (!Beans.isDesignTime()) {}
		if (expression instanceof PrefixExpression prefixExpression) {
			if (prefixExpression.getOperator() == PrefixExpression.Operator.NOT
					&& AstNodeUtils.isMethodInvocation(prefixExpression.getOperand(), "isDesignTime()")) {
				return true;
			}
		}
		// no
		return false;
	}

	/**
	 * @return single constructor that should be used by Designer. This constructor should be single
	 *         or marked with {@link #WBP_PARSER_CONSTRUCTOR} comment. If multiple constructors found
	 *         and none is marked, then exception will be thrown.
	 */
	public static MethodDeclaration getExecutionFlowConstructor(TypeDeclaration typeDeclaration) {
		// build list of constructors
		List<MethodDeclaration> constructors = new ArrayList<>();
		for (MethodDeclaration methodDeclaration : typeDeclaration.getMethods()) {
			if (methodDeclaration.isConstructor()) {
				constructors.add(methodDeclaration);
			}
		}
		// no constructors
		if (constructors.isEmpty()) {
			return null;
		}
		// check for good case - single constructor
		if (constructors.size() == 1) {
			return constructors.get(0);
		}
		// several constructors
		for (MethodDeclaration constructor : constructors) {
			if (hasJavaDocTag(constructor, WBP_PARSER_CONSTRUCTOR)) {
				return constructor;
			}
		}
		// try to find default constructor, use extensions
		for (ExecutionFlowProvider provider : getExecutionFlowProviders()) {
			MethodDeclaration constructor = provider.getDefaultConstructor(typeDeclaration);
			if (constructor != null) {
				return constructor;
			}
		}
		// we can not select correct constructor
		throw new MultipleConstructorsError();
	}

	/**
	 * @return the {@link MethodDeclaration} marked with @wbp.parse.entryPoint tag, may be
	 *         <code>null</code>.
	 */
	public static MethodDeclaration getExecutionFlow_entryPoint(TypeDeclaration typeDeclaration) {
		for (MethodDeclaration method : typeDeclaration.getMethods()) {
			if (hasJavaDocTag(method, "@wbp.parser.entryPoint")) {
				return method;
			}
		}
		return null;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Assignment
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String KEY_LAST_VARIABLE_STAMP = "KEY_LAST_VARIABLE_STAMP";
	private static final String KEY_DECLARATION = "KEY_DECLARATION";
	private static final String KEY_REFERENCES = "KEY_REFERENCES";
	private static final String KEY_ASSIGNMENTS = "KEY_ASSIGNMENTS";
	private static final String KEY_LAST_ASSIGNMENT = "KEY_LAST_ASSIGNMENT";
	private static final String KEY_LAST_DECLARATION_ASSIGNMENT = "KEY_LAST_DECLARATION_ASSIGNMENT";

	/**
	 * @return <code>true</code> if given variable has cache stamp (there was related bug, so we test
	 *         for this now).
	 */
	public static boolean hasVariableStamp(ASTNode variable) {
		return variable.getProperty(KEY_LAST_VARIABLE_STAMP) != null;
	}

	/**
	 * @return the {@link VariableDeclaration} for given variable.
	 */
	public static VariableDeclaration getDeclaration(ExecutionFlowDescription flowDescription,
			ASTNode variable) {
		if (EnvironmentUtils.isTestingTime()) {
			Assert.isTrue(isVariable(variable));
		}
		return (VariableDeclaration) getVariableCachedValue(flowDescription, variable, KEY_DECLARATION);
	}

	/**
	 * @return the {@link List} of {@link Expression}'s that reference same variable as given.
	 */
	public static List<Expression> getReferences(ExecutionFlowDescription flowDescription,
			ASTNode variable) {
		if (EnvironmentUtils.isTestingTime()) {
			Assert.isTrue(isVariable(variable));
		}
		return getVariableCachedList_notNull(flowDescription, variable, KEY_REFERENCES);
	}

	/**
	 * @return the {@link List} of {@link Expression} where values are assignment to the given
	 *         variable.
	 */
	public static List<Expression> getAssignments(ExecutionFlowDescription flowDescription,
			ASTNode variable) {
		if (EnvironmentUtils.isTestingTime()) {
			Assert.isTrue(isVariable(variable));
		}
		return getVariableCachedList_notNull(flowDescription, variable, KEY_ASSIGNMENTS);
	}

	/**
	 * @return {@link Assignment} or {@link VariableDeclarationFragment} where value was assignment
	 *         for given variable; or <code>null</code> if no assignment found.
	 *
	 * @param flowDescription
	 *          the {@link ExecutionFlowDescription} from which we should start searching.
	 */
	public static ASTNode getLastAssignment(ExecutionFlowDescription flowDescription, ASTNode variable) {
		if (EnvironmentUtils.isTestingTime()) {
			Assert.isTrue(isVariable(variable));
		}
		return (ASTNode) getVariableCachedValue(flowDescription, variable, KEY_LAST_ASSIGNMENT);
	}

	/**
	 * @return the {@link List} result from
	 *         {@link #getVariableCachedValue(ExecutionFlowDescription, ASTNode, String)}, ensures
	 *         that if no real {@link List}, empty {@link List} is returned.
	 */
	@SuppressWarnings("unchecked")
	private static <T> List<T> getVariableCachedList_notNull(ExecutionFlowDescription flowDescription,
			ASTNode variable,
			String key) {
		List<T> result = (List<T>) getVariableCachedValue(flowDescription, variable, key);
		if (result == null) {
			return Collections.emptyList();
		} else {
			return result;
		}
	}

	/**
	 * @return the final {@link Expression} for given one. This method will traverse
	 *         {@link SimpleName}'s until last assignment of "real" {@link Expression} will be found.
	 */
	public static Expression getFinalExpression(ExecutionFlowDescription flowDescription,
			Expression expression) {
		// traverse assignments
		while (expression instanceof SimpleName) {
			ASTNode assignment = getLastAssignment(flowDescription, expression);
			if (assignment instanceof Assignment) {
				expression = ((Assignment) assignment).getRightHandSide();
			} else if (assignment instanceof VariableDeclarationFragment) {
				expression = ((VariableDeclarationFragment) assignment).getInitializer();
			} else {
				break;
			}
		}
		// return final expression
		return expression;
	}

	/**
	 * @return the cached value for given variable.
	 */
	private static Object getVariableCachedValue(ExecutionFlowDescription flowDescription,
			ASTNode variable,
			String key) {
		if (clearCachedValuesForDanglingNode(variable)) {
			return null;
		}
		// check for cached value
		{
			Long lastStamp = (Long) variable.getProperty(KEY_LAST_VARIABLE_STAMP);
			if (lastStamp != null && lastStamp.longValue() == variable.getAST().modificationCount()) {
				return variable.getProperty(key);
			}
		}
		// well, we don't have cached value, so prepare
		prepareAssignmentInformation(flowDescription);
		// return newly created result
		return variable.getProperty(key);
	}

	/**
	 * When variable {@link ASTNode} is dangling, we will not able to visit it on execution flow, so
	 * have to clear cached information manually.
	 *
	 * @return <code>true</code> if node is dangling, so no need to visit execution flow.
	 */
	private static boolean clearCachedValuesForDanglingNode(ASTNode variable) {
		if (isDanglingNode(variable)) {
			for (Field field : ExecutionFlowUtils.class.getDeclaredFields()) {
				String fieldName = field.getName();
				if (fieldName.startsWith("KEY_")) {
					variable.setProperty(fieldName, null);
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Tracks assignments on execution flow starting from given {@link ExecutionFlowDescription} and
	 * remember results in {@link ASTNode} properties.
	 */
	private static void prepareAssignmentInformation(ExecutionFlowDescription flowDescription) {
		final Long assignmentStamp = flowDescription.getAST().modificationCount();
		// visit execution flow, find declarations/assignments for all variables
		visit(new VisitingContext(true), flowDescription, new AbstractVariablesExecutionFlowVisitor(
				true) {
			@Override
			public void endVisit(Assignment node) {
				Expression leftSide = node.getLeftHandSide();
				if (isVariable(leftSide)) {
					Expression variable = leftSide;
					executionFlowContext.addAssignment(variable, node);
					executionFlowContext.storeAssignments(variable);
				}
			}

			////////////////////////////////////////////////////////////////////////////
			//
			// Generic ASTNode pre/post visiting
			//
			////////////////////////////////////////////////////////////////////////////
			@Override
			public void postVisit(ASTNode node) {
				// store assignment for variable usage
				if (node instanceof Expression variable && isVariable(node)) {
					variable.setProperty(KEY_LAST_VARIABLE_STAMP, assignmentStamp);
					executionFlowContext.storeAssignments(variable);
				}
			}
		});
		// visit CompilationUnit, find references for all variables
		flowDescription.getCompilationUnit().accept(new AbstractVariablesExecutionFlowVisitor(false) {
			@Override
			public boolean visit(AnonymousClassDeclaration node) {
				return true;
			}

			@Override
			public void preVisit(ASTNode node) {
				super.preVisit(node);
				if (isFrameNode(node)) {
					enterFrame(node);
					// visit all fields on enter TypeDeclaration
					if (node instanceof TypeDeclaration typeDeclaration) {
						visitFields(this, typeDeclaration, true);
						visitFields(this, typeDeclaration, false);
					}
				}
			}

			@Override
			public void postVisit(ASTNode node) {
				super.postVisit(node);
				if (isFrameNode(node)) {
					leaveFrame(node);
				}
				if (node instanceof Expression) {
					if (isVariable(node)) {
						executionFlowContext.storeReferences((Expression) node);
					}
					// special support for "instanceOfTopType.field"
					if (node instanceof QualifiedName qualifiedName) {
						CompilationUnit unit = (CompilationUnit) qualifiedName.getRoot();
						TypeDeclaration topType = (TypeDeclaration) unit.types().get(0);
						if (qualifiedName.getQualifier().resolveTypeBinding() == topType.resolveBinding()) {
							executionFlowContext.storeReferences(qualifiedName.getName());
						}
					}
				}
			}

			private boolean isFrameNode(ASTNode node) {
				return node instanceof TypeDeclaration
						|| node instanceof MethodDeclaration
						|| node instanceof Block;
			}
		});
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractVariablesExecutionFlowVisitor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Abstract visitor that tracks frames and variables.
	 *
	 * @author scheglov_ke
	 */
	private static abstract class AbstractVariablesExecutionFlowVisitor
	extends
	ExecutionFlowFrameVisitor {
		protected final ExecutionFlowContext executionFlowContext;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public AbstractVariablesExecutionFlowVisitor(boolean forExecutionFlow) {
			executionFlowContext = new ExecutionFlowContext(forExecutionFlow);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Frames tracking
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public boolean enterFrame(ASTNode node) {
			executionFlowContext.enterFrame(node);
			if (node instanceof MethodDeclaration methodDeclaration) {
				for (SingleVariableDeclaration parameter : DomGenerics.parameters(methodDeclaration)) {
					executionFlowContext.define(parameter);
				}
			}
			return true;
		}

		@Override
		public void leaveFrame(ASTNode node) {
			executionFlowContext.leaveFrame();
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Variables tracking
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public void preVisit(ASTNode node) {
			// "pre" visit because we should visit declaration before SimpleName
			if (node instanceof VariableDeclaration variableDeclaration) {
				executionFlowContext.define(variableDeclaration);
			}
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Execution frames support
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Holder of information about execution - frames and variables.
	 *
	 * @author scheglov_ke
	 */
	private static final class ExecutionFlowContext {
		private final boolean m_forExecutionFlow;
		private final LinkedList<ExecutionFlowFrame> m_stack = new LinkedList<>();
		private ExecutionFlowFrame m_currentFrame;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public ExecutionFlowContext(boolean forExecutionFlow) {
			m_forExecutionFlow = forExecutionFlow;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Frame operation
		//
		////////////////////////////////////////////////////////////////////////////
		private MethodDeclaration getNewFrameMethod(ASTNode node) {
			if (node instanceof MethodDeclaration) {
				return (MethodDeclaration) node;
			}
			if (m_currentFrame != null) {
				return m_currentFrame.getMethod();
			}
			return null;
		}

		/**
		 * Creates new frame, all subsequent variable declarations will go into this frame.
		 */
		public void enterFrame(ASTNode node) {
			m_currentFrame =
					new ExecutionFlowFrame(getNewFrameMethod(node),
							m_forExecutionFlow,
							node instanceof TypeDeclaration);
			m_stack.addFirst(m_currentFrame);
		}

		/**
		 * Removes current frame and all its variable declarations.
		 */
		public void leaveFrame() {
			Assert.isTrue(m_stack.removeFirst() == m_currentFrame);
			m_currentFrame = m_stack.peek();
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Variables
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * Adds given variable as defined in current frame.
		 */
		public void define(VariableDeclaration variableDeclaration) {
			ExecutionFlowFrame frame = getFrameForDeclaration(variableDeclaration);
			frame.define(variableDeclaration);
		}

		private ExecutionFlowFrame getFrameForDeclaration(VariableDeclaration variableDeclaration) {
			// we may visit "type" frames too often (this is bad)
			// so, declare fields in first "type" frame
			if (variableDeclaration.getLocationInParent() == FieldDeclaration.FRAGMENTS_PROPERTY) {
				for (int i = m_stack.size() - 1; i >= 0; i--) {
					ExecutionFlowFrame frame = m_stack.get(i);
					if (frame.m_forTypeDeclaration) {
						return frame;
					}
				}
			}
			// local variables
			return m_currentFrame;
		}

		/**
		 * Notifies that given variable was assigned value using given {@link Assignment}.
		 */
		public void addAssignment(Expression variable, Assignment assignment) {
			ExecutionFlowFrame definingFrame = getDefiningFrame(variable);
			if (definingFrame != null) {
				definingFrame.addAssignment(variable, assignment);
			}
		}

		/**
		 * Stores declaration/assignments of given variable into properties.
		 */
		public void storeAssignments(Expression node) {
			ExecutionFlowFrame definingFrame = getDefiningFrame(node);
			if (definingFrame != null) {
				VariableDeclaration declaration = definingFrame.getDeclaration(node);
				node.setProperty(KEY_DECLARATION, declaration);
				node.setProperty(KEY_LAST_ASSIGNMENT, definingFrame.getLastAssignment(node));
				node.setProperty(KEY_ASSIGNMENTS, definingFrame.getAssignments(node));
			}
		}

		/**
		 * Stores references on given variable into properties.
		 */
		public void storeReferences(Expression variable) {
			ExecutionFlowFrame definingFrame = getDefiningFrame(variable);
			if (definingFrame != null) {
				variable.setProperty(KEY_REFERENCES, definingFrame.getReferences(variable));
				// if no "real last assignment", i.e. we are in ASTNode disconnected from execution flow,
				// use "last assignment" from declaration
				if (variable.getProperty(KEY_LAST_ASSIGNMENT) == null) {
					Assert.isTrue(!m_forExecutionFlow);
					VariableDeclaration declaration = definingFrame.getDeclaration(variable);
					variable.setProperty(
							KEY_LAST_ASSIGNMENT,
							declaration.getProperty(KEY_LAST_DECLARATION_ASSIGNMENT));
				}
			}
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Utils
		//
		////////////////////////////////////////////////////////////////////////////
		private ExecutionFlowFrame getDefiningFrame(Expression variable) {
			for (ExecutionFlowFrame frame : m_stack) {
				if (frame.defines(variable)) {
					return frame;
				}
			}
			return null;
		}
	}
	/**
	 * Holder of variables, declared in single frame: {@link TypeDeclaration},
	 * {@link MethodDeclaration} or {@link Block}.
	 *
	 * @author scheglov_ke
	 */
	private static final class ExecutionFlowFrame {
		private final MethodDeclaration m_method;
		private final boolean m_forExecutionFlow;
		private final boolean m_forTypeDeclaration;
		private final Map<String, VariableDeclaration> m_variableToDeclaration = new HashMap<>();
		private final Map<String, ASTNode> m_assignments = new HashMap<>();
		private final Map<String, Expression> m_assignmentsVariables = new HashMap<>();
		private final Map<String, ASTNode> m_variableToLastAssignment = new HashMap<>();
		private final Map<String, List<ASTNode>> m_variableToAssignments = new HashMap<>();
		private final Map<String, List<Expression>> m_variableToReferences = new HashMap<>();

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * @param forExecutionFlow
		 *          is <code>true</code> if this {@link ExecutionFlowFrame} is created as part of
		 *          visiting execution flow, so for example last assignment for
		 *          {@link VariableDeclaration} should be tracked. If <code>false</code>, then we just
		 *          visit {@link CompilationUnit} using {@link ASTVisitor}, and should not touch/corrupt
		 *          last assignment in {@link VariableDeclaration}.
		 * @param forTypeDeclaration
		 *          is <code>true</code> if this {@link ExecutionFlowFrame} is created for
		 *          {@link TypeDeclaration}.
		 */
		public ExecutionFlowFrame(MethodDeclaration method,
				boolean forExecutionFlow,
				boolean forTypeDeclaration) {
			m_method = method;
			m_forExecutionFlow = forExecutionFlow;
			m_forTypeDeclaration = forTypeDeclaration;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Variables tracking
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * Checks that given variable is defined in this frame.
		 */
		public boolean defines(Expression variable) {
			if (variable instanceof FieldAccess && !m_forTypeDeclaration) {
				return false;
			}
			return getDeclaration(variable) != null;
		}

		/**
		 * Adds new variable declared in this frame using given {@link VariableDeclaration}.
		 */
		public void define(VariableDeclaration declaration) {
			SimpleName variable = declaration.getName();
			String variableName = variable.getIdentifier();
			m_variableToDeclaration.put(variableName, declaration);
			// in any case remember this node as assignment
			addAssignment(variable, declaration);
		}

		/**
		 * Adds new assignment (or declaration) node for variable owned by this frame.
		 */
		public void addAssignment(Expression variable, ASTNode node) {
			String variableName = getVariableName(variable);
			// add last assignment (can be just declaration)
			m_variableToLastAssignment.put(variableName, node);
			// add assignment (real, with initializer)
			{
				// prepare initializer
				Expression initializer = null;
				if (node instanceof VariableDeclaration declaration) {
					initializer = declaration.getInitializer();
				} else if (node instanceof Assignment assignment) {
					initializer = assignment.getRightHandSide();
				}
				// add assignment to the list
				if (initializer != null) {
					getAssignments(variable).add(node);
					getAssignments().put(variableName, node);
					getAssignmentsVariables().put(variableName, variable);
				}
			}
			// remember last assignment (on execution flow) for declaration
			if (m_forExecutionFlow) {
				VariableDeclaration declaration = m_variableToDeclaration.get(variableName);
				declaration.setProperty(KEY_LAST_DECLARATION_ASSIGNMENT, node);
			}
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Results access
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * @return the {@link MethodDeclaration} to which belongs this frame.
		 */
		public MethodDeclaration getMethod() {
			return m_method;
		}

		/**
		 * @return the {@link VariableDeclaration} for given variable or <code>null</code>.
		 */
		public VariableDeclaration getDeclaration(Expression variable) {
			String variableName = getVariableName(variable);
			return m_variableToDeclaration.get(variableName);
		}

		/**
		 * @return the last assignment for given variable or <code>null</code>.
		 */
		public ASTNode getLastAssignment(Expression variable) {
			String variableName = getVariableName(variable);
			return m_variableToLastAssignment.get(variableName);
		}

		/**
		 * @return the {@link List} of assignments for given variable.
		 */
		public List<ASTNode> getAssignments(Expression variable) {
			String variableName = getVariableName(variable);
			List<ASTNode> assignments = m_variableToAssignments.get(variableName);
			if (assignments == null) {
				assignments = new ArrayList<>();
				m_variableToAssignments.put(variableName, assignments);
			}
			return assignments;
		}

		/**
		 * @return the {@link Map} of all assignments in this {@link ExecutionFlowFrame}.
		 */
		public Map<String, ASTNode> getAssignments() {
			return m_assignments;
		}

		/**
		 * @return the {@link Map} of all assignments in this {@link ExecutionFlowFrame}.
		 */
		public Map<String, Expression> getAssignmentsVariables() {
			return m_assignmentsVariables;
		}

		/**
		 * @return the {@link List} of {@link Expression}'s for same given variable.
		 */
		public List<Expression> getReferences(Expression variable) {
			// find list of references
			List<Expression> references;
			{
				String variableName = getVariableName(variable);
				references = m_variableToReferences.get(variableName);
				if (references == null) {
					references = new ArrayList<>();
					m_variableToReferences.put(variableName, references);
				}
			}
			// add given reference
			if (!references.contains(variable)) {
				references.add(variable);
			}
			// return final list
			return references;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link List} of invocations ( {@link ConstructorInvocation}, {@link MethodInvocation}
	 *         or {@link ClassInstanceCreation} ) of given method on execution flow.
	 */
	public static List<ASTNode> getInvocations(ExecutionFlowDescription flowDescription,
			MethodDeclaration methodDeclaration) {
		final List<ASTNode> invocations = new ArrayList<>();
		// prepare required values
		IMethodBinding requiredBinding = getMethodBinding(methodDeclaration);
		if (requiredBinding == null) {
			return invocations;
		}
		final String requiredType = getFullyQualifiedName(requiredBinding.getDeclaringClass(), false);
		final String requiredSignature = getMethodSignature(methodDeclaration);
		// visit execution flow
		ExecutionFlowUtils.visit(
				new VisitingContext(true),
				flowDescription,
				new ExecutionFlowFrameVisitor() {
					@Override
					public void endVisit(MethodInvocation node) {
						IMethodBinding binding = getMethodBinding(node);
						addInvocation(node, binding);
					}

					@Override
					public void endVisit(ClassInstanceCreation node) {
						IMethodBinding binding = getCreationBinding(node);
						addInvocation(node, binding);
					}

					@Override
					public void endVisit(ConstructorInvocation node) {
						IMethodBinding binding = getBinding(node);
						addInvocation(node, binding);
					}

					private void addInvocation(ASTNode invocation, IMethodBinding binding) {
						if (binding != null) {
							String signature = getMethodSignature(binding);
							String type = getFullyQualifiedName(binding.getDeclaringClass(), false);
							if (signature.equals(requiredSignature) && type.equals(requiredType)) {
								invocations.add(invocation);
							}
						}
					}
				});
		//
		return invocations;
	}

	/**
	 * <p>
	 * Custom subclass of {@link ASTVisitor}, enhanced by {@link ByteBuddy}.
	 * </p>
	 * <p>
	 * This class only exists for convenience, in order to access the fields within
	 * an {@link ExecutionFlowHandler}. In principle, it is also possible to define
	 * those fields directly in {@link ByteBuddy}. However, then those fields have
	 * to both be set and read via reflection.
	 * </p>
	 *
	 * @see ExecutionFlowHandler
	 */
	public static class VisitorStub extends ASTVisitor {
		private final VisitingContext context;
		private final ExecutionFlowDescription flowDescription;
		private final ExecutionFlowFrameVisitor visitor;

		public VisitorStub(VisitingContext context, ExecutionFlowDescription flowDescription,
				ExecutionFlowFrameVisitor visitor) {
			this.context = context;
			this.flowDescription = flowDescription;
			this.visitor = visitor;
		}
	}

	/**
	 * <p>
	 * Custom invocation handler used by {@link ByteBuddy} on an enhanced
	 * {@link ASTVisitor}.
	 * </p>
	 * <p>
	 * All method invocations of the enhanced base class are delegated to the main
	 * visitor. This visitor is stored in an internal field of the enhanced object.
	 * Special code is executed for AST nodes of type
	 * {@link AnonymousClassDeclaration}, {@link ClassInstanceCreation},
	 * {@link MethodInvocation} and {@link ConstructorInvocation}.
	 * </p>
	 * <p>
	 * This handler may <b>only</b> be used for objects of type {@link VisitorStub}.
	 * </p>
	 *
	 * @see VisitorStub
	 */
	private static class ExecutionFlowHandler implements InvocationHandler {
		@Override
		public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
			VisitorStub stub = (VisitorStub) obj;
			// routing
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length == 1) {
				Class<?> parameterType = parameterTypes[0];
				if (method.getName().equals("visit")) {
					if (parameterType == AnonymousClassDeclaration.class) {
						visit(stub, (AnonymousClassDeclaration) args[0]);
					}
				} else if (method.getName().equals("endVisit")) {
					if (parameterType == ClassInstanceCreation.class) {
						endVisit(stub, (ClassInstanceCreation) args[0]);
					} else if (parameterType == MethodInvocation.class) {
						endVisit(stub, (MethodInvocation) args[0]);
					} else if (parameterType == ConstructorInvocation.class) {
						endVisit(stub, (ConstructorInvocation) args[0]);
					}
				}
			}
			// use main visitor
			try {
				return method.invoke(stub.visitor, args);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}

		private boolean visit(VisitorStub stub, AnonymousClassDeclaration node) {
			return shouldVisitAnonymousClassDeclaration(node);
		}

		private void endVisit(VisitorStub stub, ClassInstanceCreation node) {
			// quick check
			{
				String identifier = stub.flowDescription.geTypeDeclaration().getName().getIdentifier();
				if (!node.toString().contains(identifier)) {
					return;
				}
			}
			// check for local constructor
			MethodDeclaration methodDeclaration = getLocalConstructorDeclaration(node);
			if (methodDeclaration != null) {
				// redirect execution flow to constructor
				ExecutionFlowUtils.visit(stub.context, stub.flowDescription, stub.visitor, List.of(methodDeclaration));
			}
		}

		private void endVisit(VisitorStub stub, MethodInvocation node) {
			// check for local method invocation
			MethodDeclaration methodDeclaration = getLocalMethodDeclaration(node);
			if (methodDeclaration != null) {
				methodDeclaration.setProperty(KEY_FRAME_INVOCATION, node);
				// check for qualified local invocation, for example "appl.open()", so visit it
				// as type
				if (node.getExpression() != null && !(node.getExpression() instanceof ThisExpression)) {
					ExecutionFlowUtils.visit(stub.context, stub.flowDescription, stub.visitor, List.of(methodDeclaration));
				} else {
					ExecutionFlowUtils.visit(stub.context, stub.flowDescription, stub.visitor, methodDeclaration);
				}
			}
		}

		private void endVisit(VisitorStub stub, ConstructorInvocation node) {
			MethodDeclaration constructor = getConstructor(node);
			constructor.setProperty(KEY_FRAME_INVOCATION, node);
			ExecutionFlowUtils.visit(stub.context, stub.flowDescription, stub.visitor, List.of(constructor));
		}
	}
}
