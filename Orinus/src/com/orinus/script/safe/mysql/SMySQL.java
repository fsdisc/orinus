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

package com.orinus.script.safe.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SMySQL {

	private Connection conn;
	
	public void open(String server, String database, String username, String password) throws Exception {
		close();
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection("jdbc:mysql://" + server + "/" + database + "?"+ "user=" + username + "&password=" + password);
	}
	
	public void open(Map info) throws Exception {
		String server = info.get("server") + "";
		String database = info.get("database") + "";
		String username = info.get("username") + "";
		String password = info.get("password") + "";
		open(server, database, username, password);
	}
	
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
			}
			conn = null;
		}
	}
	
	public boolean execute(String sql, List params) throws Exception {
		PreparedStatement prep = buildStatement(sql, params);
		boolean tag = prep.execute();
		prep.close();
		return tag;
	}

	public boolean execute(String sql, Map... params) throws Exception {
		PreparedStatement prep = buildStatement(sql, params);
		boolean tag = prep.execute();
		prep.close();
		return tag;
	}

	public List query(String sql, List params) throws Exception {
		PreparedStatement prep = buildStatement(sql, params);
		ResultSet rs = prep.executeQuery();
		List tag = parseResult(rs);
		rs.close();
		prep.close();
		return tag;
	}

	public List query(String sql, Map... params) throws Exception {
		PreparedStatement prep = buildStatement(sql, params);
		ResultSet rs = prep.executeQuery();
		List tag = parseResult(rs);
		rs.close();
		prep.close();
		return tag;
	}
	
	public Map paramString(String value) {
		Map tag = new HashMap();
		tag.put("kind", "String");
		tag.put("value", value);
		return tag;
	}

	public Map paramBoolean(Boolean value) {
		Map tag = new HashMap();
		tag.put("kind", "Boolean");
		tag.put("value", value);
		return tag;
	}

	public Map paramInteger(Integer value) {
		Map tag = new HashMap();
		tag.put("kind", "Integer");
		tag.put("value", value);
		return tag;
	}

	public Map paramLong(Long value) {
		Map tag = new HashMap();
		tag.put("kind", "Long");
		tag.put("value", value);
		return tag;
	}
	
	public Map paramFloat(Float value) {
		Map tag = new HashMap();
		tag.put("kind", "Float");
		tag.put("value", value);
		return tag;
	}
	
	public Map paramDouble(Double value) {
		Map tag = new HashMap();
		tag.put("kind", "Double");
		tag.put("value", value);
		return tag;
	}

	protected PreparedStatement buildStatement(String sql, Map... params) throws Exception {
		List args = new ArrayList();
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				args.add(params[i]);
			}
		}
		return buildStatement(sql, args);
	}
	
	protected PreparedStatement buildStatement(String sql, List params) throws Exception {
		PreparedStatement prep = conn.prepareStatement(sql);
		for (int i = 0; i < params.size(); i++) {
			Map item = (Map)params.get(i);
			String kind = item.get("kind") + "";
			if ("String".equals(kind)) {
				prep.setString(i + 1, (String)item.get("value"));
			} else if ("Boolean".equals(kind)) {
				prep.setBoolean(i + 1, (Boolean)item.get("value"));
			} else if ("Integer".equals(kind)) {
				prep.setInt(i + 1, (Integer)item.get("value"));
			} else if ("Long".equals(kind)) {
				prep.setLong(i + 1, (Long)item.get("value"));
			} else if ("Float".equals(kind)) {
				prep.setFloat(i + 1, (Float)item.get("value"));
			} else if ("Double".equals(kind)) {
				prep.setDouble(i + 1, (Double)item.get("value"));
			} else {
				prep.setString(i + 1, item.get("value") + "");
			}
		}
		return prep;
	}

	protected List parseResult(ResultSet rs) throws Exception {
		List tag = new ArrayList();
		ResultSetMetaData md = rs.getMetaData();
		while (rs.next()) {
			Map item = new HashMap();
			for (int i = 1; i <= md.getColumnCount(); i++) {
				String name = md.getColumnName(i);
				int type = md.getColumnType(i);
				if (type == java.sql.Types.BIGINT) {
					item.put(name, rs.getLong(name));
				} else if (type == java.sql.Types.INTEGER || type == java.sql.Types.SMALLINT || type == java.sql.Types.TINYINT) {
					item.put(name, rs.getInt(name));
				} else if (type == java.sql.Types.DECIMAL || type == java.sql.Types.DOUBLE || type == java.sql.Types.NUMERIC || type == java.sql.Types.REAL) {
					item.put(name, rs.getDouble(name));
				} else if (type == java.sql.Types.FLOAT) {
					item.put(name, rs.getFloat(name));
				} else if (type == java.sql.Types.BIT || type == java.sql.Types.BOOLEAN) {
					item.put(name, rs.getBoolean(name));
				} else {
					item.put(name, rs.getString(name));
				}
			}
			tag.add(item);
		}
		return tag;
	}
	
}
