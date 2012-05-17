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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.lucene.search.BooleanQuery;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.session.SessionHandler;

import com.orinus.Controller;
import com.orinus.resource.Files;
import com.orinus.schema.Engine;
import com.orinus.schema.FileData;
import com.orinus.schema.FileItem;
import com.orinus.schema.Folder;
import com.orinus.schema.LogData;
import com.orinus.schema.LogItem;
import com.orinus.script.LuceneHandler;
import com.orinus.script.Machine;
import com.orinus.script.RemoteHandler;
import com.orinus.script.safe.jetty.SRequest;
import com.orinus.script.safe.jetty.SResponse;
import com.orinus.script.safe.lucene.SEntity;

public class Router extends SessionHandler {

    private static Logger logger = Logger.getLogger(Router.class);
	
	private Controller controller;
	private Engine engine;
	
	public Router() {
		super();
		controller = new Controller();
	}
	
	@Override
	public void doHandle(String path, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			if ("/api.jsb".equals(path)) {
				new APIPage(baseRequest, request, response).execute();
				return;
			}
			controller.loadConfig();
			String host = request.getServerName();
			String system = controller.getSystem();
			String root = "/";
			if (controller.preservedHost(host)) {
				if (path.indexOf("/" + system + "/") >= 0 || path.equals("/" + system)) {
					routeSystem(path, baseRequest, request, response);
					return;
				}
				String[] fields = path.substring(1).split("/");
				String folder = fields[0];
				engine = findEngineByFolder(folder);
				if (engine != null) {
					root += engine.getFolder() + "/";
				}
			} else {
				engine = findEngineByDomain(host);
				if (engine == null) return;
			}
			String t_path = path.substring(root.length());
			String tpx = root;
			String tcp = t_path;
			String[] fields = t_path.split("/");
			Folder parent = null;
			int level = 1;
			String pid = "";
			if (checkRewrite(tpx, tcp, pid, baseRequest, request, response)) return;
			boolean found = false;
			if (t_path.length() > 0) {
				for (int i = 0; i < fields.length - 1; i++) {
					parent = findFolderByName(pid, level, fields[i], false);
					if (parent == null) return;
					level++;
					pid = parent.getId();
					tpx += fields[i] + "/";
					tcp = tcp.substring(fields[i].length() + 1);
					if (checkRewrite(tpx, tcp, pid, baseRequest, request, response)) return;
				}
				Folder tmp = findFolderByName(pid, level, fields[fields.length - 1], false);
				if (tmp != null) {
					parent = tmp;
					level++;
					pid = parent.getId();
					found = true;
					tpx += fields[fields.length - 1] + "/";
					tcp = tcp.substring(fields[fields.length - 1].length() + 1);
					if (checkRewrite(tpx, tcp, pid, baseRequest, request, response)) return;
				}
			} else {
				found = true;
			}
			FileItem fitem = null;
			if (found) {
				fitem = findFileByName(pid, "index.jsb", false);
				if (fitem == null) {
					fitem = findFileByName(pid, "index.htm", false);
				}
				if (fitem == null) {
					fitem = findFileByName(pid, "index.html", false);
				}
			} else {
				fitem = findFileByName(pid, fields[fields.length - 1], false);
			}
			if (fitem == null) return;
			FileData fdata = controller.newFileData(engine);
			fdata.load(fitem.getData());
	        String name = fitem.getName();
	        int pos = name.lastIndexOf(".");
	        String ext = "";
	        if (pos >= 0) {
	        	ext = name.substring(pos);
	        }
	        baseRequest.setHandled(true);
	        if (".jsb".equalsIgnoreCase(ext)) {
	        	try {
		        	Machine machine = new Machine(new DataHandler(pid, level, fitem.getId()), engine);
		        	String js = new String(fdata.getData(), "UTF-8");
		        	List<String> included = new ArrayList<String>();
		        	js = includeFile(js, pid, level, included);
		        	Map args = new HashMap();
		        	args.put("request", new SRequest(request, engine));
		        	args.put("response", new SResponse(response));
		        	args.put("root", root);
		        	args.put("folder_id", pid);
		        	args.put("folder_path", tpx);
		        	args.put("file_id", fitem.getId());
		        	args.put("file_name", fitem.getName());
		        	int to = controller.getTimeout();
		        	if (engine != null) {
		        		to = engine.getTimeout();
		        	}
		        	Machine.run(machine, js, args, to);
	        	} catch (Exception e) {
	    			Writer w = new StringWriter();
	    			e.printStackTrace(new PrintWriter(w));
	        		response.sendError(response.SC_INTERNAL_SERVER_ERROR, w.toString());
	        	}
	        } else {
	        	response.getOutputStream().write(fdata.getData());
	        }
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	private boolean checkRewrite(String root, String path, String pid, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		boolean tag = false;
		FileItem fitem = findFileByName(pid, ".htaccess", true);
		if (fitem == null) return tag;
		FileData fdata = controller.newFileData(engine);
		fdata.load(fitem.getData());
		try {
			String source = new String(fdata.getData(), "UTF-8");
			String[] lines = source.split("\n");
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i].trim();
				if (line.length() == 0) continue;
				int pos = line.indexOf(" ");
				if (pos < 0) continue;
				String find = line.substring(0, pos).trim();
				String rep = line.substring(pos).trim();
				String tmp = path.replaceAll(find, rep);
				if (!path.equals(tmp)) {
					String tp = tmp;
					String tq = "";
					pos = tmp.indexOf("?");
					if (pos >= 0) {
						tp = tmp.substring(0, pos);
						tq = tmp.substring(pos + 1);
					}
					doHandle(root + tp, baseRequest, new RequestWraper(request, root, tp, tq), response);
					tag = true;
					break;
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		
		return tag;
	}
	
	private class RequestWraper extends HttpServletRequestWrapper {

		private String root;
		private String path;
		private String query;
		private Map<String, String> mq;
		
		public RequestWraper(HttpServletRequest request, String root, String path, String query) {
			super(request);
			this.root = root;
			this.path = path;
			this.query = query;
			this.mq = new HashMap<String, String>();
			parseQueryString();
		}

		public String getParameter(String name) {
			String tag = super.getParameter(name);
			if (tag == null) {
				tag = mq.get(name);
			}
			return tag;
		}

		public String getQueryString() {
			return query;
		}
		
		private void parseQueryString() {
			String[] fields = query.split("&");
			for (int i = 0; i < fields.length; i++) {
				String fd = fields[i];
				int pos = fd.indexOf("=");
				if (pos < 0) continue;
				String name = fd.substring(0, pos);
				String val = fd.substring(pos + 1);
				mq.put(name, val);
			}
		}
		
	}
	
	public void routeSystem(String path, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String system = controller.getSystem();
		path = path.substring(system.length() + 1);
		BasePage page = null;
		if ("".equals(path) || "/".equals(path) || "/index.jsb".equals(path)) {
			page = new HomePage(baseRequest, request, response);
		} else if ("/add-engine.jsb".equals(path)) {
			page = new AddEnginePage(baseRequest, request, response);
		} else if ("/edit-engine.jsb".equals(path)) {
			page = new EditEnginePage(baseRequest, request, response);
		} else if ("/open-engine.jsb".equals(path)) {
			page = new OpenEnginePage(baseRequest, request, response);
		} else if ("/add-folder.jsb".equals(path)) {
			page = new AddFolderPage(baseRequest, request, response);
		} else if ("/edit-folder.jsb".equals(path)) {
			page = new EditFolderPage(baseRequest, request, response);
		} else if ("/add-file.jsb".equals(path)) {
			page = new AddFilePage(baseRequest, request, response);
		} else if ("/edit-file.jsb".equals(path)) {
			page = new EditFilePage(baseRequest, request, response);
		} else if ("/import.jsb".equals(path)) {
			page = new ImportPage(baseRequest, request, response);
		} else if ("/settings.jsb".equals(path)) {
			page = new SettingsPage(baseRequest, request, response);
		} else if ("/schedule.jsb".equals(path)) {
			page = new SchedulePage(baseRequest, request, response);
		} else if ("/log.jsb".equals(path)) {
			page = new LogPage(baseRequest, request, response);
		} else {
			if (!path.endsWith(".vm") && !path.endsWith(".lang")) {
				try {
					response.getOutputStream().write(loadResource(path));
			        baseRequest.setHandled(true);
				} catch (Exception e) {
				}
			}
			return;
		}
		try {
			page.execute();
		} catch (Exception e) {
			Writer w = new StringWriter();
			e.printStackTrace(new PrintWriter(w));
    		response.sendError(response.SC_INTERNAL_SERVER_ERROR, w.toString());
		}
	}

	private byte[] loadResource(String path) throws Exception {
		String filename = "/com/orinus/web/resource" + path;
		InputStream is =  Files.class.getResourceAsStream(filename);
		byte[] tag = new byte[is.available()];
		is.read(tag);
		return tag;
	}
	
    private FileItem findFileByName(String pid, String name, boolean noscope) {
    	FileItem pat = controller.newFileItem(engine);
    	BooleanQuery bq = pat.newBooleanQuery();
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.NAME, name)), pat.occurMust()));
    	if (!noscope) {
        	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PUBLISHED, "false")), pat.occurMustNot()));
    	}
    	List<SEntity> results = pat.search(pat.getKind(), bq, 1);
    	FileItem tag = null;
    	if (results.size() > 0) {
    		pat.fromString(results.get(0).toString());
    		tag = pat;
    	}
    	return tag;
    }
	
    private Folder findFolderByName(String pid, int level, String name, boolean noscope) {
    	Folder pat = controller.newFolder(engine);
    	BooleanQuery bq = pat.newBooleanQuery();
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.NAME, name)), pat.occurMust()));
    	if (!noscope) {
        	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PUBLISHED, "false")), pat.occurMustNot()));
    	}
    	bq.add(pat.newBooleanClause(pat.newIntegerRangeQuery(pat.LEVEL, level, level, true, true), pat.occurMust()));
    	List<SEntity> results = pat.search(pat.getKind(), bq, 1);
    	Folder tag = null;
    	if (results.size() > 0) {
    		pat.fromString(results.get(0).toString());
    		tag = pat;
    	}
    	return tag;
    }
	
	private Engine findEngineByDomain(String domain) {
		if (domain.trim().length() == 0) return null;
		Engine pat = controller.newEngine();
		Engine tag = null;
		List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.DOMAIN, domain)), 1);
		if (results.size() > 0) {
			pat.fromString(results.get(0).toString());
			tag = pat;
		}
		return tag;
	}

	private Engine findEngineByFolder(String folder) {
		if (folder.trim().length() == 0) return null;
		Engine pat = controller.newEngine();
		Engine tag = null;
		List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.FOLDER, folder)), 1);
		if (results.size() > 0) {
			pat.fromString(results.get(0).toString());
			tag = pat;
		}
		return tag;
	}
	
	private String includeFile(String js, String m_pid, int m_level, List<String> included) throws Exception {
		String tag = "";
		
		int posA = js.indexOf("#include");
		if (posA < 0) return js;
		int posB = js.indexOf("#end", posA);
		if (posB < 0) return js;
		
		String tmp = js.substring(posA + 8, posB);
		String[] lines = tmp.split("\n");
		tag = js.substring(posB + 4);
		
		for (int i = 0; i < lines.length; i++) {
			String filename = lines[i].trim().replaceAll("\r", "").replaceAll("\n", "");
			if (filename.length() == 0) continue;
			FolderMark fm = new FolderMark();
			fm.pid = m_pid;
			fm.level = m_level;
			tmp = new String(loadFileB(filename, fm, true), "UTF-8");
			if (!fm.success) {
				throw new Exception("Unable to load file [" + filename + "]");
			}
			if (included.indexOf(fm.fid) >= 0) continue;
			included.add(fm.fid);
			tmp = includeFile(tmp, fm.pid, fm.level, included);
			tag += "\n" + tmp;
		}
		
		return tag;
	}
	
	private byte[] loadFileB(String path, FolderMark fm, boolean noscope) {
    	byte[] tag = new byte[0];
    	if (path.startsWith("/")) {
    		fm.level = 1;
    		fm.pid = "";
    		path = path.substring(1);
    	}
    	String[] fields = path.split("/");
		Folder parent = null;
		for (int i = 0; i < fields.length - 1; i++) {
			String name = fields[i];
			if ("..".equals(name)) {
				if (fm.pid.length() == 0) return tag;
				Folder tmp = controller.newFolder(engine);
				tmp.load(fm.pid);
				fm.pid = tmp.getParent();
				fm.level = tmp.getLevel();
				continue;
			}
			parent = findFolderByName(fm.pid, fm.level, name, noscope);
			if (parent == null) return tag;
			fm.level++;
			fm.pid = parent.getId();
		}
		FileItem fitem = findFileByName(fm.pid, fields[fields.length - 1], noscope);
		if (fitem == null) return tag;
		FileData fdata = controller.newFileData(engine);
		fdata.load(fitem.getData());
		fm.success = true;
		fm.fid = fitem.getId();
    	return fdata.getData(); 
	}
	
	private class FolderMark {
		public String pid = "";
		public int level = 1;
		public boolean success = false;
		public String fid = "";
	}
	
	private class DataHandler extends Machine.Handler {
		
		private int m_level = 1;
		private String m_pid = "";
		private String m_fid = "";
		
		public DataHandler(String pid, int level, String fid) {
			super();
			this.m_pid = pid;
			this.m_level = level;
			this.m_fid = fid;
		}
		
        public void debug(String message) {
        	saveLog("DEBUG", message);
        }
        
        public void error(String message) { 
        	saveLog("ERROR", message);
        }
        
        public void fatal(String message) { 
        	saveLog("FATAL", message);
        }
        
        public void info(String message) { 
        	saveLog("INFO", message);
        }
        
        private void saveLog(String stage, String message) {
        	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        	String line = "\r\n[" + sdf.format(new Date()) + "] " + stage + "\r\n" + message;
        	saveLog(line);
        }
        
        private void saveLog(String line) {
        	try {
        		LogItem pat = controller.newLogItem(engine);
        		BooleanQuery bq = pat.newBooleanQuery();
        		bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PARENT, m_fid)), pat.occurMust()));
        		bq.add(pat.newBooleanClause(pat.newLongRangeQuery(pat.SIZE, (long)0, (long)1024 * 1024, true, true), pat.occurMust()));
        		List<SEntity> results = pat.search(pat.getKind(), bq, pat.newSort(pat.newSortField(pat.CREATED, pat.sortFieldLong(), true)), 1);
        		LogData ld = controller.newLogData(engine);
        		if (results.size() == 0) {
        			pat.setId(controller.uniqid());
        			pat.setParent(m_fid);
        			ld.setId(controller.uniqid());
        			pat.setData(ld.getId());
        		} else {
            		pat.fromString(results.get(0).toString());
            		ld.load(pat.getData());
        		}
        		String output = new String(ld.getData(), "UTF-8");
        		output += line;
        		pat.setSize(output.length());
        		ld.setData(output.getBytes("UTF-8"));
        		ld.save();
        		pat.save();
        	} catch (Exception e) {
        	}
        }
        
        public SEntity.Handler getEntityHandler() {
        	String t_data = controller.getDatDir();
        	String t_engine = new File(t_data, "common").getAbsolutePath();
        	double t_quota = controller.getQuota();
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
		
        public byte[] loadFile(String path, boolean noscope) {
        	FolderMark fm = new FolderMark();
        	fm.pid = m_pid;
        	fm.level = m_level;
        	return loadFileB(path, fm, noscope);
        }
        
        public Engine newEngine() {
        	if (engine == null) return controller.newEngine();
        	return null; 
        }
        
        public Folder newFolder() {
        	return controller.newFolder(engine); 
        }
        
        public Folder newFolder(Engine t_engine) {
        	if (engine == null) return controller.newFolder(t_engine);
        	return null; 
        }
        
        public FileItem newFileItem() {
        	return controller.newFileItem(engine);
        }
        
        public FileItem newFileItem(Engine t_engine) {
        	if (engine == null) return controller.newFileItem(t_engine);
        	return null; 
        }
        
        public FileData newFileData() {
        	return controller.newFileData(engine);
        }
        
        public FileData newFileData(Engine t_engine) {
        	if (engine == null) return controller.newFileData(t_engine);
        	return null; 
        }
        
        public SEntity newEntity(Engine t_engine) {
        	if (engine == null) return controller.newEntity(t_engine);
        	return null; 
        }
        
        public LogItem newLogItem(Engine t_engine) { 
        	return controller.newLogItem(t_engine); 
        }
        
        public LogItem newLogItem() { 
        	return controller.newLogItem(engine); 
        }
        
        public LogData newLogData(Engine t_engine) { 
        	return controller.newLogData(t_engine); 
        }
        
        public LogData newLogData() { 
        	return controller.newLogData(engine); 
        }
        
	}
	
}
