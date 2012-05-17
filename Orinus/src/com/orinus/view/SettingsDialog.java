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

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.orinus.Config;
import com.orinus.Controller;
import com.orinus.UITool;
import com.orinus.resource.Images;
import com.orinus.resource.Labels;

public class SettingsDialog extends Dialog {

    private Logger logger = Logger.getLogger(SettingsDialog.class);
    
    private Controller controller;
    private Shell shell;
    private Text textData;
    private Text textPort;
    private Text textSystem;
    private Text textHosts;
    private Text textQuota;
    private Text textMagic;
    private Text textTimeout;
    private Text textRuntime;
    private Text textToken;
    private Button checkDistributed;
    private Text textRemoteHost;
    private Text textRemotePort;
    private Text textRemoteToken;
    private Button checkRunScript;
	
    private boolean canceled = true;
	
    public SettingsDialog(Shell parent, Controller controller) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.controller = controller;
        setText(Labels.get("SettingsDialog.WindowTitle"));
        controller.loadConfig();
    }
	
    public boolean getCanceled() {
    	return canceled;
    }
    
    public void open() {
        shell = new Shell(getParent(), getStyle());
        shell.setText(getText());
        createContents(shell);
        shell.open();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
    
    private void createContents(final Shell shell) {
    	FormData fd;
    	Label label;
    	Button button;
    	Text text;
    	
        shell.setImage(Images.get("Icon.Orinus"));
        shell.setLayout(new FormLayout());
        shell.setSize(435, 730);
        UITool.placeCentered(shell, 435, 730);
    	
        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("SettingsDialog.LabelData"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 10);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textData = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 30);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setText(controller.getDatDir());
        
        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("SettingsDialog.ButtonBrowse"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 28);
        fd.right = new FormAttachment(100, -10);
        fd.width = 75;
        button.setLayoutData(fd);
        
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				DirectoryDialog dlg = new DirectoryDialog(shell, SWT.OPEN);
				dlg.setMessage(Labels.get("SettingsDialog.MessageData"));
				String filename = dlg.open();
				if (filename == null || filename.length() == 0) return;
				textData.setText(filename);
			}
        });

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("SettingsDialog.LabelPort"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 60);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textPort = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 80);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setText(controller.getPort() + "");

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("SettingsDialog.LabelSystem"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 110);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textSystem = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 130);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setText(controller.getSystem());

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("SettingsDialog.LabelHosts"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 160);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textHosts = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 180);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setEditable(false);
        text.setText(controller.getHosts());
        
        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("SettingsDialog.ButtonBrowse"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 178);
        fd.right = new FormAttachment(100, -10);
        fd.width = 75;
        button.setLayoutData(fd);
        
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				ManageHostDialog dlg = new ManageHostDialog(shell, controller, textHosts.getText());
				dlg.open();
				textHosts.setText(dlg.getHosts());
			}
        });

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("SettingsDialog.LabelQuota"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 210);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textQuota = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 230);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setText(controller.getQuota() + "");

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("SettingsDialog.LabelMagic"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 260);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textMagic = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 280);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setText(controller.getConfig().getString(Config.MAGIC));

        
        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("SettingsDialog.LabelTimeout"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 310);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textTimeout = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 330);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setText(controller.getTimeout() + "");

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("SettingsDialog.LabelRuntime"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 360);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textRuntime = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 380);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setText(controller.getRunTime() + "");

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("SettingsDialog.LabelToken"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 410);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textToken = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 430);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setEditable(false);
        text.setText(controller.getConfig().getString(Config.TOKEN));
        if (text.getText().trim().length() == 0) {
        	text.setText(controller.uniqid());
        }

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("SettingsDialog.ButtonNew"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 428);
        fd.right = new FormAttachment(100, -10);
        fd.width = 75;
        button.setLayoutData(fd);
        
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				textToken.setText(controller.uniqid());
			}
        });
        
        button = new Button(shell, SWT.CHECK);
        checkDistributed = button;
        button.setText(Labels.get("SettingsDialog.LabelDistributed"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 460);
        fd.left = new FormAttachment(0, 10);
        button.setLayoutData(fd);
        button.setSelection(controller.getConfig().getBoolean(Config.DISTRIBUTED));

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("SettingsDialog.LabelRemoteHost"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 480);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textRemoteHost = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 500);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setText(controller.getConfig().getString(Config.REMOTE_HOST));

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("SettingsDialog.LabelRemotePort"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 530);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textRemotePort = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 550);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setText(controller.getRemotePort() + "");
        
        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("SettingsDialog.LabelRemoteToken"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 580);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textRemoteToken = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 600);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setText(controller.getConfig().getString(Config.REMOTE_TOKEN));
        
        button = new Button(shell, SWT.CHECK);
        checkRunScript = button;
        button.setText(Labels.get("SettingsDialog.LabelRunScript"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 630);
        fd.left = new FormAttachment(0, 10);
        button.setLayoutData(fd);
        button.setSelection(controller.getConfig().getBoolean(Config.RUN_SCRIPT));
        
        
        label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    	fd = new FormData();
    	fd.bottom = new FormAttachment(100, -40);
    	fd.left = new FormAttachment(0, 0);
    	fd.right = new FormAttachment(100, 0);
    	label.setLayoutData(fd);

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("SettingsDialog.ButtonClose"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -10);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shell.close();
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("SettingsDialog.ButtonSave"));
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
				String data = textData.getText().trim();
				if (data.length() > 0) {
					File file = new File(data);
					if (!file.exists() || !file.isDirectory()) {
						UITool.warningBox(shell, Labels.get("SettingsDialog.DataNotExists"));
						return;
					}
				}
				String sport = textPort.getText().trim();
				if (sport.length() > 0) {
					try {
						int port = Integer.parseInt(sport);
						sport = port + "";
					} catch (Exception e) {
						UITool.warningBox(shell, Labels.get("SettingsDialog.InvalidPort"));
						return;
					}
				}
				String system = textSystem.getText().replaceAll("/", "").trim();
				String hosts = textHosts.getText();
				String squota = textQuota.getText().trim();
				if (squota.length() > 0) {
					try {
						double quota = Double.parseDouble(squota);
						squota = quota + "";
					} catch (Exception e) {
						UITool.warningBox(shell, Labels.get("SettingsDialog.InvalidQuota"));
						return;
					}
				}
				int timeout = parseInt(textTimeout.getText(), 60);
		    	if (timeout <= 0) timeout = 60;
		    	if (timeout > 60 * 24) timeout = 60 *24;
				int runtime = parseInt(textRuntime.getText(), 60 * 24);
		    	if (runtime <= 0) runtime = 60 * 24;
		    	int port = parseInt(textRemotePort.getText(), 80);
		    	if (port <= 0) port = 80;
				
				controller.loadConfig();
				controller.getConfig().setValue(Config.DATA, data);
				controller.getConfig().setValue(Config.PORT, sport);
				controller.getConfig().setValue(Config.SYSTEM, system);
				controller.getConfig().setValue(Config.HOSTS, hosts);
				controller.getConfig().setValue(Config.QUOTA, squota);
				controller.getConfig().setValue(Config.MAGIC, textMagic.getText().trim());
				controller.getConfig().setValue(Config.TIMEOUT, timeout);
				controller.getConfig().setValue(Config.RUNTIME, runtime);
				controller.getConfig().setValue(Config.TOKEN, textToken.getText().trim());
				controller.getConfig().setValue(Config.DISTRIBUTED, checkDistributed.getSelection());
				controller.getConfig().setValue(Config.REMOTE_HOST, textRemoteHost.getText().trim());
				controller.getConfig().setValue(Config.REMOTE_PORT, port);
				controller.getConfig().setValue(Config.REMOTE_TOKEN, textRemoteToken.getText().trim());
				controller.getConfig().setValue(Config.RUN_SCRIPT, checkRunScript.getSelection());
				
				controller.saveConfig();
				
				canceled = false;
				shell.close();
			}
        });
    }
    
    private int parseInt(String src, int defVal) {
    	int tag = defVal;
    	try {
    		tag = Integer.parseInt(src);
    	} catch (Exception e) {
    		tag = defVal;
    	}
    	return tag;
    }
    
}
