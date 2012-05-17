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

package com.orinus.schema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.orinus.script.safe.lucene.SEntity;

public class FileData extends SEntity {

	public static final String DATA = "data";
	
    public FileData(Handler handler) {
    	super(handler);
    	setKind("FileData");
    }

    protected void registerDefault() {
    	super.registerDefault();
        register(DATA, STRING);
    }

    public byte[] getData() {
    	byte[] tag = new byte[0];
    	try {
    		tag = decodeBase64(getString(DATA).getBytes("UTF-8"));
    	} catch (Exception e) {
    	}
    	return tag;
    }
    
    public void setData(byte[] src) {
    	try {
    		setString(DATA, new String(encodeBase64(src), "UTF-8"));
    	} catch (Exception e) {
    	}
    }
    
    private byte[] decodeBase64(byte[] b) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        InputStream b64is = javax.mail.internet.MimeUtility.decode(bais, "base64");
        byte[] tmp = new byte[b.length];
        int n = b64is.read(tmp);
        byte[] res = new byte[n];
        System.arraycopy(tmp, 0, res, 0, n);
        return res;
    }      

    private byte[] encodeBase64(byte[] b) throws Exception {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream b64os = javax.mail.internet.MimeUtility.encode(baos, "base64");
        b64os.write(b);
        b64os.close();
        return baos.toByteArray();
    }
	
}
