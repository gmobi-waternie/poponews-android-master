package com.gmobi.poponews.model;

import com.momock.data.DataList;
import com.momock.data.DataMap;

/**
 * Created by vivian on 2016/6/27.
 */
public class Comment extends DataMap<Object,Object>{
    private int count;
    private CommentEntity entity;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public CommentEntity getEntity() {
        return entity;
    }

    public void setEntity(CommentEntity entity) {
        this.entity = entity;
    }
}
