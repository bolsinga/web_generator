package com.bolsinga.music;

import com.bolsinga.web.*;

import java.util.*;

import org.apache.ecs.*;
import org.apache.ecs.html.*;

public abstract class DynamicStatsRecordFactory extends StatsRecordFactory {

    protected abstract String getTableTitle();
    protected abstract String getTableSummary();
    protected abstract String getTableType();
    
    protected abstract int getStatsSize();
    protected abstract int generateStats(StatsRecordFactory.StatsTracker tracker) throws com.bolsinga.web.WebException;
    protected abstract String getStatsLinkPrefix();

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

        String linkPrefix = getStatsLinkPrefix();
        if (linkPrefix != null) {
            sb.append("\"");
            sb.append(linkPrefix);
            sb.append("\"");
        } else
            sb.append("null");
        
        final ArrayList<org.json.JSONObject> values = new ArrayList<org.json.JSONObject>(getStatsSize());
        
        int total = generateStats(new StatsRecordFactory.StatsTracker() {
            public void track(final String name, final String link, final int value) throws com.bolsinga.web.WebException {
                org.json.JSONObject json = new org.json.JSONObject();
                try {
                    json.put("k", name);
                    if (link != null) {
                        json.put("l", link);
                    }
                    json.put("v", value);
                } catch (org.json.JSONException e) {
                    throw new com.bolsinga.web.WebException("Can't create dynamic stats json", e);
                }
                values.add(json);
            }
        });
        
        sb.append(",");
        sb.append(total);
        sb.append(",");
        
        org.json.JSONArray jarray = new org.json.JSONArray(values);
        try {
            if (com.bolsinga.web.Util.getPrettyOutput()) {
                sb.append(jarray.toString(2));
            } else {
                sb.append(jarray.toString());
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
