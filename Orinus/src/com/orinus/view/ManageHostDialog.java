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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.orinus.Controller;
import com.orinus.UITool;
import com.orinus.resource.Images;
import com.orinus.resource.Labels;

public class ManageHostDialog extends Dialog {

    private Logger logger = Logger.getLogger(ManageHostDialog.class);
    
    private Controller controller;
    private Shell shell;
    private Table tableHosts;
    private Text textHost;

    private String hosts;
	
    public ManageHostDialog(Shell parent, Controller controller, String hosts) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.controller = controller;
        this.hosts = hosts;
        setText(Labels.get("ManageHostDialog.WindowTitle"));
    }
    
    public String getHosts() {
    	return hosts;
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
    	Table table;
    	TableColumn column;
    	
        shell.setImage(Images.get("Icon.Orinus"));
        shell.setLayout(new FormLayout());
        shell.setSize(435, 330);
        UITool.placeCentered(shell, 435, 330);
    	
        table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        tableHosts = table;
        fd = new FormData();
        fd.top = new FormAttachment(0, 10);
        fd.left = new FormAttachment(0, 10);
        fd.bottom = new FormAttachment(100, -80);
        fd.right = new FormAttachment(100, -10);
        table.setLayoutData(fd);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        column = new TableColumn(table, SWT.NONE);
        column.setWidth(380);
        column.setText(Labels.get("ManageHostDialog.LabelDomain"));
        
        String vals = hosts.trim();
        if (vals.startsWith("|")) vals.substring(1).trim();
        if (vals.endsWith("|")) vals.substring(0, vals.length() - 1);
        String[] fields = vals.split("\\|");
        for (int i = 0; i < fields.length; i++) {
        	String domain = fields[i].trim();
        	if (domain.length() == 0) continue;
        	TableItem item = new TableItem(tableHosts, SWT.NONE);
        	item.setText(0, domain);
        }
    	TableItem item = new TableItem(tableHosts, SWT.NONE);
    	tableHosts.select(tableHosts.getItemCount() - 1);
        
        table.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				int selIdx = tableHosts.getSelectionIndex();
				if (selIdx < 0) {
					textHost.setText("");
				} else {
					textHost.setText(tableHosts.getItem(selIdx).getText(0));
				}
			}
        });
        
        text = new Text(shell, SWT.BORDER);
        textHost = text;
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -52);
        fd.left = new FormAttachment(0, 10);
        fd.width = 335;
        text.setLayoutData(fd);
        
        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("ManageHostDialog.ButtonSave"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -50);
        fd.right = new FormAttachment(100, -10);
        fd.width = 50;
        button.setLayoutData(fd);
        
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				int selIdx = tableHosts.getSelectionIndex();
				if (selIdx < 0) return;
				String domain = textHost.getText().trim();
				if (domain.length() == 0) {
					UITool.warningBox(shell, Labels.get("ManageHostDialog.DomainRequired"));
					return;
				}
				TableItem item = tableHosts.getItem(selIdx);
				item.setText(domain);
				if (selIdx == tableHosts.getItemCount() - 1) {
					item = new TableItem(tableHosts, SWT.NONE);
					tableHosts.select(tableHosts.getItemCount() - 1);
					textHost.setText("");
				}
			}
        });
        
        label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    	fd = new FormData();
    	fd.bottom = new FormAttachment(100, -40);
    	fd.left = new FormAttachment(0, 0);
    	fd.right = new FormAttachment(100, 0);
    	label.setLayoutData(fd);

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("ManageHostDialog.ButtonClose"));
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
				if (tableHosts.getItemCount() <= 1) {
					hosts = "";
				} else {
					String tag = "|";
					for (int i = 0; i < tableHosts.getItemCount() - 1; i++) {
						tag += tableHosts.getItem(i).getText().trim() + "|";
					}
					if ("|".equals(tag)) {
						hosts = "";
					} else {
						hosts = tag;
					}
				}
				
				shell.close();
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("ManageHostDialog.ButtonDelete"));
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
				int selIdx = tableHosts.getSelectionIndex();
				if (selIdx < 0) return;
				if (selIdx == tableHosts.getItemCount() - 1) return;
				tableHosts.remove(selIdx);
				tableHosts.select(tableHosts.getItemCount() - 1);
				textHost.setText("");
			}
        });
        
        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("ManageHostDialog.ButtonNew"));
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
				tableHosts.select(tableHosts.getItemCount() - 1);
				textHost.setText("");
			}
        });
        
    }
    
}
