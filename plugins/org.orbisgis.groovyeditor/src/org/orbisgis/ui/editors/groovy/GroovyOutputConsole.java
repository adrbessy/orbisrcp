/*
 * Groovy Editor (GE) is a library that brings a groovy console to the Eclipse RCP.
 * GE is developed by CNRS http://www.cnrs.fr/.
 *
 * GE is part of the OrbisGIS project. GE is free software;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * GE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details http://www.gnu.org/licenses.
 *
 *
 *For more information, please consult: http://www.orbisgis.org
 *or contact directly: info_at_orbisgis.org
 *
 */
package org.orbisgis.ui.editors.groovy;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.jkiss.dbeaver.model.runtime.DBRProcessController;
import org.jkiss.dbeaver.model.runtime.DBRProcessDescriptor;
import org.jkiss.dbeaver.ui.ActionUtils;
import org.jkiss.dbeaver.ui.UIUtils;
import org.orbisgis.core.ui.ToolbarButton;

/**
 * Methods which are able to print the groovy standard output in a specific groovy console.
 *
 * @author Adrien Bessy, CNRS
 */
public class GroovyOutputConsole extends ViewPart implements DBRProcessController
{
	static Composite group = null;
	static ToolbarButton button = null;
	
	public static final String VIEW_ID = "org.jkiss.dbeaver.groovy.output.clear";
    public static final String CLEAR_CMD_ID = "org.orbisgis.ui.editors.groovy.clearOutput";

	@Override
	public DBRProcessDescriptor getProcessDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void terminateProcess() {
		// TODO Auto-generated method stub
	}

	@Override
	public void createPartControl(Composite parent) {
		group = UIUtils.createPlaceholder(parent, 1);
        group.setLayout(new FillLayout());
		GroovyConsoleContent.initialize(group);
		createDeleteOutputButton(group);
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

	/**
	 * Create a button to delete the groovy output content.
	 *
	 * @param group A given composite
	 */
	private void createDeleteOutputButton(Composite group) {
		final MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(manager -> {
            manager.add(ActionUtils.makeCommandContribution(getSite(), CLEAR_CMD_ID));
        });
        group.setMenu(menuMgr.createContextMenu(group));
	}


	public static class GroovyConsoleContent{
	
		static StyledText text = null;
		static Display display = null;
		static StyleRange style1 = null;
		static String message_END =  "\nGroovy script successfully executed.\n";
		static String message_BAD_END =  "\nError while execution the Groovy script.\n";

		/**
		 * Initialize a composite.
		 *
		 * @param group A given composite
		 */
		public static void initialize(Composite group) {
	    	text = new StyledText(group, SWT.HORIZONTAL | SWT.READ_ONLY | SWT.V_SCROLL);
	    	display = PlatformUI.getWorkbench().getDisplay();
	    	text.setLayoutData(new GridData());
		}
		
		/**
		 * Write the file name into the groovy output console.
		 *
		 * @param fileName the file name
		 */
		public static void writeFileNameIntoConsole(String fileName) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					text.setText(fileName + "\n\n");
				}
			});
		}

		/**
		 * Write the standard output into the groovy output console.
		 *
		 * @param message the standard output
		 * @param resultOfExecution true if this is the standard output
		 */
		public static void writeIntoConsole(String message, boolean resultOfExecution) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					text.setText(text.getText() + message);
				}
			});
		}

		/**
		 * Write into the groovy output console.
		 *
		 * @param message the groovy output content
		 */
		public static void writeIntoConsole(String message) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					if(message.equals("ERASE")){
						text.setText("");
					} 
					else if (message.equals("END")) {
						style1 = new StyleRange(text.getText().length(), message_END.length(), display.getSystemColor(SWT.COLOR_GREEN), null);
						text.setText(text.getText() + message_END);
						text.setStyleRange(style1);
					}
					else if (message.equals("BAD_END")) {
						style1 = new StyleRange(text.getText().length(), message_BAD_END.length(), display.getSystemColor(SWT.COLOR_RED), null);
						text.setText(text.getText() + message_BAD_END);
						text.setStyleRange(style1);
					}
					else {
				    	text.setText(text.getText() + message);
					}
				}
			});
		}
		
	}

}
