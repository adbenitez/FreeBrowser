/*
 * Copyright (c) 2017 Asiel Díaz Benítez.
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * You should have received a copy of the GNU General Public License
 * along with this file.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package controller.exception;

public class InvalidFolderException extends Exception {
    
    //	================= ATTRIBUTES ==============================
	
	private static final long serialVersionUID = 1L;
	
    //	================= END ATTRIBUTES ==========================
    
    //	================= CONSTRUCTORS ===========================
    
	public InvalidFolderException (String message) {
        super(message);
    }
    
    //	================= END CONSTRUCTORS =======================
    
    //	===================== METHODS ============================
    //	====================== END METHODS =======================
    
}
