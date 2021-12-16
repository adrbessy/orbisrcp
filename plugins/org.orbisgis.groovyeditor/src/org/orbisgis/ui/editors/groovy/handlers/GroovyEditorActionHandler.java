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
 */
package org.orbisgis.ui.editors.groovy.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.navigator.DBNDataSource;
import org.jkiss.dbeaver.ui.navigator.database.DatabaseNavigatorView;
import org.jkiss.dbeaver.utils.RuntimeUtils;
import org.orbisgis.core.logger.Logger;
import org.orbisgis.ui.editors.groovy.GroovyConsoleView.GroovyConsoleContent;
import org.orbisgis.ui.editors.groovy.GroovyEditor;
import org.orbisgis.ui.editors.groovy.GroovyEditorCommands;

public class GroovyEditorActionHandler extends AbstractHandler {

    private static final Logger LOGGER = new Logger(GroovyEditorActionHandler.class);

    @Override
    public Object execute(ExecutionEvent executionEvent) {
        GroovyEditor editor = RuntimeUtils.getObjectAdapter(HandlerUtil.getActiveEditor(executionEvent), GroovyEditor.class);
        if (editor == null) {
            LOGGER.error("No active Groovy editor found");
            return null;
        }
        String actionId = executionEvent.getCommand().getId();
        switch (actionId) {
            case GroovyEditorCommands.CMD_CLEAR:
                editor.clear();
                break;
            case GroovyEditorCommands.CMD_EXECUTE_SCRIPT:
                editor.execute();
                break;
            case GroovyEditorCommands.CMD_EXECUTE_SELECTION:
                editor.executeSelection();
                break;
            case GroovyEditorCommands.CMD_EXECUTE_STOP:
                editor.stop();
                break;
            case GroovyEditorCommands.CMD_CHANGE_GROOVY_INTERPRETER:
                editor.changeGroovyInterpreter();
                break;
            case GroovyEditorCommands.CMD_CHANGE_GROOVY_DELETE_OUTPUT:
            	GroovyConsoleContent.writeIntoConsole("ERASE");
                break;
            default:
                LOGGER.error("Unsupported SQL editor command: " + actionId);
                break;
        }

        return null;
    }

    private List<DBPDataSourceContainer> getContainers(ExecutionEvent event) {
        List<DBPDataSourceContainer> list = new ArrayList<>();
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                IViewPart nav = window.getActivePage().findView(DatabaseNavigatorView.VIEW_ID);
                if (nav instanceof DatabaseNavigatorView) {
                    for (TreeItem item : ((DatabaseNavigatorView) nav).getNavigatorViewer().getTree().getItems()) {
                        if (item != null) {
                            Object data = item.getData();
                            if (data instanceof DBNDataSource) {
                                list.add(((DBNDataSource) data).getDataSourceContainer());
                            } else {
                                LOGGER.warn("No data source found");
                            }
                        } else {
                            LOGGER.warn("No tree item found");
                        }
                    }
                } else {
                    LOGGER.warn("No DatabaseNavigatorView found");
                }
            } else {
                LOGGER.warn("No IWorkbenchPage found");
            }
        } else {
            LOGGER.warn("No IWorkbenchWindow found");
        }
        return list;
    }
}
