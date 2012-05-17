package com.orinus.view;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.orinus.Controller;
import com.orinus.UITool;
import com.orinus.resource.Images;
import com.orinus.resource.Labels;
import com.orinus.schema.Engine;
import com.orinus.schema.FileItem;

public class ScheduleDialog extends Dialog {

    private Logger logger = Logger.getLogger(ScheduleDialog.class);
    
    private Controller controller;
    private Shell shell;
    private Text textMinute;
    private Text textHour;
    private Text textDay;
    private Text textMonth;
    private Text textYear;
    private Text textTimeout;
	
    private boolean canceled = true;
    
    private Engine engine;
    private FileItem fitem;
	
    public ScheduleDialog(Shell parent, Controller controller, Engine engine, FileItem fitem) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.controller = controller;
        setText(Labels.get("ScheduleDialog.WindowTitle"));
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
        shell.setSize(435, 380);
        UITool.placeCentered(shell, 435, 380);
    	
        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("ScheduleDialog.LabelMinute"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 10);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textMinute = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 30);
        fd.left = new FormAttachment(0, 10);
        fd.width = 400;
        text.setLayoutData(fd);
        text.setText(fitem.getMinute());

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("ScheduleDialog.LabelHour"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 60);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textHour = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 80);
        fd.left = new FormAttachment(0, 10);
        fd.width = 400;
        text.setLayoutData(fd);
        text.setText(fitem.getHour());

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("ScheduleDialog.LabelDay"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 110);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textDay = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 130);
        fd.left = new FormAttachment(0, 10);
        fd.width = 400;
        text.setLayoutData(fd);
        text.setText(fitem.getDay());

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("ScheduleDialog.LabelMonth"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 160);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textMonth = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 180);
        fd.left = new FormAttachment(0, 10);
        fd.width = 400;
        text.setLayoutData(fd);
        text.setText(fitem.getMonth());
        
        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("ScheduleDialog.LabelYear"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 210);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textYear = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 230);
        fd.left = new FormAttachment(0, 10);
        fd.width = 400;
        text.setLayoutData(fd);
        text.setText(fitem.getYear());

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("ScheduleDialog.LabelTimeout"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 260);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textTimeout = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 280);
        fd.left = new FormAttachment(0, 10);
        fd.width = 400;
        text.setLayoutData(fd);
        int to = fitem.getTimeout();
        if (to <= 0) to = 1;
        if (to > 60 * 24) to = 60 * 24;
        text.setText(to + "");
        
        label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    	fd = new FormData();
    	fd.bottom = new FormAttachment(100, -40);
    	fd.left = new FormAttachment(0, 0);
    	fd.right = new FormAttachment(100, 0);
    	label.setLayoutData(fd);

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("ScheduleDialog.ButtonClose"));
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
        button.setText(Labels.get("ScheduleDialog.ButtonSave"));
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
				
				fitem.setScheduled(fitem.getName().endsWith(".jsb"));
				fitem.setMinute(textMinute.getText());
				fitem.setHour(textHour.getText());
				fitem.setDay(textDay.getText());
				fitem.setMonth(textMonth.getText());
				fitem.setYear(textYear.getText());
				int timeout = 1;
				try {
					timeout = Integer.parseInt(textTimeout.getText());
					if (timeout <= 0) timeout = 1;
					if (timeout > 60 * 24) timeout = 60 * 24;
				} catch (Exception e) {
					timeout = 1;
				}
				fitem.setTimeout(timeout);
				fitem.save();
				
				canceled = false;
				shell.close();
			}
        });
        
    }
	
}
