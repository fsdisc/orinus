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

public class LogItem extends SEntity {

	public static final String DATA = "data";
	public static final String PARENT = "parent";
	public static final String SIZE = "size";
	
    public LogItem(Handler handler) {
    	super(handler);
    	setKind("LogItem");
    }

    protected void registerDefault() {
    	super.registerDefault();
        register(DATA, STRING);
        register(PARENT, STRING);
        register(SIZE, LONG);
    }

    public long getSize() {
    	return getLong(SIZE);
    }
    
    public void setSize(long src) {
    	setLong(SIZE, src);
    }
    
    public String getParent() {
    	return getString(PARENT);
    }
    
    public void setParent(String src) {
    	setString(PARENT, src);
    }

    public String getData() {
    	return getString(DATA);
    }
    
    public void setData(String src) {
    	setString(DATA, src);
    }
    
}
