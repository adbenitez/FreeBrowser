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
package controller;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class R {
    
    //	================= ATTRIBUTES ==============================
    
    private static final String BUNDLE_NAME = "language"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
        .getBundle(BUNDLE_NAME);

    //	================= END ATTRIBUTES ==========================

    //	================= CONSTRUCTORS ===========================

    private R() {
    }

    //	================= END CONSTRUCTORS =======================

    //	===================== METHODS ============================

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static char getChar(String key) {
        return getString(key).charAt(0);
    }

    //	====================== END METHODS =======================
}
