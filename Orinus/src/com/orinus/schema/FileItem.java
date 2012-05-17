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

public class FileItem extends SEntity {

	public static final String NAME = "name";
	public static final String PARENT = "parent";
	public static final String DATA = "data";
	public static final String PUBLISHED = "published";
	public static final String SCHEDULED = "scheduled";
	public static final String MINUTE = "minute";
	public static final String HOUR = "hour";
	public static final String DAY = "day";
	public static final String MONTH = "month";
	public static final String YEAR = "year";
	public static final String TIMEOUT = "timeout";
	
    public FileItem(Handler handler) {
    	super(handler);
    	setKind("FileItem");
    }

    protected void registerDefault() {
    	super.registerDefault();
        register(NAME, STRING);
        register(PARENT, STRING);
        register(DATA, STRING);
        register(PUBLISHED, STRING);
        register(SCHEDULED, STRING);
        register(MINUTE, STRING);
        register(HOUR, STRING);
        register(DAY, STRING);
        register(MONTH, STRING);
        register(YEAR, STRING);
        register(TIMEOUT, INTEGER);
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

    public String getData() {
    	return getString(DATA);
    }
    
    public void setData(String src) {
    	setString(DATA, src);
    }
    
    public boolean getPublished() {
    	return getBoolean(PUBLISHED, true);
    }
    
    public void setPublished(boolean src) {
    	setBoolean(PUBLISHED, src);
    }

    public boolean getScheduled() {
    	return getBoolean(SCHEDULED, false);
    }
    
    public void setScheduled(boolean src) {
    	setBoolean(SCHEDULED, src);
    }

    public String getMinute() {
    	return getString(MINUTE);
    }
    
    public void setMinute(String src) {
    	setString(MINUTE, src);
    }

    public String getHour() {
    	return getString(HOUR);
    }
    
    public void setHour(String src) {
    	setString(HOUR, src);
    }

    public String getDay() {
    	return getString(DAY);
    }
    
    public void setDay(String src) {
    	setString(DAY, src);
    }

    public String getMonth() {
    	return getString(MONTH);
    }
    
    public void setMonth(String src) {
    	setString(MONTH, src);
    }

    public String getYear() {
    	return getString(YEAR);
    }
    
    public void setYear(String src) {
    	setString(YEAR, src);
    }

    public int getTimeout() {
    	return getInteger(TIMEOUT);
    }
    
    public void setTimeout(int src) {
    	setInteger(TIMEOUT, src);
    }
    
}
