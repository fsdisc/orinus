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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.orinus.schema.Engine;
import com.orinus.schema.FileData;
import com.orinus.schema.FileItem;
import com.orinus.schema.Folder;
import com.orinus.schema.LogData;
import com.orinus.schema.LogItem;
import com.orinus.script.LuceneHandler;
import com.orinus.script.RemoteHandler;
import com.orinus.script.safe.lucene.SEntity;

public class Controller {

    private static Logger logger = Logger.getLogger(Controller.class);

    private Config config;
    private Config buffer;
    private String appDir;
    private String cfgDir;
    private String logDir;
    private String cfgFile;
    private String datDir;
    private String extDir;

    public Controller() {
    	config = new Config();
    	buffer = new Config();
    	appDir = System.getProperty("user.dir");
    	cfgDir = new File(appDir, "cfg").getAbsolutePath();
    	logDir = new File(appDir, "log").getAbsolutePath();
    	datDir = new File(appDir, "dat").getAbsolutePath();
    	extDir = new File(appDir, "ext").getAbsolutePath();
    	cfgFile = new File(cfgDir, "config.properties").getAbsolutePath();
    	loadConfig();
    }

    public Config getConfig() {
    	return config;
    }
    
    public Config getBuffer() {
    	return buffer;
    }
    
    public String getAppDir() {
    	return appDir;
    }
    
    public String getCfgDir() {
    	return cfgDir;
    }
    
    public String getLogDir() {
    	return logDir;
    }

    public String getExtDir() {
    	return extDir;
    }
    
    public String getCfgFile() {
    	return cfgFile;
    }
    
    public String getDatDir() {
    	String dir = config.getString(Config.DATA);
    	if (dir.length() == 0) {
        	return datDir;
    	} else {
    		return dir;
    	}
    }
    
    public void saveConfig() {
    	config.save(cfgFile);
    }
    
    public void loadConfig() {
    	config.load(cfgFile);
    }
    
    public String uniqid() {
    	return UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    public String suniqid() {
        Random random = new Random();
        return Long.toString(Math.abs(random.nextLong()), 36);
    }
    
    public int getPort() {
    	int port = config.getInt(Config.PORT);
    	if (port <= 0) {
    		port = 80;
    	}
    	return port;
    }
    
    public String getSystem() {
    	String tag = config.getString(Config.SYSTEM);
    	if (tag.length() == 0) {
    		tag = "system";
    	}
    	return tag;
    }
    
    public String getTempRoot() {
    	String t_dat = getDatDir();
    	String t_root = new File(t_dat, "temp").getAbsolutePath();
    	new File(t_root).mkdirs();
    	return t_root;
    }
    
    public String getTempDir() {
    	String t_root = getTempRoot();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	String t_date = new File(t_root, sdf.format(new Date())).getAbsolutePath();
    	String t_temp = new File(t_date, uniqid()).getAbsolutePath();
    	new File(t_temp).mkdirs();
    	return t_temp;
    }
    
    public String getHosts() {
    	String hosts = config.getString(Config.HOSTS);
    	if (hosts.length() == 0) {
    		hosts = "|localhost|";
    	}
    	return hosts;
    }
    
    public boolean preservedHost(String src) {
    	String hosts = getHosts();
    	return hosts.indexOf("|" + src + "|") >= 0;
    }
 
    public double getQuota() {
    	double quota = config.getDouble(Config.QUOTA);
    	if (quota <= 0) {
    		quota = 1024;
    	}
    	return quota;
    }
    
    public int getTimeout() {
    	int to = config.getInt(Config.TIMEOUT);
    	if (to <= 0) to = 60;
    	if (to > 60 * 24) to = 60 *24;
    	return to;
    }

    public int getRunTime() {
    	int to = config.getInt(Config.RUNTIME);
    	if (to <= 0) to = 60 * 24;
    	return to;
    }
    
    public int getRemotePort() {
    	int p = config.getInt(Config.REMOTE_PORT);
    	if (p <= 0) p = 80;
    	return p;
    }
    
    public Engine newEngine() {
    	String t_data = getDatDir();
    	String t_engine = new File(t_data, "common").getAbsolutePath();
    	String t_index = new File(t_engine, "index").getAbsolutePath();
    	String t_backup = new File(t_engine, "backup").getAbsolutePath();
    	new File(t_index).mkdirs();
    	new File(t_backup).mkdirs();
    	return new Engine(new RemoteHandler(new LuceneHandler(t_index, t_backup, getQuota()), null));
    }
    
    public Folder newFolder(Engine engine) {
    	String t_data = getDatDir();
    	String t_engine = new File(t_data, "common").getAbsolutePath();
    	double t_quota = getQuota();
    	if (engine != null) {
        	t_engine = new File(t_data, engine.getId()).getAbsolutePath();
    		t_quota = engine.getQuota();
    	}
    	String t_index = new File(t_engine, "index").getAbsolutePath();
    	String t_backup = new File(t_engine, "backup").getAbsolutePath();
    	new File(t_index).mkdirs();
    	new File(t_backup).mkdirs();
    	return new Folder(new RemoteHandler(new LuceneHandler(t_index, t_backup, t_quota), engine));
    }

    public FileItem newFileItem(Engine engine) {
    	String t_data = getDatDir();
    	String t_engine = new File(t_data, "common").getAbsolutePath();
    	double t_quota = getQuota();
    	if (engine != null) {
        	t_engine = new File(t_data, engine.getId()).getAbsolutePath();
    		t_quota = engine.getQuota();
    	}
    	String t_index = new File(t_engine, "index").getAbsolutePath();
    	String t_backup = new File(t_engine, "backup").getAbsolutePath();
    	new File(t_index).mkdirs();
    	new File(t_backup).mkdirs();
    	return new FileItem(new RemoteHandler(new LuceneHandler(t_index, t_backup, t_quota), engine));
    }

    public FileData newFileData(Engine engine) {
    	String t_data = getDatDir();
    	String t_engine = new File(t_data, "common").getAbsolutePath();
    	double t_quota = getQuota();
    	if (engine != null) {
        	t_engine = new File(t_data, engine.getId()).getAbsolutePath();
    		t_quota = engine.getQuota();
    	}
    	String t_index = new File(t_engine, "index").getAbsolutePath();
    	String t_backup = new File(t_engine, "backup").getAbsolutePath();
    	new File(t_index).mkdirs();
    	new File(t_backup).mkdirs();
    	return new FileData(new RemoteHandler(new LuceneHandler(t_index, t_backup, t_quota), engine));
    }

    public SEntity newEntity(Engine engine) {
    	String t_data = getDatDir();
    	String t_engine = new File(t_data, "common").getAbsolutePath();
    	double t_quota = getQuota();
    	if (engine != null) {
        	t_engine = new File(t_data, engine.getId()).getAbsolutePath();
    		t_quota = engine.getQuota();
    	}
    	String t_index = new File(t_engine, "index").getAbsolutePath();
    	String t_backup = new File(t_engine, "backup").getAbsolutePath();
    	new File(t_index).mkdirs();
    	new File(t_backup).mkdirs();
    	return new SEntity(new RemoteHandler(new LuceneHandler(t_index, t_backup, t_quota), engine));
    }

    public LogItem newLogItem(Engine engine) {
    	String t_data = getDatDir();
    	String t_engine = new File(t_data, "common").getAbsolutePath();
    	double t_quota = getQuota();
    	if (engine != null) {
        	t_engine = new File(t_data, engine.getId()).getAbsolutePath();
    		t_quota = engine.getQuota();
    	}
    	String t_index = new File(t_engine, "index").getAbsolutePath();
    	String t_backup = new File(t_engine, "backup").getAbsolutePath();
    	new File(t_index).mkdirs();
    	new File(t_backup).mkdirs();
    	return new LogItem(new RemoteHandler(new LuceneHandler(t_index, t_backup, t_quota), engine));
    }

    public LogData newLogData(Engine engine) {
    	String t_data = getDatDir();
    	String t_engine = new File(t_data, "common").getAbsolutePath();
    	double t_quota = getQuota();
    	if (engine != null) {
        	t_engine = new File(t_data, engine.getId()).getAbsolutePath();
    		t_quota = engine.getQuota();
    	}
    	String t_index = new File(t_engine, "index").getAbsolutePath();
    	String t_backup = new File(t_engine, "backup").getAbsolutePath();
    	new File(t_index).mkdirs();
    	new File(t_backup).mkdirs();
    	return new LogData(new RemoteHandler(new LuceneHandler(t_index, t_backup, t_quota), engine));
    }
    
    public RemoteHandler newRemoteHandler(Engine engine) {
    	String t_data = getDatDir();
    	String t_engine = new File(t_data, "common").getAbsolutePath();
    	double t_quota = getQuota();
    	if (engine != null) {
        	t_engine = new File(t_data, engine.getId()).getAbsolutePath();
    		t_quota = engine.getQuota();
    	}
    	String t_index = new File(t_engine, "index").getAbsolutePath();
    	String t_backup = new File(t_engine, "backup").getAbsolutePath();
    	new File(t_index).mkdirs();
    	new File(t_backup).mkdirs();
    	return new RemoteHandler(new LuceneHandler(t_index, t_backup, t_quota), engine);
    }
    
    public LuceneHandler newLuceneHandler(Engine engine) {
    	String t_data = getDatDir();
    	String t_engine = new File(t_data, "common").getAbsolutePath();
    	double t_quota = getQuota();
    	if (engine != null) {
        	t_engine = new File(t_data, engine.getId()).getAbsolutePath();
    		t_quota = engine.getQuota();
    	}
    	String t_index = new File(t_engine, "index").getAbsolutePath();
    	String t_backup = new File(t_engine, "backup").getAbsolutePath();
    	new File(t_index).mkdirs();
    	new File(t_backup).mkdirs();
    	return new LuceneHandler(t_index, t_backup, t_quota);
    }
    
}
