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

package com.orinus.schema;

import com.orinus.script.safe.lucene.SEntity;

public class Folder extends SEntity {

	public static final String NAME = "name";
	public static final String PARENT = "parent";
	public static final String LEVEL = "level";
	public static final String PUBLISHED = "published";
	
    public Folder(Handler handler) {
    	super(handler);
    	setKind("Folder");
    }

    protected void registerDefault() {
    	super.registerDefault();
        register(NAME, STRING);
        register(PARENT, STRING);
        register(LEVEL, INTEGER);
        register(PUBLISHED, STRING);
    }

    public String getName() {
    	return getString(NAME);
    }
    
    public void setName(String src) {
    	setString(NAME, src);
    }

    public String getParent() {
    	return getString(PARENT);
    }
    
    public void setParent(String src) {
    	setString(PARENT, src);
    }

    public int getLevel() {
    	return getInteger(LEVEL);
    }
    
    public void setLevel(int src) {
    	setInteger(LEVEL, src);
    }
 
    public boolean getPublished() {
    	return getBoolean(PUBLISHED, true);
    }
    
    public void setPublished(boolean src) {
    	setBoolean(PUBLISHED, src);
    }
    
}
