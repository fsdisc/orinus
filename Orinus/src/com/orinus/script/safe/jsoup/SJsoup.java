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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

public class SJsoup {

    public static Document parse(String html, String baseUri) {
        return Jsoup.parse(html, baseUri);
    }

    public static Document parse(String html) {
        return Jsoup.parse(html);
    }
 
    public static Document parse(URL url, int timeoutMillis) throws Exception {
        if (!"http".equals(url.getProtocol()) && !"https".equals(url.getProtocol())) throw new Exception("Protocol is not supported!");
        return Jsoup.parse(url, timeoutMillis);
    }

    public static Document parseBodyFragment(String bodyHtml) {
        return Jsoup.parseBodyFragment(bodyHtml);
    }
 
    public static Document parseBodyFragment(String bodyHtml, String baseUri) {
        return Jsoup.parseBodyFragment(bodyHtml, baseUri);
    }
 
    public static String clean(String bodyHtml, String baseUri, Whitelist whitelist) {
        return Jsoup.clean(bodyHtml, baseUri, whitelist);
    }
 
    public static String clean(String bodyHtml, Whitelist whitelist) {
        return Jsoup.clean(bodyHtml, whitelist);
    }
 
    public static boolean isValid(String bodyHtml, Whitelist whitelist) {
        return Jsoup.isValid(bodyHtml, whitelist);
    }
 
    public static SConnection connect(String url) throws Exception {
        return connect(new URL(url));
    }

    public static SConnection connect(URL url) throws Exception {
        if (!"http".equals(url.getProtocol()) && !"https".equals(url.getProtocol())) throw new Exception("Protocol is not supported!");
        return new SConnection(Jsoup.connect(url.toString()));
    }
 
}