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

package com.orinus.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.search.BooleanQuery;

import com.orinus.Config;
import com.orinus.Controller;
import com.orinus.IOTool;
import com.orinus.schema.Engine;
import com.orinus.schema.FileData;
import com.orinus.schema.FileItem;
import com.orinus.schema.Folder;
import com.orinus.script.safe.lucene.SEntity;

public class ImExTool {

    private Logger logger = Logger.getLogger(ImExTool.class);
	
	private Controller controller;
	private Engine engine;
	
	public ImExTool(Engine engine) {
		this.engine = engine;
		this.controller = new Controller();
	}
	
    public void importPackage(Folder parent, String filename) {
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
	
    public void export(List<Folder> folders, List<FileItem> fitems, String filename) {
    	String dirTemp = controller.getTempDir();
    	String name = new File(filename).getName();
    	int pos = name.lastIndexOf(".");
    	if (pos >= 0) {
    		name = name.substring(0, pos);
    	}
    	String dirRoot = new File(dirTemp, name).getAbsolutePath();
    	new File(dirRoot).mkdirs();
    	for (int i = 0; i < fitems.size(); i++) {
    		exportFile(fitems.get(i), dirRoot);
    	}
    	for (int i = 0; i < folders.size(); i++) {
    		exportFolder(folders.get(i), dirRoot);
    	}
    	try {
    		IOTool.zipFolder(dirRoot, filename);
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    	try {
    		IOTool.deleteFolder(new File(dirTemp));
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    }

    private void exportFolder(Folder folder, String container) {
    	String t_root = container;
    	if (folder != null) {
        	String name = folder.getName();
        	Config t_cfg = new Config();
        	t_cfg.setValue("published", folder.getPublished());
        	t_cfg.save(new File(t_root, name + ".orinus").getAbsolutePath());
        	int no = 0;
        	String fname = new File(t_root, name).getAbsolutePath();
        	while (new File(fname).exists()) {
        		no++;
        		fname = new File(t_root, name + no).getAbsolutePath();
        	}
        	new File(fname).mkdirs();
    		t_root = fname;
    	}
    	List<FileItem> fitems = findFiles(folder);
    	for (int i = 0; i < fitems.size(); i++) {
    		exportFile(fitems.get(i), t_root);
    	}
    	List<Folder> folders = findChildFolder(folder);
    	for (int i = 0; i < folders.size(); i++) {
    		exportFolder(folders.get(i), t_root);
    	}
    }
    
    private List<Folder> findChildFolder(Folder parent) {
    	String pid = "";
    	if (parent != null) {
    		pid = parent.getId();
    	}
    	int level = 1;
    	if (parent != null) {
    		level = parent.getLevel() + 1;
    	}
    	List<Folder> tag = new ArrayList<Folder>();
    	Folder pat = controller.newFolder(engine);
    	BooleanQuery bq = pat.newBooleanQuery();
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newIntegerRangeQuery(pat.LEVEL, level, level, true, true), pat.occurMust()));
    	List<SEntity> results = pat.search(pat.getKind(), bq, Integer.MAX_VALUE);
    	for (int i = 0; i < results.size(); i++) {
    		Folder folder = controller.newFolder(engine);
    		folder.fromString(results.get(i).toString());
    		tag.add(folder);
    	}
    	return tag;
    }
    
    private List<FileItem> findFiles(Folder parent) {
    	String pid = "";
    	if (parent != null) {
    		pid = parent.getId();
    	}
    	List<FileItem> tag = new ArrayList<FileItem>();
    	FileItem pat = controller.newFileItem(engine);
    	List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), Integer.MAX_VALUE);
    	for (int i = 0; i < results.size(); i++) {
    		FileItem fitem = controller.newFileItem(engine);
    		fitem.fromString(results.get(i).toString());
    		tag.add(fitem);
    	}
    	return tag;
    }
    
    private void exportFile(FileItem fitem, String folder) {
    	try {
    		Config t_cfg = new Config();
    		t_cfg.setValue("published", fitem.getPublished());
    		t_cfg.setValue("scheduled", fitem.getScheduled());
    		t_cfg.setValue("minute", fitem.getMinute());
    		t_cfg.setValue("hour", fitem.getHour());
    		t_cfg.setValue("day", fitem.getDay());
    		t_cfg.setValue("month", fitem.getMonth());
    		t_cfg.setValue("year", fitem.getYear());
    		t_cfg.setValue("timeout", fitem.getTimeout());
    		t_cfg.save(new File(folder, fitem.getName() + ".orinus").getAbsolutePath());
    		String filename = new File(folder, fitem.getName()).getAbsolutePath();
    		FileData fdata = controller.newFileData(engine);
    		fdata.load(fitem.getData());
    		OutputStream os = new FileOutputStream(filename);
    		os.write(fdata.getData());
    		os.close();
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    }
    
}
