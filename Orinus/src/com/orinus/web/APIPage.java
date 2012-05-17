package com.orinus.web;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.eclipse.jetty.server.Request;

import com.orinus.Config;
import com.orinus.schema.Engine;
import com.orinus.script.LuceneHandler;
import com.orinus.script.safe.lucene.SEntity;

public class APIPage extends BasePage {

    private static Logger logger = Logger.getLogger(APIPage.class);
	
	private LuceneHandler lh;
	private JSONObject input;
	private Tracking track;
	private Engine engine;
	
	public APIPage(Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		super(baseRequest, request, response);
	}
	
	public void execute() throws Exception {
		String sinput = getParameter("data");
		track = new Tracking();
		input = null;
		String action = "";
		String token = "";
		
		try {
			input = JSONObject.fromObject(sinput);
			action = input.getString("action");
			token = input.getString("token");
			if (token.trim().length() == 0) {
				throw new Exception("Token is required");
			}
		} catch (Exception e) {
			track.message = "Input is not valid: " + e.getMessage();
			track.success = false;
		}
		if (track.success) {
			if (token.equals(controller.getConfig().getString(Config.TOKEN))) {
				engine = null;
			} else {
				engine = findEngineByToken(token);
				if (engine == null) {
					track.message = "Token is not found";
					track.success = false;
				}
			}
		}
		if (track.success) {
			lh = controller.newLuceneHandler(engine);
			if ("Exists".equalsIgnoreCase(action)) {
				goExists();
			} else if ("Save".equalsIgnoreCase(action)) {
				goSave();
			} else if ("Load".equalsIgnoreCase(action)) {
				goLoad();
			} else if ("Delete".equalsIgnoreCase(action)) {
				goDelete();
			} else if ("SearchAll".equalsIgnoreCase(action)) {
				goSearchAll();
			} else if ("SearchByPage".equalsIgnoreCase(action)) {
				goSearchByPage();
			} else if ("Count".equalsIgnoreCase(action)) {
				goCount();
			} else if ("StorageQuota".equalsIgnoreCase(action)) {
				goStorageQuota();
			} else if ("StorageSize".equalsIgnoreCase(action)) {
				goStorageSize();
			} else {
				track.message = "Action is not found";
				track.success = false;
			}
		}
		
		JSONObject output = new JSONObject();
		output.put("success", track.success);
		output.put("message", track.message);
		output.put("data", track.data);
		response.getOutputStream().write(output.toString().getBytes("UTF-8"));
		baseRequest.setHandled(true);
	}

	private class Tracking {
		public boolean success = true;
		public String message = "";
		public Object data = null;
	}
	
	private Engine findEngineByToken(String token) {
		Engine pat = controller.newEngine();
		List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.TOKEN, token)), 1);
		if (results.size() == 0) return null;
		pat.fromString(results.get(0).toString());
		return pat;
	}
	
	private void goExists() {
		try {
			String id = input.getString("id");
			track.data = lh.exists(id);
		} catch (Exception e) {
			track.success = false;
			track.message = e.getMessage();
		}
	}

	private void goSave() {
		try {
			SEntity entity = new SEntity(lh);
			entity.fromString(input.getString("entity"));
			entity.save();
		} catch (Exception e) {
			track.success = false;
			track.message = e.getMessage();
		}
	}
	
	private void goLoad() {
		try {
			String id = input.getString("id");
			SEntity entity = new SEntity(lh);
			entity.load(id);
			track.data = entity.toString();
		} catch (Exception e) {
			track.success = false;
			track.message = e.getMessage();
		}
	}

	private void goDelete() {
		try {
			String id = input.getString("id");
			lh.delete(id);
		} catch (Exception e) {
			track.success = false;
			track.message = e.getMessage();
		}
	}

	private void goSearchAll() {
		try {
			Object obj = null;
			String kind = input.getString("kind");
			int max = input.getInt("max");
			SEntity et = new SEntity(lh);
			obj = deserialize(input.getString("query"));
			Query query = null;
			if (obj != null) query = (Query)obj;
			obj = deserialize(input.getString("filter"));
			Filter filter = null;
			if (obj != null) filter = (Filter)obj;
			obj = deserialize(input.getString("sort"));
			Sort sort = null;
			if (obj != null) sort = (Sort)obj;
			List<SEntity> results = lh.search(kind, query, filter, sort, max);
			JSONArray data = new JSONArray();
			for (int i = 0; i < results.size(); i++) {
				data.add(results.get(i).toString());
			}
			track.data = data;
		} catch (Exception e) {
			track.success = false;
			track.message = e.getMessage();
		}
	}

	private void goSearchByPage() {
		try {
			Object obj = null;
			String kind = input.getString("kind");
			int pagesize = input.getInt("pagesize");
			int pageno = input.getInt("pageno");
			SEntity et = new SEntity(lh);
			obj = deserialize(input.getString("query"));
			Query query = null;
			if (obj != null) query = (Query)obj;
			obj = deserialize(input.getString("filter"));
			Filter filter = null;
			if (obj != null) filter = (Filter)obj;
			obj = deserialize(input.getString("sort"));
			Sort sort = null;
			if (obj != null) sort = (Sort)obj;
			List<SEntity> results = lh.search(kind, query, filter, sort, pagesize, pageno);
			JSONArray data = new JSONArray();
			for (int i = 0; i < results.size(); i++) {
				data.add(results.get(i).toString());
			}
			track.data = data;
		} catch (Exception e) {
			track.success = false;
			track.message = e.getMessage();
		}
	}

	private void goCount() {
		try {
			Object obj = null;
			String kind = input.getString("kind");
			int max = input.getInt("max");
			SEntity et = new SEntity(lh);
			obj = deserialize(input.getString("query"));
			Query query = null;
			if (obj != null) query = (Query)obj;
			obj = deserialize(input.getString("filter"));
			Filter filter = null;
			if (obj != null) filter = (Filter)obj;
			obj = deserialize(input.getString("sort"));
			Sort sort = null;
			if (obj != null) sort = (Sort)obj;
			track.data = lh.count(kind, query, filter, sort, max);
		} catch (Exception e) {
			track.success = false;
			track.message = e.getMessage();
		}
	}

	private void goStorageQuota() {
		try {
			track.data = lh.storageQuota();
		} catch (Exception e) {
			track.success = false;
			track.message = e.getMessage();
		}
	}

	private void goStorageSize() {
		try {
			track.data = lh.storageSize();
		} catch (Exception e) {
			track.success = false;
			track.message = e.getMessage();
		}
	}
	
	private Object deserialize(String src) {
		Object tag = null;
		if (src.trim().length() == 0) return tag;
		try {
    		ByteArrayInputStream bais = new ByteArrayInputStream(decodeBase64(src.getBytes("UTF-8")));
    		ObjectInputStream ois = new ObjectInputStream(bais);
    		tag = ois.readObject();
    		ois.close();
    		bais.close();
		} catch (Exception e) {
			logger.error("", e);
		}
		return tag;
	}
	
    private byte[] decodeBase64(byte[] b) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        InputStream b64is = javax.mail.internet.MimeUtility.decode(bais, "base64");
        byte[] tmp = new byte[b.length];
        int n = b64is.read(tmp);
        byte[] res = new byte[n];
        System.arraycopy(tmp, 0, res, 0, n);
        return res;
    }      
	
}
