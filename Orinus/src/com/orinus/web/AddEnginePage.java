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

import org.eclipse.jetty.server.Request;

import com.orinus.Config;
import com.orinus.schema.Engine;
import com.orinus.script.safe.lucene.SEntity;

public class AddEnginePage extends BasePage {

	public AddEnginePage(Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		super(baseRequest, request, response);
	}
	
	public void execute() throws Exception {
		if (!checkOnline()) return;
		
		String domain = getParameter("domain").trim();
		String folder = getParameter("folder").replaceAll("/", "").trim();
		String quota = getParameter("quota");
		String timeout = getParameter("timeout").trim();
		String runtime = getParameter("runtime").trim();
		String token = getParameter("token").trim();
		String distributed = getParameter("distributed").trim();
		String remote_host = getParameter("remote_host").trim();
		String remote_port = getParameter("remote_port").trim();
		String remote_token = getParameter("remote_token").trim();
		String run_script = getParameter("run_script").trim();
		String stage = getParameter("stage").trim();
		String message = "";
		
		if (isPost()) {
			if ("new-token".equals(stage)) {
				token = controller.uniqid() + controller.uniqid() + controller.uniqid() + controller.uniqid();
			} else {
				int itimeout = parseInt(timeout, 60);
		    	if (itimeout <= 0) itimeout = 60;
		    	if (itimeout > 60 * 24) itimeout = 60 *24;
		    	timeout = itimeout + "";
				int iruntime = parseInt(runtime, 60 * 24);
		    	if (iruntime <= 0) iruntime = 60 * 24;
		    	runtime = iruntime + "";
		    	int iremote_port = parseInt(remote_port, 80);
		    	if (iremote_port <= 0) iremote_port = 80;
		    	remote_port = iremote_port + "";
				
				if (message.length() == 0 && domain.length() > 0) {
					if (findEngineByDomain(domain) != null) {
						message = "DomainExists";
					}
				}
				if (message.length() == 0 && folder.length() == 0) {
					message = "FolderRequired";
				}
				if (message.length() == 0 && findEngineByFolder(folder) != null) {
					message = "FolderExists";
				}
				double dquota = 0;
				try {
					dquota = Double.parseDouble(quota);
				} catch (Exception e) {
					dquota = 0;
				}
				if (message.length() == 0 && dquota <= 0) {
					message = "InvalidQuota";
				}
				if (message.length() == 0) {
					Engine engine = controller.newEngine();
					engine.setId(controller.uniqid());
					engine.setDomain(domain);
					engine.setFolder(folder);
					engine.setQuota(dquota);
					engine.setTimeout(itimeout);
					engine.setRuntime(iruntime);
					engine.setToken(token);
					engine.setDistributed(distributed.equalsIgnoreCase("yes"));
					engine.setRemoteHost(remote_host);
					engine.setRemotePort(iremote_port);
					engine.setRemoteToken(remote_token);
					engine.setRunScript(run_script.equalsIgnoreCase("yes"));
					engine.save();
					returnHome();
					return;
				}
			}			
		} else {
			timeout = "60";
			runtime = "1440";
			token = controller.uniqid() + controller.uniqid() + controller.uniqid() + controller.uniqid();
			remote_port = "80";
		}
		
		Map args = newArgs();
		Map data = new HashMap();
		data.put("domain", domain);
		data.put("folder", folder);
		data.put("quota", quota);
		data.put("timeout", timeout);
		data.put("runtime", runtime);
		data.put("token", token);
		data.put("distributed", distributed);
		data.put("remote_host", remote_host);
		data.put("remote_port", remote_port);
		data.put("remote_token", remote_token);
		data.put("run_script", run_script);
		data.put("message", message);
		args.put("data", data);
		loadPage("add-engine.vm", args);
	}
	
    private int parseInt(String src, int defVal) {
    	int tag = defVal;
    	try {
    		tag = Integer.parseInt(src);
    	} catch (Exception e) {
    		tag = defVal;
    	}
    	return tag;
    }
	
    private Engine findEngineByFolder(String folder) {
    	Engine pat = controller.newEngine();
    	List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.FOLDER, folder)), 1);
    	Engine tag = null;
    	if (results.size() > 0) {
    		pat.fromString(results.get(0).toString());
    		tag = pat;
    	}
    	return tag;
    }

    private Engine findEngineByDomain(String domain) {
    	Engine pat = controller.newEngine();
    	List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.DOMAIN, domain)), 1);
    	Engine tag = null;
    	if (results.size() > 0) {
    		pat.fromString(results.get(0).toString());
    		tag = pat;
    	}
    	return tag;
    }
	
}
