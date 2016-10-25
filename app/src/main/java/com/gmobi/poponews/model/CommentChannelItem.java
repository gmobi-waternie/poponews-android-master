package com.gmobi.poponews.model;

import com.gmobi.poponews.service.IConfigService;
import com.momock.app.App;
import com.momock.data.DataMap;

/**
 * 与我相关的新闻实体Bean
 * User: vivian .
 * Date: 2016-07-04
 * Time: 16:56
 */
public class CommentChannelItem extends DataMap<Object, Object> {
    public static final String IId = "IId"; // 唯一标识
    public static final String PDomain = "PDomain";
    public static final String PIcon = "PIcon";
    public static final String PName = "PName";
    public static final String Preview = "Preview";
    public static final String PSource = "Source";
    public static final String Title = "Title";
    private boolean Go2Source;

    IConfigService cs = App.get().getService(IConfigService.class);
    public String getIId(){
        return (String) this.getProperty(IId);
    }
    public void setIId(String iId){
        this.setProperty(IId, iId);
    }

    public boolean isGo2Source() {
        return Go2Source;
    }

    public void setGo2Source(boolean go2Source) {
        Go2Source = go2Source;
    }

    public String getPDomain(){
        return (String) this.getProperty(PDomain);
    }

    public void setPDomain(String pDomain){
        this.setProperty(PDomain,pDomain);
    }

    public String getPIcon(){
        return (String) this.getProperty(PIcon);
    }

    public void setPIcon(String pIcon){
        this.setProperty(PIcon,pIcon);
    }
    public String getPName(){
        return (String) this.getProperty(PName);
    }

    public void setPName(String pName){
        this.setProperty(PName,pName);
    }
    public String getPreview(){
        return (String) this.getProperty(Preview);
    }

    public void setPreview(String preview){
        this.setProperty(Preview,cs.getBaseUrl() + preview + ".240x150t5");
    }
    public String getPSource(){
        return (String) this.getProperty(PSource);
    }

    public void setPSource(String pSource){
        this.setProperty(PSource,pSource);
    }
    public String getTitle(){
        return (String) this.getProperty(Title);
    }

    public void setTitle(String title){
        this.setProperty(Title,title);
    }

}
