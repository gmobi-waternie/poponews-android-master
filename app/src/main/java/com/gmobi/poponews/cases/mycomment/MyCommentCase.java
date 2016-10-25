package com.gmobi.poponews.cases.mycomment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmobi.poponews.R;
import com.gmobi.poponews.app.GlobalConfig;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.cases.comment.CommentActivity;
import com.gmobi.poponews.cases.login.LoginActivity;
import com.gmobi.poponews.model.CommentChannelEntity;
import com.gmobi.poponews.model.CommentChannelItem;
import com.gmobi.poponews.model.CommentUserInfo;
import com.gmobi.poponews.provider.IDataProvider;
import com.gmobi.poponews.service.ICommentService;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.service.IUserService;
import com.gmobi.poponews.util.DipHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.StringUtil;
import com.gmobi.poponews.util.TimeUtil;
import com.gmobi.poponews.util.UiHelper;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.binder.IContainerBinder;
import com.momock.binder.IItemBinder;
import com.momock.binder.ItemBinder;
import com.momock.binder.ViewBinder.Setter;
import com.momock.binder.ViewBinder;
import com.momock.binder.container.ListViewBinder;
import com.momock.holder.ViewHolder;
import com.momock.message.IMessageHandler;
import com.momock.message.Message;
import com.momock.service.IImageService;
import com.momock.service.IMessageService;
import com.momock.util.ImageHelper;


import java.io.File;

import javax.inject.Inject;

/**
 * Created by vivian on 2016/6/30.
 */
public class MyCommentCase extends Case<CaseActivity> {
    @Inject
    IConfigService configService;
    @Inject
    IRemoteService remoteService;
    @Inject
    IDataService dataService;
    @Inject
    ICommentService commentService;
    @Inject
    IUserService userService;
    @Inject
    IMessageService messageService;
    @Inject
    IImageService imageService;
    @Inject
    IDataProvider dataProvider;

    ListViewBinder binderChannel;
//    IItemBinder itemBinderChannel;

    private RelativeLayout rl_article_action_bar;
    private boolean isBottom;
    private View moreView;

    public MyCommentCase(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        messageService.addHandler(MessageTopics.COMMENT_CHANNEL, new IMessageHandler() {
            @Override
            public void process(Object o, Message message) {
                if (!isAttached())
                    return;
                dataProvider.initData();
                dataProvider.getData();
            }
        });
    }

    @Override
    public void onAttach(final CaseActivity target) {

        ImageButton mycomment_back = ViewHolder.get(target, R.id.mycomment_back).getView();
        rl_article_action_bar = ViewHolder.get(target, R.id.rl_article_action_bar).getView();
        mycomment_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                target.finish();
            }
        });
        String curChannel = configService.getCurChannel();
        CommentUserInfo userInfo = userService.getUserInfo();
        if (userInfo == null){
            App.get().startActivity(LoginActivity.class);
            target.finish();
        } else {
            ItemBinder itemBinder = new ItemBinder(R.layout.mycomment_list_item,
                    new int[]{R.id.img_mycomment_item,R.id.tv_mycomment_name,R.id.tv_mycomment_time,R.id.linear_mycomment_content, R.id.tv_mycomment_content,
                            R.id.tv_mycomment_to_content,R.id.relative_mycomment_preview,R.id.tv_mycommtent_title,R.id.img_mycomment_preview},
                    new String[]{"dummy","dummy","dummy","dummy","dummy","dummy","dummy","dummy","dummy"});
            binderChannel = new ListViewBinder(itemBinder);
            Setter setter = new ViewBinder.Setter() {
                @Override
                public boolean onSet(View view, String s, int i, String s1, Object o, View parent, IContainerBinder iContainerBinder) {
                    boolean nightmode = NightModeUtil.getDayNightMode() == NightModeUtil.THEME_SUN ? false : true;
                    parent.setBackgroundColor(nightmode ? App.get().getResources().getColor(R.color.bg_black_night) :
                            App.get().getResources().getColor(R.color.bg_white));

                    if (view == null)
                        return false;
                    CommentChannelEntity item = commentService.getSizeChannelComment().getItem(i);
                    final CommentChannelItem newItems = item.getItems();
                    if (view.getId() == R.id.img_mycomment_item){
                        String avatarUrl = item.getUserAvatar();
                        if (avatarUrl == null || "".equals(avatarUrl)) {
                            ((ImageView)view).setImageResource(R.drawable.head);
                        } else {
                            Bitmap bitmap = getBitmapIfFileExist(avatarUrl);
                            if (bitmap == null)
                                imageService.bind(avatarUrl, (ImageView)view);
                            else
                                ((ImageView)view).setImageBitmap(bitmap);
                        }
                        return true;
                    } else if (view.getId() == R.id.tv_mycomment_name){
                        if (StringUtil.isReply(item.getPath()) == 1){
                            if (nightmode){
                                ((TextView)view).setText(Html.fromHtml("<font color='#587CB2'>" + item.getUserName() + "</font>"));
                                ((TextView)view).append(Html.fromHtml("<font color='#eeeeee'>" + App.get().getResources().getString(R.string.about_me_reply_me) + ":</font>"));
//                                ((TextView)view).append(Html.fromHtml("<font color='#587CB2'>"+App.get().getResources().getString(R.string.comment_mine)+":</font>"));
                            } else {
                                ((TextView)view).setText(Html.fromHtml("<font color='#587CB2'>" + item.getUserName() + "</font>"));
                                ((TextView)view).append(Html.fromHtml("<font color='#909090'>" + App.get().getResources().getString(R.string.about_me_reply_me)+":</font>"));
//                                ((TextView)view).append(Html.fromHtml("<font color='#587CB2'>"+App.get().getResources().getString(R.string.comment_mine)+":</font>"));
                            }

                        } else {
                            if (nightmode){
                                ((TextView)view).setText(Html.fromHtml("<font color='#587CB2'>" + item.getUserName() + "</font>"));
                                ((TextView)view).append(Html.fromHtml("<font color='#eeeeee'>" + App.get().getResources().getString(R.string.about_me_reply) + ":</font>"));
//                                ((TextView)view).append(Html.fromHtml("<font color='#587CB2'>"+App.get().getResources().getString(R.string.tabs_me)+":</font>"));
                            } else {
                                ((TextView)view).setText(Html.fromHtml("<font color='#587CB2'>" + item.getUserName() + "</font>"));
                                ((TextView)view).append(Html.fromHtml("<font color='#909090'>" + App.get().getResources().getString(R.string.about_me_reply) + ":</font>"));
//                                ((TextView)view).append(Html.fromHtml("<font color='#587CB2'>"+App.get().getResources().getString(R.string.tabs_me)+":</font>"));
                            }

                        }
                        return true;
                    } else if (view.getId() == R.id.linear_mycomment_content){
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                commentService.removeAllHotComment();
//                                dataService.removeAllNewsComment();
                                remoteService.startDownloadNews(newItems.getIId(),1,10);
                                Intent intent = new Intent(getAttachedObject(), CommentActivity.class);
                                intent.putExtra("nid", newItems.getIId());
                                getAttachedObject().startActivity(intent);
                            }
                        });
                        return true;
                    } else if (view.getId() == R.id.tv_mycomment_time){
                        ((TextView) view).setText(TimeUtil.getInstance().getLastTime(item.getUpdateTime()));
                        return true;
                    } else if (view.getId() == R.id.tv_mycomment_content){
                        if (item.getContent() != null){
                            if (nightmode){
                                ((TextView)view).setText(item.getContent());
                                ((TextView) view).setTextColor(App.get().getResources().getColor(R.color.bg_white));
                            } else {
                                ((TextView)view).setText(item.getContent());
                                ((TextView) view).setTextColor(App.get().getResources().getColor(R.color.bg_black_night));
                            }
                        } else {
                            ((TextView)view).setText("");
                        }
                        return true;
                    } else if (view.getId() == R.id.tv_mycomment_to_content){
                        if (item.getReply() != null){
                            ((TextView)view).setText("//" + item.getReply().getUserName() + ":" + item.getReply().getContent());
                        } else {
                            ((TextView)view).setText("");
                        }
                      return true;
                    } else if (view.getId() == R.id.relative_mycomment_preview){
                        if (nightmode){
                            view.setBackgroundDrawable(App.get().getResources().getDrawable(R.drawable.mycomment_preview_selector_night));
                        } else {
                            view.setBackgroundDrawable(App.get().getResources().getDrawable(R.drawable.mycomment_preview_selector));
                        }
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!newItems.isGo2Source()) {
                                    remoteService.getSingleNews(newItems.getIId(), null, GlobalConfig.SINGLE_COMMENT);

                                    UiHelper.openArticleFromApp(getAttachedObject(), newItems.getIId());
                                } else
                                    UiHelper.openBrowserActivity(getAttachedObject(), newItems.getIId(), "", newItems.getPSource(), newItems.getTitle(), newItems.getPDomain(), "");
                            }
                        });
                        return true;
                    } else if (view.getId() == R.id.tv_mycommtent_title){
                        if (nightmode){
                            ((TextView) view).setText(newItems.getTitle());
                            ((TextView) view).setTextColor(App.get().getResources().getColor(R.color.bg_white_night));
                        } else {
                            ((TextView) view).setText(newItems.getTitle());
                        }
                        return true;
                    } else if (view.getId() == R.id.img_mycomment_preview){
                        String previewUrl = newItems.getPreview();
                        if (previewUrl == null || "".equals(previewUrl)) {
                            ((ImageView)view).setImageResource(R.drawable.head);
                        } else {
                            Bitmap bitmap = getBitmapIfFileExist(previewUrl);
                            if (bitmap == null)
                                imageService.bind(previewUrl, (ImageView)view);
                            else
                                ((ImageView)view).setImageBitmap(bitmap);
                        }
                        return true;
                    }



                    return false;
                }
            };
            itemBinder.addSetter(setter);
            binderChannel.bind(ViewHolder.get(getAttachedObject(), R.id.lv_mycomment_list), commentService.getSizeChannelComment());
            commentService.removeAllChannelComment();
            remoteService.startDownloadChannelComment(userInfo.getUId(), curChannel);
            ListView mListView = ViewHolder.get(target, R.id.lv_mycomment_list).getView();
            moreView = target.getLayoutInflater().inflate(R.layout.footer_fresh_item_comment, null);
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, DipHelper.dip2px(81));
            moreView.setLayoutParams(lp);
            mListView.addFooterView(moreView);
            ImageView iv = ViewHolder.get(moreView, R.id.iv_loading).getView();
            iv.setBackgroundResource(R.drawable.loading_anim);
            AnimationDrawable loadAnim = (AnimationDrawable) iv.getBackground();
            loadAnim.setOneShot(false);
            loadAnim.start();
            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == SCROLL_STATE_IDLE && isBottom) {

                        dataProvider.getData();
                        moreView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (firstVisibleItem + visibleItemCount == totalItemCount) {
                        isBottom = true;
                        moreView.setVisibility(View.VISIBLE);
                    } else {
                        isBottom = false;
                    }
                }
            });
        }


    }


    private Bitmap getBitmapIfFileExist(String fullUri)
    {
        File bmpFile = imageService.getCacheOf(fullUri);
        Bitmap bitmap =null;
        if(bmpFile.exists()) {
            bitmap = ImageHelper.fromFile(bmpFile, 0, 0);
        }
        return bitmap ;
    }

    @Override
    public void onShow() {
        if (getAttachedObject() != null){
            NightModeUtil.setActionBarColor(getAttachedObject(), R.id.rl_mycomment_action_bar);
            UiHelper.setStatusBarColor(getAttachedObject(), getAttachedObject().findViewById(R.id.statusBarBackground),
                    NightModeUtil.isNightMode() ? getAttachedObject().getResources().getColor(R.color.bg_red_night) : getAttachedObject().getResources().getColor(R.color.bg_red));

        }
        super.onShow();
    }
}
