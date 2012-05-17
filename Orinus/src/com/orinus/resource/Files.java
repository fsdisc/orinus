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

package com.orinus.resource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class Files {

    private static Logger logger = Logger.getLogger(Files.class);
	
	private static Map<String, String> registry;
	
	private static void setup() {
		if (registry != null) return;
		registry = new HashMap<String, String>();
	}
	
	public static String get(String key) {
		String tag = "";
		try {
			tag = new String(getB(key), "UTF-8");
		} catch (Exception e) {
			logger.error("", e);
		}
		return tag;
	}

	public static byte[] getB(String key) {
		setup();
		String filename = "/com/orinus/resource/files/" + registry.get(key);
		byte[] tag = new byte[0];
		
		try {
			InputStream stream =  Files.class.getResourceAsStream(filename);
			tag = new byte[stream.available()];
			stream.read(tag);
		} catch (Exception e) {
			logger.error("", e);
		}
		
		return tag;
	}
	
}
