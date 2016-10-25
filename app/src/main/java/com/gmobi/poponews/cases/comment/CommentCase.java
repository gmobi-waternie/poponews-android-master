package com.gmobi.poponews.cases.comment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.cases.login.LoginActivity;
import com.gmobi.poponews.model.CommentEntity;
import com.gmobi.poponews.model.CommentUserInfo;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.outlet.CommentItemBinder;
import com.gmobi.poponews.service.ICommentService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.service.IUserService;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.DBHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.ToastUtils;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.binder.container.ListViewBinder;
import com.momock.data.DataList;
import com.momock.holder.ViewHolder;
import com.momock.message.IMessageHandler;
import com.momock.message.Message;
import com.momock.service.IImageService;
import com.momock.service.IMessageService;

import javax.inject.Inject;

/**
 * Created by nage on 2016/6/17.
 */
public class CommentCase extends Case<CaseActivity> {
    private CommentItemBinder itemBinderHot, itemBinderNews;
    private ImageButton ss_back;
    private TextView tv_hot_empty,tv_news_empty,tv_hot,main_comm_leng,ss_action_bar_title;
    private Button main_comm_send,btn_comment_send;
    private TextView main_edit_input;
    private RelativeLayout main_linearlayout_comment;
    private LinearLayout comment_linear_layout;
    private EditText edit_comment_input;
    private ScrollView comment_scroll;
    private ListViewBinder binderHot;
    private ListViewBinder binderNews;
    private int count,page;
    private View moreView;
    private boolean isBottom;
    private String nid;

    @Inject
    IMessageService messageService;
    @Inject
    IDataService dataService;
    @Inject
    IRemoteService remoteService;
    @Inject
    IUserService userService;
    @Inject
    ICommentService commentService;



    public CommentCase(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        itemBinderHot = new CommentItemBinder(getAttachedObject(),CommentItemBinder.TYPE_HOT);
        itemBinderNews = new CommentItemBinder(getAttachedObject(),CommentItemBinder.TYPE_NEWS);

        messageService.addHandler(MessageTopics.INIT_POPUP_WRITE_COMMENT, new IMessageHandler() {
            @Override
            public void process(Object o, Message message) {
                if (!isAttached())
                    return;
                Bundle bundle = (Bundle) message.getData();
                String hint = bundle.getString("hint");
                int position = bundle.getInt("position");
                int vId = bundle.getInt("vId");
                int replyPos = bundle.getInt("replyPos");
                int type = bundle.getInt("type");
                boolean isComment = bundle.getBoolean("isComment");
                boolean isReply = bundle.getBoolean("isReply");
                String userName = bundle.getString("userName");
                String uId = bundle.getString("userId");
                String uPath = bundle.getString("path");
                initPopupWindow(hint,position,vId,replyPos,type,isComment,isReply,userName,uId,uPath);
            }
        });


//        itemBinderHot = getItemBinder(true);
//        itemBinderNews = getItemBinder(false);
        messageService.addHandler(MessageTopics.COMMENT_HOTS, new IMessageHandler() {
            @Override
            public void process(Object o, Message message) {
                if (commentService.getAllHotCommentList().getItemCount() == 0) {
                    if (tv_hot_empty != null){
                        tv_hot_empty.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (tv_hot_empty != null){
                        tv_hot_empty.setVisibility(View.GONE);
                    }
                }

            }
        });

        messageService.addHandler(MessageTopics.COMMENT_NEWS, new IMessageHandler() {
            @Override
            public void process(Object o, Message message) {
                if (!isAttached())
                    return;
                if (commentService.getAllNewsCommentList().getItemCount() == 0){
                    if (tv_news_empty != null){
                        tv_news_empty.setVisibility(View.VISIBLE);
                    }
                    count = 0;
                } else {
                    count = (int) message.getData();
                    if (tv_news_empty != null){
                        tv_news_empty.setVisibility(View.GONE);
                    }
                    if (ss_action_bar_title != null){
                        if (commentService.getAllNewsCommentList().getItemCount() < count){
                            ss_action_bar_title.setText(App.get().getResources().getString(R.string.comment_write) + "(" + count + ")");
                        } else {
                            ss_action_bar_title.setText(App.get().getResources().getString(R.string.comment_write) + "(" + commentService.getAllNewsCommentList().getItemCount() + ")");
                        }
                    }
                    if (main_comm_leng != null){
                        if (commentService.getAllNewsCommentList().getItemCount() < count){
                            main_comm_leng.setText(count + "");
                        } else {
                            main_comm_leng.setText(commentService.getAllNewsCommentList().getItemCount() + "");
                        }
                    }
                }
                newsNotifyDataSetChanged();

            }
        });
    }

    @Override
    public void onAttach(final CaseActivity target) {
        binderHot = new ListViewBinder(itemBinderHot.build());
        binderNews = new ListViewBinder(itemBinderNews.build());


        binderHot.bind(ViewHolder.get(target, R.id.comment_hot_listview), commentService.getAllHotCommentList());
        // 设置listview不可滑动
        binderHot.getContainerView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        binderNews.bind(ViewHolder.get(target, R.id.comment_listview), commentService.getAllNewsCommentList());
        binderNews.getContainerView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        Intent intent = target.getIntent();
        nid = intent.getStringExtra("nid");




        ss_back = (ImageButton) target.findViewById(R.id.comment_back);
        main_linearlayout_comment = (RelativeLayout) target.findViewById(R.id.main_linearlayout_comment);
        comment_linear_layout = (LinearLayout) target.findViewById(R.id.comment_linear_layout);
        edit_comment_input = (EditText) target.findViewById(R.id.edit_comment_input);
        btn_comment_send = (Button) target.findViewById(R.id.btn_comment_send);

        main_comm_leng = (TextView) target.findViewById(R.id.main_comm_leng); // 显示评论总数
        ss_action_bar_title = (TextView) target.findViewById(R.id.ss_action_bar_title); // toolbar显示评论总数
        tv_hot = (TextView) target.findViewById(R.id.tv_hot);
        tv_hot_empty = (TextView) target.findViewById(R.id.tv_hot_empty);
        tv_news_empty = (TextView) target.findViewById(R.id.tv_news_empty);
        main_comm_send = (Button) target.findViewById(R.id.main_btn_send);
        main_edit_input = (TextView) target.findViewById(R.id.main_edit_input);
        comment_scroll = (ScrollView) target.findViewById(R.id.comment_scroll);
        comment_scroll.smoothScrollTo(0, 0);
        ss_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                target.finish();
                target.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });


//        addComment("570df86e03043016480f5560", target);
        ss_action_bar_title.setText(App.get().getResources().getString(R.string.comment_write) + "(" + commentService.getAllNewsCommentList().getItemCount() + ")");
        main_comm_leng.setText(commentService.getAllNewsCommentList().getItemCount() + "");
//        addComment(nid, target);
        comment_scroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        if (v.getScaleY() <= 0) {
                            Log.i("oye", "top");
                        } else if (comment_scroll.getChildAt(0).getMeasuredHeight() <= v.getHeight() + v.getScrollY()) {
                            Log.i("oye", "bottom");
                            if (!isBottom) {
//                                moreView = target.getLayoutInflater().inflate(R.layout.footer_fresh_item_comment, null);
//                                AbsListView.LayoutParams lp = new AbsListView.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 70);
//                                moreView.setLayoutParams(lp);
                                if (binderNews != null) {
//                                    ((ListView) binderNews.getContainerView()).addFooterView(moreView);
//                                    ImageView iv = ViewHolder.get(moreView, R.id.iv_loading).getView();
//                                    iv.setBackgroundResource(R.drawable.loading_anim);
//                                    AnimationDrawable loadAnim = (AnimationDrawable) iv.getBackground();
//                                    loadAnim.setOneShot(false);
//                                    loadAnim.start();
//                                    comment_scroll.smoothScrollTo(0, comment_scroll.getChildAt(0).getMeasuredHeight() +
//                                            moreView.getHeight() + main_linearlayout_comment.getHeight());
//                                        comment_scroll.fullScroll(ScrollView.FOCUS_DOWN);
                                    isBottom = true;
                                    page++;
                                    remoteService.startDownloadNews(nid, page, 10);
                                }

                            }

                        }
                        break;
                }
                return false;
            }
        });
        edit_comment_input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (TextUtils.isEmpty(edit_comment_input.getText())) {
                        main_linearlayout_comment.setVisibility(View.VISIBLE);
                        comment_linear_layout.setVisibility(View.GONE);
                    }
                }

            }
        });
        main_edit_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopupWindow(App.get().getResources().getString(R.string.comment_write), -1, -1, -1, CommentItemBinder.TYPE_NEWS, true, false,null,null,null);
            }
        });

        page = 1;
        commentService.removeAllNewsComment();
        NewsItem item = dataService.getNewsById(nid);
        if (item != null){
            item.setCommentCount(0);
        }
        remoteService.startDownloadNews(nid, page, 10);

        if (commentService.getAllHotCommentList().getItemCount() == 0) {
            remoteService.startDownloadHotNews(nid, 5);
            tv_hot_empty.setVisibility(View.VISIBLE);
        } else {
            tv_hot_empty.setVisibility(View.GONE);
        }
        if (commentService.getAllNewsCommentList().getItemCount() == 0){
            tv_news_empty.setVisibility(View.VISIBLE);
        } else {
            tv_news_empty.setVisibility(View.GONE);
        }
        newsNotifyDataSetChanged();
    }



    /**
     * 弹出popupwindow编辑
     * @param hint edittext提示的hint
     * @param position 最外层ListView点击item项的下标
     * @param vId  回复项的下标
     * @param replyPos 数据是否是首次回复
     */
    String addCommentPath = "";

    public void initPopupWindow(String hint, final int position,final int vId,final int replyPos,
                                final int type, final boolean isComment, final boolean isReply,
                                final String userName,final String uId,final String uPath) {
        main_linearlayout_comment.setVisibility(View.GONE);
        comment_linear_layout.setVisibility(View.VISIBLE);

        edit_comment_input.setHint(hint);
        edit_comment_input.setFocusableInTouchMode(true);
        edit_comment_input.requestFocus();
        edit_comment_input.setFocusable(true);
        edit_comment_input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(255)});
        showSoftKeyboard(App.get().getCurrentContext(), edit_comment_input);
        btn_comment_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {

                    if (TextUtils.isEmpty(edit_comment_input.getText().toString().trim())) {
                        if (isComment){
                            ToastUtils.showShortToast(App.get().getResources().getString(R.string.comment_write_notnull));
                        } else {
                            ToastUtils.showShortToast(App.get().getResources().getString(R.string.comment_reply_notnull));
                        }
                        return;
                    }
                    if (!userService.isLogin()) {
                        App.get().startActivity(LoginActivity.class);
                    } else {
                        NewsItem ni = dataService.getNewsById(nid);
                        if(ni!=null)
                            AnalysisUtil.recordArticleComment(ni.get_id(),ni.getTitle(),ni.getType());


                        String edString = edit_comment_input.getText().toString();
                        CommentUserInfo userInfo = userService.getUserInfo();
                        if (isComment) { // 评论
                            commentService.addNewsComment(nid, edString, userInfo, false);
                            newsNotifyDataSetChanged();
                            int listHeight = binderHot.getContainerView().getHeight();
                            int tvHeight = tv_hot.getHeight();
                            comment_scroll.smoothScrollTo(0, listHeight + tvHeight);
                            edit_comment_input.setHint(App.get().getResources().getString(R.string.comment_write));
                        } else {
                            commentService.replyComment(nid, edString, userInfo, type, position, replyPos, vId, userName, isReply, uId, uPath, addCommentPath);
                            hotNotifyDataSetChanged();
                            if (binderNews.getAdapter() != null) {
                                binderNews.getAdapter().notifyDataSetChanged();
                            }
                            edit_comment_input.setHint(App.get().getResources().getString(R.string.comment_reply));
                        }
                        hideSoftKeyboard(getAttachedObject(), edit_comment_input);
                        edit_comment_input.setText("");
                        main_linearlayout_comment.setVisibility(View.VISIBLE);
                        comment_linear_layout.setVisibility(View.GONE);



                    }
                }
            }
        });

    }




    public void hotNotifyDataSetChanged(){
        if (binderHot.getAdapter() != null){
            binderHot.getAdapter().notifyDataSetChanged();
        }
    }

    public void newsNotifyDataSetChanged(){
        if (binderNews != null){
            if (binderNews.getAdapter() != null) {
                binderNews.getAdapter().notifyDataSetChanged();
            }
            if (commentService.getAllNewsCommentList().getItemCount() != 0){
                tv_news_empty.setVisibility(View.GONE);
            }
            isBottom = false;
            if (moreView != null){
                if (((ListView) binderNews.getContainerView()).getFooterViewsCount() != 0){
//                    ((ListView) binderNews.getContainerView()).removeFooterView(moreView);
                }
            }
        }

    }

    @Override
    public void onShow() {
        if (getAttachedObject() != null){
            NightModeUtil.setActionBarColor(getAttachedObject(), R.id.rl_comment_action_bar);
            UiHelper.setStatusBarColor(getAttachedObject(), getAttachedObject().findViewById(R.id.statusBarBackground),
                    NightModeUtil.isNightMode() ? getAttachedObject().getResources().getColor(R.color.bg_red_night) : getAttachedObject().getResources().getColor(R.color.bg_red));
            ViewHolder.get(getAttachedObject(),R.id.comment_scroll).getView().setBackgroundColor(NightModeUtil.isNightMode() ? App.get().getResources().getColor(R.color.bg_black) :
                    App.get().getResources().getColor(R.color.bg_white));
            ((TextView)ViewHolder.get(getAttachedObject(),R.id.tv_hot_empty).getView()).setTextColor(NightModeUtil.isNightMode() ? App.get().getResources().getColor(R.color.bg_white) :
                    App.get().getResources().getColor(R.color.bg_black));
            ((TextView)ViewHolder.get(getAttachedObject(), R.id.tv_news_empty).getView()).setTextColor(NightModeUtil.isNightMode() ? App.get().getResources().getColor(R.color.bg_white) :
                    App.get().getResources().getColor(R.color.bg_black));
            ViewHolder.get(getAttachedObject(), R.id.tv_hot).getView().setBackgroundDrawable(NightModeUtil.isNightMode() ?
                    App.get().getResources().getDrawable(R.drawable.comment_text_shape_night) : App.get().getResources().getDrawable(R.drawable.comment_text_shape));
            ViewHolder.get(getAttachedObject(), R.id.tv_news).getView().setBackgroundDrawable(NightModeUtil.isNightMode() ?
                    App.get().getResources().getDrawable(R.drawable.comment_text_shape_night) : App.get().getResources().getDrawable(R.drawable.comment_text_shape));
            ViewHolder.get(getAttachedObject(),R.id.main_linearlayout_comment).getView().setBackgroundColor(NightModeUtil.isNightMode() ? App.get().getResources().getColor(R.color.about_me_night) :
                    App.get().getResources().getColor(R.color.comment_write_bg));
            ViewHolder.get(getAttachedObject(),R.id.comment_linear_layout).getView().setBackgroundColor(NightModeUtil.isNightMode() ? App.get().getResources().getColor(R.color.about_me_night) :
                    App.get().getResources().getColor(R.color.comment_write_bg));

        }
        super.onShow();
    }

    /**
     * 显示软键盘
     */
    public void showSoftKeyboard(Context context, EditText et) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 隐藏软键盘
     */
    public void hideSoftKeyboard(Context context, View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

}
