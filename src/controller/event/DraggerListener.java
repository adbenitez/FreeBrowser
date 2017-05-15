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
    
    private Point cursorStartDrag_Point;
    private Point windowStartDrag_Point;
	
    //	================= END ATTRIBUTES ==========================

    //	================= CONSTRUCTORS ===========================

    public DraggerListener(Container target) {
        this.target = target;
        this.default_Cursor = target.getCursor();
        this.drag_Cursor = new Cursor(Cursor.MOVE_CURSOR);
    }
    
    public DraggerListener(Container target, Cursor dragCursor) {
    	this.target = target;
    	this.default_Cursor = target.getCursor();
    	this.drag_Cursor = dragCursor;
    }
    
    //	================= END CONSTRUCTORS =======================

    //	===================== METHODS ============================

    public static Window getWindow(Container target) {
    	if (target instanceof Window) {
            return  (Window)target;
    	}
    	return getWindow(target.getParent());
    }

    /**
     * 
     * @return the screen location point of the mouse.
     */
    Point getScreenLocation(MouseEvent e) {
    	Point cursor = e.getPoint();
    	Point target_location = this.target.getLocationOnScreen();
    	return new Point((int) (target_location.getX() + cursor.getX()),
                         (int) (target_location.getY() + cursor.getY()));
    }
   
    /**
     * 
     * Moves the window to the new location.
     */
    public void mouseDragged(MouseEvent e) {
    	Point current_Point = this.getScreenLocation(e);

        int currentX = (int) current_Point.getX();
        int startX = (int) cursorStartDrag_Point.getX();
    	int offsetX =  currentX - startX;

        int currentY = (int) current_Point.getY();
        int startY = (int) cursorStartDrag_Point.getY();
    	int offsetY = currentY - startY;
    	
    	int newX = (int) (this.windowStartDrag_Point.getX() + offsetX);
    	int newY = (int) (this.windowStartDrag_Point.getY() + offsetY);
    	Point new_location = new Point(newX, newY);
    	
    	Window window = getWindow(target);
    	window.setLocation(new_location);
    }

    /**
     *  Changes the cursor to the drag cursor and saves the location
     *  point of the cursor and window.
     */
    public void mousePressed(MouseEvent e) {
    	Window window=getWindow(target);
    	window.setCursor(drag_Cursor);
    	
    	this.cursorStartDrag_Point = this.getScreenLocation(e);
        this.windowStartDrag_Point = window.getLocation();
    }

    /**
     * Restores the cursor to the default cursor.
     */
    public void mouseReleased(MouseEvent e) {
    	Window window = getWindow(target);
    	window.setCursor(default_Cursor);
    }

    public void mouseMoved(MouseEvent e) {
    	// TODO Not implemented yet
    }

    public void mouseClicked(MouseEvent e) {
    	// TODO Not implemented yet
    }

    public void mouseEntered(MouseEvent e) {
    	// TODO Not implemented yet
    }

    public void mouseExited(MouseEvent e) {
    	// TODO Not implemented yet
    }
    
    //	====================== END METHODS ========================

}
