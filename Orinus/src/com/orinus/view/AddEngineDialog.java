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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.orinus.Config;
import com.orinus.Controller;
import com.orinus.UITool;
import com.orinus.resource.Images;
import com.orinus.resource.Labels;
import com.orinus.schema.Engine;
import com.orinus.script.safe.lucene.SEntity;

public class AddEngineDialog extends Dialog {

    private Logger logger = Logger.getLogger(AddEngineDialog.class);
    
    private Controller controller;
    private Shell shell;
    private Text textDomain;
    private Text textFolder;
    private Text textQuota;
    private Text textTimeout;
    private Text textRuntime;
    private Text textToken;
    private Button checkDistributed;
    private Text textRemoteHost;
    private Text textRemotePort;
    private Text textRemoteToken;
    private Button checkRunScript;
	
    private boolean canceled = true;
	
    public AddEngineDialog(Shell parent, Controller controller) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.controller = controller;
        setText(Labels.get("AddEngineDialog.WindowTitle"));
    }
	
    public boolean getCanceled() {
    	return canceled;
    }
    
    public void open() {
        shell = new Shell(getParent(), getStyle());
        shell.setText(getText());
        createContents(shell);
        shell.pack();
        shell.open();
        UITool.placeCentered(shell);
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
    	
        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("AddEngineDialog.LabelDomain"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 10);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textDomain = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 30);
        fd.left = new FormAttachment(0, 10);
        fd.width = 400;
        text.setLayoutData(fd);

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("AddEngineDialog.LabelFolder"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 60);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textFolder = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 80);
        fd.left = new FormAttachment(0, 10);
        fd.width = 400;
        text.setLayoutData(fd);
        
        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("AddEngineDialog.LabelQuota"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 110);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textQuota = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 130);
        fd.left = new FormAttachment(0, 10);
        fd.width = 100;
        text.setLayoutData(fd);

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("AddEngineDialog.LabelQuotaUnit"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 132);
        fd.left = new FormAttachment(0, 130);
        label.setLayoutData(fd);
        
        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("AddEngineDialog.LabelTimeout"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 160);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textTimeout = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 180);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setText("60");

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("AddEngineDialog.LabelRuntime"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 210);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textRuntime = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 230);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setText("1440");

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("AddEngineDialog.LabelToken"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 260);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textToken = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 280);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setEditable(false);
        if (text.getText().trim().length() == 0) {
        	text.setText(controller.uniqid() + controller.uniqid() + controller.uniqid() + controller.uniqid());
        }

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("AddEngineDialog.ButtonNew"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 278);
        fd.right = new FormAttachment(100, -10);
        fd.width = 75;
        button.setLayoutData(fd);
        
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				textToken.setText(controller.uniqid() + controller.uniqid() + controller.uniqid() + controller.uniqid());
			}
        });
        
        button = new Button(shell, SWT.CHECK);
        checkDistributed = button;
        button.setText(Labels.get("AddEngineDialog.LabelDistributed"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 310);
        fd.left = new FormAttachment(0, 10);
        button.setLayoutData(fd);

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("AddEngineDialog.LabelRemoteHost"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 340);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textRemoteHost = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 360);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("AddEngineDialog.LabelRemotePort"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 390);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textRemotePort = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 410);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        text.setText("80");
        
        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("AddEngineDialog.LabelRemoteToken"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 440);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textRemoteToken = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 460);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        
        button = new Button(shell, SWT.CHECK);
        checkRunScript = button;
        button.setText(Labels.get("AddEngineDialog.LabelRunScript"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 490);
        fd.left = new FormAttachment(0, 10);
        button.setLayoutData(fd);
        
        label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    	fd = new FormData();
    	fd.bottom = new FormAttachment(100, -40);
    	fd.left = new FormAttachment(0, 0);
    	fd.right = new FormAttachment(100, 0);
    	label.setLayoutData(fd);

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("AddEngineDialog.ButtonClose"));
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
        button.setText(Labels.get("AddEngineDialog.ButtonAdd"));
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
				String domain = textDomain.getText().trim();
				if (domain.length() > 0) {
					if (findEngineByDomain(domain) != null) {
						UITool.warningBox(shell, Labels.get("AddEngineDialog.DomainExists"));
						return;
					}
				}
				
				String folder = textFolder.getText().replaceAll("/", "").trim();
				textFolder.setText(folder);
				if (folder.length() == 0) {
					UITool.warningBox(shell, Labels.get("AddEngineDialog.MessageFolderRequired"));
					return;
				}
				if (findEngineByFolder(folder) != null) {
					UITool.warningBox(shell, Labels.get("AddEngineDialog.FolderExists"));
					return;
				}
				
				double quota = 0;
				try {
					quota = Double.parseDouble(textQuota.getText());
				} catch (Exception e) {
					quota = 0;
				}
				if (quota <= 0) {
					UITool.warningBox(shell, Labels.get("AddEngineDialog.MessageInvalidQuota"));
					return;
				}
				
				int timeout = parseInt(textTimeout.getText(), 60);
		    	if (timeout <= 0) timeout = 60;
		    	if (timeout > 60 * 24) timeout = 60 *24;
				int runtime = parseInt(textRuntime.getText(), 60 * 24);
		    	if (runtime <= 0) runtime = 60 * 24;
		    	int port = parseInt(textRemotePort.getText(), 80);
		    	if (port <= 0) port = 80;
				
				Engine engine = controller.newEngine();
				engine.setId(controller.uniqid());
				engine.setDomain(domain);
				engine.setFolder(folder);
				engine.setQuota(quota);
				engine.setTimeout(timeout);
				engine.setRuntime(runtime);
				engine.setToken(textToken.getText());
				engine.setDistributed(checkDistributed.getSelection());
				engine.setRemoteHost(textRemoteHost.getText().trim());
				engine.setRemotePort(port);
				engine.setRemoteToken(textRemoteToken.getText().trim());
				engine.setRunScript(checkRunScript.getSelection());
				engine.save();
				
				canceled = false;
				shell.close();
			}
        });
        
        label = new Label(shell, SWT.LEFT);
        fd = new FormData();
        fd.top = new FormAttachment(0, 550);
        fd.left = new FormAttachment(0, 435);
        label.setLayoutData(fd);
        
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
    
    private Engine findEngineByFolder(String folder) {
    	Engine pat = controller.newEngine();
    	List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.FOLDER, folder)), 1);
    	Engine tag = null;
    	if (results.size() > 0) {
    		pat.fromString(results.get(0).toString());
    		tag = pat;
    	}
    	return tag;
    }

    private Engine findEngineByDomain(String domain) {
    	Engine pat = controller.newEngine();
    	List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.DOMAIN, domain)), 1);
    	Engine tag = null;
    	if (results.size() > 0) {
    		pat.fromString(results.get(0).toString());
    		tag = pat;
    	}
    	return tag;
    }
    
}
