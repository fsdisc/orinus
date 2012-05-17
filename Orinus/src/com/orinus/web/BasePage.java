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

import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Request;

import com.orinus.Controller;
import com.orinus.script.safe.jetty.SRequest;

public class BasePage {

    private static Logger logger = Logger.getLogger(BasePage.class);
	
    public static final String DEFAULT_ENGINE = "E4fadd0ad1d201";
    
	protected Request baseRequest;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected SRequest sreq;
	protected Controller controller;
	
	public BasePage(Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		this.baseRequest = baseRequest;
		this.request = request;
		this.response = response;
		this.sreq = new SRequest(request, null);
		this.controller = new Controller();
	}
	
	public void execute() throws Exception {
		
	}

	protected boolean checkOnline() {
		if (isOnline()) {
			return true;
		} else {
			returnHome();
			return false;
		}
	}
	
	protected void returnPage(String page) {
		try {
			String url = "/" + controller.getSystem() + "/" + page;
			response.sendRedirect(url);
			baseRequest.setHandled(true);
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	protected void returnHome() {
		try {
			String url = "/" + controller.getSystem() + "/";
			response.sendRedirect(url);
			baseRequest.setHandled(true);
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	protected boolean isPost() {
		return "post".equalsIgnoreCase(request.getMethod());
	}
	
	protected String getParameter(String name) {
		String tag = request.getParameter(name);
		if (tag == null) tag = "";
		return tag;
	}
	
	protected Map newArgs() {
		Map tag = new HashMap();
		tag.put("ROOT", "/" + controller.getSystem());
		tag.put("lang", loadLanguage("en.lang"));
		return tag;
	}
	
	protected Map loadLanguage(String path) {
		Map tag = new HashMap();
		try {
			String input = new String(loadResource("/languages/" + path), "UTF-8");
			String[] lines = input.split("\n");
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				if (line.trim().startsWith("#")) continue;
				int pos = line.indexOf("=");
				if (pos < 0) continue;
				String key = line.substring(0, pos).trim();
				String value = line.substring(pos + 1);
				tag.put(key, value);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		return tag;
	}
	
	protected boolean isOnline() {
		Object val = getSession("online");
		if (val == null) return false;
		Boolean tag = (Boolean)val;
		return tag;
	}
	
	protected Object getSession(String name) {
		return request.getSession(true).getAttribute(name);
	}
	
	protected void setSession(String name, Object value) {
		request.getSession(true).setAttribute(name, value);
	}
	
	protected void loadPage(String path, Map args) throws Exception {
		String output = merge(loadTemplate(path), args);
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		response.getOutputStream().write(output.getBytes("UTF-8"));
        baseRequest.setHandled(true);
	}
	
    protected String merge(String template, Map args) throws Exception {
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
    
    protected String merge(byte[] template, Map args) throws Exception {
    	return merge(new String(template, "UTF-8"), args);
    }
	
	protected byte[] loadTemplate(String path) throws Exception {
		String filename = "/templates/" + path;
		return loadResource(filename);
	}
    
	protected byte[] loadResource(String path) throws Exception {
		String filename = "/com/orinus/web/resource" + path;
		InputStream is =  Router.class.getResourceAsStream(filename);
		byte[] tag = new byte[is.available()];
		is.read(tag);
		return tag;
	}
    
}
