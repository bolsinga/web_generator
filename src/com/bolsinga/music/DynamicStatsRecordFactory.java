package com.bolsinga.music;

import com.bolsinga.web.*;

import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public abstract class DynamicStatsRecordFactory extends StatsRecordFactory {

    public interface StatsTracker {
        public void track(final String name, final String id, final String file, final int value) throws com.bolsinga.web.WebException;
    }
    
    protected abstract String getTableTitle();
    protected abstract String getTableSummary();
    protected abstract String getTableType();
    
    protected abstract int getStatsSize();
    protected abstract int generateStats(StatsTracker tracker) throws com.bolsinga.web.WebException;
    
    protected abstract String getStatsLinkPrefix();
    protected abstract String getStatsLinkDirectoryPath();

    public Vector<Record> getRecords() throws com.bolsinga.web.WebException {
        Vector<Record> records = super.getRecords();
        
        Script script = new Script();
        script.setType("text/javascript");
        script.removeAttribute("language");
        
        StringBuilder sb = new StringBuilder();
        sb.append("window.addEventListener(\"load\",function(){");
        sb.append("createStats(\"");
        sb.append(CSS.TABLE_ROW_ALT);
        sb.append("\",\"");
        sb.append(CSS.TABLE_FOOTER);
        sb.append("\",");
        
        org.json.JSONObject data = new org.json.JSONObject();

        try {
            String linkPrefix = getStatsLinkPrefix();
            if (linkPrefix != null)
                data.put("prefix", linkPrefix);

            String linkDirectoryPrefix = getStatsLinkDirectoryPath();
            if (linkDirectoryPrefix != null)
                data.put("directory", linkDirectoryPrefix);
            
            final ArrayList<org.json.JSONObject> values = new ArrayList<org.json.JSONObject>(getStatsSize());
            
            int total = generateStats(new StatsTracker() {
                public void track(final String name, final String id, final String file, final int value) throws com.bolsinga.web.WebException {
                    org.json.JSONObject json = new org.json.JSONObject();
                    try {
                        json.put("k", name);
                        json.put("i", id);
                        if (file != null) {
                            json.put("f", file);
                        }
                        json.put("v", value);
                    } catch (org.json.JSONException e) {
                        throw new com.bolsinga.web.WebException("Can't track dynamic stats json", e);
                    }
                    values.add(json);
                }
            });
            
            data.put("total", total);
            
            data.put("vals", new org.json.JSONArray(values));
        } catch (org.json.JSONException e) {
            throw new com.bolsinga.web.WebException("Can't create dynamic stats json", e);
        }
        
        try {
            if (com.bolsinga.web.Util.getPrettyOutput()) {
                sb.append(data.toString(2));
            } else {
                sb.append(data.toString());
            }
        } catch (org.json.JSONException e) {
            throw new com.bolsinga.web.WebException("Can't write dynamic stats json array", e);
        }

        sb.append(");");
        
        sb.append("},false);");
        script.setTagText(sb.toString());
        
        records.add(Record.createRecordSimple(script));
        
        return records;
    }
    
    protected Table getTable() {
        Table table = Util.makeTable(getTableTitle(), getTableSummary(), new TableHandler() {
            public TR getHeaderRow() {
                return new TR().addElement(new TH(getTableType())).addElement(new TH("#")).addElement(new TH("%"));
            }

            public int getRowCount() {
                return 0;
            }

            public TR getRow(final int row) {
                return null;
            }

            public TR getFooterRow() {
                return null;
            }
        });
        table.setID("stats");
        return table;
    }
}
