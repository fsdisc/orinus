package com.orinus.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

import com.orinus.schema.Engine;
import com.orinus.schema.FileData;
import com.orinus.schema.FileItem;
import com.orinus.schema.LogData;
import com.orinus.schema.LogItem;
import com.orinus.script.safe.lucene.SEntity;

public class LogPage extends BasePage {

    private Engine engine;
    private FileItem fitem;
	
	public LogPage(Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
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
		fitem = controller.newFileItem(engine);
		fitem.load(fid);
		if (fitem.getId().length() == 0) {
			returnHome();
			return;
		}
		
		if (!isPost()) {
			String stage = getParameter("stage");
			if ("download".equalsIgnoreCase(stage)) {
				String lid = getParameter("lid");
				LogItem li = controller.newLogItem(engine);
				li.load(lid);
				LogData ld = controller.newLogData(engine);
				ld.load(li.getData());
				response.setHeader("Content-Type", "application/force-download");
				response.setHeader("Content-Description", "File Transfer");
				response.setHeader("Content-Disposition", "attachment; filename=" + fitem.getName() + ".log");
				response.setHeader("Content-Transfer-Encoding", "binary");
				response.getOutputStream().write(ld.getData());
				baseRequest.setHandled(true);
				return;
			}
		} else {
			String stage = getParameter("stage");
			if ("delete".equalsIgnoreCase(stage)) {
				String ids = getParameter("ids");
				String[] fields = ids.split("\\|");
				for (int i = 0; i < fields.length; i++) {
					LogItem li = controller.newLogItem(engine);
					li.load(fields[i]);
					LogData ld = controller.newLogData(engine);
					ld.setId(li.getData());
					li.delete();
					ld.delete();
				}
			}
		}
		
		List litems = new ArrayList();
    	LogItem pat = controller.newLogItem(engine);
    	List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.PARENT, fitem.getId())), pat.newSort(pat.newSortField(pat.CREATED, pat.sortFieldLong(), true)), Integer.MAX_VALUE);
    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    	for (int i = 0; i < results.size(); i++) {
    		LogItem li = controller.newLogItem(engine);
    		li.fromString(results.get(i).toString());
    		Map item = new HashMap();
    		item.put("created", sdf.format(li.getDate(li.CREATED)));
    		item.put("size", li.getSize() + "");
    		item.put("id", li.getId());
    		litems.add(item);
    	}
		
		Map args = newArgs();
		Map data = new HashMap();
		data.put("eid", eid);
		data.put("fid", fid);
		data.put("logitems", litems);
		args.put("data", data);
		loadPage("log.vm", args);
	}
	
}
