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
package org.eclipse.wb.tests.designer.core.util.execution;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.DesignPageSite;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.tests.designer.core.model.TestObjectInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.beans.Beans;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests for {@link ExecutionUtils} and {@link RunnableEx}.
 *
 * @author scheglov_ke
 */
public class ExecutionUtilsTest extends SwingModelTest {
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
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ExecutionUtils#sleep(int)}.
	 */
	@Test
	public void test_sleep() throws Exception {
		long begin = System.currentTimeMillis();
		ExecutionUtils.sleep(60);
		long end = System.currentTimeMillis();
		Assertions.assertThat(end - begin).isGreaterThanOrEqualTo(60);
	}

	/**
	 * Test for {@link ExecutionUtils#waitEventLoop(int)}.
	 */
	@Test
	public void test_waitEventLoop() throws Exception {
		final AtomicBoolean executed = new AtomicBoolean();
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				executed.set(true);
			}
		});
		// not executed yet
		assertEquals(false, executed.get());
		// wait
		long begin = System.nanoTime();
		ExecutionUtils.waitEventLoop(60);
		long end = System.nanoTime();
		Assertions.assertThat(end - begin).isGreaterThanOrEqualTo(60 * 1000000);
		// executed
		assertEquals(true, executed.get());
	}

	/**
	 * Test for {@link ExecutionUtils#waitEventLoop(int)}.
	 */
	@Test
	public void test_waitEventLoop_notThreadSWT() throws Exception {
		long begin = System.nanoTime();
		{
			Thread thread = new Thread() {
				@Override
				public void run() {
					ExecutionUtils.waitEventLoop(60);
				}
			};
			thread.start();
			thread.join();
		}
		long end = System.nanoTime();
		Assertions.assertThat(end - begin).isGreaterThanOrEqualTo(50 * 1000000);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// void, ignore
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_void_ignore_noException() throws Exception {
		boolean success = ExecutionUtils.runIgnore(new RunnableEx() {
			@Override
			public void run() throws Exception {
			}
		});
		assertTrue(success);
	}

	@Test
	public void test_void_ignore_withException() throws Exception {
		boolean success = ExecutionUtils.runIgnore(new RunnableEx() {
			@Override
			public void run() throws Exception {
				throw new Exception();
			}
		});
		assertFalse(success);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// void, log
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_void_log_noException() throws Exception {
		boolean success = ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
			}
		});
		assertTrue(success);
	}

	@Test
	public void test_void_log_withException() throws Exception {
		final Exception exception = new Exception();
		ILog log = DesignerPlugin.getDefault().getLog();
		ILogListener logListener = new ILogListener() {
			@Override
			public void logging(IStatus status, String plugin) {
				assertEquals(IStatus.ERROR, status.getSeverity());
				assertEquals(DesignerPlugin.PLUGIN_ID, status.getPlugin());
				assertEquals(IStatus.ERROR, status.getCode());
				assertSame(exception, status.getException());
			}
		};
		//
		try {
			log.addLogListener(logListener);
			DesignerPlugin.setDisplayExceptionOnConsole(false);
			//
			boolean success = ExecutionUtils.runLog(new RunnableEx() {
				@Override
				public void run() throws Exception {
					throw exception;
				}
			});
			assertFalse(success);
		} finally {
			log.removeLogListener(logListener);
			DesignerPlugin.setDisplayExceptionOnConsole(true);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// void, log, later
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ExecutionUtils#runLogLater(RunnableEx)}.
	 */
	@Test
	public void test_void_log_later_noException() throws Exception {
		final boolean[] executed = new boolean[]{false};
		ExecutionUtils.runLogLater(new RunnableEx() {
			@Override
			public void run() throws Exception {
				executed[0] = true;
			}
		});
		// execution should be done later
		assertFalse(executed[0]);
		// wait for events loop, so Runnable should be executed
		waitEventLoop(10);
		assertTrue(executed[0]);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// void, rethrow
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_void_rethrow_noException() throws Exception {
		ExecutionUtils.runRethrow(new RunnableEx() {
			@Override
			public void run() throws Exception {
			}
		});
	}

	@Test
	public void test_void_rethrow_withException() throws Exception {
		final Exception exception = new Exception();
		try {
			ExecutionUtils.runRethrow(new RunnableEx() {
				@Override
				public void run() throws Exception {
					throw exception;
				}
			});
		} catch (Throwable e) {
			assertSame(exception, e);
		}
	}

	/**
	 * Test for {@link ExecutionUtils#runRethrow(RunnableEx, String, Object...)}.
	 */
	@Test
	public void test_void_rethrowMessage_noException() throws Exception {
		ExecutionUtils.runRethrow(new RunnableEx() {
			@Override
			public void run() throws Exception {
			}
		}, "Error message '%s' for %d.", "Not found", 42);
	}

	/**
	 * Test for {@link ExecutionUtils#runRethrow(RunnableEx, String, Object...)}.
	 */
	@Test
	public void test_void_rethrowMessage_withException() throws Exception {
		final Exception exception = new Exception();
		try {
			ExecutionUtils.runRethrow(new RunnableEx() {
				@Override
				public void run() throws Exception {
					throw exception;
				}
			}, "Error message '%s' for %d.", "Not found", 42);
		} catch (RuntimeException e) {
			assertEquals("Error message 'Not found' for 42.", e.getMessage());
			assertSame(exception, e.getCause());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// runDesignTime()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ExecutionUtils#runDesignTime(RunnableEx)}.
	 */
	@Test
	public void test_void_runDesignTime_void() throws Exception {
		ExecutionUtils.runDesignTime(new RunnableEx() {
			@Override
			public void run() throws Exception {
				assertTrue(Beans.isDesignTime());
			}
		});
	}

	/**
	 * Test for {@link ExecutionUtils#runDesignTime(Callable)}.
	 */
	@Test
	public void test_void_runDesignTime_Object() throws Exception {
		final Object o = new Object();
		Object result = ExecutionUtils.runDesignTime(() -> {
			assertTrue(Beans.isDesignTime());
			return o;
		});
		assertSame(o, result);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// object, ignore
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_object_ignore_noException() throws Exception {
		final Object myResult = new Object();
		Object result = ExecutionUtils.runObjectIgnore(() -> myResult, null);
		assertSame(myResult, result);
	}

	@Test
	public void test_object_ignore_withException() throws Exception {
		final Object myResult = new Object();
		Object result = ExecutionUtils.runObjectIgnore(() -> { throw new Exception(); }, myResult);
		assertSame(myResult, result);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// object, rethrow
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_object_rethrow_noException() throws Exception {
		Object result = ExecutionUtils.runObject(() -> null);
		assertNull(result);
	}

	@Test
	public void test_object_rethrow_noException2() throws Exception {
		final Object myResult = new Object();
		Object result = ExecutionUtils.runObject(() -> myResult);
		assertSame(myResult, result);
	}

	@Test
	public void test_object_rethrow_noException3() throws Exception {
		final Integer myResult = 12345;
		Integer result = ExecutionUtils.runObject(() -> myResult);
		assertSame(myResult, result);
	}

	@Test
	public void test_object_rethrow_withException() throws Exception {
		final Exception exception = new Exception();
		try {
			ExecutionUtils.runObject(() -> { throw exception; });
		} catch (Throwable e) {
			assertSame(exception, e);
		}
	}

	@Test
	public void test_object_rethrow_withError() throws Exception {
		class MyError extends Error {
			private static final long serialVersionUID = 0L;
		}
		final MyError myError = new MyError();
		try {
			ExecutionUtils.runObject(() -> { throw myError; });
			fail();
		} catch (MyError e) {
			assertSame(myError, e);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// object, rethrow, with message
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ExecutionUtils#runObject(Callable, String, Object...)}.
	 */
	@Test
	public void test_object_rethrowMessage_noException() throws Exception {
		Object result = ExecutionUtils.runObject(() -> null, "Error message '%s' for %d.", "Not found", 42);
		assertNull(result);
	}

	/**
	 * Test for {@link ExecutionUtils#runObject(Callable, String, Object...)}.
	 */
	@Test
	public void test_object_rethrowMessage_withException() throws Exception {
		final Exception exception = new Exception();
		try {
			ExecutionUtils.runObject(() -> { throw exception; }, "Error message '%s' for %d.", "Not found", 42);
		} catch (Error e) {
			assertEquals("Error message 'Not found' for 42.", e.getMessage());
			assertSame(exception, e.getCause());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// object, log
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_object_log_noException() throws Exception {
		final Object myResult = new Object();
		Object result = ExecutionUtils.runObjectLog(() -> myResult, null);
		assertSame(myResult, result);
	}

	@Test
	public void test_object_log_withException() throws Exception {
		final Object myResult = new Object();
		//
		Object result;
		try {
			DesignerPlugin.setDisplayExceptionOnConsole(false);
			result = ExecutionUtils.runObjectLog(() -> { throw new Exception(); }, myResult);
		} finally {
			DesignerPlugin.setDisplayExceptionOnConsole(true);
		}
		assertSame(myResult, result);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// UI
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ExecutionUtils#runLogUI(RunnableEx)}.
	 */
	@Test
	public void test_runLogUI_noException() throws Exception {
		final boolean[] executed = new boolean[1];
		final boolean[] success = new boolean[1];
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				success[0] = ExecutionUtils.runLogUI(new RunnableEx() {
					@Override
					public void run() throws Exception {
						assertNotNull(Display.getCurrent());
						executed[0] = true;
					}
				});
				// RunnableEx is already executed, because we wait for this
				assertTrue(executed[0]);
			}
		};
		// run in thread, so not in UI
		Thread thread = new Thread(runnable);
		thread.start();
		// we don't run even loop yet, so our RunnableEx is not executed yet
		assertFalse(executed[0]);
		// run event loop
		while (thread.isAlive()) {
			waitEventLoop(0);
		}
		// our RunnableEx should be executed
		assertTrue(executed[0]);
		assertTrue(success[0]);
	}

	/**
	 * Test for {@link ExecutionUtils#runLogUI(RunnableEx)}.
	 */
	@Test
	public void test_runLogUI_withException() throws Exception {
		final Exception exception = new Exception();
		ILog log = DesignerPlugin.getDefault().getLog();
		ILogListener logListener = new ILogListener() {
			@Override
			public void logging(IStatus status, String plugin) {
				assertEquals(IStatus.ERROR, status.getSeverity());
				assertEquals(DesignerPlugin.PLUGIN_ID, status.getPlugin());
				assertEquals(IStatus.ERROR, status.getCode());
				assertSame(exception, status.getException());
			}
		};
		// temporary intercept logging
		try {
			log.addLogListener(logListener);
			DesignerPlugin.setDisplayExceptionOnConsole(false);
			// prepare RunnableEx
			final boolean[] success = new boolean[1];
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					success[0] = ExecutionUtils.runLogUI(new RunnableEx() {
						@Override
						public void run() throws Exception {
							throw exception;
						}
					});
				}
			};
			// run in thread, so not in UI
			Thread thread = new Thread(runnable);
			thread.start();
			// run event loop
			while (thread.isAlive()) {
				waitEventLoop(0);
			}
			// our RunnableEx was executed, but unsuccessfully
			assertFalse(success[0]);
		} finally {
			log.removeLogListener(logListener);
			DesignerPlugin.setDisplayExceptionOnConsole(true);
		}
	}

	/**
	 * Test for {@link ExecutionUtils#runRethrowUI(RunnableEx)}.
	 */
	@Test
	public void test_runRethrowUI() throws Exception {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				ExecutionUtils.runRethrowUI(new RunnableEx() {
					@Override
					public void run() throws Exception {
						assertNotNull(Display.getCurrent());
					}
				});
			}
		};
		// run in thread, so not in UI
		Thread thread = new Thread(runnable);
		thread.start();
		while (thread.isAlive()) {
			waitEventLoop(0);
		}
	}

	/**
	 * Test for {@link ExecutionUtils#runAsync(RunnableEx)}.
	 */
	@Test
	public void test_runAsync() throws Exception {
		final boolean[] executed = new boolean[1];
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				ExecutionUtils.runAsync(new RunnableEx() {
					@Override
					public void run() throws Exception {
						assertNotNull(Display.getCurrent());
						executed[0] = true;
					}
				});
				// RunnableEx is NOT executed, we just scheduled it, and no events loop was executed
				assertFalse(executed[0]);
			}
		};
		// run in thread, so not in UI
		Thread thread = new Thread(runnable);
		thread.start();
		// RunnableEx was scheduled, but we did not execute yet events loop
		assertFalse(executed[0]);
		// Thread finished, but RunnableEx still not executed
		thread.join();
		assertFalse(executed[0]);
		// run event loop, so execute scheduled RunnableEx
		waitEventLoop(0);
		// our RunnableEx should be executed
		assertTrue(executed[0]);
	}

	/**
	 * Test for {@link ExecutionUtils#runObjectUI(RunnableObjectEx)}.
	 */
	@Test
	public void test_UI_object() throws Exception {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				final Object myResult = new Object();
				Object result = ExecutionUtils.runObjectUI(() -> {
					assertNotNull(Display.getCurrent());
					return myResult;
				});
				assertSame(myResult, result);
			}
		};
		// run in thread, so not in UI
		Thread thread = new Thread(runnable);
		thread.start();
		while (thread.isAlive()) {
			waitEventLoop(0);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// edit
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_edit() throws Exception {
		final AtomicBoolean refreshed = new AtomicBoolean();
		final TestObjectInfo object = new TestObjectInfo() {
			@Override
			protected void refresh_finish() throws Exception {
				refreshed.set(true);
			}
		};
		// do edit
		ExecutionUtils.run(object, new RunnableEx() {
			@Override
			public void run() throws Exception {
				// no refresh
				assertFalse(refreshed.get());
				// inner edit operation
				object.startEdit();
				object.endEdit();
				// still no refresh
				assertFalse(refreshed.get());
			}
		});
		// refresh happened
		assertTrue(refreshed.get());
	}

	@Test
	public void test_edit_exception_withSite() throws Exception {
		final TestObjectInfo object = new TestObjectInfo();
		final Exception exception = new Exception();
		// set site
		final boolean[] exceptionHandled = new boolean[1];
		DesignPageSite.Helper.setSite(object, new DesignPageSite() {
			@Override
			public void handleException(Throwable e) {
				assertSame(exception, e);
				exceptionHandled[0] = true;
			}
		});
		// do edit
		assertFalse(exceptionHandled[0]);
		ExecutionUtils.run(object, new RunnableEx() {
			@Override
			public void run() throws Exception {
				throw exception;
			}
		});
		// exception should be handled
		assertTrue(exceptionHandled[0]);
	}

	@Test
	public void test_edit_exception_noSite() throws Exception {
		final AtomicBoolean refreshed = new AtomicBoolean();
		final TestObjectInfo object = new TestObjectInfo() {
			@Override
			protected void refresh_finish() throws Exception {
				refreshed.set(true);
			}
		};
		// do edit
		final Exception exception = new Exception();
		try {
			ExecutionUtils.run(object, new RunnableEx() {
				@Override
				public void run() throws Exception {
					throw exception;
				}
			});
			fail();
		} catch (Throwable e) {
			assertSame(exception, e);
			// exception during edit operation, no refresh
			assertFalse(refreshed.get());
		}
	}

	/**
	 * When we throw {@link RuntimeException}, no need to wrap it.
	 */
	@Test
	public void test_edit_exception_noSite_RuntimeException() throws Exception {
		ObjectInfo object = new TestObjectInfo() {
			@Override
			public String toString() {
				throw new Error("toString() is not implemented!");
			}
		};
		// do operation
		final RuntimeException exception = new RuntimeException();
		try {
			ExecutionUtils.run(object, new RunnableEx() {
				@Override
				public void run() throws Exception {
					throw exception;
				}
			});
			fail();
		} catch (Throwable e) {
			assertSame(exception, e);
		}
	}

	/**
	 * Test for {@link ExecutionUtils#runLater(ObjectInfo, RunnableEx)} .
	 */
	@Test
	public void test_edit_later() throws Exception {
		final AtomicBoolean refreshed = new AtomicBoolean();
		final TestObjectInfo object = new TestObjectInfo() {
			@Override
			protected void refresh_finish() throws Exception {
				refreshed.set(true);
			}
		};
		// do edit
		final AtomicBoolean executed = new AtomicBoolean();
		ExecutionUtils.runLater(object, new RunnableEx() {
			@Override
			public void run() throws Exception {
				executed.set(true);
				// inner edit operation
				object.startEdit();
				object.endEdit();
				// still no refresh
				assertFalse(refreshed.get());
			}
		});
		// execution should be later
		assertFalse(executed.get());
		assertFalse(refreshed.get());
		// execute event loop, so execute all "later"
		waitEventLoop(1);
		assertTrue(executed.get());
		assertTrue(refreshed.get());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// edit, object
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ExecutionUtils#runObject(ObjectInfo, RunnableObjectEx)}.
	 */
	@Test
	public void test_edit_object() throws Exception {
		final AtomicBoolean refreshed = new AtomicBoolean();
		final TestObjectInfo object = new TestObjectInfo() {
			@Override
			protected void refresh_finish() throws Exception {
				refreshed.set(true);
			}
		};
		// do edit
		final String result = "expected";
		String actual = ExecutionUtils.runObject(object, () -> result);
		// has expected result
		assertSame(result, actual);
		// refresh happened
		assertTrue(refreshed.get());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_refresh() throws Exception {
		final AtomicBoolean refreshed = new AtomicBoolean();
		final TestObjectInfo object = new TestObjectInfo() {
			@Override
			protected void refresh_finish() throws Exception {
				refreshed.set(true);
			}
		};
		// do refresh
		ExecutionUtils.refresh(object);
		assertTrue(refreshed.get());
	}
}
