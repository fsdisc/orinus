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

package com.orinus.script.safe.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.orinus.Controller;
import com.orinus.schema.Engine;

public class SRequest {

	public static final java.lang.String BASIC_AUTH = HttpServletRequest.BASIC_AUTH;
	public static final java.lang.String FORM_AUTH = HttpServletRequest.FORM_AUTH;
	public static final java.lang.String CLIENT_CERT_AUTH = HttpServletRequest.CLIENT_CERT_AUTH;
	public static final java.lang.String DIGEST_AUTH = HttpServletRequest.DIGEST_AUTH;
	
	private HttpServletRequest req;
	private Engine engine;
	private String prefix;
	private Map<String, String> formFields;
	private Map<String, FileEntry> formFiles;
	private Map<String, String> formFileData;
	
	public SRequest(HttpServletRequest req, Engine engine) {
		this.req = req;
		this.engine = engine;
		prefix = "common_";
		if (engine != null) {
			prefix = engine.getId() + "_";
		}
	}

	public String getAuthType() {
		return req.getAuthType();
	}
	
	public String getContextPath() {
		return req.getContextPath();
	}
	
	public Cookie[] getCookies() {
		return req.getCookies();
	}
	
	public long getDateHeader(String name) {
		return req.getDateHeader(name);
	}
	
	public String getHeader(String name) {
		return req.getHeader(name);
	}
	
	public List<String> getHeaderNames() {
		List<String> tag = new ArrayList<String>();
		Enumeration<String> et = req.getHeaderNames();
		while (et.hasMoreElements()) {
			tag.add(et.nextElement());
		}
		return tag;
	}
	
	public List<String> getHeaders(String name) {
		List<String> tag = new ArrayList<String>();
		Enumeration<String> et = req.getHeaders(name);
		while (et.hasMoreElements()) {
			tag.add(et.nextElement());
		}
		return tag;
	}
	
	public int getIntHeader(String name) {
		return req.getIntHeader(name);
	}
	
	public String getMethod() {
		return req.getMethod();
	}
	
	public String getPathInfo() {
		return req.getPathInfo();
	}
	
	public String getPathTranslated() {
		return req.getPathTranslated();
	}
	
	public String getQueryString() {
		return req.getQueryString();
	}
	
	public String getRemoteUser() {
		return req.getRemoteUser();
	}
	
	public String getRequestedSessionId() {
		return req.getRequestedSessionId();
	}
	
	public String getRequestURI() {
		return req.getRequestURI();
	}
	
	public String getRequestURL() {
		return req.getRequestURL().toString();
	}
	
	public String getServletPath() {
		return req.getServletPath();
	}
	
	public Object getSession(String name) {
		return req.getSession().getAttribute(prefix + name);
	}
	
	public void setSession(String name, Object value) {
		req.getSession().setAttribute(prefix + name, value);
	}
	
	public Principal getUserPrincipal() {
		return req.getUserPrincipal();
	}
	
	public boolean isRequestedSessionIdFromCookie() {
		return req.isRequestedSessionIdFromCookie();
	}
	
	public boolean isRequestedSessionIdFromURL() {
		return req.isRequestedSessionIdFromURL();
	}
	
	public boolean isRequestedSessionIdValid() {
		return req.isRequestedSessionIdValid();
	}
	
	public boolean isUserInRole(String role) {
		return req.isUserInRole(role);
	}
	
	public Object getAttribute(String name) {
		return req.getAttribute(name);
	}
	
	public List<String> getAttributeNames() {
		List<String> tag = new ArrayList<String>();
		Enumeration<String> et = req.getAttributeNames();
		while (et.hasMoreElements()) {
			tag.add(et.nextElement());
		}
		return tag;
	}
	
	public String getCharacterEncoding() {
		return req.getCharacterEncoding();
	}
	
	public int getContentLength() {
		return req.getContentLength();
	}
	
	public String getContentType() {
		return req.getContentType();
	}
	
	public byte[] getRequestData() {
		byte[] tag = new byte[0];
		try {
			ServletInputStream sis = req.getInputStream();
			tag = new byte[sis.available()];
			sis.read(tag);
		} catch (Exception e) {
		}
		return tag;
	}

	public String getLocalAddr() {
		return req.getLocalAddr();
	}
	
	public Locale getLocale() {
		return req.getLocale();
	}
	
	public List<Locale> getLocales() {
		List<Locale> tag = new ArrayList<Locale>();
		Enumeration<Locale> et = req.getLocales();
		while (et.hasMoreElements()) {
			tag.add(et.nextElement());
		}
		return tag;
	}
	
	public String getLocalName() {
		return req.getLocalName();
	}
	
	public int getLocalPort() {
		return req.getLocalPort();
	}
	
	public String getParameter(String name) {
		String tag = req.getParameter(name);
		if (tag == null) tag = "";
		return tag;
	}
	
	public Map<String, String[]> getParameterMap() {
		return req.getParameterMap();
	}
	
	public List<String> getParameterNames() {
		List<String> tag = new ArrayList<String>();
		Enumeration<String> et = req.getParameterNames();
		while (et.hasMoreElements()) {
			tag.add(et.nextElement());
		}
		return tag;
	}
	
	public String[] getParameterValues(String name) {
		return req.getParameterValues(name);
	}
	
	public String getProtocol() {
		return req.getProtocol();
	}
	
	public String getRemoteAddr() {
		return req.getRemoteAddr();
	}
	
	public String getRemoteHost() {
		return req.getRemoteHost();
	}
	
	public int getRemotePort() {
		return req.getRemotePort();
	}
	
	public String getScheme() {
		return req.getScheme();
	}
	
	public String getServerName() {
		return req.getServerName();
	}
	
	public int getServerPort() {
		return req.getServerPort();
	}
	
	public boolean isSecure() {
		return req.isSecure();
	}
	
	public void removeAttribute(String name) {
		req.removeAttribute(name);
	}
	
	public void setAttribute(String name, Object o) {
		req.setAttribute(name, o);
	}
	
	public void setCharacterEncoding(String env) throws Exception {
		req.setCharacterEncoding(env);
	}

	public boolean isMultipartContent() {
		return ServletFileUpload.isMultipartContent(req);
	}
	
	public void parseMultipartContent() {
		if (formFields != null && formFiles != null && formFileData != null) return;
		formFields = new HashMap<String, String>();
		formFiles = new HashMap<String, FileEntry>();
		formFileData = new HashMap<String, String>();
		try {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			Controller controller = new Controller();
			factory.setRepository(new File(controller.getTempDir()));
			ServletFileUpload upload = new ServletFileUpload(factory);
			List items = upload.parseRequest(req);
			for (int i = 0; i < items.size(); i++) {
				FileItem item = (FileItem)items.get(i);
				if (item.isFormField()) {
					formFields.put(item.getFieldName(), item.getString());
				} else {
					FileEntry fe = new FileEntry();
					fe.fieldName = item.getFieldName();
					fe.contentType = item.getContentType();
					fe.filename = new File(item.getName()).getName();
					fe.fileSize = item.getSize();
					String filename = new File(controller.getTempDir(), fe.filename).getAbsolutePath();
					item.write(new File(filename));
					formFiles.put(fe.fieldName, fe);
					formFileData.put(fe.fieldName, filename);
				}
			}
		} catch (Exception e) {
		}
	}
	
	public String getPartField(String name) {
		String tag = "";
		if (formFields.containsKey(name)) {
			tag = formFields.get(name);
		}
		return tag;
	}
	
	public FileEntry getPartFile(String name) {
		return formFiles.get(name);
	}
	
	public byte[] getPartFileData(String name) {
		byte[] tag = new byte[0];
		try {
			if (formFileData.containsKey(name)) {
				String filename = formFileData.get(name);
				InputStream is = new FileInputStream(filename);
				tag = new byte[is.available()];
				is.read(tag);
				is.close();
			}
		} catch (Exception e) {
		}
		return tag;
	}
	
	public static class FileEntry {
		public String fieldName = "";
		public String filename = "";
		public long fileSize = 0;
		public String contentType = "";
	}
	
}
