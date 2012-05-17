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
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.jetty.server.Server;

import com.orinus.view.MainWindow;
import com.orinus.web.Router;

public class Runner {

    private static Logger logger = Logger.getLogger(Runner.class);
    private static boolean isService = false;
    private static Server server = null;
	
	public static void main(String[] args) {
		if ("4fa9f51572c6d".equals(System.getProperty("magic")) || (args.length > 0 && "-silent".equals(args[0]))) {
			isService = true;
			initLog(true);
			startSpider();
			start();
		} else {
			initLog(false);
			startSpider();
			start();
			new MainWindow().open();
			stop();
			System.exit(0);
		}
	}

	private static void startSpider() {
		Timer timer = new Timer();
		timer.schedule(new SpiderTask(), 10);
	}
	
	private static class SpiderTask extends TimerTask {
		@Override
		public void run() {
			new Spider().run();
		}
	}
	
	public static void start() {
		try {
			Controller controller = new Controller();
			server = new Server(controller.getPort());
			server.setHandler(new Router());
	        server.start();
	        if (isService) {
				logger.info("Starting Orinus Service");
		        server.join();		
	        } else {
				logger.info("Starting Orinus");
	        }
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	public static void stop() {
		try {
			server.stop();
			server = null;
			if (isService) {
				logger.info("Ending Orinus Service");
				System.exit(0);
			} else {
				logger.info("Ending Orinus");
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	private static void initLog(boolean ws) {
		try {
            String configDir = new File(System.getProperty("user.dir"), "cfg").getAbsolutePath();
            String logConfigFile = new File(configDir, "log-conf.xml").getAbsolutePath();
            if (ws) {
            	logConfigFile = new File(configDir, "log-conf-ws.xml").getAbsolutePath();
            }
            String logDir = new File(System.getProperty("user.dir"), "log").getAbsolutePath();
            String stdoutLogFile = new File(logDir, "stdout.log").getAbsolutePath();
			
            DOMConfigurator.configure(logConfigFile);
            System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(stdoutLogFile)), true));
            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(stdoutLogFile)), true));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
