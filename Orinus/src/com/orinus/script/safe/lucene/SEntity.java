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

package com.orinus.script.safe.lucene;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldValueFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.NGramPhraseQuery;
import org.apache.lucene.search.NumericRangeFilter;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixFilter;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeFilter;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.util.Version;

public class SEntity {

    public static final String STRING = "s";
    public static final String DOUBLE = "d";
    public static final String FLOAT = "f";
    public static final String INTEGER = "i";
    public static final String LONG = "l";
    public static final String ANALYZED = "a";
 
    public static final String ALL_KINDS = "|s|d|f|i|l|a|";
 
    public static final String SCHEMA = "F4f8cc93237f50";
    public static final String ID = "F4f8cce61643dd";
    public static final String CREATED = "F4f8cd83fcca31";
    public static final String UPDATED = "F4f8cd84e2b74a";
    public static final String KIND = "F4f8cd9c8ee13d";
    public static final String MARK = "F4f8cda27d62fb";

    protected Properties data = new Properties();
    protected Properties schema = new Properties();
    protected Handler handler = null;
 
    public SEntity(Handler handler) {
        this.handler = handler;
        registerDefault();
    }
 
    public void register(String field, String type) {
        if (ALL_KINDS.indexOf("|" + type + "|") < 0) return;
        schema.put(field, type);
        saveSchema();
    }
 
    public void setSchema(String src) {
        String[] fields = src.split("\\|");
        schema.clear();
        for (int i = 0; i < fields.length && i + 1 < fields.length; i+= 2) {
            register(fields[i + 1], fields[i]);
        }
        registerDefault();
        saveSchema();
    }
 
    public String getSchema() {
        String tag = data.getProperty(SCHEMA);
        if (tag == null) tag = "";
        return tag;
    }
 
    public void fromString(String src) {
        data.clear();
        schema.clear();
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(src.getBytes("UTF-8"));
            data.load(bais);
            bais.close();
        } catch (Exception e) {
        }
        loadSchema();
    }
 
    public String toString() {
        String tag = "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            data.store(baos, "");
            tag = baos.toString();
            baos.close();
        } catch (Exception e) {
        }
        return tag;
    }
 
    public String getString(String field) {
        String tag = data.getProperty(field);
        if (tag == null) tag = "";
        return tag;
    }
 
    public void setString(String field, String value) {
        if (schema.containsKey(field)) {
            if (value == null) value = "";
            data.setProperty(field, value);
        }
    }
 
    public double getDouble(String field) {
        double tag = 0;
        try {
            tag = Double.parseDouble(getString(field));
        } catch (Exception e) {
            tag = 0;
        }
        return tag;
    }
 
    public void setDouble(String field, double value) {
        setString(field, Double.toString(value));
    }

    public float getFloat(String field) {
        float tag = 0;
        try {
            tag = Float.parseFloat(getString(field));
        } catch (Exception e) {
            tag = 0;
        }
        return tag;
    }
 
    public void setFloat(String field, float value) {
        setString(field, Float.toString(value));
    }

    public long getLong(String field) {
        long tag = 0;
        try {
            tag = Long.parseLong(getString(field));
        } catch (Exception e) {
            tag = 0;
        }
        return tag;
    }
 
    public void setLong(String field, long value) {
        setString(field, Long.toString(value));
    }

    public int getInteger(String field) {
        int tag = 0;
        try {
            tag = Integer.parseInt(getString(field));
        } catch (Exception e) {
            tag = 0;
        }
        return tag;
    }
 
    public void setInteger(String field, int value) {
        setString(field, Integer.toString(value));
    }
 
    public String getId() {
        return getString(ID);
    }
 
    public void setId(String src) {
        setString(ID, src);
    }

    public String getKind() {
        return getString(KIND);
    }
 
    public void setKind(String src) {
        setString(KIND, src);
    }
 
    public String getMark() {
        return getString(MARK);
    }
 
    public void setMark(String src) {
        setString(MARK, src);
    }
 
    public Date getCreated() {
        return new Date(getLong(CREATED));
    }
 
    public Date getUpdated() {
        return new Date(getLong(UPDATED));
    }
 
    public boolean exists() {
        if (handler == null) {
            return false;
        } else {
            return handler.exists(getId());
        }
    }
 
    public void save() {
        if (handler != null) {
            long now = new Date().getTime();
            if (handler.exists(getId())) {
                setLong(UPDATED, now);
                handler.update(this);
            } else {
                setLong(CREATED, now);
                setLong(UPDATED, now);
                handler.create(this);
            }
        }
    }

    public int count(String kind, Query query, int max) {
        if (handler != null) {
            return handler.count(kind, query, max);
        }
        return 0; 
    }
 
    public int count(String kind, Query query, Sort sort, int max) {
        if (handler != null) {
            return handler.count(kind, query, sort, max);
        }
        return 0; 
    }
 
    public int count(String kind, Query query, Filter filter, int max) {
        if (handler != null) {
            return handler.count(kind, query, filter, max);
        }
        return 0; 
    }
 
    public int count(String kind, Query query, Filter filter, Sort sort, int max) {
        if (handler != null) {
            return handler.count(kind, query, filter, sort, max);
        }
        return 0; 
    }
 
    public List<SEntity> search(String kind, Query query, int max) {
        if (handler != null) {
            return handler.search(kind, query, max);
        }
        return new ArrayList<SEntity>(); 
    }
 
    public List<SEntity> search(String kind, Query query, Sort sort, int max) {
        if (handler != null) {
            return handler.search(kind, query, sort, max);
        }
        return new ArrayList<SEntity>(); 
    }
 
    public List<SEntity> search(String kind, Query query, Filter filter, int max) {
        if (handler != null) {
            return handler.search(kind, query, filter, max);
        }
        return new ArrayList<SEntity>(); 
    }
 
    public List<SEntity> search(String kind, Query query, Filter filter, Sort sort, int max) {
        if (handler != null) {
            return handler.search(kind, query, filter, sort, max);
        }
        return new ArrayList<SEntity>(); 
    }
 
    public List<SEntity> search(String kind, Query query, int pagesize, int pageno) {
        if (handler != null) {
            return handler.search(kind, query, pagesize, pageno);
        }
        return new ArrayList<SEntity>(); 
    }
 
    public List<SEntity> search(String kind, Query query, Sort sort, int pagesize, int pageno) {
        if (handler != null) {
            return handler.search(kind, query, sort, pagesize, pageno);
        }
        return new ArrayList<SEntity>(); 
    }
 
    public List<SEntity> search(String kind, Query query, Filter filter, int pagesize, int pageno) {
        if (handler != null) {
            return handler.search(kind, query, filter, pagesize, pageno);
        }
        return new ArrayList<SEntity>(); 
    }
 
    public List<SEntity> search(String kind, Query query, Filter filter, Sort sort, int max, int pagesize, int pageno) {
        if (handler != null) {
            return handler.search(kind, query, filter, sort, pagesize, pageno);
        }
        return new ArrayList<SEntity>(); 
    }
 
    public void load(String id) {
        if (handler != null) {
            handler.load(id, this);
        }
    }
 
    public BooleanQuery newBooleanQuery() {
        return new BooleanQuery();
    }
 
    public BooleanClause newBooleanClause(Query query, Occur occur) {
        return new BooleanClause(query, occur);
    }
 
    public Occur occurMust() {
        return Occur.MUST;
    }
 
    public Occur occurMustNot() {
        return Occur.MUST_NOT;
    }
 
    public Occur occurShould() {
        return Occur.SHOULD;
    }

    public MatchAllDocsQuery newMatchAllDocsQuery() {
        return new MatchAllDocsQuery();
    }
 
    public MultiPhraseQuery newMultiPhraseQuery() {
        return new MultiPhraseQuery();
    }
 
    public PhraseQuery newPhraseQuery() {
        return new PhraseQuery();
    }
 
    public NGramPhraseQuery newNGramPhraseQuery(int n) {
        return new NGramPhraseQuery(n);
    }
 
    public Term newTerm(String field, String value) {
        return new Term(field, value);
    }
 
    public NumericRangeQuery<Double> newDoubleRangeQuery(String field, Double min, Double max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeQuery.newDoubleRange(field, min, max, minInclusive, maxInclusive);
    }
 
    public NumericRangeQuery<Double> newDoubleRangeQuery(String field, int precisionStep, Double min, Double max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeQuery.newDoubleRange(field, precisionStep, min, max, minInclusive, maxInclusive);
    }

    public NumericRangeQuery<Float> newFloatRangeQuery(String field, Float min, Float max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeQuery.newFloatRange(field, min, max, minInclusive, maxInclusive);
    }

    public NumericRangeQuery<Float> newFloatRangeQuery(String field, int precisionStep, Float min, Float max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeQuery.newFloatRange(field, precisionStep, min, max, minInclusive, maxInclusive);
    }

    public NumericRangeQuery<Integer> newIntegerRangeQuery(String field, Integer min, Integer max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeQuery.newIntRange(field, min, max, minInclusive, maxInclusive);
    }
 
    public NumericRangeQuery<Integer> newIntegerRangeQuery(String field, int precisionStep, Integer min, Integer max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeQuery.newIntRange(field, precisionStep, min, max, minInclusive, maxInclusive);
    }
 
    public NumericRangeQuery<Long> newLongRangeQuery(String field, Long min, Long max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeQuery.newLongRange(field, min, max, minInclusive, maxInclusive);
    }

    public NumericRangeQuery<Long> newLongRangeQuery(String field, int precisionStep, Long min, Long max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeQuery.newLongRange(field, precisionStep, min, max, minInclusive, maxInclusive);
    }
 
    public PrefixQuery newPrefixQuery(Term term) {
        return new PrefixQuery(term);
    }
 
    public TermQuery newTermQuery(Term term) {
        return new TermQuery(term);
    }
 
    public TermRangeQuery newTermRangeQuery(String field, String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper) {
        return new TermRangeQuery(field, lowerTerm, upperTerm, includeLower, includeUpper); 
    }
 
    public WildcardQuery newWildcardQuery(Term term) {
        return new WildcardQuery(term);
    }
 
    public FieldValueFilter newFieldValueFilter(String field, boolean negate) {
        return new FieldValueFilter(field, negate);
    }
 
    public NumericRangeFilter<Double> newDoubleRangeFilter(String field, Double min, Double max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeFilter.newDoubleRange(field, min, max, minInclusive, maxInclusive);
    }

    public NumericRangeFilter<Double> newDoubleRangeFilter(String field, int precisionStep, Double min, Double max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeFilter.newDoubleRange(field, precisionStep, min, max, minInclusive, maxInclusive);
    }

    public NumericRangeFilter<Float> newFloatRangeFilter(String field, Float min, Float max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeFilter.newFloatRange(field, min, max, minInclusive, maxInclusive);
    }

    public NumericRangeFilter<Float> newFloatRangeFilter(String field, int precisionStep, Float min, Float max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeFilter.newFloatRange(field, precisionStep, min, max, minInclusive, maxInclusive);
    }
 
    public NumericRangeFilter<Integer> newIntegerRangeFilter(String field, Integer min, Integer max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeFilter.newIntRange(field, min, max, minInclusive, maxInclusive);
    }

    public NumericRangeFilter<Integer> newIntegerRangeFilter(String field, int precisionStep, Integer min, Integer max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeFilter.newIntRange(field, precisionStep, min, max, minInclusive, maxInclusive);
    }
 
    public NumericRangeFilter<Long> newLongRangeFilter(String field, Long min, Long max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeFilter.newLongRange(field, min, max, minInclusive, maxInclusive);
    }

    public NumericRangeFilter<Long> newLongRangeFilter(String field, int precisionStep, Long min, Long max, boolean minInclusive, boolean maxInclusive) {
        return NumericRangeFilter.newLongRange(field, precisionStep, min, max, minInclusive, maxInclusive);
    }
 
    public PrefixFilter newPrefixFilter(Term term) {
        return new PrefixFilter(term);
    }
 
    public QueryWrapperFilter newQueryWrapperFilter(Query query) {
        return new QueryWrapperFilter(query);
    }
 
    public TermRangeFilter newTermRangeFilter(String fieldName, String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper) {
        return new TermRangeFilter(fieldName, lowerTerm, upperTerm, includeLower, includeUpper);
    }
 
    public SortField newSortField(String field, int type, boolean reverse) {
        return new SortField(field, type, reverse);
    }
 
    public Sort newSort() {
        return new Sort();
    }

    public Sort newSort(SortField... fields) {
        return new Sort(fields);
    }

    public Sort newSort(SortField field) {
        return new Sort(field);
    }
 
    public Query parseQuery(String[] queries, String[] fields) throws Exception {
        return MultiFieldQueryParser.parse(Version.LUCENE_36, queries, fields, new StandardAnalyzer(Version.LUCENE_36));
    }
 
    public Query parseQuery(String[] queries, String[] fields, BooleanClause.Occur[] flags) throws Exception {
        return MultiFieldQueryParser.parse(Version.LUCENE_36, queries, fields, flags, new StandardAnalyzer(Version.LUCENE_36));
    }
 
    public Query parseQuery(String query, String[] fields, BooleanClause.Occur[] flags) throws Exception {
        return MultiFieldQueryParser.parse(Version.LUCENE_36, query, fields, flags, new StandardAnalyzer(Version.LUCENE_36));
    }
 
    public String highlight(Query query, String text, String field, int fragmentSize, int maxNumFragments, String separator) throws Exception {
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
        CachingTokenFilter tokenStream = new CachingTokenFilter(analyzer.tokenStream(field, new StringReader(text)));
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter();
        Scorer scorer = new org.apache.lucene.search.highlight.QueryScorer(query);
        Highlighter highlighter = new Highlighter(formatter, scorer);
        highlighter.setTextFragmenter(new SimpleFragmenter(fragmentSize));
        tokenStream.reset();
        String rv = highlighter.getBestFragments(tokenStream, text, maxNumFragments, separator);
        return rv.length() == 0 ? text : rv;
    }
 
    protected void registerDefault() {
        register(SCHEMA, "s");
        register(ID, "s");
        register(CREATED, "l");
        register(UPDATED, "l");
        register(KIND, "s");
        register(MARK, "s");
    }
 
    protected void saveSchema() {
        String tag = "";
        for (Object key : schema.keySet()) {
            if (tag.length() > 0) tag += "|";
            tag += schema.get(key) + "|" + key;
        }
        data.put(SCHEMA, tag);
    }

    protected void loadSchema() {
        String src = data.getProperty(SCHEMA);
        if (src == null) src = "";
        String[] fields = src.split("\\|");
        schema.clear();
        for (int i = 0; i < fields.length && i + 1 < fields.length; i+= 2) {
            register(fields[i + 1], fields[i]);
        }
        registerDefault();

        String tag = "";
        for (Object key : schema.keySet()) {
            if (tag.length() > 0) tag += "|";
            tag += schema.get(key) + "|" + key;
        }
        data.put(SCHEMA, tag);
    }
 
    public void delete() {
        delete(getId());
    }
 
    public void delete(String id) {
        if (handler != null) {
            handler.delete(id);
        }
    }

    public SortField sortFieldDoc() {
        return SortField.FIELD_DOC;
    }
    
    public SortField sortFieldScore() {
        return SortField.FIELD_SCORE;
    }
    
    public int sortFieldLong() {
        return SortField.LONG;
    }
    
    public int sortFieldInteger() {
        return SortField.INT;
    }
    
    public int sortFieldDouble() {
        return SortField.DOUBLE;
    }
    
    public int sortFieldFloat() {
        return SortField.FLOAT;
    }
    
    public int sortFieldString() {
        return SortField.STRING_VAL;
    }

    public double storageQuota() {
        if (handler != null) {
            return handler.storageQuota();
        }
        return 0; 
    }

    public double storageSize() { 
        if (handler != null) {
            return handler.storageSize();
        }
        return 0; 
    }

    public boolean getBoolean(String name) {
    	return getBoolean(name, false);
    }
    
    public boolean getBoolean(String name, boolean defValue) {
    	String val = getString(name);
    	boolean tag = defValue;
    	if ("true".equalsIgnoreCase(val)) tag = true;
    	if ("false".equalsIgnoreCase(val)) tag = false;
    	return tag;
    }
    
    public void setBoolean(String name, boolean value) {
    	setString(name, value + "");
    }
    
    public Date getDate(String name) {
    	return new Date(getLong(name));
    }
    
    public void setDate(String name, Date value) {
    	setLong(name, value.getTime());
    }
    
    public List<String> getMultipleString(String name) {
    	List<String> tag = new ArrayList<String>();
    	String val = getString(name);
    	String[] fields = val.split("\n");
    	for (int i = 0; i < fields.length; i++) {
    		String item = fields[i];
    		item = item.replaceAll("\\n", "\n");
    		item = item.replaceAll("\\\\", "\\");
    		tag.add(item);
    	}
    	return tag;
    }
    
    public void setMultipleString(String name, List<String> value) {
    	String tag = "";
    	for (int i = 0; i < value.size(); i++) {
    		String item = value.get(i);
    		item = item.replaceAll("\\", "\\\\");
    		item = item.replaceAll("\n", "\\n");
    		if (i > 0) tag += "\n";
    		tag += item;
    	}
    	setString(name, tag);
    }

    public List<Integer> getMultipleInteger(String name) {
    	return getMultipleInteger(name, 0);
    }
    
    public List<Integer> getMultipleInteger(String name, int defValue) {
    	List<Integer> tag = new ArrayList<Integer>();
    	String[] fields = getString(name).split("\n");
    	for (int i = 0; i < fields.length; i++) {
    		Integer item = defValue;
    		try {
    			item = Integer.parseInt(fields[i]);
    		} catch (Exception e) {
    		}
    		tag.add(item);
    	}
    	return tag;
    }
    
    public void setMultipleInteger(String name, List<Integer> value) {
    	String tag = "";
    	for (int i = 0; i < value.size(); i++) {
    		if (i > 0) tag += "\n";
    		tag += value.get(i) + "";
    	}
    	setString(name, tag);
    }

    public List<Long> getMultipleLong(String name) {
    	return getMultipleLong(name, 0);
    }
    
    public List<Long> getMultipleLong(String name, long defValue) {
    	List<Long> tag = new ArrayList<Long>();
    	String[] fields = getString(name).split("\n");
    	for (int i = 0; i < fields.length; i++) {
    		long item = defValue;
    		try {
    			item = Long.parseLong(fields[i]);
    		} catch (Exception e) {
    		}
    		tag.add(item);
    	}
    	return tag;
    }
    
    public void setMultipleLong(String name, List<Long> value) {
    	String tag = "";
    	for (int i = 0; i < value.size(); i++) {
    		if (i > 0) tag += "\n";
    		tag += value.get(i) + "";
    	}
    	setString(name, tag);
    }

    public List<Float> getMultipleFloat(String name) {
    	return getMultipleFloat(name, 0);
    }
    
    public List<Float> getMultipleFloat(String name, float defValue) {
    	List<Float> tag = new ArrayList<Float>();
    	String[] fields = getString(name).split("\n");
    	for (int i = 0; i < fields.length; i++) {
    		float item = defValue;
    		try {
    			item = Float.parseFloat(fields[i]);
    		} catch (Exception e) {
    		}
    		tag.add(item);
    	}
    	return tag;
    }
    
    public void setMultipleFloat(String name, List<Float> value) {
    	String tag = "";
    	for (int i = 0; i < value.size(); i++) {
    		if (i > 0) tag += "\n";
    		tag += value.get(i) + "";
    	}
    	setString(name, tag);
    }

    public List<Double> getMultipleDouble(String name) {
    	return getMultipleDouble(name, 0);
    }
    
    public List<Double> getMultipleDouble(String name, double defValue) {
    	List<Double> tag = new ArrayList<Double>();
    	String[] fields = getString(name).split("\n");
    	for (int i = 0; i < fields.length; i++) {
    		double item = defValue;
    		try {
    			item = Double.parseDouble(fields[i]);
    		} catch (Exception e) {
    		}
    		tag.add(item);
    	}
    	return tag;
    }
    
    public void setMultipleDouble(String name, List<Double> value) {
    	String tag = "";
    	for (int i = 0; i < value.size(); i++) {
    		if (i > 0) tag += "\n";
    		tag += value.get(i) + "";
    	}
    	setString(name, tag);
    }
    
    public List<Boolean> getMultipleBoolean(String name) {
    	return getMultipleBoolean(name, false);
    }
    
    public List<Boolean> getMultipleBoolean(String name, boolean defValue) {
    	List<Boolean> tag = new ArrayList<Boolean>();
    	String[] fields = getString(name).split("\n");
    	for (int i = 0; i < fields.length; i++) {
    		boolean item = defValue;
    		if ("true".equalsIgnoreCase(fields[i])) item = true;
    		if ("false".equalsIgnoreCase(fields[i])) item = false;
    		tag.add(item);
    	}
    	return tag;
    }
    
    public void setMultipleBoolean(String name, List<Boolean> value) {
    	String tag = "";
    	for (int i = 0; i < value.size(); i++) {
    		if (i > 0) tag += "\n";
    		tag += value.get(i) + "";
    	}
    	setString(name, tag);
    }

    public List<Date> getMultipleDate(String name) {
    	return getMultipleDate(name, "yyyy-MM-dd", new Date());
    }
    
    public List<Date> getMultipleDate(String name, String format) {
    	return getMultipleDate(name, format, new Date());
    }
    
    public List<Date> getMultipleDate(String name, String format, Date defValue) {
    	List<Date> tag = new ArrayList<Date>();
    	String[] fields = getString(name).split("\n");
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
    	for (int i = 0; i < fields.length; i++) {
    		Date item = defValue;
    		try {
    			item = sdf.parse(fields[i]);
    		} catch (Exception e) {
    		}
    		tag.add(item);
    	}
    	return tag;
    }

    public void setMultipleDate(String name, List<Date> value) {
    	setMultipleDate(name, "yyyy-MM-dd", value);
    }
    
    public void setMultipleDate(String name, String format, List<Date> value) {
    	String tag = "";
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
    	for (int i = 0; i < value.size(); i++) {
    		try {
    			String item = sdf.format(value.get(i));
        		if (i > 0) tag += "\n";
        		tag += item;
    		} catch (Exception e) {
    		}
    	}
    	setString(name, tag);
    }
    
    public static class Handler {
  
        public boolean exists(String id) { return false; }
        public void create(SEntity src) { }
        public void update(SEntity src) { }
        public void load(String id, SEntity src) { }
        public void delete(String id) { }
        public List<SEntity> search(String kind, Query query, int max) { return new ArrayList<SEntity>(); }
        public List<SEntity> search(String kind, Query query, Sort sort, int max) { return new ArrayList<SEntity>(); }
        public List<SEntity> search(String kind, Query query, Filter filter, int max) { return new ArrayList<SEntity>(); }
        public List<SEntity> search(String kind, Query query, Filter filter, Sort sort, int max) { return new ArrayList<SEntity>(); }
        public List<SEntity> search(String kind, Query query, int pagesize, int pageno) { return new ArrayList<SEntity>(); }
        public List<SEntity> search(String kind, Query query, Sort sort, int pagesize, int pageno) { return new ArrayList<SEntity>(); }
        public List<SEntity> search(String kind, Query query, Filter filter, int pagesize, int pageno) { return new ArrayList<SEntity>(); }
        public List<SEntity> search(String kind, Query query, Filter filter, Sort sort, int pagesize, int pageno) { return new ArrayList<SEntity>(); }
        public int count(String kind, Query query, int max) { return 0; }
        public int count(String kind, Query query, Sort sort, int max) { return 0; }
        public int count(String kind, Query query, Filter filter, int max) { return 0; }
        public int count(String kind, Query query, Filter filter, Sort sort, int max) { return 0; }
        public double storageQuota() { return 0; }
        public double storageSize() { return 0; }
  
    }
 
}