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

public class Engine extends SEntity {

	public static final String DOMAIN = "domain";
	public static final String FOLDER = "folder";
	public static final String QUOTA = "quota";
	public static final String TIMEOUT = "timeout";
	public static final String RUNTIME = "runtime";
	public static final String TOKEN = "token";
	public static final String DISTRIBUTED = "distributed";
	public static final String REMOTE_HOST = "remote_host";
	public static final String REMOTE_PORT = "remote_port";
	public static final String REMOTE_TOKEN = "remote_token";
	public static final String RUN_SCRIPT = "run_script";
	
    public Engine(Handler handler) {
    	super(handler);
    	setKind("Engine");
    }

    protected void registerDefault() {
    	super.registerDefault();
        register(DOMAIN, STRING);
        register(FOLDER, STRING);
        register(QUOTA, DOUBLE);
        register(TIMEOUT, INTEGER);
        register(RUNTIME, INTEGER);
        register(TOKEN, STRING);
        register(DISTRIBUTED, STRING);
        register(REMOTE_HOST, STRING);
        register(REMOTE_PORT, INTEGER);
        register(REMOTE_TOKEN, STRING);
        register(RUN_SCRIPT, STRING);
    }

    public String getDomain() {
    	return getString(DOMAIN);
    }
    
    public void setDomain(String src) {
    	setString(DOMAIN, src);
    }

    public String getFolder() {
    	return getString(FOLDER);
    }
    
    public void setFolder(String src) {
    	setString(FOLDER, src);
    }
    
    public double getQuota() {
    	return getDouble(QUOTA);
    }
    
    public void setQuota(double src) {
    	setDouble(QUOTA, src);
    }
	
    public int getTimeout() {
    	int to = getInteger(TIMEOUT);
    	if (to <= 0) to = 60;
    	if (to > 60 * 24) to = 60 *24;
    	return to;
    }
    
    public void setTimeout(int to) {
    	if (to <= 0) to = 60;
    	if (to > 60 * 24) to = 60 *24;
    	setInteger(TIMEOUT, to);
    }
    
    public int getRunTime() {
    	int to = getInteger(RUNTIME);
    	if (to <= 0) to = 60 * 24;
    	return to;
    }

    public void setRuntime(int to) {
    	if (to <= 0) to = 60 * 24;
    	setInteger(RUNTIME, to);
    }
    
    public String getToken() {
    	return getString(TOKEN);
    }
    
    public void setToken(String src) {
    	setString(TOKEN, src);
    }
    
    public boolean getDistributed() {
    	return getBoolean(DISTRIBUTED, false);
    }
    
    public void setDistributed(boolean src) {
    	setBoolean(DISTRIBUTED, src);
    }
    
    public String getRemoteHost() {
    	return getString(REMOTE_HOST);
    }
    
    public void setRemoteHost(String src) {
    	setString(REMOTE_HOST, src);
    }

    public int getRemotePort() {
    	int p = getInteger(REMOTE_PORT);
    	if (p <= 0) p = 80;
    	return p;
    }
    
    public void setRemotePort(int p) {
    	if (p <= 0) p = 80;
    	setInteger(REMOTE_PORT, p);
    }
    
    public String getRemoteToken() {
    	return getString(REMOTE_TOKEN);
    }
    
    public void setRemoteToken(String src) {
    	setString(REMOTE_TOKEN, src);
    }
    
    public boolean getRunScript() {
    	return getBoolean(RUN_SCRIPT, false);
    }
    
    public void setRunScript(boolean src) {
    	setBoolean(RUN_SCRIPT, src);
    }
    
}
