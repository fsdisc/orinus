package com.orinus.view;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
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
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.orinus.Controller;
import com.orinus.IOTool;
import com.orinus.UITool;
import com.orinus.resource.Images;
import com.orinus.resource.Labels;
import com.orinus.schema.Engine;
import com.orinus.schema.FileData;
import com.orinus.schema.FileItem;
import com.orinus.schema.LogData;
import com.orinus.schema.LogItem;
import com.orinus.script.safe.lucene.SEntity;

public class LogDialog extends Dialog {

    private Logger logger = Logger.getLogger(AddEngineDialog.class);
    
    private Controller controller;
    private Shell shell;
    private Engine engine;
    private FileItem fitem;
    private Table tableLog;    
	
    public LogDialog(Shell parent, Controller controller, Engine engine, FileItem fitem) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.controller = controller;
        setText(Labels.get("LogDialog.WindowTitle"));
        this.engine = engine;
        this.fitem = fitem;
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
    	Button button;
    	Label label;
    	Table table;
    	TableColumn column;
    	
    	shell.setImage(Images.get("Icon.Orinus"));
    	shell.setSize(400, 500);
        UITool.placeCentered(shell, 400, 500);
    	
    	shell.setLayout(new FormLayout());
    	
        table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
        tableLog = table;
        fd = new FormData();
        fd.top = new FormAttachment(0, 10);
        fd.left = new FormAttachment(0, 10);
        fd.bottom = new FormAttachment(100, -50);
        fd.right = new FormAttachment(100, -10);
        table.setLayoutData(fd);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        column = new TableColumn(table, SWT.NONE);
        column.setWidth(30);
        column = new TableColumn(table, SWT.NONE);
        column.setWidth(200);
        column.setText(Labels.get("LogDialog.LabelCreated"));
        column = new TableColumn(table, SWT.NONE);
        column.setWidth(100);
        column.setText(Labels.get("LogDialog.LabelSize"));
    	
        label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    	fd = new FormData();
    	fd.bottom = new FormAttachment(100, -40);
    	fd.left = new FormAttachment(0, 0);
    	fd.right = new FormAttachment(100, 0);
    	label.setLayoutData(fd);

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("LogDialog.ButtonClose"));
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
        button.setText(Labels.get("LogDialog.ButtonDelete"));
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
				List<LogItem> litems = new ArrayList<LogItem>();
				for (int i = 0; i < tableLog.getItemCount(); i++) {
					TableItem item = tableLog.getItem(i);
					if (item.getChecked() && item.getData() != null) {
						litems.add((LogItem)item.getData());
					}
				}
				if (litems.size() == 0) return;
				for (int i = 0; i < litems.size(); i++) {
					LogItem li = litems.get(i);
					LogData ld = controller.newLogData(engine);
					ld.setId(li.getData());
					li.delete();
					ld.delete();
				}
				fillData();
			}
        });
        
        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("LogDialog.ButtonDownload"));
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
				LogItem litem = null;
				for (int i = 0; i < tableLog.getItemCount(); i++) {
					TableItem item = tableLog.getItem(i);
					if (item.getChecked() && item.getData() != null) {
						litem = (LogItem)item.getData();
						break;
					}
				}
				if (litem == null) return;
				FileDialog dlg = new FileDialog(shell, SWT.SAVE);
				String filename = dlg.open();
				if (filename == null || filename.length() == 0) return;
				try {
					LogData ldata = controller.newLogData(engine);
					ldata.load(litem.getData());
					OutputStream os = new FileOutputStream(filename);
					os.write(ldata.getData());
					os.close();
				} catch (Exception e) {
					logger.error("", e);
					UITool.errorBox(shell, UITool.getMessage(e));
				}
			}
        });
        
        fillData();
    }
    
    private void fillData() {
    	tableLog.removeAll();
    	LogItem pat = controller.newLogItem(engine);
    	List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.PARENT, fitem.getId())), pat.newSort(pat.newSortField(pat.CREATED, pat.sortFieldLong(), true)), Integer.MAX_VALUE);
    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    	for (int i = 0; i < results.size(); i++) {
    		LogItem li = controller.newLogItem(engine);
    		li.fromString(results.get(i).toString());
    		TableItem item = new TableItem(tableLog, SWT.NONE);
    		item.setText(1, sdf.format(li.getDate(li.CREATED)));
    		item.setText(2, li.getSize() + "");
    		item.setData(li);
    	}
    	
    }
    
}
