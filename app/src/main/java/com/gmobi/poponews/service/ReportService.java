package com.gmobi.poponews.service;

import javax.inject.Inject;

import org.json.JSONObject;

import android.content.Context;

import com.gmobi.poponews.BuildConfig;
import com.gmobi.poponews.R;
import com.gmobi.poponews.model.CommentUserInfo;
import com.gmobi.poponews.util.DataCollect;
import com.momock.app.App;
import com.momock.data.Settings;
import com.momock.event.IEventHandler;
import com.momock.service.IAsyncTaskService;
import com.momock.service.ICacheService;
import com.momock.service.ICrashReportService;
import com.momock.service.IHttpService;
import com.momock.service.IMessageService;
import com.momock.service.ICrashReportService.CrashEventArgs;
import com.momock.util.JsonDatabase;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;
import com.momock.util.JsonDatabase.Collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ReportService implements IReportService {

	public static final String DATABASE = "com.poponews.report";
    public final static String COL_DATA = "ReportData";
	
	DataCollect dc = null;
    List<String> reportDataId;
    boolean isReporting = false;
    Timer reportTaskTimer;

	@Inject
	Context context;
	@Inject
    IConfigService configService;
    @Inject
    IRemoteService remoteService;
    @Inject
    IAsyncTaskService asyncTaskService;
    @Inject
    ICrashReportService crashReportService;
    @Inject
    IUserService meService;

    
	@Override
	public Class<?>[] getDependencyServices() {
		return new Class<?>[]{ICrashReportService.class};
	}

	@Override
	public void start() {
		crashReportService.addCrashHandler(new IEventHandler<CrashEventArgs>() {
			
			@Override
			public void process(Object sender, CrashEventArgs args) {
				
				String msg = args.getError().getMessage()+"\n";
				JSONObject deviceJo = remoteService.getDeviceInfo();


				
				for(StackTraceElement elem : args.getError().getStackTrace()) {
					msg = msg+elem+"\n";
		        }

                msg = msg+Logger.getStackTrace(args.getError());

				 
				String thread = args.getThread().getName();
				Logger.debug("msg="+msg+"\n"+"thread="+thread);
				
				recordCrash(thread, msg,deviceJo.toString());
			}
		});
		
		
		
        JsonDatabase db = JsonDatabase.get(context, DATABASE);
        Collection report = db.getCollection(COL_DATA);
        report.setCachable(true);
        dc = new DataCollect(report);
        reportDataId = new ArrayList<String>();
        TimerTask reportTask = new TimerTask(){

            @Override
            public void run() {
                if(null != configService
                        && null != configService.getDid()) {
                    startReport();
                }
            }

        };
        reportTaskTimer = new Timer();
        reportTaskTimer.schedule(reportTask, 5 * 1000, 60 * 1000);
        
        
        
	}

	@Override
	public void stop() {
        reportTaskTimer.cancel();
	}

	@Override
	public boolean canStop() {
		return true;
	}

    private void startReport(){
        if(isReporting){
            return;
        }
        isReporting = true;
        try{
            reportDataId.clear();
            JSONObject jo = dc.genCollectData(reportDataId);
            if(null != jo && remoteService.startDefaultReport(jo.toString())){
                dc.delCollectData(reportDataId);
            }
        }catch(Exception e){
            Logger.error(e);
        }
        isReporting = false;
    }

    /**
     *  sales tarck report
     */
    private final static String KEY_TRACK = "sales.track";

    @Override
	public boolean recordTrackData(Object jsonData){
        boolean ret = false;
        if(null == dc || null == jsonData){
            return ret;
        }
        JSONObject jo = null;
        if(jsonData instanceof String){
            jo = JsonHelper.parse((String)jsonData);
        }else if(jsonData instanceof JSONObject){
            jo = (JSONObject)jsonData;
        }
        ret = dc.recordObjectData(configService.getDid(), KEY_TRACK, jo);
        return ret;
	}

    /**
     * User Data Report API
     */
    private final static String KEY_EMO = "mood";
    private final static String KEY_FAV = "fav";
    private final static String KEY_FEEDBACK = "feedback";
    private final static String KEY_CRASH = "crash";
    private final static String KEY_ACTION = "action";
    private final static String KEY_UNLIKE = "unlike";

    private final static String KEY_ARTICAL_ID = "aid";
    private final static String KEY_ID = "id";
    private final static String KEY_DATA = "data";
    private final static String KEY_TYPE = "type";

    private final static String KEY_THREAD = "thread";
    private final static String KEY_STACK = "stack";
    private final static String KEY_DEVICE = "device";
    private final static String KEY_GROUP = "group";
    private final static String KEY_CHANNEL = "channel";
    private final static String KEY_CATEGORY = "category";
    private final static String KEY_DID = "did";
    private final static String KEY_UID = "uid";
    
    private final static String KEY_TYPE_PV = "pv";
    private final static String KEY_TYPE_LIST = "list";
    private final static String KEY_TYPE_PUSH_RECV = "push_received";
    private final static String KEY_TYPE_PUSH_CLICK = "push_clicked";


    @Override
    public boolean recordEmo(String aid, String emo) {
        boolean ret = false;
        if (null == dc || null == aid || null == emo) {
            return ret;
        }
        try {
            JSONObject jo = new JSONObject();
            jo.put(KEY_ARTICAL_ID, aid);
            jo.put(KEY_DATA, emo);
            ret = dc.recordArrayData(configService.getDid(), KEY_EMO, jo);
        } catch (Exception e) {
            Logger.error(e);
        }
        return ret;
    }


    @Override
    public boolean recordUninterest(String aid, String cid) {
        boolean ret = false;
        if (null == dc || null == aid || null == cid) {
            return ret;
        }
        try {
            JSONObject jo = new JSONObject();
            jo.put(KEY_ARTICAL_ID, aid);
            jo.put(KEY_CATEGORY, cid);

            String did = configService.getDid();
            if(did!=null)
                jo.put(KEY_DID, did);

            CommentUserInfo user = meService.getUserInfo();
            if(user!=null)
                jo.put(KEY_UID, user.getUId());

            ret = dc.recordArrayData(configService.getDid(), KEY_UNLIKE, jo);
        } catch (Exception e) {
            Logger.error(e);
        }
        return ret;
    }


    @Override
    public boolean recordFav(String aid, int fav) {
        boolean ret = false;
        if (null == dc || null == aid || fav < 0) {
            return ret;
        }
        try {
            JSONObject jo = new JSONObject();
            jo.put(KEY_ARTICAL_ID, aid);
            jo.put(KEY_DATA, fav);
            ret = dc.recordArrayData(configService.getDid(), KEY_FAV, jo);
        } catch (Exception e) {
            Logger.error(e);
        }
        return ret;
    }

    @Override
    public boolean recordFeedback(String msg) {
        boolean ret = false;
        if (null == dc || null == msg || msg.isEmpty()) {
            return ret;
        }
        try {
            JSONObject jo = new JSONObject();
            jo.put(KEY_GROUP, BuildConfig.GROUP);
            jo.put(KEY_CHANNEL, configService.getCurChannel());
            jo.put(KEY_DATA, msg);
            ret = dc.recordObjectData(configService.getDid(), KEY_FEEDBACK, jo);
        } catch (Exception e) {
            Logger.error(e);
        }
        return ret;
    }
    
    
    @Override
    public boolean recordCrash(String thread, String msg,String deviceInfo) {
        boolean ret = false;
        if (null == dc || null == msg || msg.isEmpty()) {
            return ret;
        }
        try {
            JSONObject jo = new JSONObject();
            jo.put(KEY_GROUP, BuildConfig.GROUP);
            jo.put(KEY_CHANNEL, configService.getCurChannel());
            JSONObject crashjo = new JSONObject();
            crashjo.put(KEY_THREAD, thread);
            crashjo.put(KEY_STACK, msg);
            crashjo.put(KEY_DEVICE, deviceInfo);
            
            jo.put(KEY_DATA,crashjo);
            ret = dc.recordObjectData(configService.getDid(), KEY_CRASH, jo);
        } catch (Exception e) {
            Logger.error(e);
        }
        return ret;
    }
    
    @Override
    public boolean recordPv(String aid) {
        boolean ret = false;
        if (null == dc || null == aid) {
            return ret;
        }
        try {
            JSONObject jo = new JSONObject();
            jo.put(KEY_ID, aid);
            jo.put(KEY_DATA, 1);
            jo.put(KEY_TYPE, KEY_TYPE_PV);
            ret = dc.recordArrayData(configService.getDid(), KEY_ACTION, jo);
        } catch (Exception e) {
            Logger.error(e);
        }
        return ret;
    }
    
    
    @Override
    public boolean recordList(String cid) {
        boolean ret = false;
        if (null == dc || null == cid) {
            return ret;
        }
        try {
            JSONObject jo = new JSONObject();
            jo.put(KEY_ID, cid);
            jo.put(KEY_DATA, 1);
            jo.put(KEY_TYPE, KEY_TYPE_LIST);            
            ret = dc.recordArrayData(configService.getDid(), KEY_ACTION, jo);
        } catch (Exception e) {
            Logger.error(e);
        }
        return ret;
    }

    @Override
    public boolean recordPushRecv(String aid) {
        boolean ret = false;
        if (null == dc || null == aid) {
            return ret;
        }

        try {
            JSONObject jo = new JSONObject();
            jo.put(KEY_ID, aid);
            jo.put(KEY_DATA, 1);
            jo.put(KEY_TYPE, KEY_TYPE_PUSH_RECV);
            ret = dc.recordArrayData(configService.getDid(), KEY_ACTION, jo);
        } catch (Exception e) {
            Logger.error(e);
        }
        return ret;
    }

    @Override
    public boolean recordPushClick(String aid) {
        boolean ret = false;
        if (null == dc || null == aid) {
            return ret;
        }

        try {
            JSONObject jo = new JSONObject();
            jo.put(KEY_ID, aid);
            jo.put(KEY_DATA, 1);
            jo.put(KEY_TYPE, KEY_TYPE_PUSH_CLICK);
            ret = dc.recordArrayData(configService.getDid(), KEY_ACTION, jo);
        } catch (Exception e) {
            Logger.error(e);
        }
        return ret;
    }
}

