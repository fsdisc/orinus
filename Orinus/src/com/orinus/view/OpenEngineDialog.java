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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.orinus.Config;
import com.orinus.Controller;
import com.orinus.IOTool;
import com.orinus.UITool;
import com.orinus.resource.Images;
import com.orinus.resource.Labels;
import com.orinus.schema.Engine;
import com.orinus.schema.FileData;
import com.orinus.schema.FileItem;
import com.orinus.schema.Folder;
import com.orinus.schema.LogData;
import com.orinus.schema.LogItem;
import com.orinus.script.safe.lucene.SEntity;

public class OpenEngineDialog extends Dialog {

    private Logger logger = Logger.getLogger(OpenEngineDialog.class);
    
    private Controller controller;
    private Shell shell;
    private Tree treeFolder;
    private Table tableFile;
    
    private Engine engine;
    private String engineTitle;
	
    public OpenEngineDialog(Shell parent, Controller controller, Engine engine) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.controller = controller;
        if (engine != null) {
        	engineTitle = engine.getDomain() + " / " + engine.getFolder();
        } else {
            engineTitle = "localhost /";
        }
        setText(Labels.get("OpenEngineDialog.WindowTitle") + engineTitle);
        this.engine = engine;
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
    	Tree tree;
    	Table table;
    	TableColumn column;
    	
        shell.setImage(Images.get("Icon.Orinus"));
        shell.setLayout(new FormLayout());
        shell.setSize(800, 600);
    	UITool.placeCentered(shell, 800, 600);
        
    	tree = new Tree(shell, SWT.BORDER | SWT.CHECK);
    	treeFolder = tree;
    	fd = new FormData();
    	fd.top = new FormAttachment(0, 10);
    	fd.bottom = new FormAttachment(100, -50);
    	fd.left = new FormAttachment(0, 10);
    	fd.right = new FormAttachment(0, 300);
    	tree.setLayoutData(fd);
    	
    	tree.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				fillList();
			}
    	});
    	
        table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
        tableFile = table;
        fd = new FormData();
        fd.top = new FormAttachment(0, 10);
        fd.left = new FormAttachment(0, 310);
        fd.bottom = new FormAttachment(100, -50);
        fd.right = new FormAttachment(100, -10);
        table.setLayoutData(fd);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        column = new TableColumn(table, SWT.NONE);
        column.setWidth(30);
        column = new TableColumn(table, SWT.NONE);
        column.setWidth(400);
        column.setText(Labels.get("OpenEngineDialog.LabelFilename"));
        
        label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    	fd = new FormData();
    	fd.bottom = new FormAttachment(100, -40);
    	fd.left = new FormAttachment(0, 0);
    	fd.right = new FormAttachment(100, 0);
    	label.setLayoutData(fd);

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("OpenEngineDialog.ButtonClose"));
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
        button.setText(Labels.get("OpenEngineDialog.ButtonFileAdd"));
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
				TreeItem[] items = treeFolder.getSelection();
				if (items.length == 0) return;
				TreeItem item = items[0];
				Folder parent = null;
				if (item.getData() != null) {
					parent = (Folder)item.getData();
				}
				AddFileDialog dlg = new AddFileDialog(shell, controller, engine, parent);
				dlg.open();
				if (!dlg.getCanceled()) {
					fillList();
				}
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("OpenEngineDialog.ButtonFileDelete"));
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
				List<FileItem> fitems = new ArrayList<FileItem>();
				for (int i = 0; i < tableFile.getItemCount(); i++) {
					TableItem item = tableFile.getItem(i);
					if (item.getChecked() && item.getData() != null) {
						fitems.add((FileItem)item.getData());
					}
				}
				if (fitems.size() == 0) return;
				for (int i = 0; i < fitems.size(); i++) {
					FileItem fitem = fitems.get(i);
					FileData fdata = controller.newFileData(engine);
					fdata.load(fitem.getData());
					fdata.delete();
					fitem.delete();
					deleteLog(fitem);
				}
				fillList();
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("OpenEngineDialog.ButtonFileEdit"));
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
				FileItem fitem = null;
				for (int i = 0; i < tableFile.getItemCount(); i++) {
					TableItem item = tableFile.getItem(i);
					if (item.getChecked() && item.getData() != null) {
						fitem = (FileItem)item.getData();
						break;
					}
				}
				if (fitem == null) return;
				EditFileDialog dlg = new EditFileDialog(shell, controller, engine, fitem);
				dlg.open();
				if (!dlg.getCanceled()) {
					fillList();
				}
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("OpenEngineDialog.ButtonFolderAdd"));
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
				TreeItem item = getFirstChecked();
				if (item == null) return;
				Folder folder = null;
				if (item.getData() != null) {
					folder = (Folder)item.getData();
				}
				AddFolderDialog dlg = new AddFolderDialog(shell, controller, engine, folder);
				dlg.open();
				if (!dlg.getCanceled()) {
					fillTree();
				}
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("OpenEngineDialog.ButtonFolderDelete"));
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
				List<TreeItem> items = new ArrayList<TreeItem>();
				getTopChecked(items);
				if (items.size() == 0) return;
				for (int i = 0; i < items.size(); i++) {
					TreeItem item = items.get(i);
					if (item.getData() == null) {
						deleteFolder(null);
					} else {
						deleteFolder((Folder)item.getData());
					}
				}
				fillTree();
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("OpenEngineDialog.ButtonFolderEdit"));
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
				TreeItem item = getFirstChecked();
				if (item == null) return;
				Folder folder = null;
				if (item.getData() != null) {
					folder = (Folder)item.getData();
				}
				if (folder == null) return;
				EditFolderDialog dlg = new EditFolderDialog(shell, controller, engine, folder);
				dlg.open();
				if (!dlg.getCanceled()) {
					fillTree();
				}
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("OpenEngineDialog.ButtonImport"));
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
				TreeItem[] items = treeFolder.getSelection();
				if (items.length == 0) return;
				Folder folder = null;
				if (items[0].getData() != null) {
					folder = (Folder)items[0].getData();
				}
				FileDialog dlg = new FileDialog(shell, SWT.OPEN);
				dlg.setFilterExtensions(new String[] { "*.zip" });
				dlg.setFilterNames(new String[] { "Package Files (*.zip)" });
				String filename = dlg.open();
				if (filename == null || filename.length() == 0) return;
				importPackage(folder, filename);
				fillTree();
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("OpenEngineDialog.ButtonExport"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -690);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg) {
				List<TreeItem> items = new ArrayList<TreeItem>();
				getTopChecked(items);
				List<Folder> folders = new ArrayList<Folder>();
				List<FileItem> fitems = new ArrayList<FileItem>();
				for (int i = 0; i < items.size(); i++) {
					TreeItem item = items.get(i);
					if (item.getData() == null) {
						folders.add(null);
					} else {
						folders.add((Folder)item.getData());
					}
				}
				for (int i = 0; i < tableFile.getItemCount(); i++) {
					TableItem item = tableFile.getItem(i);
					if (item.getChecked() && item.getData() != null) {
						fitems.add((FileItem)item.getData());
					}
				}
				if (folders.size() == 0 && fitems.size() == 0) return;
				FileDialog dlg = new FileDialog(shell, SWT.SAVE);
				dlg.setFilterExtensions(new String[] { "*.zip" });
				dlg.setFilterNames(new String[] { "Package Files (*.zip)" });
				String filename = dlg.open();
				if (filename == null || filename.length() == 0) return;
				export(folders, fitems, filename);
			}
        });
        
        fillTree();
    }
    
    private void deleteLog(FileItem fitem) {
    	LogItem pat = controller.newLogItem(engine);
    	List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.PARENT, fitem.getId())), Integer.MAX_VALUE);
    	for (int i = 0; i < results.size(); i++) {
    		LogItem li = controller.newLogItem(engine);
    		li.fromString(results.get(i).toString());
    		LogData ld = controller.newLogData(engine);
    		ld.setId(li.getData());
    		ld.delete();
    		li.delete();
    	}
    }
    
    private void importPackage(Folder parent, String filename) {
    	String dirTemp = controller.getTempDir();
    	String name = new File(filename).getName();
    	int pos = name.lastIndexOf(".");
    	if (pos >= 0) {
    		name = name.substring(0, pos);
    	}
    	String dirRoot = new File(dirTemp, name).getAbsolutePath();
    	new File(dirRoot).mkdirs();
    	try {
    		IOTool.unzipFile(filename, dirRoot);
    		File file = new File(dirRoot);
    		File[] children = file.listFiles();
    		if (children.length > 0) {
        		file = children[0];
        		if (file.isDirectory()) {
        			importFolder(parent, file);
        		}
    		}
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    	try {
    		IOTool.deleteFolder(new File(dirTemp));
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    }
    
    private void importFolder(Folder parent, File folder) {
    	File[] children = folder.listFiles();
    	for (int i = 0; i < children.length; i++) {
    		File child = children[i];
    		if (child.isDirectory()) {
    			Folder fcur = importFolder(parent, child.getName(), child.getAbsolutePath());
    			importFolder(fcur, child);
    		} else if (child.isFile()) {
    			if (!child.getAbsolutePath().endsWith(".orinus")) {
        			importFile(parent, child);
    			}
    		}
    	}
    }

    private Folder importFolder(Folder parent, String name, String path) {
    	String pid = "";
    	if (parent != null) {
    		pid = parent.getId();
    	}
    	int level = 1;
    	if (parent != null) {
    		level = parent.getLevel() + 1;
    	}
    	String t_name = name;
    	int t_no = 0;
    	while (findFolderByName(pid, level, t_name)) {
    		t_no++;
    		t_name = name + t_no;
    	}
    	Folder tag = controller.newFolder(engine);
    	tag.setId(controller.uniqid());
    	tag.setParent(pid);
    	tag.setLevel(level);
    	tag.setName(t_name);

    	Config t_cfg = new Config();
    	if (new File(path + ".orinus").exists()) {
        	t_cfg.load(path + ".orinus");
    		tag.setPublished(t_cfg.getBoolean("published"));
    	} else {
    		tag.setPublished(true);
    	}
    	
    	tag.save();
    	return tag;
    }
    
    private void importFile(Folder parent, File file) {
    	String ext = "";
    	String name = file.getName();
    	int pos = name.lastIndexOf(".");
    	if (pos >= 0) {
    		ext = name.substring(pos);
    		name = name.substring(0, pos);
    	}
    	String pid = "";
    	if (parent != null) {
    		pid = parent.getId();
    	}
    	String t_name = name + ext;
    	int t_no = 0;
    	while (findFileByName(pid, t_name)) {
    		t_no++;
    		t_name = name + t_no + ext;
    	}
    	
    	byte[] data = new byte[0];
    	try {
    		InputStream is = new FileInputStream(file);
    		data = new byte[is.available()];
    		is.read(data);
    		is.close();
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    	
    	FileData fdata = controller.newFileData(engine);
    	fdata.setId(controller.uniqid());
    	fdata.setData(data);
    	fdata.save();
    	
    	FileItem fitem = controller.newFileItem(engine);
    	fitem.setId(controller.uniqid());
    	fitem.setName(t_name);
    	fitem.setData(fdata.getId());
    	fitem.setParent(pid);
    	
    	Config t_cfg = new Config();
    	if (new File(file.getAbsolutePath() + ".orinus").exists()) {
        	t_cfg.load(file.getAbsolutePath() + ".orinus");
    		fitem.setPublished(t_cfg.getBoolean("published"));
    	} else {
    		fitem.setPublished(true);
    	}
    	fitem.setScheduled(t_cfg.getBoolean("scheduled"));
    	fitem.setMinute(t_cfg.getString("minute"));
    	fitem.setHour(t_cfg.getString("hour"));
    	fitem.setDay(t_cfg.getString("day"));
    	fitem.setMonth(t_cfg.getString("month"));
    	fitem.setYear(t_cfg.getString("year"));
    	fitem.setTimeout(t_cfg.getInt("timeout"));
    	
    	fitem.save();
    }
    
    private boolean findFileByName(String pid, String name) {
    	FileItem pat = controller.newFileItem(engine);
    	BooleanQuery bq = pat.newBooleanQuery();
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.NAME, name)), pat.occurMust()));
    	int count = pat.count(pat.getKind(), bq, 1);
    	return count > 0;
    }
    
    private boolean findFolderByName(String pid, int level, String name) {
    	Folder pat = controller.newFolder(engine);
    	BooleanQuery bq = pat.newBooleanQuery();
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.NAME, name)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newIntegerRangeQuery(pat.LEVEL, level, level, true, true), pat.occurMust()));
    	int count = pat.count(pat.getKind(), bq, 1);
    	return count > 0;
    }
    
    private void export(List<Folder> folders, List<FileItem> fitems, String filename) {
    	String dirTemp = controller.getTempDir();
    	String name = new File(filename).getName();
    	int pos = name.lastIndexOf(".");
    	if (pos >= 0) {
    		name = name.substring(0, pos);
    	}
    	String dirRoot = new File(dirTemp, name).getAbsolutePath();
    	new File(dirRoot).mkdirs();
    	for (int i = 0; i < fitems.size(); i++) {
    		exportFile(fitems.get(i), dirRoot);
    	}
    	for (int i = 0; i < folders.size(); i++) {
    		exportFolder(folders.get(i), dirRoot);
    	}
    	try {
    		IOTool.zipFolder(dirRoot, filename);
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    	try {
    		IOTool.deleteFolder(new File(dirTemp));
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    }

    private void exportFolder(Folder folder, String container) {
    	String t_root = container;
    	if (folder != null) {
        	String name = folder.getName();
        	Config t_cfg = new Config();
        	t_cfg.setValue("published", folder.getPublished());
        	t_cfg.save(new File(t_root, name + ".orinus").getAbsolutePath());
        	int no = 0;
        	String fname = new File(t_root, name).getAbsolutePath();
        	while (new File(fname).exists()) {
        		no++;
        		fname = new File(t_root, name + no).getAbsolutePath();
        	}
        	new File(fname).mkdirs();
    		t_root = fname;
    	}
    	List<FileItem> fitems = findFiles(folder);
    	for (int i = 0; i < fitems.size(); i++) {
    		exportFile(fitems.get(i), t_root);
    	}
    	List<Folder> folders = findChildFolder(folder);
    	for (int i = 0; i < folders.size(); i++) {
    		exportFolder(folders.get(i), t_root);
    	}
    }
    
    private List<Folder> findChildFolder(Folder parent) {
    	String pid = "";
    	if (parent != null) {
    		pid = parent.getId();
    	}
    	int level = 1;
    	if (parent != null) {
    		level = parent.getLevel() + 1;
    	}
    	List<Folder> tag = new ArrayList<Folder>();
    	Folder pat = controller.newFolder(engine);
    	BooleanQuery bq = pat.newBooleanQuery();
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newIntegerRangeQuery(pat.LEVEL, level, level, true, true), pat.occurMust()));
    	List<SEntity> results = pat.search(pat.getKind(), bq, Integer.MAX_VALUE);
    	for (int i = 0; i < results.size(); i++) {
    		Folder folder = controller.newFolder(engine);
    		folder.fromString(results.get(i).toString());
    		tag.add(folder);
    	}
    	return tag;
    }
    
    private List<FileItem> findFiles(Folder parent) {
    	String pid = "";
    	if (parent != null) {
    		pid = parent.getId();
    	}
    	List<FileItem> tag = new ArrayList<FileItem>();
    	FileItem pat = controller.newFileItem(engine);
    	List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), Integer.MAX_VALUE);
    	for (int i = 0; i < results.size(); i++) {
    		FileItem fitem = controller.newFileItem(engine);
    		fitem.fromString(results.get(i).toString());
    		tag.add(fitem);
    	}
    	return tag;
    }
    
    private void exportFile(FileItem fitem, String folder) {
    	try {
    		Config t_cfg = new Config();
    		t_cfg.setValue("published", fitem.getPublished());
    		t_cfg.setValue("scheduled", fitem.getScheduled());
    		t_cfg.setValue("minute", fitem.getMinute());
    		t_cfg.setValue("hour", fitem.getHour());
    		t_cfg.setValue("day", fitem.getDay());
    		t_cfg.setValue("month", fitem.getMonth());
    		t_cfg.setValue("year", fitem.getYear());
    		t_cfg.setValue("timeout", fitem.getTimeout());
    		t_cfg.save(new File(folder, fitem.getName() + ".orinus").getAbsolutePath());
    		String filename = new File(folder, fitem.getName()).getAbsolutePath();
    		FileData fdata = controller.newFileData(engine);
    		fdata.load(fitem.getData());
    		OutputStream os = new FileOutputStream(filename);
    		os.write(fdata.getData());
    		os.close();
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    }
	
    private void deleteFolder(Folder folder) {
    	deleteFiles(folder);
    	deleteChildFolder(folder);
    	if (folder == null) return;
    	folder.delete();
    }
    
    private void deleteFiles(Folder parent) {
    	String pid = "";
    	if (parent != null) {
    		pid = parent.getId();
    	}
    	FileItem pat = controller.newFileItem(engine);
    	List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), Integer.MAX_VALUE);
    	for (int i = 0; i < results.size(); i++) {
    		FileItem fitem = controller.newFileItem(engine);
    		fitem.fromString(results.get(i).toString());
    		FileData fdata = controller.newFileData(engine);
    		fdata.load(fitem.getData());
    		fdata.delete();
    		fitem.delete();
    		deleteLog(fitem);
    	}
    }
    
    private void deleteChildFolder(Folder parent) {
    	String pid = "";
    	if (parent != null) {
    		pid = parent.getId();
    	}
    	int level = 1;
    	if (parent != null) {
    		level = parent.getLevel() + 1;
    	}
    	Folder pat = controller.newFolder(engine);
    	BooleanQuery bq = pat.newBooleanQuery();
    	bq.add(pat.newBooleanClause(pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), pat.occurMust()));
    	bq.add(pat.newBooleanClause(pat.newIntegerRangeQuery(pat.LEVEL, level, level, true, true), pat.occurMust()));
    	List<SEntity> results = pat.search(pat.getKind(), bq, Integer.MAX_VALUE);
    	for (int i = 0; i < results.size(); i++) {
    		Folder folder = controller.newFolder(engine);
    		folder.fromString(results.get(i).toString());
    		deleteFolder(folder);
    	}
    }
    
    private void fillList() {
    	tableFile.removeAll();
    	TreeItem[] treeItems = treeFolder.getSelection();
    	if (treeItems.length == 0) return;
    	TreeItem treeItem = treeItems[0];
    	Folder parent = null;
    	if (treeItem.getData() != null) {
    		parent = (Folder)treeItem.getData();
    	}
    	String pid = "";
    	if (parent != null) {
    		pid = parent.getId();
    	}
    	FileItem pat = controller.newFileItem(engine);
    	List<SEntity> results = pat.search(pat.getKind(), pat.newTermQuery(pat.newTerm(pat.PARENT, pid)), pat.newSort(pat.newSortField(pat.NAME, pat.sortFieldString(), false)) , Integer.MAX_VALUE);
    	for (int i = 0; i < results.size(); i++) {
    		FileItem fitem = controller.newFileItem(engine);
    		fitem.fromString(results.get(i).toString());
    		TableItem item = new TableItem(tableFile, SWT.NONE);
    		item.setData(fitem);
    		item.setText(1, fitem.getName());
    	}
    }
    
    private void fillTree() {
    	treeFolder.removeAll();
    	TreeItem item = new TreeItem(treeFolder, SWT.NONE);
    	item.setText(engineTitle);
    	Map<String, TreeItem> mapItem = new HashMap<String, TreeItem>();
    	mapItem.put("", item);
    	Folder pat = controller.newFolder(engine);
    	List<SEntity> results = pat.search(pat.getKind(), pat.newMatchAllDocsQuery(), pat.newSort(pat.newSortField(pat.LEVEL, pat.sortFieldInteger(), false), pat.newSortField(pat.PARENT, pat.sortFieldString(), false), pat.newSortField(pat.NAME, pat.sortFieldString(), false)), Integer.MAX_VALUE);
    	for (int i = 0; i < results.size(); i++) {
    		Folder folder = controller.newFolder(engine);
    		folder.fromString(results.get(i).toString());
    		TreeItem pitem = mapItem.get(folder.getParent());
    		item = new TreeItem(pitem, SWT.NONE);
    		item.setText(folder.getName());
    		item.setData(folder);
    		mapItem.put(folder.getId(), item);
    	}
    	item = mapItem.get("");
    	item.setExpanded(true);
    	fillList();
    }
 
    private void getTopChecked(List<TreeItem> tag) {
    	TreeItem[] items = treeFolder.getItems();
    	for (int i = 0; i < items.length; i++) {
    		TreeItem item = items[i];
    		if (item.getChecked()) {
    			tag.add(item);
    		} else {
    			getTopChecked(tag, item);
    		}
    	}
    }

    private void getTopChecked(List<TreeItem> tag, TreeItem parent) {
    	TreeItem[] items = parent.getItems();
    	for (int i = 0; i < items.length; i++) {
    		TreeItem item = items[i];
    		if (item.getChecked()) {
    			tag.add(item);
    		} else {
    			getTopChecked(tag, item);
    		}
    	}
    }
    
    private TreeItem getFirstChecked() {
    	TreeItem tag = null;
    	TreeItem[] items = treeFolder.getItems();
    	for (int i = 0; i < items.length; i++) {
    		TreeItem item = items[i];
    		if (item.getChecked()) {
    			tag = item;
    			break;
    		}
    	}
    	if (tag == null) {
        	for (int i = 0; i < items.length; i++) {
        		TreeItem item = items[i];
        		item = getFirstChecked(item);
        		if (item != null) {
        			tag = item;
        			break;
        		}
        	}
    	}
    	return tag;
    }
    
    private TreeItem getFirstChecked(TreeItem parent) {
    	TreeItem tag = null;
    	TreeItem[] items = parent.getItems();
    	for (int i = 0; i < items.length; i++) {
    		TreeItem item = items[i];
    		if (item.getChecked()) {
    			tag = item;
    			break;
    		}
    	}
    	if (tag == null) {
        	for (int i = 0; i < items.length; i++) {
        		TreeItem item = items[i];
        		item = getFirstChecked(item);
        		if (item != null) {
        			tag = item;
        			break;
        		}
        	}
    	}
    	return tag;
    }
    
}
