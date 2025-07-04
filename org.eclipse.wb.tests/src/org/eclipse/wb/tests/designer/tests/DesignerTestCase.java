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
package org.eclipse.wb.tests.designer.tests;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.editor.DesignContextMenuProvider;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.TestUtils;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.ui.internal.UIPlugin;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Superclass for all Designer test cases.
 *
 * @author scheglov_ke
 */
public abstract class DesignerTestCase extends Assertions {

	private static boolean m_firstDesignerTest = true;
	private final Logger logger = Logger.getLogger(getClass().getName());

	////////////////////////////////////////////////////////////////////////////
	//
	// Logging
	//
	////////////////////////////////////////////////////////////////////////////

	@RegisterExtension
	private LoggerExtension extension = new LoggerExtension();

	public class LoggerExtension implements BeforeTestExecutionCallback {
		@Override
		public void beforeTestExecution(ExtensionContext context) throws Exception {
			logger.info(context.getRequiredTestClass().getSimpleName() + ':' + context.getDisplayName());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setUp/tearDown
	//
	////////////////////////////////////////////////////////////////////////////
	@BeforeEach
	public void setUp() throws Exception {
		configureFirstTime();
	}

	@AfterEach
	public void clearMemoryLeaks() throws Exception {
		// ObjectInfoUtils
		((Map<?, ?>) ReflectionUtils.getFieldObject(ObjectInfoUtils.class, "m_idToObjectInfo")).clear();
		((Map<?, ?>) ReflectionUtils.getFieldObject(ObjectInfoUtils.class, "m_objectInfoToId")).clear();
	}

	private void configureFirstTime() {
		configureEclipseWindowLocation();
		if (!m_firstDesignerTest) {
			return;
		}
		m_firstDesignerTest = false;
		// minimize main Window
		//UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell().setMinimized(true);
		// specify testing time
		EnvironmentUtils.setTestingTime(true);
		//
		/*System.out.println("VM: " + ManagementFactory.getRuntimeMXBean().getInputArguments());
    System.out.println("commands: " + System.getProperty("eclipse.commands"));*/
		//
		IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(
				IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW,
				false);
		UIPlugin.getDefault().getPreferenceStore().setValue("ENABLE_ANIMATIONS", false);
		// disable spell checking
		{
			IPreferenceStore preferenceStore = EditorsPlugin.getDefault().getPreferenceStore();
			preferenceStore.setValue(SpellingService.PREFERENCE_SPELLING_ENABLED, false);
		}
		// hide views
		TestUtils.closeAllViews();
	}

	/**
	 * Ensures that Eclipse main window is in top-right corner of screen.
	 */
	private void configureEclipseWindowLocation() {
		int x = 450;
		int y = 0;
		Shell shell = Activator.getShell();
		Point shellLocation = shell.getLocation();
		if (shellLocation.x != x || shellLocation.y != y) {
			Rectangle clientArea = Display.getDefault().getClientArea();
			shell.setBounds(x, y, clientArea.width - x, clientArea.height - 300);
			waitEventLoop(100, 10);
		}
	}

	@AfterEach
	public void tearDown() throws Exception {
		clearInstanceFields();
	}

	private void clearInstanceFields() throws Exception {
		clearInstanceFields(getClass());
	}

	private void clearInstanceFields(Class<?> clazz) throws Exception {
		for (Field field : clazz.getDeclaredFields()) {
			boolean isObject = Object.class.isAssignableFrom(field.getType());
			boolean isStatic = Modifier.isStatic(field.getModifiers());
			if (isObject && !isStatic && field.getName().indexOf('$') == -1) {
				field.setAccessible(true);
				field.set(this, null);
			}
		}
		if (clazz != DesignerTestCase.class) {
			clearInstanceFields(clazz.getSuperclass());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Exceptions in log
	//
	////////////////////////////////////////////////////////////////////////////
	private int m_numberOfExceptionsDuringThisEditorSession = 0;
	private final ILogListener m_logListener = new ILogListener() {
		@Override
		public void logging(IStatus status, String plugin) {
			m_numberOfExceptionsDuringThisEditorSession++;
		}
	};

	/**
	 * Adds listener for log.
	 */
	protected final void addExceptionsListener() {
		m_numberOfExceptionsDuringThisEditorSession = 0;
		ILog log = DesignerPlugin.getDefault().getLog();
		log.addLogListener(m_logListener);
	}

	/**
	 * Removes listener for log.
	 */
	protected final void removeExceptionsListener() {
		ILog log = DesignerPlugin.getDefault().getLog();
		log.removeLogListener(m_logListener);
	}

	/**
	 * Asserts that no exceptions was logged into {@link DesignerPlugin}.
	 */
	protected final void assertNoLoggedExceptions() {
		assertEquals(
				0,
				m_numberOfExceptionsDuringThisEditorSession,
				"Check console for logged exceptions.");
	}

	/**
	 * Asserts that given object is instance of expected class.
	 */
	protected static void assertInstanceOf(String expectedClassName, Object o) {
		assertNotNull(o, expectedClassName + " class expected, but 'null' found.");
		if (!ReflectionUtils.isSuccessorOf(o, expectedClassName)) {
			fail(expectedClassName + " class expected, but " + o.getClass().getName() + " found.");
		}
	}

	/**
	 * Asserts that given object is not instance of expected class.
	 */
	protected static void assertNotInstanceOf(Class<?> expectedClass, Object o) {
		assertNotNull(o);
		assertFalse(expectedClass.isAssignableFrom(o.getClass()),
				expectedClass.getName()
				+ " class expected, but "
				+ o.getClass().getName()
				+ " found.");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Image
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link RGB} of single pixel.
	 */
	public static RGB getPixelRGB(Image image, int x, int y) {
		ImageData imageData = image.getImageData();
		int pixel = imageData.getPixel(x, y);
		return imageData.palette.getRGB(pixel);
	}

	/**
	 * Asserts that {@link RGB} has given component values.
	 */
	public static void assertRGB(RGB rgb, int red, int green, int blue) {
		String message = rgb.toString();
		assertEquals(rgb.red, red, message);
		assertEquals(rgb.green, green, message);
		assertEquals(rgb.blue, blue, message);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return content of given file from "org.eclipse.wb.tests" project as string.
	 */
	protected static String readProjectFileContent(String path) throws Exception {
		FileInputStream inputStream = new FileInputStream(path);
		try {
			return IOUtils.toString(inputStream);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	/**
	 * @return content of given file from folder "resources" as string.
	 */
	protected static String readResourceFileContent(String path) throws Exception {
		return readFileContent("/resources/" + path);
	}

	/**
	 * @return content of given file from class loader.
	 */
	protected static String readFileContent(String path) throws Exception {
		URL entry = Activator.getDefault().getBundle().getEntry(path);
		InputStream inputStream = entry.openStream();
		try {
			return IOUtils.toString(inputStream);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Source utils
	//
	////////////////////////////////////////////////////////////////////////////
	public static boolean m_getSource_ignoreSpaces = false;
	public static boolean m_getSource_ignoreSpacesCheck = false;

	/**
	 * @return the array of {@link String} with replaced sub-strings.
	 */
	public static String[] replace(String[] lines, String repl, String with) {
		String[] newLines = new String[lines.length];
		for (int i = 0; i < lines.length; i++) {
			String s = lines[i];
			newLines[i] = StringUtils.replace(s, repl, with);
		}
		return newLines;
	}

	/**
	 * @return the {@link String} for joined given {@link String}'s using "\n".
	 */
	public static String getSource(String... lines) {
		return getSource(new String[][]{lines});
	}

	/**
	 * @return the {@link String} for joined given {@link String}'s using "\n".
	 */
	public static String getSource2(String[] lines_1, String[] lines_2) {
		return getSource(new String[][]{lines_1, lines_2});
	}

	/**
	 * @return the {@link String} for joined given {@link String}'s using "\n".
	 */
	public static String getSource3(String[] lines_1, String[] lines_2, String[] lines_3) {
		return getSource(new String[][]{lines_1, lines_2, lines_3});
	}

	/**
	 * @return the {@link String} for joined given {@link String}'s using "\n".
	 */
	public static String getSource(String[][] lines2) {
		StringBuffer buffer = new StringBuffer();
		for (String[] lines : lines2) {
			if (lines == null) {
				continue;
			}
			//
			for (String line : lines) {
				if (!m_getSource_ignoreSpaces) {
					// prepare count of leading spaces
					int spaceCount = 0;
					for (char c : line.toCharArray()) {
						if (c != ' ') {
							break;
						}
						spaceCount++;
					}
					// replace each two leading spaces with one \t
					if (!m_getSource_ignoreSpacesCheck) {
						assertEquals(0, spaceCount % 2);
						line = StringUtils.repeat("\t", spaceCount / 2) + line.substring(spaceCount);
					} else {
						int tabCount = spaceCount / 2;
						line = StringUtils.repeat("\t", tabCount) + line.substring(2 * tabCount);
					}
				}
				// append line
				buffer.append(line);
				buffer.append("\n");
			}
		}
		m_getSource_ignoreSpaces = false;
		m_getSource_ignoreSpacesCheck = false;
		return buffer.toString();
	}

	/**
	 * Replaces each line in array that use single quotes, with double quotes.
	 *
	 * @return the updated array.
	 */
	public static String[] getDoubleQuotes(String[] lines) {
		for (int i = 0; i < lines.length; i++) {
			lines[i] = getSourceDQ(lines[i]);
		}
		return lines;
	}

	/**
	 * @return {@link String} that used single quotes, replaced with double quotes.
	 */
	public static String getSourceDQ(String s) {
		return StringUtils.replace(s, "'", "\"");
	}

	/**
	 * @return the single string from several lines, where input lines may use single quotes, but
	 *         result string will have double quotes.
	 */
	public static String getSourceDQ(String... lines) {
		String source = getSource(lines);
		return StringUtils.replace(source, "'", "\"");
	}

	/**
	 * Creates source for given lines, that can be used later in {@link #getSourceDQ(String...)}.
	 */
	protected static String getLinesForSourceDQ(String... lines) {
		StringBuffer buffer = new StringBuffer();
		// lines
		for (String line : lines) {
			buffer.append('"');
			line = StringUtils.replace(line, "\t", "  ");
			{
				line = line.replace('"', '\'');
				buffer.append(StringUtilities.escapeForJavaSource(line));
			}
			buffer.append('"');
			buffer.append(",\n");
		}
		// end
		String result = buffer.toString();
		result = StringUtils.removeEnd(result, ",\n");
		return result;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// UI utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Waits given number of milliseconds and runs events loop every 1 millisecond.<br>
	 * At least one events loop will be executed.
	 */
	protected static void waitEventLoop(int time) {
		waitEventLoop(time, 0);
	}

	/**
	 * Waits given number of milliseconds and runs events loop every <code>sleepMillis</code>
	 * milliseconds.<br>
	 * At least one events loop will be executed.
	 */
	public static void waitEventLoop(int time, long sleepMillis) {
		long start = System.currentTimeMillis();
		do {
			try {
				Thread.sleep(sleepMillis);
			} catch (Throwable e) {
			}
			while (Display.getCurrent().readAndDispatch()) {
				// do nothing
			}
		} while (System.currentTimeMillis() - start < time);
	}

	/**
	 * Animates "Open type" dialog, set filter and waits for first result in types list.
	 */
	public static void animateOpenTypeSelection(SWTBot bot, String typeName, String buttonName) {
		SWTBotShell shellBot = bot.shell("Open type");
		SWTBot shell = shellBot.bot();
		// set filter
		{
			SWTBotText filterText = shell.text();
			filterText.setText(typeName);
		}
		// wait for types
		{
			final SWTBotTable typesTable = shell.table();
			bot.waitUntil(new DefaultCondition() {
				@Override
				public boolean test() throws Exception {
					return typesTable.rowCount() != 0;
				}

				@Override
				public String getFailureMessage() {
					return "\"Open type\" dialog took too long to find types.";
				}
			});
		}
		shell.button(buttonName).click();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IAction utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link MenuManager} prepared as Designer context menu.
	 */
	protected static MenuManager getDesignerMenuManager() {
		MenuManager menuManager = new MenuManager();
		DesignContextMenuProvider.addGroups(menuManager);
		return menuManager;
	}

	/**
	 * @return the {@link IMenuManager} child with given text.
	 */
	protected static IMenuManager findChildMenuManager(IContributionManager manager, String text) {
		IContributionItem[] items = manager.getItems();
		for (int i = 0; i < items.length; i++) {
			IContributionItem contributionItem = items[i];
			if (contributionItem instanceof MenuManager managerChild) {
				if (managerChild.getMenuText().equals(text)) {
					return managerChild;
				}
			}
		}
		// not found
		return null;
	}

	/**
	 * @return the {@link IAction} child with given text.
	 */
	protected static IAction findChildAction(IContributionManager manager, String text) {
		List<IAction> actions = findChildActions(manager, text);
		return GenericsUtils.getFirstOrNull(actions);
	}

	/**
	 * @return the {@link IAction}-s children with given text.
	 */
	protected static List<IAction> findChildActions(IContributionManager manager, String text) {
		List<IAction> actions = new ArrayList<>();
		text = getNormalizedActionText(text);
		for (IContributionItem contributionItem : manager.getItems()) {
			if (contributionItem instanceof ActionContributionItem actionContributionItem) {
				IAction action = actionContributionItem.getAction();
				if (getNormalizedActionText(action.getText()).equals(text)) {
					actions.add(action);
				}
			}
		}
		// done
		return actions;
	}

	/**
	 * @return the "normalized" text of {@link Action}, without accelerator specification ( text after
	 *         "\t") and hot key (<code>"&"</code> character).
	 * @param text
	 * @return
	 */
	private static String getNormalizedActionText(String text) {
		{
			int index = text.indexOf('\t');
			if (index != -1) {
				text = text.substring(0, index);
			}
		}
		text = StringUtils.remove(text, "&");
		return text;
	}

	/**
	 * @return the {@link IAction} with given text/tooltip.
	 */
	protected static IAction findAction(List<?> actions, String text) {
		for (Object object : actions) {
			if (object instanceof IAction action) {
				if (text.equals(action.getText()) || text.equals(action.getToolTipText())) {
					return action;
				}
			}
		}
		// not found
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the context menu for {@link ObjectInfo} selection.
	 */
	public static IMenuManager getContextMenu(ObjectInfo... objectsArray) throws Exception {
		IMenuManager manager = getDesignerMenuManager();
		List<ObjectInfo> objects = List.of(objectsArray);
		ObjectInfo object = objectsArray[0];
		object.getBroadcastObject().addContextMenu(objects, object, manager);
		return manager;
	}

	/**
	 * @return the selection actions for empty selection.
	 */
	public static List<Object> getSelectionActions_noSelection(ObjectInfo root) throws Exception {
		List<Object> actions = new ArrayList<>();
		List<ObjectInfo> objects = Collections.emptyList();
		root.getBroadcastObject().addSelectionActions(objects, actions);
		return actions;
	}

	/**
	 * @return the selection actions, displayed on editor toolbar.
	 */
	public static List<Object> getSelectionActions(ObjectInfo... objectsArray) throws Exception {
		List<Object> actions = new ArrayList<>();
		if (objectsArray.length != 0) {
			ObjectInfo object = objectsArray[0];
			List<ObjectInfo> objects = List.of(objectsArray);
			object.getBroadcastObject().addSelectionActions(objects, actions);
		}
		return actions;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Notifies that given {@link ObjectInfo} is selecting.
	 *
	 * @return the refresh flag.
	 */
	protected static boolean notifySelecting(ObjectInfo object) throws Exception {
		boolean[] refresh = new boolean[]{false};
		object.getBroadcastObject().selecting(object, refresh);
		return refresh[0];
	}
}
