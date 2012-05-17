/*
 *  Orinus - JavaScript SandBox
 * 
 *  Copyright (c) 2011 Tran Dinh Thoai <dthoai@yahoo.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.orinus;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.orinus.resource.Labels;

public class UITool {

    public static void placeCentered(Shell shell) {
    	Point size = shell.computeSize(-1, -1);
    	placeCentered(shell, size.x, size.y);
    }

    public static void placeCentered(Shell shell, int width, int height) {
    	Rectangle screen = Display.getCurrent().getBounds();
    	shell.setLocation((screen.width - width) / 2, (screen.height - height) / 2);
    }
    
    public static int warningBox(Shell shell, String message) {
        return messageBox(shell, Labels.get("UITool.Warning"), SWT.ICON_WARNING | SWT.OK, message);
    }

    public static int confirmBox(Shell shell, String message) {
        return messageBox(shell, Labels.get("UITool.Confirmation"), SWT.ICON_QUESTION | SWT.YES | SWT.NO, message);
    }
    
    public static int errorBox(Shell shell, Exception e) {
        return errorBox(shell, getMessage(e)); 
    }
    
    public static String getMessage(Exception e) {
    	String msg = "";
    	if (e.getCause() != null) {
    		if (e.getCause().getMessage() != null) {
    			msg = e.getCause().getMessage();
    		} else {
    			msg = e.getCause().toString();
    		}
    	} else {
    		if (e.getMessage() != null) {
    			msg = e.getMessage();
    		} else {
    			msg = e.toString();
    		}
    	}
    	return msg;
    }
    
    public static int errorBox(Shell shell, String message) {
        return messageBox(shell, Labels.get("UITool.Error"), SWT.OK | SWT.ICON_ERROR, message);
    }
    
    public static int messageBox(Shell shell, String title, int uiProps, String message) {
        MessageBox mb = new MessageBox (shell, uiProps);
        if(title != null && title.length() > 0) {
            mb.setText(title);
        }
        mb.setMessage(message);
        return mb.open();
    }

    public static int infoBox(Shell shell, String message) {
    	return messageBox(shell, Labels.get("UITool.Information"), SWT.OK | SWT.ICON_INFORMATION, message);
    }
	
}
