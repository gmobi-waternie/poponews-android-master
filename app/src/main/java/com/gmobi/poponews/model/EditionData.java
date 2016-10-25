package com.gmobi.poponews.model;

public class EditionData {
    public static final String TAG_ICON = "Icon";
    public static final String TAG_NAME = "Name";

    private String ic;
    private String na;
    private String ch;
    private String lang;

    public EditionData(String icon, String name, String channel,String lang){
        this.ic = icon;
        this.na = name;
        this.ch = channel;
        this.lang = lang;
    }

    public String getName(){
        return na;
    }
    public String getIcon(){
        return ic;
    }
    public String getChannel(){
        return ch;
    }
    public String getLang(){
        return lang;
    }

}
