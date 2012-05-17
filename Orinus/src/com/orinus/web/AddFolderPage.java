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

import com.orinus.UITool;
import com.orinus.resource.Labels;
import com.orinus.schema.Engine;
import com.orinus.schema.Folder;
import com.orinus.script.safe.lucene.SEntity;

public class AddFolderPage extends BasePage {

    private Engine engine;
    private Folder parent;
	
	public AddFolderPage(Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
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
		
		String pid = getParameter("pid");
		parent = controller.newFolder(engine);
		parent.load(pid);
		if (parent.getId().length() == 0) {
			parent = null;
			pid = "";
		}
		
		String message = "";
		String name = getParameter("name").replaceAll("/", "").trim();
		String published = getParameter("published");
		if (!isPost()) published = "yes";
		
		if (isPost()) {
			if (message.length() == 0 && name.length() == 0) {
				message = "NameRequired";
			}
			if (message.length() == 0 && findFolderByName(name) != null) {
				message = "NameExists";
			}
			if (message.length() == 0) {
		    	int level = 1;
		    	if (parent != null) {
		    		level = parent.getLevel() + 1;
		    	}
		    	
		    	Folder folder = controller.newFolder(engine);
		    	folder.setId(controller.uniqid());
		    	folder.setParent(pid);
		    	folder.setLevel(level);
		    	folder.setName(name);
		    	folder.setPublished(published.equals("yes"));
		    	folder.save();
				
		    	returnPage("open-engine.jsb?eid=" + eid);
		    	return;
			}
		}
		
		Map args = newArgs();
		Map data = new HashMap();
		data.put("eid", eid);
		data.put("pid", pid);
		data.put("name", name);
		data.put("published", published);
		data.put("message", message);
		args.put("data", data);
		loadPage("add-folder.vm", args);
	}
	
    private Folder findFolderByName(String name) {
    	Folder pat = controller.newFolder(engine);
    	Folder tag = null;
    	String pid = "";
    	if (parent != null) {
    		pid = parent.getId();
    	}
    	int level = 1;
    	if (parent != null) {
    		level = parent.getLevel() + 1;
    	}
    	BooleanQuery bq = pat.newBooleanQuery();
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newIntegerRangeQuery(pat.LEVEL, level, level, true, true), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.NAME, name)), pat.occurMust()));
    	List<SEntity> results = pat.search(pat.getKind(), bq, 1);
    	if (results.size() > 0) {
    		pat.fromString(results.get(0).toString());
    		tag = pat;
    	}
    	return tag;
    }
	
}
