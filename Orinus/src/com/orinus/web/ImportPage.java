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

package com.orinus.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.lucene.search.BooleanQuery;
import org.eclipse.jetty.server.Request;

import com.orinus.Config;
import com.orinus.IOTool;
import com.orinus.schema.Engine;
import com.orinus.schema.FileData;
import com.orinus.schema.FileItem;
import com.orinus.schema.Folder;

public class ImportPage extends BasePage {

    private Logger logger = Logger.getLogger(ImportPage.class);
	
    private Engine engine;
    private Folder folder;
	
	public ImportPage(Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		super(baseRequest, request, response);
	}
	
	public void execute() throws Exception {
		if (!checkOnline()) return;
		
		String eid = getParameter("eid");
		
		if (!DEFAULT_ENGINE.equals(eid)) {
			engine = controller.newEngine();
			engine.load(eid);
			if (engine.getId().length() == 0) {
				returnHome();
				return;
			}
		}
		
		String fid = getParameter("fid");
		folder = controller.newFolder(engine);
		folder.load(fid);
		if (folder.getId().length() == 0) {
			folder = null;
			fid = "";
		}
		
		String message = "";
		
		if (isPost()) {
			if (!sreq.isMultipartContent()) {
				returnHome();
				return;
			}
			sreq.parseMultipartContent();
			if (message.length() == 0 && sreq.getPartFile("data") == null) {
				message = "DataNotExists";
			}
			if (message.length() == 0) {
				String dirTemp = controller.getTempDir();
				String filename = sreq.getPartFile("data").filename;
				filename = new File(dirTemp, filename).getAbsolutePath();
				try {
					OutputStream os = new FileOutputStream(filename);
					os.write(sreq.getPartFileData("data"));
					os.close();
				} catch (Exception e) {
					logger.error("", e);
				}
				
				importPackage(folder, filename);
				
				try {
					IOTool.deleteFolder(new File(dirTemp));
				} catch (Exception e) {
					logger.error("", e);
				}
				
		    	returnPage("open-engine.jsb?eid=" + eid);
		    	return;
			}
		}
		
		Map args = newArgs();
		Map data = new HashMap();
		data.put("eid", eid);
		data.put("fid", fid);
		data.put("message", message);
		args.put("data", data);
		loadPage("import.vm", args);
	}
	
    private void importPackage(Folder parent, String filename) {
    	String dirTemp = controller.getTempDir();
    	String name = new File(filename).getName();
    	int pos = name.lastIndexOf(".");
    	if (pos >= 0) {
    		name = name.substring(0, pos);
    	}
    	String dirRoot = new File(dirTemp, name).getAbsolutePath();
    	new File(dirRoot).mkdirs();
    	try {
    		IOTool.unzipFile(filename, dirRoot);
    		File file = new File(dirRoot);
    		File[] children = file.listFiles();
    		if (children.length > 0) {
        		file = children[0];
        		if (file.isDirectory()) {
        			importFolder(parent, file);
        		}
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    	try {
    		IOTool.deleteFolder(new File(dirTemp));
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    }
    
    private void importFolder(Folder parent, File folder) {
    	File[] children = folder.listFiles();
    	for (int i = 0; i < children.length; i++) {
    		File child = children[i];
    		if (child.isDirectory()) {
    			Folder fcur = importFolder(parent, child.getName(), child.getAbsolutePath());
    			importFolder(fcur, child);
    		} else if (child.isFile()) {
    			if (!child.getAbsolutePath().endsWith(".orinus")) {
        			importFile(parent, child);
    			}
    		}
    	}
    }

    private Folder importFolder(Folder parent, String name, String path) {
    	String pid = "";
    	if (parent != null) {
    		pid = parent.getId();
    	}
    	int level = 1;
    	if (parent != null) {
    		level = parent.getLevel() + 1;
    	}
    	String t_name = name;
    	int t_no = 0;
    	while (findFolderByName(pid, level, t_name)) {
    		t_no++;
    		t_name = name + t_no;
    	}
    	Folder tag = controller.newFolder(engine);
    	tag.setId(controller.uniqid());
    	tag.setParent(pid);
    	tag.setLevel(level);
    	tag.setName(t_name);

    	Config t_cfg = new Config();
    	if (new File(path + ".orinus").exists()) {
        	t_cfg.load(path + ".orinus");
    		tag.setPublished(t_cfg.getBoolean("published"));
    	} else {
    		tag.setPublished(true);
    	}
    	
    	tag.save();
    	return tag;
    }
    
    private void importFile(Folder parent, File file) {
    	String ext = "";
    	String name = file.getName();
    	int pos = name.lastIndexOf(".");
    	if (pos >= 0) {
    		ext = name.substring(pos);
    		name = name.substring(0, pos);
    	}
    	String pid = "";
    	if (parent != null) {
    		pid = parent.getId();
    	}
    	String t_name = name + ext;
    	int t_no = 0;
    	while (findFileByName(pid, t_name)) {
    		t_no++;
    		t_name = name + t_no + ext;
    	}
    	
    	byte[] data = new byte[0];
    	try {
    		InputStream is = new FileInputStream(file);
    		data = new byte[is.available()];
    		is.read(data);
    		is.close();
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    	
    	FileData fdata = controller.newFileData(engine);
    	fdata.setId(controller.uniqid());
    	fdata.setData(data);
    	fdata.save();
    	
    	FileItem fitem = controller.newFileItem(engine);
    	fitem.setId(controller.uniqid());
    	fitem.setName(t_name);
    	fitem.setData(fdata.getId());
    	fitem.setParent(pid);
    	
    	Config t_cfg = new Config();
    	if (new File(file.getAbsolutePath() + ".orinus").exists()) {
        	t_cfg.load(file.getAbsolutePath() + ".orinus");
    		fitem.setPublished(t_cfg.getBoolean("published"));
    	} else {
    		fitem.setPublished(true);
    	}
    	fitem.setScheduled(t_cfg.getBoolean("scheduled"));
    	fitem.setMinute(t_cfg.getString("minute"));
    	fitem.setHour(t_cfg.getString("hour"));
    	fitem.setDay(t_cfg.getString("day"));
    	fitem.setMonth(t_cfg.getString("month"));
    	fitem.setYear(t_cfg.getString("year"));
    	fitem.setTimeout(t_cfg.getInt("timeout"));
    	
    	fitem.save();
    }
    
    private boolean findFileByName(String pid, String name) {
    	FileItem pat = controller.newFileItem(engine);
    	BooleanQuery bq = pat.newBooleanQuery();
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.NAME, name)), pat.occurMust()));
    	int count = pat.count(pat.getKind(), bq, 1);
    	return count > 0;
    }
    
    private boolean findFolderByName(String pid, int level, String name) {
    	Folder pat = controller.newFolder(engine);
    	BooleanQuery bq = pat.newBooleanQuery();
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.NAME, name)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newIntegerRangeQuery(pat.LEVEL, level, level, true, true), pat.occurMust()));
    	int count = pat.count(pat.getKind(), bq, 1);
    	return count > 0;
    }
	
}
