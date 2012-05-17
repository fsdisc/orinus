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

package com.orinus.script.safe.jsoup;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

public class SConnection {

    private Connection data;
 
    public SConnection(Connection data) {
        this.data = data;
    }
 
    public SConnection method(String src) {
        if ("get".equalsIgnoreCase(src)) {
            this.data.method(Method.GET);
        }
        if ("post".equalsIgnoreCase(src)) {
            this.data.method(Method.POST);
        }
        return this;
    }
 
    public Response execute() throws Exception {
        return this.data.execute();
    }

    public SConnection cookie(String name, String value) {
        this.data.cookie(name, value);
        return this;
    }
 
    public SConnection cookies(Map cookies) {
        for (Object key : cookies.keySet()) {
            cookie(key + "", cookies.get(key) + "");
        }
        return this;
    }
 
    public SConnection data(Map src) {
        Map<String, String> tag = new HashMap<String, String>();
        for (Object key : src.keySet()) {
            tag.put(key + "", src.get(key) + "");
        }
        this.data.data(tag);
        return this;
    }
 
    public SConnection data(String... keyvals) {
        this.data.data(keyvals);
        return this;
    }

    public Document get() throws Exception {
        return this.data.get();
    }
 
    public SConnection header(String name, String value) {
        this.data.header(name, value);
        return this;
    }

    public Document post() throws Exception {
        return this.data.post();
    }
 
    public Map getCookies() {
        return this.data.response().cookies();
    }
 
    public SConnection referrer(String referrer) {
        this.data.referrer(referrer);
        return this;
    }
 
    public SConnection timeout(int millis) {
        this.data.timeout(millis);
        return this;
    }
 
    public SConnection url(URL url) throws Exception {
        if (!"http".equals(url.getProtocol()) && !"https".equals(url.getProtocol())) throw new Exception("Protocol is not supported!");
        this.data.url(url);
        return this;
    }

    public SConnection url(String url) throws Exception {
        return url(new URL(url));
    }
 
    public SConnection userAgent(String userAgent) {
        this.data.userAgent(userAgent);
        return this;
    }
 
}