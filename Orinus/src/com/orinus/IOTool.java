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

package com.orinus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class IOTool {

	public static void unzipFile(String filename, String zipFolderSrc) throws Exception {
    	(new File(zipFolderSrc)).mkdirs();
    	ZipFile zipFile = new ZipFile(filename);
    	Enumeration zipEntries = zipFile.entries();
    	while(zipEntries.hasMoreElements()) {
    		ZipEntry zipEntry = (ZipEntry)zipEntries.nextElement();
    		if (zipEntry.isDirectory()) {
    			(new File(zipEntry.getName())).mkdir();
    			continue;
    		}

    		(new File(zipFolderSrc, zipEntry.getName())).getParentFile().mkdirs();
    		InputStream in = zipFile.getInputStream(zipEntry);
    		OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(zipFolderSrc, zipEntry.getName())));
    		byte[] buffer = new byte[1024];
    		int len;
    		while((len = in.read(buffer)) >= 0) {
    			out.write(buffer, 0, len);
    		}
    		in.close();
    		out.close();
    	}
    	zipFile.close();
	}
	
	public static void deleteFolder(File file) {
		if (!file.exists()) return;
		File[] files = file.listFiles();
		if (files == null) return;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				try {
					files[i].delete();
				} catch (Exception e) { }
			}
			if (files[i].isDirectory()) {
				deleteFolder(files[i]);
			}
		}
		try {
			file.delete();
		} catch (Exception e) { }
	}
	
	public static void zipFolder(String srcFolder, String destZipFile) throws Exception {
		ZipOutputStream zip = null;
		FileOutputStream fileWriter = null;

		fileWriter = new FileOutputStream(destZipFile);
		zip = new ZipOutputStream(fileWriter);

		addFolderToZip("", srcFolder, zip);
		zip.flush();
		zip.close();
	}

	private static void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws Exception {
		File folder = new File(srcFile);
		if (folder.isDirectory()) {
			addFolderToZip(path, srcFile, zip);
		} else {
			byte[] buf = new byte[1024];
			int len;
			FileInputStream in = new FileInputStream(srcFile);
			zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
			while ((len = in.read(buf)) > 0) {
				zip.write(buf, 0, len);
			}
			in.close();
		}
	}

	private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception {
		File folder = new File(srcFolder);

		for (String fileName : folder.list()) {
			if (path.equals("")) {
				addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
			} else {
				addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
			}
		}
	}
	
}
