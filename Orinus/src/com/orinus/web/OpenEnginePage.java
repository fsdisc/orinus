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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.orinus.schema.LogData;
import com.orinus.schema.LogItem;
import com.orinus.script.safe.lucene.SEntity;

public class OpenEnginePage extends BasePage {

    private Logger logger = Logger.getLogger(OpenEnginePage.class);
	
    private Engine engine;
	
	public OpenEnginePage(Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		super(baseRequest, request, response);
	}
	
	public void execute() throws Exception {
		if (!checkOnline()) return;
		
		String eid = getParameter("eid");
		String pagetitle = "localhost /";
		
		if (!DEFAULT_ENGINE.equals(eid)) {
			engine = controller.newEngine();
			engine.load(eid);
			if (engine.getId().length() == 0) {
				returnHome();
				return;
			}
			pagetitle = engine.getDomain() + " / " + engine.getFolder();
		}
		
		String stage = getParameter("stage");
		if ("load-file".equalsIgnoreCase(stage)) {
			String fid = getParameter("fid");
			if (fid.equals(eid)) {
				fid = "";
			}
			Map args = newArgs();
			Map data = new HashMap();
			data.put("page_title", pagetitle);
			data.put("eid", eid);
			data.put("files", loadTable(fid));
			args.put("data", data);
			loadPage("open-engine-file.vm", args);
			return;
		} else if ("delete-file".equalsIgnoreCase(stage)) {
			String ids = getParameter("file_ids");
			String[] fields = ids.split("\\|");
			for (int i = 0; i < fields.length; i++) {
				FileItem fitem = controller.newFileItem(engine);
				fitem.load(fields[i]);
				if (fitem.getId().length() == 0) continue;
				FileData fdata = controller.newFileData(engine);
				fdata.load(fitem.getData());
				fdata.delete();
				fitem.delete();
				deleteLog(fitem);
			}
		} else if ("delete-folder".equalsIgnoreCase(stage)) {
			String ids = getParameter("folder_ids");
			String[] fields = ids.split("\\|");
			for (int i = 0; i < fields.length; i++) {
				Folder folder = null;
				if (!eid.equals(fields[i])) {
					folder = controller.newFolder(engine);
					folder.load(fields[i]);
					if (folder.getId().length() == 0) continue;
				}
				deleteFolder(folder);
			}
		} else if ("export".equalsIgnoreCase(stage)) {
			List<Folder> folders = new ArrayList<Folder>();
			List<FileItem> fitems = new ArrayList<FileItem>();
			String ids = getParameter("folder_ids");
			String[] fields = ids.split("\\|");
			for (int i = 0; i < fields.length; i++) {
				Folder folder = null;
				if (!eid.equals(fields[i])) {
					folder = controller.newFolder(engine);
					folder.load(fields[i]);
					if (folder.getId().length() == 0) continue;
				}
				folders.add(folder);
			}
			ids = getParameter("file_ids");
			fields = ids.split("\\|");
			for (int i = 0; i < fields.length; i++) {
				FileItem fitem = controller.newFileItem(engine);
				fitem.load(fields[i]);
				if (fitem.getId().length() == 0) continue;
				fitems.add(fitem);
			}
			
			String filename = "localhost";
			if (engine != null) filename = engine.getFolder();
			String dirTemp = controller.getTempDir();
			filename = new File(dirTemp, filename + ".zip").getAbsolutePath();
			export(folders, fitems, filename);
			byte[] data = new byte[0];
			try {
				InputStream is = new FileInputStream(filename);
				data = new byte[is.available()];
				is.read(data);
				is.close();
			} catch (Exception e) {
				logger.error("", e);
			}
	    	try {
	    		IOTool.deleteFolder(new File(dirTemp));
	    	} catch (Exception e) {
	    		logger.error("", e);
	    	}
	    	
			response.setHeader("Content-Type", "application/force-download");
			response.setHeader("Content-Description", "File Transfer");
			response.setHeader("Content-Disposition", "attachment; filename=" + new File(filename).getName());
			response.setHeader("Content-Transfer-Encoding", "binary");
			response.getOutputStream().write(data);
			baseRequest.setHandled(true);
	    	
	    	return;
		}
		
		Map args = newArgs();
		Map data = new HashMap();
		data.put("page_title", pagetitle);
		data.put("eid", eid);
		data.put("folders", loadTree());
		args.put("data", data);
		loadPage("open-engine.vm", args);
	}
	
    private void deleteLog(FileItem fitem) {
    	LogItem pat = controller.newLogItem(engine);
    	List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.PARENT, fitem.getId())), Integer.MAX_VALUE);
    	for (int i = 0; i < results.size(); i++) {
    		LogItem li = controller.newLogItem(engine);
    		li.fromString(results.get(i).toString());
    		LogData ld = controller.newLogData(engine);
    		ld.setId(li.getData());
    		ld.delete();
    		li.delete();
    	}
    }
	
    private void export(List<Folder> folders, List<FileItem> fitems, String filename) {
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
	
	private List loadTree() {
    	Map<String, List> mapItem = new HashMap<String, List>();
    	mapItem.put("", new ArrayList());
    	Folder pat = controller.newFolder(engine);
    	List<SEntity> results = pat.search(pat.getKind(), pat.newMatchAllDocsQuery(), pat.newSort(pat.newSortField(pat.LEVEL, pat.sortFieldInteger(), false), pat.newSortField(pat.PARENT, pat.sortFieldString(), false), pat.newSortField(pat.NAME, pat.sortFieldString(), false)), Integer.MAX_VALUE);
    	for (int i = 0; i < results.size(); i++) {
    		Folder folder = controller.newFolder(engine);
    		folder.fromString(results.get(i).toString());
    		List plist = new ArrayList();
    		if (mapItem.containsKey(folder.getParent())) {
    			plist = mapItem.get(folder.getParent());
    		} else {
    			mapItem.put(folder.getParent(), plist);
    		}
    		Map item = new HashMap();
    		item.put("id", folder.getId());
    		item.put("name", folder.getName());
    		item.put("level", folder.getLevel() + "");
    		item.put("parent", folder.getParent());
    		plist.add(item);
    	}
		List tag = new ArrayList();
		loadTree(tag, mapItem, "", "");
		for (int i = 0; i < tag.size(); i++) {
			Map item = (Map)tag.get(i);
		}
		return tag;
	}
	
	private void loadTree(List tag, Map<String, List> mapItem, String parent, String path) {
		if (!mapItem.containsKey(parent)) return;
		List plist = mapItem.get(parent);
		for (int i = 0; i < plist.size(); i++) {
			Map item = (Map)plist.get(i);
			item.put("path", path + "/" + item.get("id"));
			tag.add(item);
			loadTree(tag, mapItem, item.get("id") + "", item.get("path") + "");
		}
	}
	
    private List loadTable(String pid) {
    	List tag = new ArrayList();
    	FileItem pat = controller.newFileItem(engine);
    	List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), pat.newSort(pat.newSortField(pat.NAME, pat.sortFieldString(), false)) , Integer.MAX_VALUE);
    	for (int i = 0; i < results.size(); i++) {
    		FileItem fitem = controller.newFileItem(engine);
    		fitem.fromString(results.get(i).toString());
    		Map item = new HashMap();
    		item.put("id", fitem.getId());
    		item.put("name", fitem.getName());
    		tag.add(item);
    	}
    	return tag;
    }
	
    private void deleteFolder(Folder folder) {
    	deleteFiles(folder);
    	deleteChildFolder(folder);
    	if (folder == null) return;
    	folder.delete();
    }
    
    private void deleteFiles(Folder parent) {
    	String pid = "";
    	if (parent != null) {
    		pid = parent.getId();
    	}
    	FileItem pat = controller.newFileItem(engine);
    	List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), Integer.MAX_VALUE);
    	for (int i = 0; i < results.size(); i++) {
    		FileItem fitem = controller.newFileItem(engine);
    		fitem.fromString(results.get(i).toString());
    		FileData fdata = controller.newFileData(engine);
    		fdata.load(fitem.getData());
    		fdata.delete();
    		fitem.delete();
    		deleteLog(fitem);
    	}
    }
    
    private void deleteChildFolder(Folder parent) {
    	String pid = "";
    	if (parent != null) {
    		pid = parent.getId();
    	}
    	int level = 1;
    	if (parent != null) {
    		level = parent.getLevel() + 1;
    	}
    	Folder pat = controller.newFolder(engine);
    	BooleanQuery bq = pat.newBooleanQuery();
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newIntegerRangeQuery(pat.LEVEL, level, level, true, true), pat.occurMust()));
    	List<SEntity> results = pat.search(pat.getKind(), bq, Integer.MAX_VALUE);
    	for (int i = 0; i < results.size(); i++) {
    		Folder folder = controller.newFolder(engine);
    		folder.fromString(results.get(i).toString());
    		deleteFolder(folder);
    	}
    }
    
}
