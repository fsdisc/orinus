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

package com.orinus.script.safe.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.orinus.Controller;
import com.orinus.IOTool;

public class SZip {

    private Logger logger = Logger.getLogger(SZip.class);
	
	private Controller controller;
	private String root;
	
	public SZip() {
		controller = new Controller();
		root = controller.getTempDir();
	}
	
	public void create(String folder) {
		folder = folder.replaceAll("\\.\\.", "");
		folder = folder.replaceAll("/", "");
		folder = folder.replaceAll("\\\\", "");
		root = new File(controller.getTempDir(), folder).getAbsolutePath();
	}
	
	public void load(byte[] data) {
		try {
			String srcDir = controller.getTempDir();
			String filename = new File(srcDir, "data.zip").getAbsolutePath();
        	OutputStream os = new FileOutputStream(filename);
        	os.write(data);
        	os.close();
			String tagDir = controller.getTempDir();
    		IOTool.unzipFile(filename, tagDir);
    		File file = new File(tagDir);
    		File[] children = file.listFiles();
    		root = controller.getTempDir();
    		if (children.length > 0) {
        		file = children[0];
        		if (file.isDirectory()) {
        			root = file.getAbsolutePath();
        		}
    		}
		} catch (Exception e) {
			logger.error("", e);
			root = controller.getTempDir();
		}
	}

	public byte[] save() {
		byte[] tag = new byte[0];
		try {
			String dirTemp = controller.getTempDir();
			String filename = new File(dirTemp, "data.zip").getAbsolutePath();
			IOTool.zipFolder(root, filename);
			InputStream is = new FileInputStream(filename);
			tag = new byte[is.available()];
			is.read(tag);
			is.close();
		} catch (Exception e) {
			logger.error("", e);
		}
		return tag;
	}
	
	public boolean exists(String path) {
		String filename = parse(path);
		return new File(filename).exists();
	}
	
	public byte[] read(String path) {
		byte[] tag = new byte[0];
		try {
			String filename = parse(path);
    		InputStream is = new FileInputStream(filename);
    		tag = new byte[is.available()];
    		is.read(tag);
    		is.close();
		} catch (Exception e) {
			logger.error("", e);
		}
		return tag;
	}
	
	public void mkdirs(String path) {
		new File(parse(path)).mkdirs();
	}
	
	public void write(String path, byte[] data) {
		try {
			String filename = parse(path);
			if (root.equals(filename)) return;
			File file = new File(filename);
			if (file.getName().lastIndexOf(".") < 0) return;
			file.getParentFile().mkdirs();
			OutputStream os = new FileOutputStream(filename);
			os.write(data);
			os.close();
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	public List<FileEntry> list(String path) {
		List<FileEntry> tag = new ArrayList<FileEntry>();
		try {
			File[] children = new File(parse(path)).listFiles(); 
			for (int i = 0; i < children.length; i++) {
				File child = children[i];
				FileEntry fe = new FileEntry();
				fe.name = child.getName();
				fe.size = child.length();
				fe.directory = child.isDirectory();
				tag.add(fe);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		return tag;
	}
	
	private String parse(String path) {
		String tag = path;
		tag = tag.replaceAll("\\.\\.", "");
		if (tag.startsWith("/")) tag = tag.substring(1);
		if (File.pathSeparator.equals("\\")) {
			tag = tag.replaceAll("/", File.pathSeparator);
		}
		if (tag.length() == 0) {
			return root;
		} else {
			return new File(root, tag).getAbsolutePath();
		}
	}
	
	public static class FileEntry {
		public String name = "";
		public long size = 0;
		public boolean directory = false;
	}
	
}
