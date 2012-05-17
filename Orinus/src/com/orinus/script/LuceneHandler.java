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

package com.orinus.script;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.orinus.script.safe.lucene.SEntity;

public class LuceneHandler extends SEntity.Handler {

    public static final String KIND_QUOTA = "C4f91ee1eb414a";
    public static final String QUOTA_SYSTEM = "F4f91ee659b1ec";
 
    protected String dirIndex = "";
    protected String dirBackup = "";
    protected double systemQuota = 0;

    public LuceneHandler(String dirIndex, String dirBackup, double systemQuota) {
        this.dirIndex = dirIndex;
        this.dirBackup = dirBackup;
        this.systemQuota = systemQuota;
    }
 
    public boolean exists(String id) {
        boolean tag = false;
        if (id.length() == 0) return tag;
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(dirIndex)));
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs td = searcher.search(new TermQuery(new Term(SEntity.ID, id)), 1);
            if (td.totalHits > 0) {
                tag = true;
            }
            searcher.close();
            reader.close();
        } catch (Exception e) {
        }
  
        return tag; 
    }
 
    public void create(SEntity src) {
        Monitor monitor = new Monitor();
        Timer timer = new Timer();
        timer.schedule(new CreateTask(timer, src, monitor), 1);
        while (!monitor.finished) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        timer = null;
    }

    protected boolean quotaCreate(SEntity src) {
        boolean tag = false;
        SEntity quota = findSystemQuota();
        if (quota == null) {
            quota = newSystemQuota();
        }
        double newSize = quota.getDouble("size") + ((double)src.toString().length() / 1048576.0);
        if (newSize < 0) newSize = 0;
        if (newSize < systemQuota) {
            tag = true;
            quota.setDouble("size", newSize);
            quota.save();
        }
        return tag;
    }

    protected boolean quotaUpdate(SEntity src) {
        boolean tag = false;
        SEntity quota = findSystemQuota();
        if (quota == null) {
            quota = newSystemQuota();
        }
        double newSize = quota.getDouble("size") - ((double)getFileSize(src.getId(), src.getKind()) / 1048576.0) + ((double)src.toString().length() / 1048576.0);
        if (newSize < 0) newSize = 0;
        if (newSize < systemQuota) {
            tag = true;
            quota.setDouble("size", newSize);
            quota.save();
        }
        return tag;
    }

    protected boolean quotaDelete(String id, String kind) {
        boolean tag = false;
        SEntity quota = findSystemQuota();
        if (quota == null) {
            quota = newSystemQuota();
        }
        double newSize = quota.getDouble("size") - ((double)getFileSize(id, kind) / 1048576.0);
        if (newSize < 0) newSize = 0;
        if (newSize < systemQuota) {
            tag = true;
            quota.setDouble("size", newSize);
            quota.save();
        }
        return tag;
    }
 
    protected long getFileSize(String id, String kind) {
        long tag = 0;
        String fid = "";
        for (int i = 0; i < id.length() && i + 1 < id.length(); i += 2) {
            if (fid.length() > 0) fid += File.separator;
            fid += id.substring(i, i + 2);
        }
        File file = new File(dirBackup, kind);
        file = new File(file.getAbsolutePath(), fid);
        String folder = file.getAbsolutePath();
        file = new File(folder, id + ".txt");
        if (file.exists()) {
            tag = file.length();
        }
        return tag;
    }
 
    protected SEntity newSystemQuota() {
        SEntity tag = new SEntity(this);
        tag.setSchema("s|kind|d|size");
        tag.setKind(KIND_QUOTA);
        tag.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        tag.setString("kind", QUOTA_SYSTEM);
        return tag;
    }
 
    protected SEntity findSystemQuota() {
        List<SEntity> results = search(KIND_QUOTA, new TermQuery(new Term("kind", QUOTA_SYSTEM)), 1);
        if (results.size() == 0) return null;
        return results.get(0);
    }

    protected void createEntity(SEntity src) { 
        if (src.getId().length() == 0) return;
        if (src.getKind().length() == 0) return;

        try {
            if (!src.getKind().equals(KIND_QUOTA)) {
                if (!quotaCreate(src)) return;
            }
            backup(src);
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(FSDirectory.open(new File(dirIndex)), iwc);
            Document doc = new Document();
            write(src, doc);
            writer.addDocument(doc);
            writer.close();
        } catch (Exception e) {
        }
    }
 
    public void update(SEntity src) {
        Monitor monitor = new Monitor();
        Timer timer = new Timer();
        timer.schedule(new UpdateTask(timer, src, monitor), 1);
        while (!monitor.finished) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        timer = null;
    }

    protected void updateEntity(SEntity src) { 
        if (src.getId().length() == 0) return;
        if (src.getKind().length() == 0) return;

        try {
            if (!src.getKind().equals(KIND_QUOTA)) {
                if (!quotaUpdate(src)) return;
            }
            backup(src);
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(FSDirectory.open(new File(dirIndex)), iwc);
            Document doc = new Document();
            write(src, doc);
            writer.updateDocument(new Term(SEntity.ID, src.getId()), doc);
            writer.close();
        } catch (Exception e) {
        }
    }
 
    public void load(String id, SEntity src) {
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(dirIndex)));
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs td = searcher.search(new TermQuery(new Term(SEntity.ID, id)), 1);
            if (td.totalHits > 0) {
                Document doc = searcher.doc(td.scoreDocs[0].doc);
                if (allowLoad(id, doc.get(SEntity.KIND))) {
                    src.setSchema(doc.get(SEntity.SCHEMA));
                    read(src, doc);
                }
            }
            searcher.close();
            reader.close();
        } catch (Exception e) {
        }
    }
 
    protected boolean allowLoad(String id, String kind) {
        return true;
    }
 
    public int count(String kind, Query query, Filter filter, Sort sort, int max) {
        int tag = 0;
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(dirIndex)));
            IndexSearcher searcher = new IndexSearcher(reader);
            BooleanQuery boolQuery = new BooleanQuery();
            boolQuery.add(new BooleanClause(new TermQuery(new Term(SEntity.KIND, kind)), Occur.MUST));
            if (query != null) {
                boolQuery.add(new BooleanClause(query, Occur.MUST));
            }
            TopDocs td = null;
            if (filter != null && sort != null) {
                td = searcher.search(boolQuery, filter, max, sort);
            } else if (filter != null) {
                td = searcher.search(boolQuery, filter, max);
            } else if (sort != null) {
                td = searcher.search(boolQuery, max, sort);
            } else {
                td = searcher.search(boolQuery, max);
            }
            tag = td.totalHits;
            searcher.close();
            reader.close();
        } catch (Exception e) {
        }
        return tag;
    }

    public int count(String kind, Query query, int max) {
        return count(kind, query, null, null, max);
    }

    public int count(String kind, Query query, Sort sort, int max) {
        return count(kind, query, null, sort, max);
    }
 
    public int count(String kind, Query query, Filter filter, int max) {
        return count(kind, query, filter, null, max);
    }
 
    public List<SEntity> search(String kind, Query query, int max) {
        return search(kind, query, null, null, max);
    }

    public List<SEntity> search(String kind, Query query, Sort sort, int max) {
        return search(kind, query, null, sort, max);
    }
 
    public List<SEntity> search(String kind, Query query, Filter filter, int max) {
        return search(kind, query, filter, null, max);
    }
 
    public List<SEntity> search(String kind, Query query, int pagesize, int pageno) { 
        return search(kind, query, null, null, pagesize, pageno);
    }
 
    public List<SEntity> search(String kind, Query query, Sort sort, int pagesize, int pageno) { 
        return search(kind, query, null, sort, pagesize, pageno);
    }
 
    public List<SEntity> search(String kind, Query query, Filter filter, int pagesize, int pageno) {
        return search(kind, query, filter, null, pagesize, pageno);
    }
 
    public List<SEntity> search(String kind, Query query, Filter filter, Sort sort, int max) {
        List<SEntity> tag = new ArrayList<SEntity>();
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(dirIndex)));
            IndexSearcher searcher = new IndexSearcher(reader);
            BooleanQuery boolQuery = new BooleanQuery();
            boolQuery.add(new BooleanClause(new TermQuery(new Term(SEntity.KIND, kind)), Occur.MUST));
            if (query != null) {
                boolQuery.add(new BooleanClause(query, Occur.MUST));
            }
            TopDocs td = null;
            if (filter != null && sort != null) {
                td = searcher.search(boolQuery, filter, max, sort);
            } else if (filter != null) {
                td = searcher.search(boolQuery, filter, max);
            } else if (sort != null) {
                td = searcher.search(boolQuery, max, sort);
            } else {
                td = searcher.search(boolQuery, max);
            }
            for (int i = 0; i < td.totalHits; i++) {
                SEntity item = new SEntity(this);
                Document doc = searcher.doc(td.scoreDocs[i].doc);
                item.setSchema(doc.get(SEntity.SCHEMA));
                read(item, doc);
                tag.add(item);
            }
            searcher.close();
            reader.close();
        } catch (Exception e) {
        }
        return tag;
    }

    public List<SEntity> search(String kind, Query query, Filter filter, Sort sort, int pagesize, int pageno) {
        List<SEntity> tag = new ArrayList<SEntity>();
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(dirIndex)));
            IndexSearcher searcher = new IndexSearcher(reader);
            BooleanQuery boolQuery = new BooleanQuery();
            boolQuery.add(new BooleanClause(new TermQuery(new Term(SEntity.KIND, kind)), Occur.MUST));
            if (query != null) {
                boolQuery.add(new BooleanClause(query, Occur.MUST));
            }
            if (pagesize <= 0) pagesize = 10;
            if (pageno <= 0) pageno = 1;
            int max = pageno * pagesize;
            TopDocs td = null;
            if (filter != null && sort != null) {
                td = searcher.search(boolQuery, filter, max, sort);
            } else if (filter != null) {
                td = searcher.search(boolQuery, filter, max);
            } else if (sort != null) {
                td = searcher.search(boolQuery, max, sort);
            } else {
                td = searcher.search(boolQuery, max);
            }
            for (int i = (pageno - 1) * pagesize; i < td.totalHits && i < max; i++) {
                SEntity item = new SEntity(this);
                Document doc = searcher.doc(td.scoreDocs[i].doc);
                item.setSchema(doc.get(SEntity.SCHEMA));
                read(item, doc);
                tag.add(item);
            }
            searcher.close();
            reader.close();
        } catch (Exception e) {
        }
        return tag;
    }
 
    protected void backup(SEntity src) {
        String id = src.getId();
        if (id.length() == 0) return;
        String kind = src.getKind();
        if (kind.length() == 0) return;
        String fid = "";
        for (int i = 0; i < id.length() && i + 1 < id.length(); i += 2) {
            if (fid.length() > 0) fid += File.separator;
            fid += id.substring(i, i + 2);
        }
        try {
            File file = new File(dirBackup, kind);
            file = new File(file.getAbsolutePath(), fid);
            file.mkdirs();
            String folder = file.getAbsolutePath();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(folder, id + ".txt"))));
            writer.write(src.toString());
            writer.close();
        } catch (Exception e) {
        }
    }
 
    protected void read(SEntity entity, Document doc) {
        String schema = doc.get(SEntity.SCHEMA);
        if (schema == null) schema = "";
        String[] fields = schema.split("\\|");
        for (int i = 0; i < fields.length && i + 1 < fields.length; i+= 2) {
            String kind = fields[i];
            String fname = fields[i + 1];
            String val = doc.get(fname);
            if (val == null) val = "";
            if (SEntity.ALL_KINDS.indexOf("|" + kind + "|") < 0) continue;
            entity.setString(fname, val);
        }
    }
 
    protected void write(SEntity entity, Document doc) {
        String schema = entity.getSchema();
        if (schema == null) schema = "";
        String[] fields = schema.split("\\|");
        for (int i = 0; i < fields.length && i + 1 < fields.length; i+= 2) {
            String kind = fields[i];
            String fname = fields[i + 1];
            if (SEntity.STRING.equalsIgnoreCase(kind)) {
                Field field = new Field(fname, entity.getString(fname), Store.YES, Index.NOT_ANALYZED_NO_NORMS);
                doc.add(field);
            } else if (SEntity.DOUBLE.equalsIgnoreCase(kind)) {
                NumericField field = new NumericField(fname, Store.YES, true);
                field.setDoubleValue(entity.getDouble(fname));
                doc.add(field);
            } else if (SEntity.FLOAT.equalsIgnoreCase(kind)) {
                NumericField field = new NumericField(fname, Store.YES, true);
                field.setFloatValue(entity.getFloat(fname));
                doc.add(field);
            } else if (SEntity.INTEGER.equalsIgnoreCase(kind)) {
                NumericField field = new NumericField(fname, Store.YES, true);
                field.setIntValue(entity.getInteger(fname));
                doc.add(field);
            } else if (SEntity.LONG.equalsIgnoreCase(kind)) {
                NumericField field = new NumericField(fname, Store.YES, true);
                field.setLongValue(entity.getLong(fname));
                doc.add(field);
            } else if (SEntity.ANALYZED.equalsIgnoreCase(kind)) {
                Field field = new Field(fname, entity.getString(fname), Store.YES, Index.ANALYZED);
                doc.add(field);
            }
        }
    }
 
    public void delete(String id) {
        Monitor monitor = new Monitor();
        Timer timer = new Timer();
        timer.schedule(new DeleteTask(timer, id, monitor), 1);
        while (!monitor.finished) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        timer = null;
    }
 
    protected void deleteEntity(String id) { 
        if (id.length() == 0) return;
        String kind = "";
        
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(dirIndex)));
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs td = searcher.search(new TermQuery(new Term(SEntity.ID, id)), 1);
            if (td.totalHits > 0) {
                Document doc = searcher.doc(td.scoreDocs[0].doc);
                kind = doc.get(SEntity.KIND);
            }
            searcher.close();
            reader.close();
        } catch (Exception e) {
        }
        if (kind.length() == 0) return;
        if (!allowDelete(id, kind)) return;
        
        try {
            if (!kind.equals(KIND_QUOTA)) {
                if (!quotaDelete(id, kind)) return;
            }
            removeBackup(id, kind);
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_36, analyzer);
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(FSDirectory.open(new File(dirIndex)), iwc);
            writer.deleteDocuments(new Term(SEntity.ID, id));
            writer.close();
        } catch (Exception e) {
        }
    }
 
    protected boolean allowDelete(String id, String kind) {
        return true;
    }
 
    protected void removeBackup(String id, String kind) {
        if (id.length() == 0) return;
        if (kind.length() == 0) return;
        String fid = "";
        for (int i = 0; i < id.length() && i + 1 < id.length(); i += 2) {
            if (fid.length() > 0) fid += File.separator;
            fid += id.substring(i, i + 2);
        }
        try {
            File file = new File(dirBackup, kind);
            file = new File(file.getAbsolutePath(), fid);
            String folder = file.getAbsolutePath();
            file = new File(folder, id + ".txt");
            file.delete();
        } catch (Exception e) {
        }
    }

    public double storageQuota() {
        return systemQuota;
    }
 
    public double storageSize() {
        SEntity tag = findSystemQuota();
        if (tag == null) return 0;
        return tag.getDouble("size");
    }

    private class DeleteTask extends TimerTask {

        private String id;
        private Timer timer;
        private Monitor monitor;
  
        public DeleteTask(Timer timer, String id, Monitor monitor) {
            this.timer = timer;
            this.id = id;
            this.monitor = monitor;
        }
  
        @Override
        public void run() {
            deleteEntity(id);
            monitor.finished = true;
            timer.cancel();
            timer.purge();
            timer = null;
        }
  
    }

    private class CreateTask extends TimerTask {

        private SEntity entity;
        private Timer timer;
        private Monitor monitor;
  
        public CreateTask(Timer timer, SEntity entity, Monitor monitor) {
            this.timer = timer;
            this.entity = entity;
            this.monitor = monitor;
        }
  
        @Override
        public void run() {
            createEntity(entity);
            monitor.finished = true;
            timer.cancel();
            timer.purge();
            timer = null;
        }
  
    }

    private class UpdateTask extends TimerTask {

        private SEntity entity;
        private Timer timer;
        private Monitor monitor;
  
        public UpdateTask(Timer timer, SEntity entity, Monitor monitor) {
            this.timer = timer;
            this.entity = entity;
            this.monitor = monitor;
        }
  
        @Override
        public void run() {
            updateEntity(entity);
            monitor.finished = true;
            timer.cancel();
            timer.purge();
            timer = null;
        }
  
    }
 
    private class Monitor {
        public boolean finished = false;
    }
 
}