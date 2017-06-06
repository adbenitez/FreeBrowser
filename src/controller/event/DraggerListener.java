/*
 * Copyright (c) 2016-2017 Asiel Díaz Benítez.
 *
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * You should have received a copy of the GNU General Public License
 * along with this file.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package controller.event;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * The listener for drag a Container's window.
 *
 */
public class DraggerListener
    implements MouseMotionListener, MouseListener {

    //	================= ATTRIBUTES ==============================

    private Cursor default_Cursor;
    private Cursor drag_Cursor;

    private Container target;
    private Window window;

    private Point previousDrag_Point;
    private Point window_Point;

    //	================= END ATTRIBUTES ==========================

    //	================= CONSTRUCTORS ===========================

    public DraggerListener(Container target) {
        this.target = target;
        window = getWindow(target);
        this.default_Cursor = target.getCursor();
        this.drag_Cursor = new Cursor(Cursor.MOVE_CURSOR);
    }

    public DraggerListener(Container target, Cursor dragCursor) {
    	this.target = target;
        window = getWindow(target);
    	this.default_Cursor = target.getCursor();
    	this.drag_Cursor = dragCursor;
    }

    //	================= END CONSTRUCTORS =======================

    //	===================== METHODS ============================

    private Window getWindow(Container target) {
        if (target == null) {
            return null;
        }
        if (target instanceof Window) {
            return  (Window)target;
        }
        return getWindow(target.getParent());
    }

    /**
     *
     * @return the screen location point of the mouse.
     */
    private Point getScreenLocation(MouseEvent e) {
    	Point cursor = e.getPoint();
    	Point target_location = target.getLocationOnScreen();
        target_location.translate(cursor.x, cursor.y);
    	return target_location;
    }

    /**
     *
     * Moves the window to the new location.
     */
    public void mouseDragged(MouseEvent e) {
    	Point currentDrag_Point = this.getScreenLocation(e);
        if (previousDrag_Point == null) {
            previousDrag_Point = new Point(currentDrag_Point);
        } else {
            int offsetX =  currentDrag_Point.x - previousDrag_Point.x;
            int offsetY = currentDrag_Point.y - previousDrag_Point.y;
            previousDrag_Point.setLocation(currentDrag_Point);
            window_Point.translate(offsetX, offsetY);
            window.setLocation(window_Point);
        }
    }

    /**
     *  Changes the cursor to the drag cursor and saves the location
     *  point of the cursor and window.
     */
    public void mousePressed(MouseEvent e) {
    	Window window=getWindow(target);
    	window.setCursor(drag_Cursor);

    	this.previousDrag_Point = this.getScreenLocation(e);
        this.window_Point = window.getLocation();
    }

    /**
     * Restores the cursor to the default cursor.
     */
    public void mouseReleased(MouseEvent e) {
    	Window window = getWindow(target);
    	window.setCursor(default_Cursor);
    }

    public void mouseMoved(MouseEvent e) {
    	// Not needed, we could use a MouseAdapter
    }

    public void mouseClicked(MouseEvent e) {
    	// Not needed, we could use a MouseAdapter
    }

    public void mouseEntered(MouseEvent e) {
    	// Not needed, we could use a MouseAdapter
    }

    public void mouseExited(MouseEvent e) {
    	// Not needed, we could use a MouseAdapter
    }

    //	====================== END METHODS ========================

}
