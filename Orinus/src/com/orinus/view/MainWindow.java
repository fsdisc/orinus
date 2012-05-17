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

package com.orinus.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.orinus.Controller;
import com.orinus.IOTool;
import com.orinus.Runner;
import com.orinus.UITool;
import com.orinus.resource.Images;
import com.orinus.resource.Labels;
import com.orinus.schema.Engine;
import com.orinus.script.safe.lucene.SEntity;

public class MainWindow {

    private static Logger logger = Logger.getLogger(MainWindow.class);
	
	private Controller controller;
	private Shell shell;
    private Table tableEngine;

	private static MainWindow instance;
	
    public MainWindow() {
    	instance = this;
    	this.controller = new Controller();
    	Display display = Display.getDefault();
    	shell = new Shell(display, SWT.CLOSE | SWT.MIN);
    	decorate();
    }
    
    public static MainWindow getInstance() {
    	return instance;
    }
    
    public Shell getShell() {
    	return shell;
    }
    
    public void open() {
    	shell.open();
    	while (!shell.isDisposed()) {
    		if (!shell.getDisplay().readAndDispatch())
    			shell.getDisplay().sleep();
    	}
    }
    
    private void decorate() {
    	FormData fd;
    	Button button;
    	Label label;
    	Table table;
    	TableColumn column;
    	
		shell.setText(Labels.get("MainWindow.WindowTitle"));
    	shell.setSize(800, 600);
    	shell.setImage(Images.get("Icon.Orinus"));
    	
    	shell.setLayout(new FormLayout());
    	
    	label = new Label(shell, SWT.NONE);
    	label.setText(Labels.get("MainWindow.LabelEngines"));
    	fd = new FormData();
    	fd.top = new FormAttachment(0, 10);
    	fd.left = new FormAttachment(0, 10);
    	label.setLayoutData(fd);
    	
        table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
        tableEngine = table;
        fd = new FormData();
        fd.top = new FormAttachment(0, 30);
        fd.left = new FormAttachment(0, 10);
        fd.bottom = new FormAttachment(100, -50);
        fd.right = new FormAttachment(100, -10);
        table.setLayoutData(fd);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        column = new TableColumn(table, SWT.NONE);
        column.setWidth(30);
        column = new TableColumn(table, SWT.NONE);
        column.setWidth(300);
        column.setText(Labels.get("MainWindow.LabelDomain"));
        column = new TableColumn(table, SWT.NONE);
        column.setWidth(300);
        column.setText(Labels.get("MainWindow.LabelFolder"));
        column = new TableColumn(table, SWT.NONE);
        column.setWidth(100);
        column.setText(Labels.get("MainWindow.LabelQuota"));
        
        label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    	fd = new FormData();
    	fd.bottom = new FormAttachment(100, -40);
    	fd.left = new FormAttachment(0, 0);
    	fd.right = new FormAttachment(100, 0);
    	label.setLayoutData(fd);

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("MainWindow.Exit"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -10);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				shell.close();
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("MainWindow.ButtonAdd"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -95);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				AddEngineDialog dlg = new AddEngineDialog(shell, controller);
				dlg.open();
				if (!dlg.getCanceled()) {
					fillData();
				}
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("MainWindow.ButtonDelete"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -180);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				List<Engine> engines = new ArrayList<Engine>();
				for (int i = 0; i < tableEngine.getItemCount(); i++) {
					TableItem item = tableEngine.getItem(i);
					if (item.getChecked() && item.getData() != null) {
						engines.add((Engine)item.getData());
					}
				}
				if (engines.size() == 0) return;
				String datDir = controller.getDatDir();
				for (int i = 0; i < engines.size(); i++) {
					Engine engine = engines.get(i);
					engine.delete();
					String folder = new File(datDir, engine.getId()).getAbsolutePath();
					try {
						IOTool.deleteFolder(new File(folder));
					} catch (Exception e) {
						logger.error("", e);
					}
				}
				fillData();
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("MainWindow.ButtonEdit"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -265);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				Engine engine = null;
				for (int i = 0; i < tableEngine.getItemCount(); i++) {
					TableItem item = tableEngine.getItem(i);
					if (item.getChecked() && item.getData() != null) {
						engine = (Engine)item.getData();
						break;
					}
				}
				if (engine == null) return;
				EditEngineDialog dlg = new EditEngineDialog(shell, controller, engine);
				dlg.open();
				if (!dlg.getCanceled()) {
					fillData();
				}
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("MainWindow.ButtonOpen"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -350);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				Engine engine = null;
				boolean found = false;
				for (int i = 0; i < tableEngine.getItemCount(); i++) {
					TableItem item = tableEngine.getItem(i);
					if (item.getChecked()) {
						if (item.getData() != null) {
							engine = (Engine)item.getData();
						}
						found = true;
						break;
					}
				}
				if (!found) return;
				OpenEngineDialog dlg = new OpenEngineDialog(shell, controller, engine);
				dlg.open();
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("MainWindow.ButtonSettings"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -435);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				SettingsDialog dlg = new SettingsDialog(shell, controller);
				dlg.open();
				if (!dlg.getCanceled()) {
					fillData();
				}
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("MainWindow.ButtonRestart"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -520);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				Runner.stop();
				Runner.start();
				fillData();
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("MainWindow.ButtonRefresh"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -605);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				fillData();
			}
        });
        
    	UITool.placeCentered(shell, 800, 600);
    	
    	fillData();
    }
	
    private void fillData() {
    	try {
    		Engine pat = controller.newEngine();
    		List<SEntity> results = pat.search(pat.getKind(), pat.newMatchAllDocsQuery(), Integer.MAX_VALUE);
    		tableEngine.removeAll();
			TableItem item = new TableItem(tableEngine, SWT.NONE);
			item.setText(1, "localhost");
			item.setText(3, controller.getQuota() + "");
    		for (int i = 0; i < results.size(); i++) {
    			Engine engine = controller.newEngine();
    			engine.fromString(results.get(i).toString());
    			item = new TableItem(tableEngine, SWT.NONE);
    			item.setData(engine);
    			item.setText(1, engine.getDomain());
    			item.setText(2, engine.getFolder());
    			item.setText(3, engine.getQuota() + "");
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    }
    
}
