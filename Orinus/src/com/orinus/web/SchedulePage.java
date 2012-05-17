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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.orinus.schema.Engine;
import com.orinus.schema.FileItem;

public class SchedulePage extends BasePage {

    private Engine engine;
    private FileItem fitem;
	
	public SchedulePage(Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		super(baseRequest, request, response);
	}
	
	public void execute() throws Exception {
		if (!checkOnline()) return;
		
		String eid = getParameter("eid");
		
		if (!DEFAULT_ENGINE.equals(eid)) {
			engine = controller.newEngine();
			engine.load(eid);
			if (engine.getId().length() == 0) {
				returnHome();
				return;
			}
		}
		
		String fid = getParameter("fid");
		fitem = controller.newFileItem(engine);
		fitem.load(fid);
		if (fitem.getId().length() == 0) {
			returnHome();
			return;
		}
		
		if (!fitem.getName().endsWith(".jsb")) {
			returnPage("edit-file.jsb?eid=" + eid + "&fid=" + fid);
			return;
		}
		
		String message = "";
		String minute = fitem.getMinute();
		String hour = fitem.getHour();
		String day = fitem.getDay();
		String month = fitem.getMonth();
		String year = fitem.getYear();
		int itimeout = fitem.getTimeout();
        if (itimeout <= 0) itimeout = 1;
        if (itimeout > 60 * 24) itimeout = 60 * 24;
        String timeout = itimeout + "";
		
		if (isPost()) {
			minute = getParameter("minute");
			hour = getParameter("hour");
			day = getParameter("day");
			month = getParameter("month");
			year = getParameter("year");
			timeout = getParameter("timeout");
			try {
				itimeout = Integer.parseInt(timeout);
		        if (itimeout <= 0) itimeout = 1;
		        if (itimeout > 60 * 24) itimeout = 60 * 24;
			} catch (Exception e) {
				itimeout = 1;
			}
			timeout = itimeout + "";
			
			fitem.setScheduled(true);
			fitem.setMinute(minute);
			fitem.setHour(hour);
			fitem.setDay(day);
			fitem.setMonth(month);
			fitem.setYear(year);
			fitem.setTimeout(itimeout);
			fitem.save();
			
			returnPage("edit-file.jsb?eid=" + eid + "&fid=" + fid);
			return;
		}
		
		Map args = newArgs();
		Map data = new HashMap();
		data.put("eid", eid);
		data.put("fid", fid);
		data.put("minute", minute);
		data.put("hour", hour);
		data.put("day", day);
		data.put("month", month);
		data.put("year", year);
		data.put("timeout", timeout);
		data.put("message", message);
		args.put("data", data);
		loadPage("schedule.vm", args);
	}
	
}
