package com.gmobi.poponews.util;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.momock.util.JsonDatabase.Collection;
import com.momock.util.JsonDatabase.Document;
import com.momock.util.Logger;

public class DataCollect {

	// keys in request
	private final static String KEY_ROOT = "$";
	private final static String KEY_ID = "did";
	private final static String KEY_TIME = "at";
	// database record keys
	private final static String RECORD_KEY_DATA = "dt";
	private final static String RECORD_KEY_TYPE = "tp";
    private final static String RECORD_KEY_SERVER = "svr";
	private final static String RECORD_KEY_ID = "did";
    private final static String RECORD_KEY_DATA_NAME = "dk";
    // some value
    public final static String RECORD_TYPE_ARRAY = "ar";
    public final static String RECORD_TYPE_OBJECT = "ob";
    public final static String DEFAULT_SERVER = "defSvr";

	private Collection colData = null;
	
	public DataCollect(Collection dbc){
		Logger.info("data collect create");
		colData = dbc;
	}

    protected boolean recordReportData(String did, String serverName,
                                     String recordeType, String dataKey, Object data){
		boolean ret = false;
        // check parameters
        if(null == did || null == serverName || null == recordeType
                || null == dataKey || null == data){
            Logger.error("record input is invalid!");
            return ret;
        }
		try {
            JSONObject jo = new JSONObject();
            jo.put(RECORD_KEY_ID, did);
            jo.put(RECORD_KEY_DATA, data);
            jo.put(RECORD_KEY_TYPE, recordeType);
            jo.put(RECORD_KEY_SERVER, serverName);
            jo.put(RECORD_KEY_DATA_NAME, dataKey);
            String id = colData.set(null, jo);
            Logger.debug("Record Data " + id + ":" + jo);
            ret = true;
		} catch (Exception e) {
			Logger.error(e);
		}
		return ret;
	}

    protected void addSharedFeatureInRequest(JSONObject parent) throws JSONException {
           // add additional shared feature here
    }

	public JSONObject genCollectData(String server, List<String> docIds) {

		if (colData == null || colData.size() <= 0
                || null == docIds || null == server) {
			Logger.info("no data to send");
			return null;
		}
        JSONObject requestBody = new JSONObject();
		try {
			List<Document> docs = colData.list();
			String curDid = null;
			for (Document d : docs) {
				JSONObject joi = d.getData();
                // check server
                String svr = joi.getString(RECORD_KEY_SERVER);
                if(!server.equals(svr)){
                    // if server is not matched, skip to next
                    continue;
                }
                // check device id
                String did = joi.getString(RECORD_KEY_ID);
				if(null == curDid){
					curDid = did;
				}else if(!curDid.equals(did)){
					continue;
				}
                // build request
                String requestType = joi.getString(RECORD_KEY_TYPE);
                String requestDataName = joi.getString(RECORD_KEY_DATA_NAME);
                if(requestType.equals(RECORD_TYPE_OBJECT) && !requestBody.has(requestDataName)){
                    requestBody.put(requestDataName, joi.get(RECORD_KEY_DATA));
                }else if(requestType.equals(RECORD_TYPE_ARRAY)){
                    JSONArray dataArray;
                    if(requestBody.has(requestDataName)){
                        dataArray = requestBody.getJSONArray(requestDataName);
                    }else{
                        dataArray = new JSONArray();
                        requestBody.put(requestDataName, dataArray);
                    }	
                    dataArray.put(joi.get(RECORD_KEY_DATA));
                }else{
                    continue;
                }
				docIds.add(d.getId());
			}
            // create shared feature
            JSONObject joc = new JSONObject();
            joc.put(KEY_TIME, System.currentTimeMillis());
            joc.put(KEY_ID, curDid);
            addSharedFeatureInRequest(joc);
            requestBody.put(KEY_ROOT, joc);
			Logger.debug("Send collected data : " + requestBody);
			return requestBody;
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}

	public void delCollectData(List<String> docIds) {
		for (String id : docIds) {
			Logger.debug("delete collect data id = " + id);
			colData.set(id, null);
		}
	}

    // API for project

    public boolean recordArrayData(String did, String dataKey, Object data){
        return recordReportData(did, DEFAULT_SERVER, RECORD_TYPE_ARRAY, dataKey, data);
    }

    public boolean recordObjectData(String did, String dataKey, Object data){
        return recordReportData(did, DEFAULT_SERVER, RECORD_TYPE_OBJECT, dataKey, data);
    }

    public JSONObject genCollectData(List<String> docIds){
        return genCollectData(DEFAULT_SERVER, docIds);
    }
}
