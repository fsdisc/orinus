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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.orinus.Config;

public class SettingsPage extends BasePage {

	public SettingsPage(Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		super(baseRequest, request, response);
	}
	
	public void execute() throws Exception {
		if (!checkOnline()) return;
		
		String data = getParameter("data").trim();
		String port = getParameter("port").trim();
		String system = getParameter("system").replaceAll("/", "").trim();
		String hosts = getParameter("hosts").trim();
		String quota = getParameter("quota").trim();
		String magic = getParameter("magic").trim();
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
				token = controller.uniqid();
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
				
				if (message.length() == 0 && data.length() > 0) {
					File file = new File(data);
					if (!file.exists() || !file.isDirectory()) {
						message = "DataNotExists";
					}
				}
				if (message.length() == 0 && port.length() > 0) {
					try {
						int iport = Integer.parseInt(port);
						port = iport + "";
					} catch (Exception e) {
						message = "InvalidPort";
					}
				}
				if (message.length() == 0 && quota.length() > 0) {
					try {
						double dquota = Double.parseDouble(quota);
						quota = dquota + "";
					} catch (Exception e) {
						message = "InvalidQuota";
					}
				}
				if (message.length() == 0) {
					controller.loadConfig();
					controller.getConfig().setValue(Config.DATA, data);
					controller.getConfig().setValue(Config.PORT, port);
					controller.getConfig().setValue(Config.SYSTEM, system);
					controller.getConfig().setValue(Config.HOSTS, hosts);
					controller.getConfig().setValue(Config.QUOTA, quota);
					controller.getConfig().setValue(Config.MAGIC, magic);
					controller.getConfig().setValue(Config.TIMEOUT, itimeout);
					controller.getConfig().setValue(Config.RUNTIME, iruntime);
					controller.getConfig().setValue(Config.TOKEN, token);
					controller.getConfig().setValue(Config.DISTRIBUTED, distributed.equalsIgnoreCase("yes"));
					controller.getConfig().setValue(Config.REMOTE_HOST, remote_host);
					controller.getConfig().setValue(Config.REMOTE_PORT, iremote_port);
					controller.getConfig().setValue(Config.REMOTE_TOKEN, remote_token);
					controller.getConfig().setValue(Config.RUN_SCRIPT, run_script.equalsIgnoreCase("yes"));
					controller.saveConfig();
					
					returnHome();
					return;
				}
			}
		} else {
			data = controller.getDatDir();
			port = controller.getPort() + "";
			system = controller.getSystem();
			hosts = controller.getHosts();
			quota = controller.getQuota() + "";
			magic = controller.getConfig().getString(Config.MAGIC);
			timeout = controller.getTimeout() + "";
			runtime = controller.getRunTime() + "";
			token = controller.getConfig().getString(Config.TOKEN);
			distributed = controller.getConfig().getBoolean(Config.DISTRIBUTED) ? "yes" : "no";
			remote_host = controller.getConfig().getString(Config.REMOTE_HOST);
			remote_port = controller.getRemotePort() + "";
			remote_token = controller.getConfig().getString(Config.REMOTE_TOKEN);
			run_script = controller.getConfig().getBoolean(Config.RUN_SCRIPT) ? "yes" : "no";
		}
		
		Map args = newArgs();
		Map mdata = new HashMap();
		mdata.put("data", data);
		mdata.put("port", port);
		mdata.put("system", system);
		mdata.put("hosts", hosts);
		mdata.put("quota", quota);
		mdata.put("magic", magic);
		mdata.put("timeout", timeout);
		mdata.put("runtime", runtime);
		mdata.put("token", token);
		mdata.put("distributed", distributed);
		mdata.put("remote_host", remote_host);
		mdata.put("remote_port", remote_port);
		mdata.put("remote_token", remote_token);
		mdata.put("run_script", run_script);
		mdata.put("message", message);
		args.put("data", mdata);
		loadPage("settings.vm", args);
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
	
}
