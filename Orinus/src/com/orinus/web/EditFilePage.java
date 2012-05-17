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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.search.BooleanQuery;
import org.eclipse.jetty.server.Request;

import com.orinus.schema.Engine;
import com.orinus.schema.FileData;
import com.orinus.schema.FileItem;
import com.orinus.schema.Folder;
import com.orinus.script.safe.lucene.SEntity;

public class EditFilePage extends BasePage {

    private Engine engine;
    private FileItem fitem;
	
	public EditFilePage(Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
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
				FileData fdata = controller.newFileData(engine);
				fdata.load(fitem.getData());
				response.setHeader("Content-Type", "application/force-download");
				response.setHeader("Content-Description", "File Transfer");
				response.setHeader("Content-Disposition", "attachment; filename=" + fitem.getName());
				response.setHeader("Content-Transfer-Encoding", "binary");
				response.getOutputStream().write(fdata.getData());
				baseRequest.setHandled(true);
				return;
			}
		}
		
		String message = "";
		String name = fitem.getName();
		String published = fitem.getPublished() ? "yes" : "no";
		
		if (isPost()) {
			if (!sreq.isMultipartContent()) {
				returnHome();
				return;
			}
			sreq.parseMultipartContent();
			name = sreq.getPartField("name").replaceAll("/", "").trim();
			published = sreq.getPartField("published");
			
			if (message.length() == 0 && name.length() == 0) {
				message = "NameRequired";
			}
			if (message.length() == 0 && name.lastIndexOf(".") < 0) {
				message = "ExtensionRequired";
			}
			if (message.length() == 0 && findFileByName(name) != null) {
				message = "NameExists";
			}
			if (message.length() == 0) {
				if (sreq.getPartFile("data") != null) {
					FileData fdata = controller.newFileData(engine);
					fdata.load(fitem.getData());
					fdata.setData(sreq.getPartFileData("data"));
					fdata.save();
				}
				
				fitem.setName(name);
				fitem.setPublished(published.equals("yes"));
				fitem.setScheduled(name.endsWith(".jsb"));
				fitem.save();
				
		    	returnPage("open-engine.jsb?eid=" + eid);
		    	return;
			}
		}
		
		Map args = newArgs();
		Map data = new HashMap();
		data.put("eid", eid);
		data.put("fid", fid);
		data.put("name", name);
		data.put("published", published);
		data.put("scheduled", fitem.getName().endsWith(".jsb"));
		data.put("message", message);
		args.put("data", data);
		loadPage("edit-file.vm", args);
	}
	
    private FileItem findFileByName(String name) {
    	FileItem tag = null;
    	FileItem pat = controller.newFileItem(engine);
    	String pid = fitem.getParent();
    	BooleanQuery bq = pat.newBooleanQuery();
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.NAME, name)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.ID, fitem.getId())), pat.occurMustNot()));
    	List<SEntity> results = pat.search(pat.getKind(), bq, 1);
    	if (results.size() > 0) {
    		pat.fromString(results.get(0).toString());
    		tag = pat;
    	}
    	return tag;
    }
	
}
