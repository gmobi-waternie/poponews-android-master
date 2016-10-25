package com.gmobi.poponews.model;

import com.gmobi.poponews.R;
import com.gmobi.poponews.service.IReportService;
import com.momock.data.DataMap;

public class EmoVote extends DataMap<String, Integer> {
    String mainEmo = null;
    int mainEmoVote = 0;

    public EmoVote(){
        setProperty(IReportService.KEY_EMO_WORRY, 0);
        setProperty(IReportService.KEY_EMO_SAD, 0);
        setProperty(IReportService.KEY_EMO_MOVING, 0);
        setProperty(IReportService.KEY_EMO_ANGRY, 0);
        setProperty(IReportService.KEY_EMO_AMAZE, 0);
        setProperty(IReportService.KEY_EMO_HAPPY, 0);
    }

    @Override
    public void setProperty(String name, Integer val) {
        super.setProperty(name, val);
        if(val > mainEmoVote){
            mainEmoVote = val;
            mainEmo = name;
        }
    }

    public String getMainEmo(){
        return mainEmo;
    }

    public int getMainEmoVote(){
        return mainEmoVote;
    }

    public int getTotalVote(){
        int total = 0;
        for(String name : getPropertyNames()){
            total += getProperty(name);
        }
        return total;
    }

    public static int getSmallVoteIcon(String voteName){
        int emoImageId = R.drawable.expression_happy_newslist;
        if(null != voteName){
            if(voteName.equals(IReportService.KEY_EMO_AMAZE)){
                emoImageId = R.drawable.expression_amazing_newslist;
            }else if(voteName.equals(IReportService.KEY_EMO_ANGRY)){
                emoImageId = R.drawable.expression_angry_newslist;
            }else if(voteName.equals(IReportService.KEY_EMO_MOVING)){
                emoImageId = R.drawable.expression_moving_newslist;
            }else if(voteName.equals(IReportService.KEY_EMO_SAD)){
                emoImageId = R.drawable.expression_sad_newslist;
            }else if(voteName.equals(IReportService.KEY_EMO_WORRY)){
                emoImageId = R.drawable.expression_worry_newslist;
            }
        }
        return emoImageId;
    }

    public static int getVoteIdByName(String voteName){
        int emoId;
        if(null == voteName) {
            emoId = 0;
        }else if(voteName.equals(IReportService.KEY_EMO_AMAZE)){
            emoId = R.id.emo_item_amazing;
        }else if(voteName.equals(IReportService.KEY_EMO_ANGRY)){
            emoId = R.id.emo_item_angry;
        }else if(voteName.equals(IReportService.KEY_EMO_HAPPY)){
            emoId = R.id.emo_item_happy;
        }else if(voteName.equals(IReportService.KEY_EMO_MOVING)){
            emoId = R.id.emo_item_moving;
        }else if(voteName.equals(IReportService.KEY_EMO_SAD)){
            emoId = R.id.emo_item_sad;
        }else if(voteName.equals(IReportService.KEY_EMO_WORRY)){
            emoId = R.id.emo_item_worry;
        }else{
            emoId = 0;
        }
        return emoId;
    }

    public static String getVoteName(int voteId){
        String name;
        switch (voteId){
            case R.id.emo_item_amazing:
                name = IReportService.KEY_EMO_AMAZE;
                break;
            case R.id.emo_item_angry:
                name = IReportService.KEY_EMO_ANGRY;
                break;
            case R.id.emo_item_happy:
                name = IReportService.KEY_EMO_HAPPY;
                break;
            case R.id.emo_item_moving:
                name = IReportService.KEY_EMO_MOVING;
                break;
            case R.id.emo_item_sad:
                name = IReportService.KEY_EMO_SAD;
                break;
            case R.id.emo_item_worry:
                name = IReportService.KEY_EMO_WORRY;
                break;
            default:
                name = null;
        }
        return name;
    }

}
