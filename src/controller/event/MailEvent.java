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

package controller.event;

import java.util.EventObject;

public class MailEvent extends EventObject {
    
    //	================= ATTRIBUTES ==============================
    
    private static final long serialVersionUID = 1L;
    
    private String attachmentPath;
    private Exception e;
    
    //	================= END ATTRIBUTES ==========================
    
    //	================= CONSTRUCTORS ===========================
    
    public MailEvent (Object source, String attachmentPath) {
        super(source);
        this.attachmentPath = attachmentPath;
    }

    public MailEvent (Object source, Exception e) {
        super(source);
        this.e = e;
    }

    //	================= END CONSTRUCTORS =======================
    
    //	===================== METHODS ============================

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public Exception getError() {
        return e;
    }
    
    //	====================== END METHODS =======================
    
}
