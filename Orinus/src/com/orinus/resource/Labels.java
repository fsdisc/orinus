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

package com.orinus.resource;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Labels {

    private static final String BUNDLE_NAME = "com.orinus.resource.Labels";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Labels() {}

    public static String get(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String get(String key, Object arg) {
         String format= null;
         try {
             format= RESOURCE_BUNDLE.getString(key);
         } catch (MissingResourceException e) {
             return "!" + key + "!";
         }
         if (arg == null)
             arg= "";
         return MessageFormat.format(format, new Object[] { arg });
     }
    
     public static String get(String key, String[] args) {
         return MessageFormat.format(RESOURCE_BUNDLE.getString(key), (Object[])args);
     }
	
}
