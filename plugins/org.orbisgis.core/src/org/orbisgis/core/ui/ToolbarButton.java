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
package org.orbisgis.core.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.services.IServiceLocator;
import org.orbisgis.core.utils.CommandUtils;

public class ToolbarButton extends Canvas {

    private static final int STYLE = SWT.LEFT | SWT.PUSH;
    private static final int IMAGE_BORDER_MARGIN = 2;

    private boolean hit = false;
    private boolean isHover;

    private Image image = null;
    private IServiceLocator serviceLocator;
    private String commandId;

    public static void create(Toolbar toolbar, IServiceLocator serviceLocator, String commandId) {
    	ToolbarButton button = new ToolbarButton(toolbar, STYLE);
        button.setCommand(serviceLocator, commandId);
    }

    public ToolbarButton(Toolbar toolbar, int style) {
        super(toolbar, style | SWT.NO_FOCUS);
        setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        addListeners();
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        Point iconSize = new Point(0, 0);
        if (image != null) {
            Rectangle imageBounds = image.getBounds();
            iconSize.x = imageBounds.width + IMAGE_BORDER_MARGIN;
            iconSize.y = imageBounds.height + IMAGE_BORDER_MARGIN * 2;
        }
        return iconSize;
    }

    private void addListeners(){

        this.addPaintListener(this::paint);
        addMouseMoveListener(e -> {
            if (!isHover) {
                isHover = true;
                redraw();
            }
        });

        this.addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseEnter(MouseEvent e) {
                isHover = true;
                redraw();
            }

            @Override
            public void mouseExit(MouseEvent e) {
                isHover = false;
                redraw();
            }

            @Override
            public void mouseHover(MouseEvent e) {
                if (!isHover) {
                    isHover = true;
                    redraw();
                }
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {            	
                isHover = true;
                hit = true;
                redraw();
            }

            @Override
            public void mouseUp(MouseEvent e) {
                isHover = true;
                redraw();
                if (hit) {
                    Event event = new Event();
                    event.widget = ToolbarButton.this;
                    event.item = ToolbarButton.this;
                    runAction(event);
                }
                hit = false;
            }
        });

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == '\r' || e.character == ' ') {
                    Event event = new Event();
                    event.widget = ToolbarButton.this;
                    event.item = ToolbarButton.this;
                    runAction(event);
                }
            }
        });
    }

    private void runAction(Event event) {
        notifyListeners(SWT.Selection, event);
        if (commandId != null) {
            CommandUtils.runCommand(commandId, serviceLocator);
        }
    }

    private void paint(PaintEvent e) {
        Point size = computeSize(0, 0, false);

        if (isHover) {
            Color bg = e.gc.getBackground();
            Color black = new Color(Display.getCurrent(), new RGB(0, 0, 0));
            e.gc.setBackground(black);
            e.gc.fillRectangle(0, 0, size.x, size.y);
            e.gc.setBackground(bg);
            e.gc.fillRectangle(1, 1, size.x-1, size.y-1);
        }
        if (image != null) {
            e.gc.drawImage(image, 0, IMAGE_BORDER_MARGIN);
        }
    }

    public void setCommand(IServiceLocator serviceLocator, String commandId) {
        this.serviceLocator = serviceLocator;
        this.commandId = commandId;
        this.image = CommandUtils.getCommandImage(commandId);
        addDisposeListener(e -> image.dispose());
    }
}
