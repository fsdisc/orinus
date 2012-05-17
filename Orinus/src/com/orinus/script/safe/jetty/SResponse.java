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

import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class SResponse {

	public static final int SC_CONTINUE = HttpServletResponse.SC_CONTINUE;
	public static final int SC_SWITCHING_PROTOCOLS = HttpServletResponse.SC_SWITCHING_PROTOCOLS;
	public static final int SC_OK = HttpServletResponse.SC_OK;
	public static final int SC_CREATED = HttpServletResponse.SC_CREATED;
	public static final int SC_ACCEPTED = HttpServletResponse.SC_ACCEPTED;
	public static final int SC_NON_AUTHORITATIVE_INFORMATION = HttpServletResponse.SC_NON_AUTHORITATIVE_INFORMATION;
	public static final int SC_NO_CONTENT = HttpServletResponse.SC_NO_CONTENT;
	public static final int SC_RESET_CONTENT = HttpServletResponse.SC_RESET_CONTENT;
	public static final int SC_PARTIAL_CONTENT = HttpServletResponse.SC_PARTIAL_CONTENT;
	public static final int SC_MULTIPLE_CHOICES = HttpServletResponse.SC_MULTIPLE_CHOICES;
	public static final int SC_MOVED_PERMANENTLY = HttpServletResponse.SC_MOVED_PERMANENTLY;
	public static final int SC_MOVED_TEMPORARILY = HttpServletResponse.SC_MOVED_TEMPORARILY;
	public static final int SC_FOUND = HttpServletResponse.SC_FOUND;
	public static final int SC_SEE_OTHER = HttpServletResponse.SC_SEE_OTHER;
	public static final int SC_NOT_MODIFIED = HttpServletResponse.SC_NOT_MODIFIED;
	public static final int SC_USE_PROXY = HttpServletResponse.SC_USE_PROXY;
	public static final int SC_TEMPORARY_REDIRECT = HttpServletResponse.SC_TEMPORARY_REDIRECT;
	public static final int SC_BAD_REQUEST = HttpServletResponse.SC_BAD_REQUEST;
	public static final int SC_UNAUTHORIZED = HttpServletResponse.SC_UNAUTHORIZED;
	public static final int SC_PAYMENT_REQUIRED = HttpServletResponse.SC_PAYMENT_REQUIRED;
	public static final int SC_FORBIDDEN = HttpServletResponse.SC_FORBIDDEN;
	public static final int SC_NOT_FOUND = HttpServletResponse.SC_NOT_FOUND;
	public static final int SC_METHOD_NOT_ALLOWED = HttpServletResponse.SC_METHOD_NOT_ALLOWED;
	public static final int SC_NOT_ACCEPTABLE = HttpServletResponse.SC_NOT_ACCEPTABLE;
	public static final int SC_PROXY_AUTHENTICATION_REQUIRED = HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED;
	public static final int SC_REQUEST_TIMEOUT = HttpServletResponse.SC_REQUEST_TIMEOUT;
	public static final int SC_CONFLICT = HttpServletResponse.SC_CONFLICT;
	public static final int SC_GONE = HttpServletResponse.SC_GONE;
	public static final int SC_LENGTH_REQUIRED = HttpServletResponse.SC_LENGTH_REQUIRED;
	public static final int SC_PRECONDITION_FAILED = HttpServletResponse.SC_PRECONDITION_FAILED;
	public static final int SC_REQUEST_ENTITY_TOO_LARGE = HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE;
	public static final int SC_REQUEST_URI_TOO_LONG = HttpServletResponse.SC_REQUEST_URI_TOO_LONG;
	public static final int SC_UNSUPPORTED_MEDIA_TYPE = HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
	public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE;
	public static final int SC_EXPECTATION_FAILED = HttpServletResponse.SC_EXPECTATION_FAILED;
	public static final int SC_INTERNAL_SERVER_ERROR = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	public static final int SC_NOT_IMPLEMENTED = HttpServletResponse.SC_NOT_IMPLEMENTED;
	public static final int SC_BAD_GATEWAY = HttpServletResponse.SC_BAD_GATEWAY;
	public static final int SC_SERVICE_UNAVAILABLE = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
	public static final int SC_GATEWAY_TIMEOUT = HttpServletResponse.SC_GATEWAY_TIMEOUT;
	public static final int SC_HTTP_VERSION_NOT_SUPPORTED = HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED;
	
	private HttpServletResponse resp;
	
	public SResponse(HttpServletResponse resp) {
		this.resp = resp;
		this.resp.setCharacterEncoding("UTF-8");
	}

	public void write(String data) throws Exception {
		resp.getOutputStream().write(data.getBytes("UTF-8"));
	}

	public void write(byte[] data) throws Exception {
		resp.getOutputStream().write(data);
	}

	public void addCookie(Cookie src) {
		resp.addCookie(src);
	}
	
	public void addDateHeader(String name, long date) {
		resp.addDateHeader(name, date);
	}
	
	public void addHeader(String name, String value) {
		resp.addHeader(name, value);
	}
	
	public void addIntHeader(String name, int value) {
		resp.addIntHeader(name, value);
	}
	
	public boolean containsHeader(String name) {
		return resp.containsHeader(name);
	}
	
	public String encodeRedirectURL(String url) {
		return resp.encodeRedirectURL(url);
	}
	
	public String encodeURL(String url) {
		return resp.encodeURL(url);
	}
	
	public void sendError(int sc) throws Exception {
		resp.sendError(sc);
	}
	
	public void sendError(int sc, String msg) throws Exception {
		resp.sendError(sc, msg);
	}
	
	public void sendRedirect(String location) throws Exception {
		resp.sendRedirect(location);
	}
	
	public void setDateHeader(String name, long date) {
		resp.setDateHeader(name, date);
	}
	
	public void setHeader(String key, String value) {
		resp.setHeader(key, value);
	}
	
	public void setIntHeader(String name, int value) {
		resp.setIntHeader(name, value);
	}
	
	public void setStatus(int sc) {
		resp.setStatus(sc);
	}
	
	public void flushBuffer() throws Exception {
		resp.flushBuffer();
	}
	
	public int getBufferSize() {
		return resp.getBufferSize();
	}
	
	public String getCharacterEncoding() {
		return resp.getCharacterEncoding();
	}
	
	public String getContentType() {
		return resp.getContentType();
	}
	
	public Locale getLocale() {
		return resp.getLocale();
	}
	
	public boolean isCommitted() {
		return resp.isCommitted();
	}
	
	public void reset() {
		resp.reset();
	}
	
	public void resetBuffer() {
		resp.resetBuffer();
	}
	
	public void setBufferSize(int size) {
		resp.setBufferSize(size);
	}
	
	public void setCharacterEncoding(String charset) {
		resp.setCharacterEncoding(charset);
	}
	
	public void setContentLength(int len) {
		resp.setContentLength(len);
	}
	
	public void setContentType(String type) {
		resp.setContentType(type);
	}
	
	public void setLocale(Locale loc) {
		resp.setLocale(loc);
	}
	
}
