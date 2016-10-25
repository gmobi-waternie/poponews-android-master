package com.gmobi.poponews.service;

import com.gmobi.poponews.app.GlobalConfig;
import com.gmobi.poponews.model.Comment;
import com.gmobi.poponews.model.CommentChannelEntity;
import com.gmobi.poponews.model.CommentEntity;
import com.gmobi.poponews.model.CommentReplyEntity;
import com.gmobi.poponews.model.CommentUserInfo;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.outlet.CommentItemBinder;
import com.gmobi.poponews.util.AddCommentUtils;
import com.gmobi.poponews.util.PathUtils;
import com.momock.data.DataList;
import com.momock.data.DataListView;
import com.momock.data.IDataList;
import com.momock.data.IDataView;

import javax.inject.Inject;

/**
 * User: vivian .
 * Date: 2016-08-03
 * Time: 10:43
 */
public class CommentService implements ICommentService {
    @Inject
    IUserService userService;
    @Inject
    IDataService dataService;
    @Inject
    IRemoteService remoteService;

    DataList<CommentEntity> commearlyHotList = new DataList<CommentEntity>();
    DataList<CommentEntity> commList = new DataList<CommentEntity>();
    DataList<CommentEntity> commHotList = new DataList<CommentEntity>();
    DataList<CommentEntity> localList = new DataList<CommentEntity>();
    DataList<CommentChannelEntity> commChannelList = new DataList<CommentChannelEntity>();
    DataList<CommentChannelEntity> sizeChannelList = new DataList<CommentChannelEntity>();
    DataListView<CommentEntity> commListViews = null;
    DataListView<CommentEntity> commHotListViews = null;
    DataListView<CommentChannelEntity> commChannelListViews = null;


    @Override
    public IDataList<CommentEntity> getAllNewsCommentList() {
        if (commListViews == null) {
            commListViews = new DataListView<CommentEntity>(commList);
            commListViews.setOrder(new IDataView.IOrder<CommentEntity>() {
                @Override
                public int compare(CommentEntity lhs, CommentEntity rhs) {
                    if (lhs.getUpdateTime() > rhs.getUpdateTime()) {
                        return -1;
                    }
                    return 0;
                }
            });
        }
        return commListViews.getData();
        //return commList;
    }

    @Override
    public IDataList<CommentEntity> getAllHotCommentList() {
        return commHotList;
    }

    @Override
    public IDataList<CommentEntity> getMoreHotCommentList() {
        if (commHotListViews == null) {
            commHotListViews = new DataListView<CommentEntity>(commHotList);
			/*commHotListViews.setOrder(new IOrder<CommentEntity>() {
				@Override
				public int compare(CommentEntity lhs, CommentEntity rhs) {
					if (lhs.getUpdateTime() > rhs.getUpdateTime()) {
						return -1;
					}
					return 0;
				}
			});*/
            commHotListViews.setLimit(3);
        }
        return commHotListViews.getData();
    }



    @Override
    public IDataList<CommentReplyEntity> getAllReplyList(String nid, int position, int type) {
        if (type == CommentItemBinder.TYPE_HOT) {
            return getAllHotCommentList().getItem(position).getReplyList();
//			return getHotCommentById(nid).getItem(position).getReplyList();
        } else {
            return getAllNewsCommentList().getItem(position).getReplyList();
//			return getNewsCommentById(nid).getItem(position).getReplyList();
        }
    }

    @Override
    public void addItemEarlyHotComment(CommentEntity entity) {
        commearlyHotList.beginBatchChange();
        boolean has = false;
        for (int i = 0; i < commearlyHotList.getItemCount(); i++) {
            if (commearlyHotList.getItem(i).getIId().equals(entity.getIId())) {
                has = true;
                break;
            }
        }
        if (!has) {
            commearlyHotList.addItem(entity);
        }
        commearlyHotList.endBatchChange();
    }

    @Override
    public void addItemHotComment(CommentEntity entity) {
        commHotList.beginBatchChange();
        boolean has = false;
        for (int i = 0; i < commHotList.getItemCount(); i++) {
            if (commHotList.getItem(i).getIId().equals(entity.getIId())) {
                has = true;
                break;
            }
        }
        if (!has) {
            commHotList.addItem(entity);
        }
        commHotList.endBatchChange();
    }

    @Override
    public void addItemNewsComment(String nid, CommentEntity entity) {
        commList.beginBatchChange();
        boolean has = false;
        for (int i = 0; i < commList.getItemCount(); i++) {
            if (commList.getItem(i).getIId().equals(entity.getIId())) {
                has = true;
                break;
            }
        }
        if (!has) {
            commList.addItem(entity);
            NewsItem item = dataService.getNewsById(nid);
            if (item != null) {
                int count = item.getCommentCount();
                count++;
                item.setCommentCount(count);
            }
        }
        commList.endBatchChange();


    }

    @Override
    public void addItemComment(String nid, CommentEntity entity) {
        commList.beginBatchChange();
        commList.addItem(entity);
        NewsItem item = dataService.getNewsById(nid);
        if (item != null) {
            int count = item.getCommentCount();
            count++;
            item.setCommentCount(count);
        }
        commList.endBatchChange();
    }

    /**
     * 每条新闻对于的评论的总数
     **/
    @Override
    public int getCommentCount(Comment comment) {
        return comment.getCount();
    }

    /**
     * 特定用户的所有评论
     **/
    @Override
    public IDataList<CommentChannelEntity> getAllChannelComment() {
        if (commChannelListViews == null) {
            commChannelListViews = new DataListView<CommentChannelEntity>(commChannelList);
            commChannelListViews.setOrder(new IDataView.IOrder<CommentChannelEntity>() {
                @Override
                public int compare(CommentChannelEntity lhs, CommentChannelEntity rhs) {
                    if (lhs.getUpdateTime() > rhs.getUpdateTime()) {
                        return -1;
                    }
                    return 0;
                }
            });
        }
        return commChannelListViews.getData();
    }

    /**
     * 添加特定用户评论
     **/
    @Override
    public void addChannelComment(CommentChannelEntity entity) {
        commChannelList.beginBatchChange();
        boolean has = false;
        for (int i = 0; i < commChannelList.getItemCount(); i++) {
            if (commChannelList.getItem(i).getIId().equals(entity.getIId())) {
                has = true;
                break;
            }
        }
        if (!has) {
            commChannelList.addItem(entity);
        }
        commChannelList.endBatchChange();
    }

    @Override
    public void removeAllChannelComment() {
        commChannelList.beginBatchChange();
        commChannelList.removeAllItems();
        commChannelList.endBatchChange();
        sizeChannelList.beginBatchChange();
        sizeChannelList.removeAllItems();
        sizeChannelList.endBatchChange();
    }

    @Override
    public void batchAddItems(DataList<CommentChannelEntity> ls) {
        sizeChannelList.beginBatchChange();
        for (int i =0; i< ls.getItemCount(); i++){
            sizeChannelList.addItem(ls.getItem(i));
        }
        sizeChannelList.endBatchChange();
    }

    @Override
    public IDataList<CommentChannelEntity> getSizeChannelComment() {
        return sizeChannelList;
    }

    @Override
    public void removeAllHotComment() {
        commHotList.beginBatchChange();
        commHotList.removeAllItems();
        commHotList.endBatchChange();
    }

    @Override
    public void removeAllNewsComment() {
        commList.beginBatchChange();
        commList.removeAllItems();
        commList.endBatchChange();
    }

    @Override
    public DataList<CommentEntity> getLocalList() {
        return localList;
    }

    @Override
    public void addLocalList(CommentEntity entity) {
        localList.beginBatchChange();
        localList.addItem(entity);
        localList.endBatchChange();
    }


    @Override
    public void addNewsComment(String curNid,String edString, CommentUserInfo userInfo, boolean article) {
        String addCommentPath = PathUtils.getFirstPath();
        if (article){
            addItemNewsComment(curNid, AddCommentUtils.addNewsComment(userService.getUserInfo(), addCommentPath, edString, null));
            remoteService.addComment(AddCommentUtils.remoteUserInfo(curNid, userService.getUserInfo().getUId(), null, addCommentPath, edString));
        } else {
            addCommentPath = PathUtils.getFirstPath();
            remoteService.addComment(AddCommentUtils.remoteUserInfo(curNid, userInfo.getUId(), null, addCommentPath, edString));
            CommentEntity entity = AddCommentUtils.addNewsComment(userInfo,curNid, addCommentPath, edString);
            addItemComment(curNid, entity);
            localList.addItem(entity);
        }

    }

    private void replyInt(String nid,String edString, CommentUserInfo userInfo, int type, int position, int replyPos,
                          int vId, String userName, boolean isReply, String uId, String uPath,String addCommentPath)
    {
        CommentEntity comm;
        String toName;
        String pa;
        if (type == CommentItemBinder.TYPE_NEWS){
            comm = getAllNewsCommentList().getItem(position);
        } else {
            comm = getAllHotCommentList().getItem(position);
        }
        if (replyPos == GlobalConfig.REPLY_SECOND) { //回复xxx
            toName = getAllReplyList(nid, position, type).getItem(vId).getUserName();
        } else {
            toName = userName;
        }
        String userId = userInfo.getUId() == null ? userInfo.getUserName() : userInfo.getUId();


        if (!isReply){ // 评论的回复
            String tId = uId == null ? userInfo.getUId() : uId;
            pa = PathUtils.getPath(localList, position, -1, uPath, addCommentPath, isReply);

            remoteService.addReply(AddCommentUtils.remoteUserInfo(nid, userInfo.getUId(), tId, pa, edString), false);
        } else { // 回复的回复
            String tId = getAllReplyList(nid, position, type).getItem(vId).getUserId();
            String tPa = getAllReplyList(nid, position, type).getItem(vId).getPath();
            tId = tId == null ? userInfo.getUId() : tId;
            pa = PathUtils.getPath(localList,position,vId,tPa,addCommentPath,isReply);
            remoteService.addReply(AddCommentUtils.remoteUserInfo(nid, userInfo.getUId(), tId, pa, edString), true);

        }
        AddCommentUtils.replyComment(comm, edString, userId, userInfo.getUserName(), toName, pa);
    }

    @Override
    public void replyComment(String nid,String edString, CommentUserInfo userInfo, int type, int position, int replyPos,
                             int vId, String userName, boolean isReply, String uId, String uPath,String addCommentPath) {
        replyInt(nid,edString,userInfo,type,position,replyPos,vId,userName,isReply,uId,uPath,addCommentPath);
    }

    @Override
    public void replyReplies(CommentEntity entity) {
    }

    @Override
    public Class<?>[] getDependencyServices() {
        return new Class<?>[0];
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean canStop() {
        return false;
    }
}
