package com.orinus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.lucene.search.BooleanQuery;

import com.orinus.schema.Engine;
import com.orinus.schema.FileData;
import com.orinus.schema.FileItem;
import com.orinus.schema.Folder;
import com.orinus.schema.LogData;
import com.orinus.schema.LogItem;
import com.orinus.script.LuceneHandler;
import com.orinus.script.Machine;
import com.orinus.script.RemoteHandler;
import com.orinus.script.safe.lucene.SEntity;

public class Spider {

    private static Logger logger = Logger.getLogger(Spider.class);
	
	private Controller controller;
	private String today;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private Map<String, Integer> mapRT = new HashMap<String, Integer>();
	
	public Spider() {
		controller = new Controller();
		today = sdf.format(new Date());
	}
	
	public void run() {
		controller.loadConfig();
		Timer timer = new Timer();
		timer.schedule(new CleanTask(), 10);
		while (!controller.getConfig().getBoolean(Config.STOP)) {
			try {
				runSpiders();
			} catch (Exception e) {
				logger.error("", e);
			}
			try {
				Thread.sleep(1000 * 60);
			} catch (Exception e) {
				logger.error("", e);
			}
			controller.loadConfig();
		}
	}

	private class CleanTask extends TimerTask {
		@Override
		public void run() {
			while (!controller.getConfig().getBoolean(Config.STOP)) {
				try {
					clean();
				} catch (Exception e) {
					logger.error("", e);
				}
				try {
					Thread.sleep(1000 * 60);
				} catch (Exception e) {
					logger.error("", e);
				}
				controller.loadConfig();
			}
		}
	}
	
	private void clean() throws Exception {
		String root = controller.getTempRoot();
		File froot = new File(root);
		File[] children = froot.listFiles();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -2);
		String today = sdf.format(cal.getTime());
		for (int i = 0; i < children.length; i++) {
			File child = children[i];
			if (!child.isDirectory()) continue;
			if (today.compareTo(child.getName()) > 0) {
				try {
					IOTool.deleteFolder(child);
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		
		root = controller.getDatDir();
		froot = new File(root);
		children = froot.listFiles();
		for (int i = 0; i < children.length; i++) {
			File child = children[i];
			if (!child.isDirectory()) continue;
			if ("temp".equals(child.getName()) || "common".equals(child.getName())) continue;
			try {
				Engine engine = controller.newEngine();
				engine.load(child.getName());
				if (engine.getId().length() == 0) {
					IOTool.deleteFolder(child);
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}
	}
	
	private void runSpiders() throws Exception {
		List<Engine> engines = new ArrayList<Engine>();
		engines.add(null);
		Engine epat = controller.newEngine();
		List<SEntity> results = epat.search(epat.getKind(), epat.newMatchAllDocsQuery(), Integer.MAX_VALUE);
		for (int i = 0; i < results.size(); i++) {
			Engine eng = controller.newEngine();
			eng.fromString(results.get(i).toString());
			engines.add(eng);
		}
		if (!today.equals(sdf.format(new Date()))) {
			mapRT = new HashMap<String, Integer>();
			today = sdf.format(new Date());
		}
		Calendar cal = Calendar.getInstance();
		int minute = cal.get(Calendar.MINUTE);
		int hour = cal.get(Calendar.HOUR);
		int day = cal.get(Calendar.DATE);
		int month = cal.get(Calendar.MONTH + 1);
		int year = cal.get(Calendar.YEAR);
		for (int i = 0; i < engines.size(); i++) {
			controller.loadConfig();
			Engine engine = engines.get(i);
			String eid = "";
			boolean runscript = controller.getConfig().getBoolean(Config.RUN_SCRIPT);
			int runtime = controller.getRunTime();
			if (engine != null) {
				runscript = engine.getRunScript();
				runtime = engine.getRunTime();
				eid = engine.getId();
			}
			if (!runscript) continue;
			FileItem fpat = controller.newFileItem(engine);
			results = fpat.search(fpat.getKind(), fpat.newTermQuery(fpat.newTerm(fpat.SCHEDULED, "true")), Integer.MAX_VALUE);
			for (int j = 0; j < results.size(); j++) {
				FileItem fitem = controller.newFileItem(engine);
				fitem.fromString(results.get(j).toString());
				int to = fitem.getTimeout();
				if (to <= 0) to = 1;
				if (to > 60 * 24) to = 60 * 24;
				if (mapRT.containsKey(eid)) {
					if (to + mapRT.get(eid) > runtime) continue;
				} else {
					if (to > runtime) continue;
				}
				int n = parseInt(fitem.getMinute(), 0, 59, -2);
				int h = parseInt(fitem.getHour(), 0, 59, -2);
				int d = parseInt(fitem.getDay(), 1, 31, -2);
				int m = parseInt(fitem.getMonth(), 1, 12, -2);
				int y = parseInt(fitem.getYear(), 2000, 3000, -2);
				boolean found = true;
				if (y != -1 && year != y) found = false;
				if (m != -1 && month != m) found = false;
				if (d != -1 && day != d) found = false;
				if (h != -1 && hour != h) found = false;
				if (n != -1 && minute != n) found = false;
				if (found) {
					if (mapRT.containsKey(eid)) {
						mapRT.put(eid, to + mapRT.get(eid));
					} else {
						mapRT.put(eid, to);
					}
					Timer timer = new Timer();
					timer.schedule(new SpiderTask(engine, fitem), 10);
				}
			}
		}
	}
	
	private int parseInt(String src, int min, int max, int defVal) {
		int tag = defVal;
		if ("*".equals(src.trim())) {
			tag = -1;
		} else {
			try {
				tag = Integer.parseInt(src);
				if (tag < min || tag > max) tag = -1;
			} catch (Exception e) {
				tag = defVal;
			}
		}
		return tag;
	}
	
	private class SpiderTask extends TimerTask {
		
		private Engine engine;
		private FileItem fitem;
		
		public SpiderTask(Engine engine, FileItem fitem) {
			this.engine = engine;
			this.fitem = fitem;
		}
		
		@Override
		public void run() {
			int timeout = fitem.getTimeout();
			if (timeout <= 0) timeout = 1;
			if (timeout > 60 * 24) timeout = 60 * 24;
		    try {
	        	String pid = "";
	        	int level = 1;
	        	Folder folder = controller.newFolder(engine);
	        	folder.load(fitem.getParent());
	        	if (folder.getId().length() > 0) {
	        		pid = folder.getId();
	        		level = folder.getLevel();
	        	}
	        	FileData fdata = controller.newFileData(engine);
	        	fdata.load(fitem.getData());
	        	
	        	Machine machine = new Machine(new DataHandler(engine, pid, level, fitem.getId()), engine);
	        	String js = new String(fdata.getData(), "UTF-8");
	        	List<String> included = new ArrayList<String>();
	        	js = includeFile(js, pid, level, included, engine);
	        	Map args = new HashMap();
	        	Machine.run(machine, js, args, timeout);
		    } catch (Exception e) {
		    	logger.error("", e);
		    }			
		}
		
	}
	
	private String includeFile(String js, String m_pid, int m_level, List<String> included, Engine engine) throws Exception {
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
			tmp = new String(loadFileB(filename, fm, true, engine), "UTF-8");
			if (!fm.success) {
				throw new Exception("Unable to load file [" + filename + "]");
			}
			if (included.indexOf(fm.fid) >= 0) continue;
			included.add(fm.fid);
			tmp = includeFile(tmp, fm.pid, fm.level, included, engine);
			tag += "\n" + tmp;
		}
		
		return tag;
	}
	
	private byte[] loadFileB(String path, FolderMark fm, boolean noscope, Engine engine) {
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
			parent = findFolderByName(fm.pid, fm.level, name, noscope, engine);
			if (parent == null) return tag;
			fm.level++;
			fm.pid = parent.getId();
		}
		FileItem fitem = findFileByName(fm.pid, fields[fields.length - 1], noscope, engine);
		if (fitem == null) return tag;
		FileData fdata = controller.newFileData(engine);
		fdata.load(fitem.getData());
		fm.success = true;
		fm.fid = fitem.getId();
    	return fdata.getData(); 
	}
	
    private FileItem findFileByName(String pid, String name, boolean noscope, Engine engine) {
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
	
    private Folder findFolderByName(String pid, int level, String name, boolean noscope, Engine engine) {
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
	
	private class DataHandler extends Machine.Handler {
		
		private int m_level = 1;
		private String m_pid = "";
		private Engine engine;
		private String m_fid = "";
		
		public DataHandler(Engine engine, String pid, int level, String fid) {
			super();
			this.engine = engine;
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
        	return loadFileB(path, fm, noscope, engine);
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
	
	private class FolderMark {
		public String pid = "";
		public int level = 1;
		public boolean success = false;
		public String fid = "";
	}
	
}
