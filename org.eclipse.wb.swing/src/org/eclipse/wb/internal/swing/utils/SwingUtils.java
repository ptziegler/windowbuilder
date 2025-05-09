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
package org.eclipse.wb.internal.swing.utils;

import org.eclipse.wb.core.model.broadcast.DisplayEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Synchronizer;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * Various utilities to operate with Swing.
 *
 * @author mitin_aa
 * @coverage swing.utils
 */
public final class SwingUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private SwingUtils() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Runs the {@link RunnableEx} in the AWT event dispatching thread using
	 * {@link SwingUtilities#invokeLater(Runnable)} and waits for it to get done
	 * with SWT message pumping. Due to SWT principles this means pumping system
	 * message loop. Using {@link SwingUtilities#invokeAndWait(Runnable)} is not
	 * acceptable because it could produce deadlocks between SWT and AWT dispatch
	 * threads. Any exceptions that are thrown are logged using
	 * {@link DesignerPlugin#log(Throwable)}.
	 *
	 * Note: must be invoked from SWT UI thread.
	 *
	 * @return {@code true} if execution was finished without exception.
	 */
	public static boolean runLog(final RunnableEx runnable) {
		try {
			runLaterAndWait(runnable);
			return true;
		} catch (Throwable e) {
			DesignerPlugin.log(e);
			return false;
		}
	}

	/**
	 * Runs the {@link RunnableEx} in the AWT event dispatching thread using
	 * {@link SwingUtilities#invokeLater(Runnable)} and waits for it to get done with SWT message
	 * pumping. Due to SWT principles this means pumping system message loop. Using
	 * {@link SwingUtilities#invokeAndWait(Runnable)} is not acceptable because it could produce
	 * deadlocks between SWT and AWT dispatch threads.
	 *
	 * Note: must be invoked from SWT UI thread.
	 */
	public static void runLaterAndWait(final RunnableEx runnableEx) throws Exception {
		final AtomicBoolean done = new AtomicBoolean();
		final Throwable ex[] = new Throwable[1];
		invokeLaterAndWait(done, () -> {
			try {
				runnableEx.run();
			} catch (Throwable e) {
				ex[0] = e;
			} finally {
				done.set(true);
			}
		});
		propagateIfNotNull(ex[0]);
	}

	/**
	 * Ensures that the AWT EventQueue is empty by adding fake event into queue and pumping SWT
	 * message loop until fake event processed.
	 */
	public static void ensureQueueEmpty() {
		if (EventQueue.isDispatchThread()) {
			return;
		}
		final AtomicBoolean done = new AtomicBoolean();
		SwingUtilities.invokeLater(() -> done.set(true));
		// wait and pump SWT message loop
		while (!done.get()) {
			ExecutionUtils.waitEventLoop(0);
		}
	}

	/**
	 * Same as {@link #runLaterAndWait(RunnableEx)} but returns the result of execution. See
	 * {@link ExecutionUtils#runObject(Callable)}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T runObjectLaterAndWait(final Callable<T> runnableEx) throws Exception {
		final AtomicBoolean done = new AtomicBoolean();
		final Throwable ex[] = new Throwable[1];
		final Object[] result = new Object[1];
		invokeLaterAndWait(done, () -> {
			try {
				result[0] = runnableEx.call();
			} catch (Throwable e) {
				ex[0] = e;
			} finally {
				done.set(true);
			}
		});
		propagateIfNotNull(ex[0]);
		return (T) result[0];
	}

	private static void invokeLaterAndWait(final AtomicBoolean done, Runnable job) {
		Display display = Display.getCurrent();
		/*
		 * Different platforms requires different Swing execution:
		 * 1. Mac OS X is very slow while invoking Swing using main thread;
		 * 2. Linux synchronizes GTK calls and if being invoked from different threads it may lock up.
		 * 3. Windows is indifferent. :-)
		 */
		if (!EventQueue.isDispatchThread() && display != null && !EnvironmentUtils.IS_LINUX) {
			// async events should be disabled while waiting AWT to be done.
			// Otherwise its possible the state at which AWT still does something
			// and puts async events into Display, which immediately would be executed
			// because the SWT message loop is pumping up.
			// Do this by Synchronizer delegate.
			final Synchronizer oldSynchronizer = getSynchronizer(display);
			Synchronizer newSynchronizer = new Synchronizer(display) {
				@Override
				protected void asyncExec(Runnable runnable) {
					ReflectionUtils.invokeMethodEx(oldSynchronizer, "asyncExec(java.lang.Runnable)", runnable);
				}
			};
			// notify about running SWT message loop (to prevent MouseUp event during current refresh)
			DisplayEventListener displayListener = null;
			if (GlobalState.getActiveObject() != null) {
				displayListener = GlobalState.getActiveObject().getBroadcast(DisplayEventListener.class);
				displayListener.beforeMessagesLoop();
			}
			// run and clean up
			setMainShellEnabled(false);
			try {
				// set new Synchronizer, do not use Display.setSynchronizer() because it
				// gets pending events executed
				setSynchronizer(display, newSynchronizer);
				// schedule runnable to AWT dispatch thread
				SwingUtilities.invokeLater(job);
				// wait and pump SWT message loop
				while (!done.get()) {
					ExecutionUtils.waitEventLoop(0);
				}
			} finally {
				setMainShellEnabled(true);
				if (displayListener != null) {
					displayListener.afterMessagesLoop();
				}
				// restore old Synchronizer
				display.setSynchronizer(oldSynchronizer);
			}
		} else {
			// just run if in dispatch thread
			OSSupport.get().runAwt(job);
		}
	}

	private static void propagateIfNotNull(Throwable throwable) {
		if (throwable != null) {
			ReflectionUtils.propagate(throwable);
		}
	}

	/**
	 * We set this filter to disable some events during rendering. Specifically we disable
	 * {@link SWT#MouseDoubleClick} because it is sent event when {@link Shell} is disabled on
	 * {@link SWT#MouseUp}.
	 */
	private static final Listener m_disableEventFilter = event -> event.type = SWT.None;

	/**
	 * We should disable main Eclipse {@link Shell} during running SWT events loop to prevent user
	 * from interacting with it, while models may be temporary in unusable state.
	 */
	private static void setMainShellEnabled(boolean enabled) {
		Shell shell = DesignerPlugin.getShell();
		// process outstanding paint events before disabling any drawing.
		if (!enabled) {
			if (!EnvironmentUtils.IS_WINDOWS) {
				shell.update();
			}
		}
		// set/remove filter
		Display display = shell.getDisplay();
		if (enabled) {
			display.removeFilter(SWT.MouseDoubleClick, m_disableEventFilter);
		} else {
			display.addFilter(SWT.MouseDoubleClick, m_disableEventFilter);
		}
		// do disable/enable
		shell.setRedraw(enabled);
		shell.setEnabled(enabled);
	}

	private static Synchronizer getSynchronizer(Display display) {
		synchronized (Device.class) {
			return (Synchronizer) ReflectionUtils.getFieldObject(display, "synchronizer");
		}
	}

	private static void setSynchronizer(Display display, Synchronizer newSynchronizer) {
		synchronized (Device.class) {
			ReflectionUtils.setField(display, "synchronizer", newSynchronizer);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Clean up
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * For unknown reason, when we have {@link Display} created and add any {@link JTextComponent}
	 * sub-class to {@link JFrame}, we see {@link JFrame} in <code>[JNI global]</code> roots until we
	 * dispose {@link Display} (until OleUninitialize() invocation). So, we clear all
	 * {@link Container}'s to minimize number of components referenced from {@link JFrame}.
	 * <p>
	 * See also https://bugs.eclipse.org/bugs/show_bug.cgi?id=127374 and
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6407026
	 * <p>
	 * I found also that in Java6 {@link JFrame} may stuck somewhere in Draw2D/DirectDraw, so we
	 * should clear not only for text components, but even when we make Swing screen shot.
	 */
	public static void clearSwingTree(final Container container) throws Exception {
		runLaterAndWait(() -> {
			if (container != null) {
				// even if this container can not be effectively cleared, we will clear children,
				// so minimize potential memory as much as possible
				for (Component child : container.getComponents()) {
					if (child instanceof Container) {
						clearSwingTree((Container) child);
					}
				}
				// remove all children Component's
				ExecutionUtils.runIgnore(() -> container.removeAll());
				// "tab order" property may install our FocusTraversalOnArray, so clear it
				container.setFocusTraversalPolicy(null);
				// remove layout manager: under JVM < 1.5 JFrame.setLayout() throws exception that
				// JFrame.getContentPane().setLayout() should be used, so do this safely
				ExecutionUtils.runIgnore(() -> container.setLayout(null));
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Coordinate utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return location of given {@link Component} in screen coordinates.
	 */
	public static Point getScreenLocation(final Component component) throws Exception {
		try {
			return runObjectLaterAndWait(() -> component.getLocationOnScreen());
		} catch (IllegalComponentStateException e) {
			return new Point();
		}
	}

	/**
	 * @return location of given {@code child} {@link Component} relative to the
	 *         {@code parent} {@link Component}.
	 */
	public static Point getRelativeLocation(final Component parentComponent, final Component childComponent)
			throws Exception {
		try {
			return runObjectLaterAndWait(() -> {
				Point parentLocation = parentComponent.getLocationOnScreen();
				Point childLocation = childComponent.getLocationOnScreen();
				int relX = childLocation.x - parentLocation.x;
				int relY = childLocation.y - parentLocation.y;
				return new Point(relX, relY);
			});
		} catch (IllegalComponentStateException e) {
			return new Point();
		}
	}

	/**
	 * Convert SWT color to AWT color.
	 */
	public static java.awt.Color getAWTColor(org.eclipse.swt.graphics.Color color) {
		return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
	}
}
