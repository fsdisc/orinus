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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.orinus.Config;
import com.orinus.Controller;
import com.orinus.schema.Engine;
import com.orinus.script.safe.lucene.SEntity;

public class RemoteHandler extends SEntity.Handler {

    private static Logger logger = Logger.getLogger(RemoteHandler.class);
	
	private Controller controller;
	private Engine engine;
	private SEntity.Handler localHandler;
	private boolean distributed;
	private String remoteHost;
	private String remotePort;
	private String remoteToken;
	private String remoteUrl;
	
	public RemoteHandler(SEntity.Handler localHandler, Engine engine) {
		this.controller = new Controller();
		this.engine = engine;
		this.localHandler = localHandler;
		if (engine == null) {
			this.distributed = controller.getConfig().getBoolean(Config.DISTRIBUTED);
			this.remoteHost = controller.getConfig().getString(Config.REMOTE_HOST);
			this.remotePort = controller.getRemotePort() + "";
			this.remoteToken = controller.getConfig().getString(Config.REMOTE_TOKEN);
		} else {
			this.distributed = engine.getDistributed();
			this.remoteHost = engine.getRemoteHost();
			this.remotePort = engine.getRemotePort() + "";
			this.remoteToken = engine.getRemoteToken();
		}
		this.remoteUrl = "http://" + this.remoteHost + ":" + this.remotePort + "/api.jsb"; 
	}
	
	private JSONObject createData() {
		JSONObject tag = new JSONObject();
		tag.put("token", remoteToken);
		return tag;
	}

	private JSONObject execute(JSONObject input) throws Exception {
		JSONObject tag = null;
		Connection conn = Jsoup.connect(remoteUrl);
		conn.data("data", input.toString());
		conn.timeout(60000);
		String output = conn.execute().body();
		tag = JSONObject.fromObject(output);
		return tag;
	}
	
    public boolean exists(String id) {
    	if (!distributed) return localHandler.exists(id);
    	try {
    		JSONObject input = createData();
    		input.put("action", "Exists");
    		input.put("id", id);
    		JSONObject output = execute(input);
    		if (output.getBoolean("success")) {
    			return output.getBoolean("data");
    		} else {
    			logger.error(output.getString("message"));
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    	return false; 
    }
    
    public void create(SEntity src) { 
    	if (!distributed) { 
    		localHandler.create(src);
    		return;
    	}
    	try {
    		JSONObject input = createData();
    		input.put("action", "Save");
    		input.put("entity", src.toString());
    		JSONObject output = execute(input);
    		if (output.getBoolean("success")) {
    			
    		} else {
    			logger.error(output.getString("message"));
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    }
    
    public void update(SEntity src) { 
    	if (!distributed) { 
    		localHandler.update(src);
    		return;
    	}
    	try {
    		JSONObject input = createData();
    		input.put("action", "Save");
    		input.put("entity", src.toString());
    		JSONObject output = execute(input);
    		if (output.getBoolean("success")) {
    			
    		} else {
    			logger.error(output.getString("message"));
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    }
    
    public void load(String id, SEntity src) {
    	if (!distributed) {
    		localHandler.load(id, src);
    		return;
    	}
    	try {
    		JSONObject input = createData();
    		input.put("action", "Load");
    		input.put("id", id);
    		JSONObject output = execute(input);
    		if (output.getBoolean("success")) {
    			src.fromString(output.getString("data"));
    		} else {
    			logger.error(output.getString("message"));
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    }
    
    public void delete(String id) { 
    	if (!distributed) {
    		localHandler.delete(id);
    		return;
    	}
    	try {
    		JSONObject input = createData();
    		input.put("action", "Delete");
    		input.put("id", id);
    		JSONObject output = execute(input);
    		if (output.getBoolean("success")) {

    		} else {
    			logger.error(output.getString("message"));
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    }
    
    public List<SEntity> search(String kind, Query query, int max) {
    	return search(kind, query, null, null, max);
    }
    
    public List<SEntity> search(String kind, Query query, Sort sort, int max) {
    	return search(kind, query, null, sort, max);
    }
    
    public List<SEntity> search(String kind, Query query, Filter filter, int max) {
    	return search(kind, query, filter, null, max);
    }
    
    public List<SEntity> search(String kind, Query query, Filter filter, Sort sort, int max) { 
    	if (!distributed) return localHandler.search(kind, query, filter, sort, max);
    	List<SEntity> tag = new ArrayList<SEntity>();
    	try {
    		JSONObject input = createData();
    		input.put("action", "SearchAll");
    		input.put("kind", kind);
    		input.put("query", serialize(query));
    		input.put("filter", serialize(filter));
    		input.put("sort", serialize(sort));
    		input.put("max", max);
    		JSONObject output = execute(input);
    		if (output.getBoolean("success")) {
    			JSONArray data = output.getJSONArray("data");
    			for (int i = 0; i < data.size(); i++) {
    				SEntity et = controller.newEntity(engine);
    				et.fromString(data.getString(i));
    				tag.add(et);
    			}
    		} else {
    			logger.error(output.getString("message"));
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    	return tag; 
    }
    
    public List<SEntity> search(String kind, Query query, int pagesize, int pageno) {
    	return search(kind, query, null, null, pagesize, pageno);
    }
    
    public List<SEntity> search(String kind, Query query, Sort sort, int pagesize, int pageno) {
    	return search(kind, query, null, sort, pagesize, pageno);
    }
    
    public List<SEntity> search(String kind, Query query, Filter filter, int pagesize, int pageno) {
    	return search(kind, query, filter, null, pagesize, pageno);
    }
    
    public List<SEntity> search(String kind, Query query, Filter filter, Sort sort, int pagesize, int pageno) { 
    	if (!distributed) return localHandler.search(kind, query, filter, sort, pagesize, pageno);
    	List<SEntity> tag = new ArrayList<SEntity>();
    	try {
    		JSONObject input = createData();
    		input.put("action", "SearchByPage");
    		input.put("kind", kind);
    		input.put("query", serialize(query));
    		input.put("filter", serialize(filter));
    		input.put("sort", serialize(sort));
    		input.put("pagesize", pagesize);
    		input.put("pageno", pageno);
    		JSONObject output = execute(input);
    		if (output.getBoolean("success")) {
    			JSONArray data = output.getJSONArray("data");
    			for (int i = 0; i < data.size(); i++) {
    				SEntity et = controller.newEntity(engine);
    				et.fromString(data.getString(i));
    				tag.add(et);
    			}
    		} else {
    			logger.error(output.getString("message"));
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    	return tag; 
    }
    
    public int count(String kind, Query query, int max) {
    	return count(kind, query, null, null, max);
    }
    
    public int count(String kind, Query query, Sort sort, int max) {
    	return count(kind, query, null, sort, max);
    }
    
    public int count(String kind, Query query, Filter filter, int max) {
    	return count(kind, query, filter, null, max);
    }
    
    public int count(String kind, Query query, Filter filter, Sort sort, int max) {
    	if (!distributed) return localHandler.count(kind, query, filter, sort, max);
    	int tag = 0;
    	try {
    		JSONObject input = createData();
    		input.put("action", "Count");
    		input.put("kind", kind);
    		input.put("query", serialize(query));
    		input.put("filter", serialize(filter));
    		input.put("sort", serialize(sort));
    		input.put("max", max);
    		JSONObject output = execute(input);
    		if (output.getBoolean("success")) {
    			tag = output.getInt("data");
    		} else {
    			logger.error(output.getString("message"));
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    	return tag; 
    }
    
    public double storageQuota() { 
    	if (!distributed) return localHandler.storageQuota();
    	try {
    		JSONObject input = createData();
    		input.put("action", "StorageQuota");
    		JSONObject output = execute(input);
    		if (output.getBoolean("success")) {
    			return output.getDouble("data");
    		} else {
    			logger.error(output.getString("message"));
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    	return 0; 
    }
    
    public double storageSize() { 
    	if (!distributed) return localHandler.storageSize();
    	try {
    		JSONObject input = createData();
    		input.put("action", "StorageSize");
    		JSONObject output = execute(input);
    		if (output.getBoolean("success")) {
    			return output.getDouble("data");
    		} else {
    			logger.error(output.getString("message"));
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    	return 0; 
    }
	
    private String serialize(Object obj) {
    	String tag = "";
    	if (obj == null) return tag;
    	try {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		ObjectOutputStream oos = new ObjectOutputStream(baos);
    		oos.writeObject(obj);
    		oos.flush();
    		oos.close(); 
    		tag = new String(encodeBase64(baos.toByteArray()), "UTF-8");
    		baos.close();
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    	return tag;
    }
    
    private byte[] encodeBase64(byte[] b) throws Exception {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream b64os = javax.mail.internet.MimeUtility.encode(baos, "base64");
        b64os.write(b);
        b64os.close();
        return baos.toByteArray();
    }
    
}
