package com.gmobi.poponews.provider;

import com.gmobi.poponews.model.CommentChannelEntity;
import com.gmobi.poponews.service.ICommentService;
import com.momock.data.DataList;
import com.momock.data.IDataList;
import com.momock.message.Message;
import com.momock.service.IMessageService;

import javax.inject.Inject;

/**
 * User: vivian .
 * Date: 2016-08-04
 * Time: 09:47
 */
public class CommentProvider implements IDataProvider {
    @Inject
    ICommentService commentService;
    @Inject
    IMessageService messageService;

    DataList<CommentChannelEntity> cachePool;
    private final static int GET_SIZE = 10;


    //Return
    //false:pool is empty.  true:has data.
    @Override
    public synchronized  boolean getData() {
        if(cachePool.getItemCount() == 0)
            return false;

        DataList<CommentChannelEntity> ls = new DataList<>();
        int getCount = (cachePool.getItemCount() >= GET_SIZE) ? GET_SIZE : cachePool.getItemCount();

        for(int i=0;i<getCount; i++)
        {
            ls.addItem((cachePool.getItem(0)));
            cachePool.removeItemAt(0);
        }

        commentService.batchAddItems(ls);
        if (getCount < GET_SIZE)
            return false;
        return true;
    }

    @Override
    public void initData() {
        cachePool = (DataList<CommentChannelEntity>) commentService.getAllChannelComment();
    }


    @Override
    public Class<?>[] getDependencyServices() {
        return new Class<?>[0];
    }

    @Override
    public void start() {
        if(cachePool == null)
            cachePool = new DataList<>();
        cachePool.removeAllItems();
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean canStop() {
        return false;
    }
}
