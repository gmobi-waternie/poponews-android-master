package com.gmobi.poponews.outlet;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.GlobalConfig;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.model.CommentEntity;
import com.gmobi.poponews.model.CommentUserInfo;
import com.gmobi.poponews.service.ICommentService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.service.IUserService;
import com.gmobi.poponews.util.DBHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.widget.CommentUpView;
import com.momock.app.App;
import com.momock.app.CaseActivity;
import com.momock.binder.IContainerBinder;
import com.momock.binder.IItemBinder;
import com.momock.binder.ViewBinder;
import com.momock.data.IDataList;
import com.momock.holder.ViewHolder;
import com.momock.message.Message;
import com.momock.service.IImageService;
import com.momock.service.IMessageService;
import com.momock.util.ImageHelper;
import com.momock.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * User: vivian .
 * Date: 2016-08-01
 * Time: 15:35
 */
public class CommentItemBinder {
    private CaseActivity context;
    private ICommentService commentService;
    private IDataService dataService;
    private IRemoteService remoteService;
    private IUserService userService;
    private IImageService imageService;
    private IMessageService messageService;
    private DBHelper dh;
    private int type;
    private String nid;

    public static final int TYPE_MORE_HOT = 1;
    public static final int TYPE_HOT = 2;
    public static final int TYPE_NEWS = 3;

    public CommentItemBinder(CaseActivity context, int type) {
        this.context = context;
        this.type = type;
        commentService = App.get().getService(ICommentService.class);
        dataService = App.get().getService(IDataService.class);
        remoteService = App.get().getService(IRemoteService.class);
        userService = App.get().getService(IUserService.class);
        imageService = App.get().getService(IImageService.class);
        messageService = App.get().getService(IMessageService.class);
        dh = DBHelper.getInstance();
        nid = dataService.getCurNid();
    }









    private ViewBinder.Setter setter = new ViewBinder.Setter() {
        @Override
        public boolean onSet(View view, String s, int i, String s1, Object o, View parent, IContainerBinder iContainerBinder) {

            boolean nightmode = NightModeUtil.getDayNightMode() == NightModeUtil.THEME_SUN ? false : true;
            parent.findViewById(R.id.relative_comment_list_item).setBackgroundColor(nightmode ? App.get().getResources().getColor(R.color.bg_black_night) :
                    App.get().getResources().getColor(R.color.bg_white));

            if (view == null)
                return false;
            if (view.getId() == R.id.tv_content){
                if (nightmode){
                    ((TextView)view).setTextColor(App.get().getResources().getColor(R.color.bg_article));
                } else {
                    ((TextView)view).setTextColor(App.get().getResources().getColor(R.color.bg_black));
                }
                return true;
            } else if (view.getId() == R.id.item_linear){
                if (nightmode){
                    for (int j=0;j<((LinearLayout)view).getChildCount();j++){
                        ((LinearLayout) view).getChildAt(j).
                                setBackgroundDrawable(App.get().getResources().getDrawable(R.drawable.mycomment_preview_selector_night));
                    }
                } else {
                    for (int j=0;j<((LinearLayout)view).getChildCount();j++){
                        ((LinearLayout) view).getChildAt(j).
                                setBackgroundDrawable(App.get().getResources().getDrawable(R.drawable.comment_ding_textview));
                    }
                }
                return true;
            } else if (view.getId() == R.id.btn_add){
                if (nightmode){
                    view.setBackgroundColor(App.get().getResources().getColor(R.color.about_me_night));
                    ((TextView)view).setTextColor(App.get().getResources().getColor(R.color.menu_item_background));
                } else {
                    view.setBackgroundColor(App.get().getResources().getColor(R.color.comment_more_bg));
                    ((TextView)view).setTextColor(App.get().getResources().getColor(R.color.comment_more));
                }
                return true;
            }

            return false;
        }
    };

    public CommentBinder build(){
//        if (type == TYPE_MORE_HOT || type == TYPE_HOT){
            CommentBinder hotItemBinder = new CommentBinder(0,new int[]{R.id.tv_content, R.id.item_linear, R.id.btn_add},
                    new String[]{"dummy","dummy","dummy"});
            hotItemBinder.addSetter(setter);
            return hotItemBinder;
//        } else if (type == TYPE_NEWS){
//            CommentBinder newsItemBinder = new CommentBinder(0,new int[]{R.id.tv_content, R.id.item_linear, R.id.btn_add},
//                new String[]{"dummy","dummy","dummy"});
//            newsItemBinder.addSetter(setter);
//            return newsItemBinder;
//        }
//        return null;
    }

    String path;
    private class CommentBinder extends ViewBinder implements IItemBinder{
        int itemViewId;
        String i_id;
        IDataList<CommentEntity> dataList;;
        public CommentBinder(int itemViewId, int[] childViewIds, String[] props) {
            Logger.check(childViewIds != null && props != null && childViewIds.length == props.length, "Parameter error!");
            this.itemViewId = itemViewId;

            for(int i = 0; i < props.length; ++i) {
                this.link(props[i], childViewIds[i]);
            }

        }

        @Override
        public View onCreateItemView(View convertView, final int position, IContainerBinder container) {

            if (type == CommentItemBinder.TYPE_MORE_HOT){
                dataList = commentService.getMoreHotCommentList();
            } else if (type == CommentItemBinder.TYPE_HOT){
                dataList = commentService.getAllHotCommentList();
            } else if (type == CommentItemBinder.TYPE_NEWS){
                dataList = commentService.getAllNewsCommentList();
            }
            LinearLayout linearLayout = null;
            int size = 0;
            View view = convertView;
            if (view == null) {
                view = ViewHolder.create(App.get().getCurrentContext(), R.layout.comment_list_item).getView();
                if (type != CommentItemBinder.TYPE_MORE_HOT){
                    linearLayout = (LinearLayout) view.findViewById(R.id.item_linear);
                    size = initTextView(position,dataList, linearLayout, type);
                    view.setTag(position);
                }
            } else {
                if (type != CommentItemBinder.TYPE_MORE_HOT){
                    if (position != (Integer)view.getTag() || dataList.getItem(position).getUpdate() == GlobalConfig.DATA_UPDATE || type == CommentItemBinder.TYPE_NEWS) {
                        linearLayout = (LinearLayout) view.findViewById(R.id.item_linear);
                        linearLayout.setFocusable(true);
                        linearLayout.removeAllViews();
                        size = initTextView(position,dataList, linearLayout, type);
                    }
                }
            }
            // 设置用户头像
            ImageView imgUser = (ImageView) view.findViewById(R.id.img_item);
            String imgUrl = dataList.getItem(position).getUserAvatar();
            if (imgUrl == null || "".equals(imgUrl)) {
                if (userService.isLogin() && userService.getUserInfo().getAvatar().equals(dataList.getItem(position).getUserAvatar())) {
                    String userAvatar = userService.getUserInfo().getAvatar();
                    if (userAvatar == null || "".equals(userAvatar)) {
                        imgUser.setImageResource(R.drawable.head);
                    } else {
                        Bitmap bitmap = getBitmapIfFileExist(userAvatar);
                        if (bitmap == null)
                            imageService.bind(userAvatar, imgUser);
                        else
                            imgUser.setImageBitmap(bitmap);
                    }
                } else {
                    imgUser.setImageResource(R.drawable.head);
                }
            } else {
                Bitmap bitmap = getBitmapIfFileExist(imgUrl);
                if (bitmap == null)
                    imageService.bind(imgUrl, imgUser);
                else
                    imgUser.setImageBitmap(bitmap);
            }




            // 设置用户昵称
            TextView tvUserName = (TextView) view.findViewById(R.id.tv_username);
            if (type != CommentItemBinder.TYPE_MORE_HOT){
                replyFirst(position, tvUserName, dataList, type, false, false);
            }
            if (dataList.getItem(position).getUserName() == null ||
                    "".equalsIgnoreCase(dataList.getItem(position).getUserName())) {
                tvUserName.setText(dataList.getItem(position).getUserId());
            } else {
                tvUserName.setText(dataList.getItem(position).getUserName());
            }

            // 设置发表评论时间
            TextView tvUpdate = (TextView) view.findViewById(R.id.tv_time);
            tvUpdate.setText(dataList.getItem(position).getLastTime());
            if (type != CommentItemBinder.TYPE_MORE_HOT){
                replyFirst(position, tvUpdate, dataList, type, false, false);
            }
            // 设置评论内容
            TextView tvContent = (TextView) view.findViewById(R.id.tv_content);
            tvContent.setText(dataList.getItem(position).getContent());
            if (type != CommentItemBinder.TYPE_MORE_HOT){
                replyFirst(position, tvContent, dataList, type, false, false);
            }
            // 点赞View
            CommentUpView myUpView = (CommentUpView) view.findViewById(R.id.item_myupview);
            final int dingNumber; // 点赞数量
            if (type == CommentItemBinder.TYPE_MORE_HOT){
                dingNumber = commentService.getMoreHotCommentList().getItem(position).getDingNumber();
            } else if (type == CommentItemBinder.TYPE_HOT){
                dingNumber = commentService.getAllHotCommentList().getItem(position).getDingNumber();
            } else {
                dingNumber = commentService.getAllNewsCommentList().getItem(position).getDingNumber();
            }

            final String path = dataList.getItem(position).getPath();
            JSONObject jsonObject = dh.getApproval(path.substring(2, path.length()));
            final boolean isDing; // 是否点过赞
            try {
                if (jsonObject == null) {
                    isDing = false;
                } else {
                    isDing = jsonObject.getBoolean("like");
                }
                myUpView.setDing(isDing);
                myUpView.setDingNum(dingNumber);
                myUpView.setOnUpListener(new CommentUpView.OnUpListener() {
                    @Override
                    public void onClick() {
                        if (!isDing) {
                            dataList.getItem(position).setIsDing(true);
                            dataList.getItem(position).setDingNumber(dingNumber + 1);
                            try {
                                JSONObject jo = new JSONObject();
                                CommentUserInfo userInfo = userService.getUserInfo();
                                if (userInfo == null) {
                                    jo.put("userPath", "00000000");
                                    jo.put("like", true);
                                } else {
                                    jo.put("userPath", path.substring(2, path.length()));
                                    jo.put("like", true);
                                }
                                Log.i("oye", "jo--" + jo.toString());
                                dh.setApproval(path.substring(2, path.length()), jo);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (i_id == nid || i_id.equals(nid)){
                                remoteService.doApproval("", path);
                            } else {
                                remoteService.doApproval(dataList.getItem(position).getIId(), "");
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Log.i("oye", "approval-error:" + e.getMessage());
            }

            if (type != CommentItemBinder.TYPE_MORE_HOT){
                // 展开更多回复
                final TextView btn = (TextView) view.findViewById(R.id.btn_add);
                boolean isClick;
                if (type == CommentItemBinder.TYPE_HOT){
                    isClick = dataList.getItem(position).isIsClick();
                } else {
                    isClick = dataList.getItem(position).isIsClick();
                }
                if (isClick) {
                    btn.setVisibility(View.GONE);
                    for (int i = 0; i < size; i++) {
                        Spanned text = null;
                        TextView tv = (TextView) linearLayout.getChildAt(i);
                        String replyPath = commentService.getAllReplyList(nid,position, type).getItem(i).getPath();
                        if (userService.isLogin()) {
                            if (replyPath == null || "".equals(replyPath)){
                                text = getReplyTextLocal(position, i, type);
                            } else {
                                text = getReplyText(position, i, replyPath, type);
                            }
                        } else {
                            text = commentService.getAllReplyList(nid,position, type).getItem(i)
                                    .getFullText("", "",replyPath);
                        }
                        tv.setText(text);
                        tv.setVisibility(View.VISIBLE);
                    }
                } else {
                    btn.setVisibility(View.VISIBLE);
                    btn.setText(App.get().getResources().getString(R.string.comment_reply_more));
                    for (int i = 0; i < size; i++) {
                        TextView tv = (TextView) linearLayout.getChildAt(i);
                        Spanned text = null;
                        String replyPath = commentService.getAllReplyList(nid,position, type).getItem(i).getPath();
                        if (replyPath == null || "".equals(replyPath)){
                            replyPath = path;
                        }
                        if (userService.isLogin()) {
                            if (replyPath == null || "".equals(replyPath)){
                                text = getReplyTextLocal(position, i, type);
                            } else {
                                text = getReplyText(position, i, replyPath, type);
                            }
                        } else {
                            text = commentService.getAllReplyList(nid,position, type).getItem(i)
                                    .getFullText("", "", replyPath);
                        }
                        tv.setVisibility(View.GONE);
                        tv.setText(text);
                        if (i < 4) {
                            tv.setVisibility(View.VISIBLE);
                        }
                    }
                }

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (type == CommentItemBinder.TYPE_HOT) {
                            clickToAll(btn,type,position);
//                            hotNotifyDataSetChanged();
                        } else {
                            clickToAll(btn, type, position);
//                            newsNotifyDataSetChanged();
                        }
                    }
                });
            }
            this.bind(view, position, container);
            return view;
        }
    }

    private void replyFirst(final int position, TextView tv, final IDataList<CommentEntity> dataList, final int type, final boolean isComment, final boolean isReply) {
        tv.setTag(position);
        tv.setClickable(true);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                path = dataList.getItem((Integer) v.getTag()).getPath();
                Bundle bundle = new Bundle();
                bundle.putString("hint", App.get().getResources().getString(R.string.comment_reply));
                bundle.putInt("position", (Integer) v.getTag());
                bundle.putInt("vId", -1);
                bundle.putInt("replyPos", GlobalConfig.REPLY_FIRST);
                bundle.putInt("type", type);
                bundle.putBoolean("isComment", isComment);
                bundle.putBoolean("isReply", isReply);
                bundle.putString("userName", dataList.getItem(position).getUserName());
                bundle.putString("userId", dataList.getItem(position).getUserId());
                bundle.putString("path",dataList.getItem(position).getPath());
                messageService.send("", new Message(MessageTopics.INIT_POPUP_WRITE_COMMENT, bundle));
            }
        });
    }

    /**
     * 评论回复字符串(本地)
     **/
    private Spanned getReplyTextLocal(int position, int i, int type) {
        return commentService.getAllReplyList(nid, position, type).getItem(i)
                .getFullText(userService.getUserInfo().getUserName(),
                        commentService.getAllReplyList(nid, position, type).getItem(i).getToName(),
                        path);
    }

    /**
     * 评论回复字符串(网络)
     **/
    private Spanned getReplyText(int position, int i, String replyPath, int type) {
        return commentService.getAllReplyList(nid, position, type).getItem(i)
                .getFullText(commentService.getAllReplyList(nid, position, type).getItem(i).getUserName(),
                        commentService.getAllReplyList(nid, position, type).getItem(i).getToName(),
                        replyPath);
    }

    /**
     * 点击展开更多
     **/
    private void clickToAll(TextView btn, int type, int position) {
        CommentEntity comm;
        btn.setVisibility(View.GONE);
        if (type == CommentItemBinder.TYPE_HOT) {
            comm = commentService.getAllHotCommentList().getItem(position);

        } else {
            comm = commentService.getAllNewsCommentList().getItem(position);
        }
        comm.setIsClick(true);
        comm.setUpdate(GlobalConfig.DATA_UPDATE);// 数据改变，重新创建TextView设置
    }

    /**
     * 动态addTextView至LinearLayout
     **/
    private int initTextView(final int position,final IDataList<CommentEntity> dataList, LinearLayout linearLayout, final int type) {
        int size;
        size = commentService.getAllReplyList(nid, position, type).getItemCount();
        for (int i = 0; i < size; i++) {
            TextView tv = new TextView(App.get().getCurrentContext());
            tv.setBackgroundResource(R.drawable.comment_ding_textview);// 为TextView设置背景选择器
            tv.setClickable(true);// 设置TextView可点击
            tv.setFocusable(true);// false为TextView释放焦点给父View、true为Textview可获取焦点
            tv.setTag(i);
            tv.setPadding(4, 4, 4, 4);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String u_name = commentService.getAllReplyList(nid, position, type).getItem((Integer) v.getTag()).getUserName();
                    path = commentService.getAllReplyList(nid, position, type).getItem((Integer) v.getTag()).getPath();
                    Bundle bundle = new Bundle();
                    bundle.putString("hint", App.get().getResources().getString(R.string.comment_reply) + ":" + u_name);
                    bundle.putInt("position", position);
                    bundle.putInt("vId", (Integer) v.getTag());
                    bundle.putInt("replyPos", GlobalConfig.REPLY_SECOND);
                    bundle.putInt("type", type);
                    bundle.putBoolean("isComment", false);
                    bundle.putBoolean("isReply", true);
                    bundle.putString("userName", dataList.getItem(position).getUserName());
                    bundle.putString("userId", dataList.getItem(position).getUserId());
                    bundle.putString("path", dataList.getItem(position).getPath());
                    messageService.send("", new Message(MessageTopics.INIT_POPUP_WRITE_COMMENT, bundle));
                }
            });
            linearLayout.addView(tv);
        }
        return size;
    }


    private Bitmap getBitmapIfFileExist(String fullUri) {
        File bmpFile = imageService.getCacheOf(fullUri);
        Bitmap bitmap = null;
        if (bmpFile.exists()) {
            bitmap = ImageHelper.fromFile(bmpFile, 0, 0);
        }
        return bitmap;
    }
}
