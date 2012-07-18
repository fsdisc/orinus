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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.http.Cookie;

import org.apache.lucene.search.BooleanQuery;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.orinus.Controller;
import com.orinus.IOTool;
import com.orinus.schema.Engine;
import com.orinus.schema.FileData;
import com.orinus.schema.FileItem;
import com.orinus.schema.Folder;
import com.orinus.schema.LogData;
import com.orinus.schema.LogItem;
import com.orinus.script.safe.jsoup.SJsoup;
import com.orinus.script.safe.lucene.SEntity;
import com.orinus.script.safe.mysql.SMySQL;
import com.orinus.script.safe.zip.SZip;

public class Machine {

	private static boolean langdetect = false;
    private Handler handler;
    private Engine engine;
    private Controller controller;
    
    public static void run(Machine env, String js, Map args, int timeout) throws Exception {
        try {
            Context cx = new SFactory().enterContext();
            SContext mcx = (SContext)cx;
            mcx.setTimeout(timeout);
            cx.setClassShutter(new ClassShutter() {
                public boolean visibleToScripts(String className) {  
                    if ("com.orinus.script.Machine".equals(className)) return true;
                    if ("java.lang.String".equals(className)) return true;
                    if ("java.lang.Object".equals(className)) return true;
                    if ("java.util.HashMap".equals(className)) return true;
                    if ("java.util.ArrayList".equals(className)) return true;
                    if ("java.lang.Byte".equals(className)) return true;
                    if ("java.lang.Short".equals(className)) return true;
                    if ("java.lang.Integer".equals(className)) return true;
                    if ("java.lang.Long".equals(className)) return true;
                    if ("java.lang.Float".equals(className)) return true;
                    if ("java.lang.Double".equals(className)) return true;
                    if ("java.lang.Boolean".equals(className)) return true;
                    if ("java.lang.Character".equals(className)) return true;
                    if ("java.util.Collection".equals(className)) return true;
                    if ("java.util.List".equals(className)) return true;
                    if ("java.util.Map".equals(className)) return true;
                    if ("java.util.LinkedHashMap".equals(className)) return true;
                    if ("java.util.Iterator".equals(className)) return true;
                    if ("java.util.ListIterator".equals(className)) return true;
                    if ("java.lang.Iterable".equals(className)) return true;
                    if ("java.net.URL".equals(className)) return true;
                    if (className.startsWith("org.jsoup.nodes.")) return true;
                    if (className.startsWith("org.jsoup.select.")) return true;
                    if (className.startsWith("org.jsoup.safety.")) return true;
                    if (className.startsWith("org.jsoup.parser.")) return true;
                    if (className.startsWith("com.orinus.script.safe.")) return true;
                    if ("org.jsoup.helper.HttpConnection$Response".equals(className)) return true;
                    if ("java.util.Date".equals(className)) return true;
                    if ("java.text.SimpleDateFormat".equals(className)) return true;
                    if ("org.apache.lucene.search.Query".equals(className)) return true;
                    if ("org.apache.lucene.search.Filter".equals(className)) return true;
                    if ("org.apache.lucene.search.Sort".equals(className)) return true;
                    if ("org.apache.lucene.search.BooleanQuery".equals(className)) return true;
                    if ("org.apache.lucene.search.BooleanClause".equals(className)) return true;
                    if (className.startsWith("org.apache.lucene.search.BooleanClause$")) return true;
                    if ("org.apache.lucene.search.PhraseQuery".equals(className)) return true;
                    if ("org.apache.lucene.index.Term".equals(className)) return true;
                    if ("org.apache.lucene.search.MultiPhraseQuery".equals(className)) return true;
                    if ("org.apache.lucene.search.NGramPhraseQuery".equals(className)) return true;
                    if ("org.apache.lucene.search.NumericRangeQuery".equals(className)) return true;
                    if ("org.apache.lucene.search.PrefixQuery".equals(className)) return true;
                    if ("org.apache.lucene.search.TermQuery".equals(className)) return true;
                    if ("org.apache.lucene.search.TermRangeQuery".equals(className)) return true;
                    if ("org.apache.lucene.search.WildcardQuery".equals(className)) return true;
                    if ("org.apache.lucene.search.MatchAllDocsQuery".equals(className)) return true;
                    if ("org.apache.lucene.search.FieldValueFilter".equals(className)) return true;
                    if ("org.apache.lucene.search.NumericRangeFilter".equals(className)) return true;
                    if ("org.apache.lucene.search.PrefixFilter".equals(className)) return true;
                    if ("org.apache.lucene.search.QueryWrapperFilter".equals(className)) return true;
                    if ("org.apache.lucene.search.TermRangeFilter".equals(className)) return true;
                    if ("org.apache.lucene.search.SortField".equals(className)) return true;
                    if (className.startsWith("java.util.Collections")) return true;
                    if ("java.util.LinkedHashSet".equals(className)) return true;
                    if ("java.util.Locale".equals(className)) return true;
                    if ("java.util.TimeZone".equals(className)) return true;
                    if ("java.util.SimpleTimeZone".equals(className)) return true;
                    if ("java.util.Calendar".equals(className)) return true;
                    if ("java.util.GregorianCalendar".equals(className)) return true;
                    if ("javax.servlet.http.Cookie".equals(className)) return true;
                    if ("java.security.Principal".equals(className)) return true;
                    if ("com.orinus.schema.Engine".equals(className)) return true;
                    if ("com.orinus.schema.FileData".equals(className)) return true;
                    if ("com.orinus.schema.FileItem".equals(className)) return true;
                    if ("com.orinus.schema.Folder".equals(className)) return true;
                    if ("com.cybozu.labs.langdetect.Language".equals(className)) return true;
                    
                    return false;
                }
            });   
            Scriptable scope = cx.initStandardObjects();
            Object result = cx.evaluateString(scope, js, "<js>", 1, null);
            Object fObj = scope.get("main", scope);
            if (!(fObj instanceof Function)) {
                throw new Exception("main() is undefined or not a function.");
            } else {
                Object functionArgs[] = { env, args };
                Function f = (Function)fObj;
                result = f.call(cx, scope, scope, functionArgs);
            }   
        } catch (Exception e) {
            throw e;
        } finally {
            Context.exit();   
        }
    }
 
    public String newString(String src) {
        return src;
    }
 
    public String newString(byte[] src, String charset) throws Exception {
        return new String(src, charset);
    }
 
    public HashMap newHashMap() {
        return new HashMap();
    }
 
    public ArrayList newArrayList() {
        return new ArrayList();
    }
 
    public Byte newByte(byte src) {
        return (Byte)src;
    }
 
    public Short newShort(short src) {
        return (Short)src;
    }
 
    public Integer newInteger(int src) {
        return (Integer)src;
    }
 
    public Long newLong(long src) {
        return (Long)src;
    }
 
    public Float newFloat(float src) {
        return (Float)src;
    }
 
    public Double newDouble(double src) {
        return (Double)src;
    }
 
    public Boolean newBoolean(boolean src) {
        return (Boolean)src;
    }
 
    public Character newCharacter(char src) {
        return (Character)src;
    }
 
    public List getKeys(Map src) {
        List tag = new ArrayList();
        for (Object key : src.keySet()) {
            tag.add(key);
        }
        return tag;
    }
 
    public URL newURL(String protocol, String host, int port, String file) throws Exception {
        return new URL(protocol, host, port, file);
    }

    public URL newURL(String protocol, String host, String file) throws Exception {
        return new URL(protocol, host, file);
    }
 
    public URL newURL(String spec) throws Exception {
        return new URL(spec);
    }
 
    public URL newURL(URL context, String spec) throws Exception {
        return new URL(context, spec);
    }
 
    public SJsoup newJsoup() {
        return new SJsoup();
    }
 
    public String encodeURL(String src, String charset) {
        try {
            return URLEncoder.encode(src, charset);
        } catch (Exception e) {
            return "";
        }
    }

    public String decodeURL(String src, String charset) {
        try {
            return URLDecoder.decode(src, charset);
        } catch (Exception e) {
            return "";
        }
    }

    public SEntity newEntity() {
    	SEntity.Handler seh = null;
    	if (handler != null) {
    		seh = handler.getEntityHandler();
    	}
    	return new SEntity(seh);
    }

    public String uniqid() {
    	return UUID.randomUUID().toString().replaceAll("-", "");
    }
    
    public String suniqid() {
        Random random = new Random();
        return Long.toString(Math.abs(random.nextLong()), 36);
    }
    
    public Date newDate() {
    	return new Date();
    }

    public Date newDate(long time) {
    	return new Date(time);
    }
    
    public SimpleDateFormat newDateFormat(String format) {
    	return new SimpleDateFormat(format);
    }
    
    public byte[] decodeBase64(byte[] b) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        InputStream b64is = javax.mail.internet.MimeUtility.decode(bais, "base64");
        byte[] tmp = new byte[b.length];
        int n = b64is.read(tmp);
        byte[] res = new byte[n];
        System.arraycopy(tmp, 0, res, 0, n);
        return res;
    }      

    public byte[] encodeBase64(byte[] b) throws Exception {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream b64os = javax.mail.internet.MimeUtility.encode(baos, "base64");
        b64os.write(b);
        b64os.close();
        return baos.toByteArray();
    }
    
    public List<Object> setToList(Set src) {
    	List<Object> tag = new ArrayList<Object>();
    	for (Object item : src) {
    		tag.add(item);
    	}
    	return tag;
    }
    
    public SMySQL newMySQL() {
    	return new SMySQL();
    }
    
    public Locale newLocale(String language) {
    	return new Locale(language);
    }

    public Locale newLocale(String language, String country) {
    	return new Locale(language, country);
    }

    public Locale newLocale(String language, String country, String variant) {
    	return new Locale(language, country, variant);
    }
    
    public TimeZone newTimeZone(int rawOffset, String ID) {
    	return new SimpleTimeZone(rawOffset, ID);
    }

    public TimeZone newTimeZone(int rawOffset, String ID, int startMonth, int startDay, int startDayOfWeek, int startTime, int endMonth, int endDay, int endDayOfWeek, int endTime) {
    	return new SimpleTimeZone(rawOffset, ID, startMonth, startDay, startDayOfWeek, startTime, endMonth, endDay, endDayOfWeek, endTime);
    }

    public TimeZone newTimeZone(int rawOffset, String ID, int startMonth, int startDay, int startDayOfWeek, int startTime, int endMonth, int endDay, int endDayOfWeek, int endTime, int dstSavings) {
    	return new SimpleTimeZone(rawOffset, ID, startMonth, startDay, startDayOfWeek, startTime, endMonth, endDay, endDayOfWeek, endTime, dstSavings);
    }

    public TimeZone newTimeZone(int rawOffset, String ID, int startMonth, int startDay, int startDayOfWeek, int startTime, int startTimeMode, int endMonth, int endDay, int endDayOfWeek, int endTime, int endTimeMode, int dstSavings) {
    	return new SimpleTimeZone(rawOffset, ID, startMonth, startDay, startDayOfWeek, startTime, startTimeMode, endMonth, endDay, endDayOfWeek, endTime, endTimeMode, dstSavings);
    }
    
    public Calendar newCalendar() {
    	return Calendar.getInstance();
    }

    public Calendar newCalendar(Locale aLocale) {
    	return Calendar.getInstance(aLocale);
    }

    public Calendar newCalendar(TimeZone zone) {
    	return Calendar.getInstance(zone);
    }

    public Calendar newCalendar(TimeZone zone, Locale aLocale) {
    	return Calendar.getInstance(zone, aLocale);
    }

    public Cookie newCookie(String key, String value) {
    	return new Cookie(key, value);
    }

    public byte[] loadFile(String path) {
    	return loadFile(path, false);
    }
    
    public byte[] loadFile(String path, boolean noscope) {
    	if (handler != null) {
    		return handler.loadFile(path, noscope);
    	} else {
    		return new byte[0];
    	}
    }

    public String merge(String template, Map args) throws Exception {
    	VelocityEngine engine = new VelocityEngine();
    	engine.init();
    	VelocityContext ctx = new VelocityContext();
    	for (Object key : args.keySet()) {
    		ctx.put(key + "", args.get(key));
    	}
    	Writer writer = new StringWriter();
    	engine.evaluate(ctx, writer, "", template);
    	return writer.toString();
    }
    
    public String merge(byte[] template, Map args) throws Exception {
    	return merge(new String(template, "UTF-8"), args);
    }
    
    public String detectLang(String text) throws Exception {
    	if (!langdetect) {
    		DetectorFactory.loadProfile(new File(controller.getExtDir(), "langdetect").getAbsolutePath());
    		langdetect = true;
    	}
    	Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.detect();    	
    }
    
    public List<com.cybozu.labs.langdetect.Language> detectLangs(String text) throws Exception {
    	if (!langdetect) {
    		DetectorFactory.loadProfile(new File(controller.getExtDir(), "langdetect").getAbsolutePath());
    		langdetect = true;
    	}
    	Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.getProbabilities();    	
    }
    
    public void sleep(long ms) {
    	try {
    		Thread.sleep(ms);
    	} catch (Exception e) {
    	}
    }
    
    public Engine newEngine() {
    	if (handler != null) {
    		return handler.newEngine();
    	}
    	return null; 
    }
    
    public Folder newFolder() {
    	if (handler != null) {
    		return handler.newFolder();
    	}
    	return null; 
    }
    
    public Folder newFolder(Engine engine) {
    	if (handler != null) {
    		return handler.newFolder(engine);
    	}
    	return null; 
    }
    
    public FileItem newFileItem() {
    	if (handler != null) {
    		return handler.newFileItem();
    	}
    	return null; 
    }
    
    public FileItem newFileItem(Engine engine) {
    	if (handler != null) {
    		return handler.newFileItem(engine);
    	}
    	return null; 
    }
    
    public FileData newFileData() {
    	if (handler != null) {
    		return handler.newFileData();
    	}
    	return null; 
    }
    
    public FileData newFileData(Engine engine) {
    	if (handler != null) {
    		return handler.newFileData(engine);
    	}
    	return null; 
    }
    
    public SEntity newEntity(Engine engine) {
    	if (handler != null) {
    		return handler.newEntity(engine);
    	}
    	return null; 
    }

    public SEntity newEntity(Map<String, String> conn) {
    	return controller.newEntity(conn);
    }
    
    public LogItem newLogItem() {
    	if (handler != null) {
    		return handler.newLogItem();
    	}
    	return null; 
    }
    
    public LogItem newLogItem(Engine engine) {
    	if (handler != null) {
    		return handler.newLogItem(engine);
    	}
    	return null; 
    }

    public LogData newLogData() {
    	if (handler != null) {
    		return handler.newLogData();
    	}
    	return null; 
    }
    
    public LogData newLogData(Engine engine) {
    	if (handler != null) {
    		return handler.newLogData(engine);
    	}
    	return null; 
    }
    
    public void doImport(Folder parent, byte[] data) {
    	try {
        	Controller controller = new Controller();
        	String dirTemp = controller.getTempDir();
        	String filename = new File(dirTemp, "data.zip").getAbsolutePath();
        	OutputStream os = new FileOutputStream(filename);
        	os.write(data);
        	os.close();
        	ImExTool tool = new ImExTool(engine);
        	tool.importPackage(parent, filename);
    		IOTool.deleteFolder(new File(dirTemp));
    	} catch (Exception e) {
    	}
    }
    
    public byte[] doExport(List<Folder> folders, List<FileItem> fitems) {
    	byte[] tag = new byte[0];
    	try {
        	Controller controller = new Controller();
        	String dirTemp = controller.getTempDir();
        	String filename = new File(dirTemp, "data.zip").getAbsolutePath();
        	ImExTool tool = new ImExTool(engine);
    		tool.export(folders, fitems, filename);
    		InputStream is = new FileInputStream(filename);
    		tag = new byte[is.available()];
    		is.read(tag);
    		is.close();
    	} catch (Exception e) {
    	}
    	return tag;
    }
    
    public SZip newZip() {
    	return new SZip();
    }
    
    public void deleteFolder(Folder folder) {
    	deleteFiles(folder);
    	deleteChildFolder(folder);
    	if (folder == null) return;
    	folder.delete();
    }
    
    public void deleteFile(FileItem fitem) {
    	FileData fdata = controller.newFileData(engine);
    	fdata.setId(fitem.getData());
    	fdata.delete();
    	fitem.delete();
    	deleteLog(fitem);
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
    
    public Machine(Handler handler, Engine engine) {
        this.handler = handler;
        this.engine = engine;
        this.controller = new Controller();
    }
 
    public void debug(String message) { 
        if (handler != null) {
            handler.debug(message);
        }
    }
 
    public void error(String message) { 
        if (handler != null) {
            handler.error(message);
        }
    }
 
    public void fatal(String message) { 
        if (handler != null) {
            handler.fatal(message);
        }
    }
 
    public void info(String message) { 
        if (handler != null) {
            handler.info(message);
        }
    }
 
    public static class Handler {
  
        public void debug(String message) { }
        public void error(String message) { }
        public void fatal(String message) { }
        public void info(String message) { }
        public SEntity.Handler getEntityHandler() { return null; }
        public byte[] loadFile(String path, boolean noscope) { return new byte[0]; }
        public Engine newEngine() { return null; }
        public Folder newFolder() { return null; }
        public Folder newFolder(Engine engine) { return null; }
        public FileItem newFileItem() { return null; }
        public FileItem newFileItem(Engine engine) { return null; }
        public FileData newFileData() { return null; }
        public FileData newFileData(Engine engine) { return null; }
        public SEntity newEntity(Engine engine) { return null; }
        public LogItem newLogItem(Engine engine) { return null; }
        public LogItem newLogItem() { return null; }
        public LogData newLogData(Engine engine) { return null; }
        public LogData newLogData() { return null; }
  
    }
 
    public static class SContext extends Context {
    	
    	protected int timeout = 1;
    	public long startTime;
    	
    	public int getTimeout() {
    		return timeout;
    	}
    	
    	public void setTimeout(int src) {
    		if (src <= 0) src = 1;
    		if (src > 60 * 24) src = 60 * 24;
    		timeout = src;
    	}
    	
    }
    
    public static class SFactory extends ContextFactory {

        static {
            ContextFactory.initGlobal(new SFactory());
        }

        protected Context makeContext() {
            SContext cx = new SContext();
            cx.setOptimizationLevel(-1);
            cx.setInstructionObserverThreshold(10000);
            return cx;
        }

        public boolean hasFeature(Context cx, int featureIndex) {
            switch (featureIndex) {
                case Context.FEATURE_NON_ECMA_GET_YEAR:
                    return true;

                case Context.FEATURE_MEMBER_EXPR_AS_FUNCTION_NAME:
                    return true;

                case Context.FEATURE_RESERVED_KEYWORD_AS_IDENTIFIER:
                    return true;

                case Context.FEATURE_PARENT_PROTO_PROPERTIES:
                    return false;
            }
            return super.hasFeature(cx, featureIndex);
        }

        protected void observeInstructionCount(Context cx, int instructionCount) {
            SContext mcx = (SContext)cx;
            long currentTime = System.currentTimeMillis();
            if (mcx.getTimeout() > 0 && currentTime - mcx.startTime > mcx.getTimeout() * 60 * 1000) {
                throw new Error();
            }
        }

        protected Object doTopCall(Callable callable, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            SContext mcx = (SContext)cx;
            mcx.startTime = System.currentTimeMillis();
            return super.doTopCall(callable, cx, scope, thisObj, args);
        }

    }    
    
}