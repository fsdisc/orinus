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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;

import com.orinus.Config;
import com.orinus.Controller;
import com.orinus.IOTool;
import com.orinus.schema.Engine;
import com.orinus.script.safe.lucene.SEntity;

public class HomePage extends BasePage {

    private static Logger logger = Logger.getLogger(HomePage.class);
	
	public HomePage(Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		super(baseRequest, request, response);
	}
	
	public void execute() throws Exception {
		if (isOnline()) {
			forOnline();
		} else {
			forOffline();
		}
	}
	
	private void forOnline() throws Exception {
		if (isPost()) {
			String stage = getParameter("stage");
			if ("exit".equalsIgnoreCase(stage)) {
				setSession("online", false);
				returnHome();
				return;
			}
			if ("delete".equalsIgnoreCase(stage)) {
				String[] ids = getParameter("ids").split("\\|");
				String datDir = controller.getDatDir();
				for (int i = 0; i < ids.length; i++) {
					Engine engine = controller.newEngine();
					engine.load(ids[i]);
					if (engine.getId().length() == 0) return;
					engine.delete();
					String folder = new File(datDir, engine.getId()).getAbsolutePath();
					try {
						IOTool.deleteFolder(new File(folder));
					} catch (Exception e) {
						logger.error("", e);
					}
				}
				returnHome();
				return;
			}
		}
		
		Engine pat = controller.newEngine();
		List<SEntity> results = pat.search(pat.getKind(), pat.newMatchAllDocsQuery(), Integer.MAX_VALUE);
		List engines = new ArrayList();
		Map item = new HashMap();
		item.put("id", DEFAULT_ENGINE);
		item.put("domain", "localhost");
		item.put("folder", "");
		item.put("quota", controller.getQuota() + "");
		engines.add(item);
		for (int i = 0; i < results.size(); i++) {
			SEntity se = results.get(i);
			item = new HashMap();
			item.put("id", se.getId());
			item.put("domain", se.getString(pat.DOMAIN));
			item.put("folder", se.getString(pat.FOLDER));
			item.put("quota", se.getDouble(pat.QUOTA) + "");
			engines.add(item);
		}
		
		Map args = newArgs();
		Map data = new HashMap();
		data.put("engines", engines);
		data.put("default_engine", DEFAULT_ENGINE);
		args.put("data", data);
		loadPage("home-online.vm", args);
	}
	
	private void forOffline() throws Exception {
		String password = getParameter("password");
		String message = "";

		if (isPost()) {
			if (!password.equals(new Controller().getConfig().getString(Config.MAGIC))) {
				message = "PasswordNotMatch";
			}
			if (message.length() == 0) {
				setSession("online", true);
				returnHome();
				return;
			}
		}
		
		Map args = newArgs();
		Map data = new HashMap();
		data.put("password", password);
		data.put("message", message);
		args.put("data", data);
		loadPage("home-offline.vm", args);
	}
	
}
