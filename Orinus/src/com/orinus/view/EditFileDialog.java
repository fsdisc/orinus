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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.search.BooleanQuery;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.orinus.Controller;
import com.orinus.UITool;
import com.orinus.resource.Images;
import com.orinus.resource.Labels;
import com.orinus.schema.Engine;
import com.orinus.schema.FileData;
import com.orinus.schema.FileItem;
import com.orinus.script.safe.lucene.SEntity;

public class EditFileDialog extends Dialog {

    private Logger logger = Logger.getLogger(EditFileDialog.class);
    
    private Controller controller;
    private Shell shell;
    private Text textName;
    private Text textData;
    private Button checkPublished;
	
    private boolean canceled = true;
    
    private Engine engine;
    private FileItem fitem;
	
    public EditFileDialog(Shell parent, Controller controller, Engine engine, FileItem fitem) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.controller = controller;
        setText(Labels.get("EditFileDialog.WindowTitle"));
        this.engine = engine;
        this.fitem = fitem;
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
        shell.setSize(435, 210);
        UITool.placeCentered(shell, 435, 210);
    	
        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("EditFileDialog.LabelName"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 10);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textName = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 30);
        fd.left = new FormAttachment(0, 10);
        fd.width = 400;
        text.setLayoutData(fd);
        textName.setText(fitem.getName());

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("EditFileDialog.LabelData"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 60);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textData = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 80);
        fd.left = new FormAttachment(0, 10);
        fd.width = 310;
        text.setLayoutData(fd);
        
        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("EditFileDialog.ButtonBrowse"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 78);
        fd.right = new FormAttachment(100, -10);
        fd.width = 75;
        button.setLayoutData(fd);
        
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				FileDialog dlg = new FileDialog(shell, SWT.OPEN);
				String filename = dlg.open();
				if (filename == null || filename.length() == 0) return;
				textData.setText(filename);
			}
        });
        
        button = new Button(shell, SWT.CHECK);
        checkPublished = button;
        button.setText(Labels.get("EditFileDialog.LabelPublished"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 110);
        fd.left = new FormAttachment(0, 10);
        button.setLayoutData(fd);
        button.setSelection(fitem.getPublished());
        
        label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    	fd = new FormData();
    	fd.bottom = new FormAttachment(100, -40);
    	fd.left = new FormAttachment(0, 0);
    	fd.right = new FormAttachment(100, 0);
    	label.setLayoutData(fd);

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("EditFileDialog.ButtonClose"));
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
        button.setText(Labels.get("EditFileDialog.ButtonSave"));
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
				String name = textName.getText().replaceAll("/", "").trim();
				if (name.length() == 0) {
					UITool.warningBox(shell, Labels.get("EditFileDialog.NameRequired"));
					return;
				}
				if (name.lastIndexOf(".") < 0) {
					UITool.warningBox(shell, Labels.get("EditFileDialog.ExtensionRequired"));
					return;
				}
				if (findFileByName(name) != null) {
					UITool.warningBox(shell, Labels.get("EditFileDialog.NameExists"));
					return;
				}
				if (textData.getText().length() > 0) {
					File file = new File(textData.getText());
					if (!file.exists()) {
						UITool.warningBox(shell, Labels.get("EditFileDialog.DataNotExists"));
						return;
					}

					byte[] data = new byte[0];
					try {
						InputStream is = new FileInputStream(file);
						data = new byte[is.available()];
						is.read(data);
						is.close();
					} catch (Exception e) {
						UITool.warningBox(shell, UITool.getMessage(e));
						return;
					}
					
					FileData fdata = controller.newFileData(engine);
					fdata.load(fitem.getData());
					fdata.setData(data);
					fdata.save();
				}
				
				fitem.setName(name);
				fitem.setPublished(checkPublished.getSelection());
				fitem.setScheduled(name.endsWith(".jsb"));
				fitem.save();
				
				canceled = false;
				shell.close();
			}
        });
        
        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("EditFileDialog.ButtonDownload"));
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
				FileDialog dlg = new FileDialog(shell, SWT.SAVE);
				dlg.setFileName(fitem.getName());
				String filename = dlg.open();
				if (filename == null || filename.length() == 0) return;
				try {
					FileData fdata = controller.newFileData(engine);
					fdata.load(fitem.getData());
					OutputStream os = new FileOutputStream(filename);
					os.write(fdata.getData());
					os.close();
				} catch (Exception e) {
					logger.error("", e);
					UITool.errorBox(shell, UITool.getMessage(e));
				}
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("EditFileDialog.ButtonLog"));
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
				LogDialog dlg = new LogDialog(shell, controller, engine, fitem);
				dlg.open();
			}
        });
        
        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("EditFileDialog.ButtonSchedule"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -350);
        fd.width = 75;
        button.setLayoutData(fd);
        button.setVisible(fitem.getName().endsWith(".jsb"));
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				ScheduleDialog dlg = new ScheduleDialog(shell, controller, engine, fitem);
				dlg.open();
			}
        });

        
    }
	
    private FileItem findFileByName(String name) {
    	FileItem tag = null;
    	FileItem pat = controller.newFileItem(engine);
    	String pid = fitem.getParent();
    	BooleanQuery bq = pat.newBooleanQuery();
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.NAME, name)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.ID, fitem.getId())), pat.occurMustNot()));
    	List<SEntity> results = pat.search(pat.getKind(), bq, 1);
    	if (results.size() > 0) {
    		pat.fromString(results.get(0).toString());
    		tag = pat;
    	}
    	return tag;
    }
	
}
