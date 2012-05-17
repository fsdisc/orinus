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
import com.orinus.schema.Folder;
import com.orinus.script.safe.lucene.SEntity;

public class EditFolderPage extends BasePage {

    private Engine engine;
    private Folder folder;
	
	public EditFolderPage(Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
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
		folder = controller.newFolder(engine);
		folder.load(fid);
		if (folder.getId().length() == 0) {
			returnHome();
			return;
		}
		
		String message = "";
		String name = getParameter("name").replaceAll("/", "").trim();
		String published = getParameter("published");
		if (!isPost()) {
			name = folder.getName();
			published = folder.getPublished() ? "yes" : "no";
		}
		
		if (isPost()) {
			if (message.length() == 0 && name.length() == 0) {
				message = "NameRequired";
			}
			if (message.length() == 0 && findFolderByName(name) != null) {
				message = "NameExists";
			}
			if (message.length() == 0) {
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
		data.put("fid", fid);
		data.put("name", name);
		data.put("published", published);
		data.put("message", message);
		args.put("data", data);
		loadPage("edit-folder.vm", args);
	}
	
    private Folder findFolderByName(String name) {
    	Folder pat = controller.newFolder(engine);
    	Folder tag = null;
    	BooleanQuery bq = pat.newBooleanQuery();
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PARENT, folder.getParent())), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newIntegerRangeQuery(pat.LEVEL, folder.getLevel(), folder.getLevel(), true, true), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.NAME, name)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.ID, folder.getId())), pat.occurMustNot()));
    	List<SEntity> results = pat.search(pat.getKind(), bq, 1);
    	if (results.size() > 0) {
    		pat.fromString(results.get(0).toString());
    		tag = pat;
    	}
    	return tag;
    }
	
}
