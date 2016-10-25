package com.gmobi.poponews.cases.article;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gmobi.poponews.R;
import com.gmobi.poponews.activity.SplashActivity;
import com.gmobi.poponews.app.CacheNames;
import com.gmobi.poponews.app.DataNames;
import com.gmobi.poponews.app.IntentNames;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.cases.comment.CommentActivity;
import com.gmobi.poponews.cases.login.LoginActivity;
import com.gmobi.poponews.javascript.ExecJsApi;
import com.gmobi.poponews.model.CommentEntity;
import com.gmobi.poponews.model.CommentUserInfo;
import com.gmobi.poponews.model.EmoVote;
import com.gmobi.poponews.model.NewsImage;
import com.gmobi.poponews.model.NewsItem;
import com.gmobi.poponews.outlet.CommentItemBinder;
import com.gmobi.poponews.service.ICommentService;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IDataService;
import com.gmobi.poponews.service.INewsCacheService;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.service.IReportService;
import com.gmobi.poponews.service.IShareService;
import com.gmobi.poponews.service.IUserService;
import com.gmobi.poponews.share.IShare;
import com.gmobi.poponews.util.AdHelper;
import com.gmobi.poponews.util.AddCommentUtils;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.DBHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.PathUtils;
import com.gmobi.poponews.util.UiHelper;
import com.gmobi.poponews.widget.CommentUpView;
import com.gmobi.poponews.widget.EmoMenu;
import com.gmobi.poponews.widget.EmoMenu.IEmoListener;
import com.gmobi.poponews.widget.MenuSetting;
import com.gmobi.poponews.widget.TouchImageView;
import com.momock.app.App;
import com.momock.app.Case;
import com.momock.app.CaseActivity;
import com.momock.binder.IContainerBinder;
import com.momock.binder.IItemBinder;
import com.momock.binder.ViewBinder;
import com.momock.binder.container.ListViewBinder;
import com.momock.binder.container.ViewPagerBinder;
import com.momock.data.DataList;
import com.momock.data.DataNodeView;
import com.momock.data.IDataList;
import com.momock.data.IDataNode;
import com.momock.event.IEventHandler;
import com.momock.event.ItemEventArgs;
import com.momock.holder.ImageHolder;
import com.momock.holder.ViewHolder;
import com.momock.message.IMessageHandler;
import com.momock.message.Message;
import com.momock.service.IAsyncTaskService;
import com.momock.service.ICacheService;
import com.momock.service.IImageService;
import com.momock.service.IMessageService;
import com.momock.service.IUITaskService;
import com.momock.util.DataHelper;
import com.momock.util.FileHelper;
import com.momock.util.ImageHelper;
import com.momock.util.Logger;
import com.momock.widget.IIndexIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

@SuppressLint("ResourceAsColor")
public class ArticleCase extends Case<CaseActivity> implements View.OnClickListener {
    public final static String SHARE_TEMPLATE = "http://poponews.com/share/{aid}";

    public ArticleCase(String name) {
        super(name);
    }


    ViewPagerBinder binder;
    CommentItemBinder hotBinder;
    SparseArray<Integer> emoVoteData = new SparseArray<Integer>();

    @Inject
    Resources resources;
    @Inject
    IUITaskService uiTaskService;
    @Inject
    IImageService imageService;
    @Inject
    IMessageService messageService;
    @Inject
    IDataService dataService;
    @Inject
    IConfigService configService;
    @Inject
    ICacheService cacheService;
    @Inject
    IRemoteService remoteService;
    @Inject
    INewsCacheService newsCacheService;
    @Inject
    IReportService reportService;
    @Inject
    IAsyncTaskService asyncTaskService;
    @Inject
    IUserService userService;
    @Inject
    ICommentService commentService;

    private String curNid,input_comment,addCommentPath;


    private int fontStatus = 1;
    private DBHelper dh;
    private int from;


    @Override
    public void onCreate() {

        hotBinder = new CommentItemBinder(getAttachedObject(),CommentItemBinder.TYPE_MORE_HOT);

        //limitString = getImageLimit();
        messageService.addHandler(MessageTopics.SHARE_MORE, new IMessageHandler() {
            @Override
            public void process(Object o, Message message) {
                Map<String, String> data = (Map<String, String>) message.getData();
                String title = data.get("title");
                String webUrl = data.get("weburl");
                String imageuri = data.get("imageuri");

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, App.get().getResources().getText(R.string.share_subject));
                intent.putExtra(Intent.EXTRA_TEXT, App.get().getResources().getText(R.string.share_subject) + ":" + title + "\n" + webUrl); // 分享的内容

                App.get().startActivity(Intent.createChooser(intent, App.get().getResources().getText(R.string.share_title)));
            }
        });


        messageService.addHandler(MessageTopics.NEWS_CONTENT_LOADED, new IMessageHandler() {

            @Override
            public void process(Object sender, Message msg) {
                //CacheContent下面保存了2个Cache文件
                String nid = (String) msg.getData();
                NewsItem item = dataService.getNewsById(nid);
                getDataSet().setData(DataNames.CURRENT_NEWS, item);
                File temp = cacheService.getCacheOf(CacheNames.NEWS_CONTENT_CACHEDIR, CacheNames.TEMPLATE_REGULAR_NAME);
                File body = cacheService.getCacheOf(CacheNames.NEWS_CONTENT_CACHEDIR, configService.getBaseUrl() + item.getBody());
                String templateHtml = "";
                try {
                    templateHtml = FileHelper.readText(temp, "UTF-8");
                    templateHtml = templateHtml.replace("{{content}}", FileHelper.readText(body));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                newsCacheService.setNewsContentCache(item.get_id(), templateHtml);


                uiTaskService.runDelayed(new Runnable() {

                    @Override
                    public void run() {
                        messageService.send(this, MessageTopics.NEWS_HTML_READY);

                    }
                }, 500);


            }

        });

        messageService.addHandler(MessageTopics.NEWS_CONTENT_LOADED_COMMENT, new IMessageHandler() {

            @Override
            public void process(Object sender, Message msg) {
                //CacheContent下面保存了2个Cache文件
                NewsItem item = (NewsItem) msg.getData();
                doRemoteActions(item,true);
                getDataSet().setData(DataNames.CURRENT_NEWS, item);
                CaseActivity target = getAttachedObject();
                if (target == null)
                    return;
                ViewPager vp = ViewHolder.get(target, R.id.news_img_vp).getView();
                WebView wv = ViewHolder.get(target, R.id.wv_newscontent).getView();
                WebView orgwv = ViewHolder.get(target, R.id.wv_orgcontent).getView();
                RelativeLayout vp_rl = ViewHolder.get(target, R.id.image_newscontent).getView();

                if (vp == null || wv == null || orgwv == null || vp_rl == null)
                    return;

                if (item != null) {
                    if (item.getType().equals(NewsItem.NEWS_TYPE_IMAGE) || item.getType().equals(NewsItem.NEWS_TYPE_FEATURED)) {
                        vp_rl.setVisibility(View.VISIBLE);
                        wv.setVisibility(View.GONE);
                        orgwv.setVisibility(View.GONE);
                        ShowLoading(target, false);
                        binder.bind(vp, item.getImgs(), (IIndexIndicator) ViewHolder.get(target, R.id.ciiCarousel).getView(), false);
                    } else {

                        wv.setVisibility(View.VISIBLE);
                        orgwv.setVisibility(View.VISIBLE);
                        vp_rl.setVisibility(View.GONE);
                    }
                }


//                hotBinder.addSetter(hotSetter);
//                binderMoreHot = new ListViewBinder(hotBinder);
//                binderMoreHot.bind(ViewHolder.get(target, R.id.listview_more_comment), dataService.getMoreHotCommentList());
//                binderMoreHot.getItemClickedEvent().addEventHandler(new IEventHandler<ItemEventArgs>() {
//                    @Override
//                    public void process(Object o, ItemEventArgs itemEventArgs) {
//                        Intent intent = new Intent(getAttachedObject(), CommentActivity.class);
//                        intent.putExtra("nid", dataService.getCurNid());
//                        getAttachedObject().startActivity(intent);
//                    }
//                });

            }

        });

		/*
        messageService.addHandler(MessageTopics.INTERSTITIAL_AD_CLOSE, new IMessageHandler() {
			@Override
			public void process(Object o, Message message) {
				AdHelper.closeInterstitialAd();
				UiHelper.stopCloseAdTimer();
				adStatus = AD_IS_SHOWED;
			}
		});*/
        messageService.addHandler(MessageTopics.COMMENT_HOTS, new IMessageHandler() {
            @Override
            public void process(Object o, Message message) {
                if (!isAttached())
                    return;
                if (commentService.getMoreHotCommentList().getItemCount() != 0) {
                    if (article_linear != null) {
                        article_linear.setVisibility(View.VISIBLE);
                    }
                    if (listview_more_comment != null) {
                        listview_more_comment.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (article_linear != null) {
                        article_linear.setVisibility(View.GONE);
                    }
                }

            }
        });



        messageService.addHandler(MessageTopics.NEWS_HTML_READY, new IMessageHandler() {

            @Override
            public void process(Object sender, Message msg) {
                if (!isAttached())
                    return;
                CaseActivity target = getAttachedObject();
                ShowLoading(target, false);
                String nid = dataService.getCurNid();
                NewsItem ni = dataService.getNewsById(nid);

                final String mimeType = "text/html";
                final String encoding = "utf-8";
                final String html = FileHelper.getCacheOf(target, CacheNames.NEWS_CONTENT_CACHEDIR, dataService.getCurNid() + ".html").getAbsolutePath();

                if (configService.isBannerAdEnable()) {
                    if (UiHelper.needShow(configService.getBannerFacebookAdPercent())) {
                        AdHelper.showFacebookBannerAd(getAttachedObject(), (ViewGroup) ViewHolder.get(target, R.id.fl_ad_content).getView(), ni);
                    } else {
                        AdHelper.initBannerAd(getAttachedObject(), (ViewGroup) ViewHolder.get(target, R.id.fl_ad_content).getView(), ni.getPdomain(), ni);
                    }
                }


                AnalysisUtil.recordArticleRead(ni.get_id(), ni.getTitle(), ni.getType());

                WebView wv = ViewHolder.get(target, R.id.wv_newscontent).getView();


                ExecJsApi execJsApi = new ExecJsApi(wv, target);
                wv.addJavascriptInterface(execJsApi, "_JsToNativePluginApi");


                WebSettings webSettings = wv.getSettings();
                webSettings.setJavaScriptEnabled(true);
                //webSettings.setLoadWithOverviewMode(true);
                //webSettings.setBlockNetworkImage(false);


                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                wv.setLayoutParams(params);

                wv.loadUrl("file://" + html + "?nid=" + nid);

				/*
				Logger.error("Begin to load org source");
				WebView orgwv = ViewHolder.get(target, R.id.wv_orgcontent).getView();
				initWebSettings(orgwv);
				orgwv.loadUrl(ni.getSource());

				orgwv.setWebViewClient(new WebViewClient() {
					@Override
					public void onPageStarted(WebView view, String url, Bitmap favicon) {
						Logger.error("Loading orginal source in onPageStarted:"+url);
						super.onPageStarted(view, url, favicon);
					}

					@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url) {

							Logger.error("Loading orginal source:"+url);
							return super.shouldOverrideUrlLoading(view, url);

					}

					@Override
					public void onPageFinished(WebView view, String url) {
						Logger.error("Loading orginal source in onPageFinished:"+url);
						super.onPageFinished(view, url);
					}

					@Override
					public void onReceivedError(WebView view, int errorCode,
												String description, String failingUrl) {

						Logger.error("Loading orginal source in onReceivedError");
						super.onReceivedError(view, errorCode, description, failingUrl);
					}

					@Override
					public void onReceivedSslError(WebView view,
												   SslErrorHandler handler, SslError error) {
						Logger.error("Loading orginal source in onReceivedSslError");
						super.onReceivedSslError(view, handler, error);
					}
				});
				*/

                remoteService.startDownloadHotNews(curNid, 5);
            }
        });

        messageService.addHandler(MessageTopics.SHOW_CURRENT_NEWS, new IMessageHandler() {

            @Override
            public void process(Object sender, Message msg) {
                run();
            }

        });


        IItemBinder ivbImageContent = new IItemBinder() {

            @Override
            public View onCreateItemView(View convertView, int index,
                                         IContainerBinder container) {
                NewsItem i = getDataSet().getData(DataNames.CURRENT_NEWS);


                if (convertView == null) {
                    ViewHolder vh = ViewHolder.create(getAttachedObject(),
                            R.layout.news_image_article);
                    convertView = vh.getView();

                }
                convertView.setTag(index);
                final TouchImageView iv = ViewHolder.get(convertView, R.id.image_news_photo).getView();


                if (i.getImgs() == null || i.getImgs().getItemCount() <= index)
                    return convertView;
                final NewsImage img = i.getImgs().getItem(index);

                iv.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final File f = imageService.getCacheOf(configService.getBaseImageUrl() + img.getFile());
                        final File newF = new File(getAppDir(getAttachedObject() == null ? App.get() : getAttachedObject(), "Download"), img.getFile() + ".png");


                        if (f.exists()) {
                            asyncTaskService.run(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        if (newF.exists())
                                            FileHelper.delete(newF);

                                        FileHelper.copy(f, newF);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                            startScaleAnimation(iv);
                            Toast.makeText(getAttachedObject() == null ? App.get() : getAttachedObject(), resources.getString(R.string.pic_saved) + newF.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                        }

                        return true;
                    }
                });
                iv.setTag(index);

                Bitmap bmp = imageService.loadBitmap(configService.getBaseImageUrl() + img.getFile());
                iv.setImageBitmap(bmp);
                if (bmp == null) {
                    iv.setImageBitmap(ImageHolder.get(R.drawable.news_nonpicture).getAsBitmap());
                    imageService.bind(configService.getBaseImageUrl() + img.getFile(), container, index);
                }
                TextView tv = ViewHolder.get(convertView, R.id.image_news_des).getView();
                tv.setText(img.getDesc() + "(" + resources.getString(R.string.pic_can_save) + ")");
                tv.setMovementMethod(ScrollingMovementMethod.getInstance());
                tv.scrollTo(0, 0);
                int fontSize = configService.getFontSize();
                if (fontSize == 0)
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                else if (fontSize == 1)
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                else if (fontSize == 2)
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

                return convertView;
            }

        };

        binder = new ViewPagerBinder(ivbImageContent);
        binder.getItemSelectedEvent().addEventHandler(new IEventHandler<ItemEventArgs>() {
            @Override
            public void process(Object o, ItemEventArgs itemEventArgs) {
                ViewPager vp = (ViewPager) itemEventArgs.getView();
                ViewGroup itemView = (ViewGroup) vp.findViewWithTag(itemEventArgs.getIndex());
                TouchImageView tiv = (TouchImageView) itemView.getChildAt(0);
                tiv.resetZoom();
            }
        });


    }


//	@Override
//	public void run(Object... args) {
//		App.get().startActivity(ArticleActivity.class);
//	}

    private void ActionShareNews(String title, String id, String mmUrl, ArrayList<String> mms) {
//		NewsItem item = getDataSet().getData(DataNames.CURRENT_NEWS);
//		if(item==null)
//			item  = dataService.getNewsById(id);
//
//		if(item!=null)
//			AnalysisUtil.recordArticleShare(item.get_id(), item.getTitle(), item.getType(), "more");
//
//
//
//
//
//		Intent intent = new Intent(Intent.ACTION_SEND);
//		intent.setType("text/plain");
//		intent.putExtra(Intent.EXTRA_SUBJECT, getAttachedObject().getResources().getText(R.string.share_subject));
//		intent.putExtra(Intent.EXTRA_TEXT, getAttachedObject().getResources().getText(R.string.share_subject) + ":" + title + "\n" + SHARE_TEMPLATE.replace("{aid}", id)); // 分享的内容
//		getAttachedObject().startActivity(Intent.createChooser(intent, getAttachedObject().getResources().getText(R.string.share_title)));
//
/*
		//shareToQzone(title,url,mms);
		//new WbShare().share(title, url, mmUrl);
		//new FbShare().share(title, url, mmUrl);

		Logger.debug("[SHARE]：" + mms.get(0));


		Intent in = new Intent(getAttachedObject(), ShareActivity.class);
		Bundle b = new Bundle();
		b.putString(IntentNames.INTENT_EXTRA_TITLE,title);
		b.putString(IntentNames.INTENT_EXTRA_URL,SHARE_TEMPLATE.replace("{aid}",id));
		b.putString(IntentNames.INTENT_EXTRA_IMAGE,
				App.get().getService(IConfigService.class).getBaseImageUrl() + mmUrl  + "." + 240 + "x" + 152+ "t5"
				);
		in.putExtras(b);
		getAttachedObject().startActivity(in);
*/

		/*

		new QqShare().share();
		*/


        View mask = ViewHolder.get(getAttachedObject(), R.id.rl_mask).getView();
        mask.setVisibility(View.VISIBLE);
        mask.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                View mask = ViewHolder.get(getAttachedObject(), R.id.rl_mask).getView();
                View share = ViewHolder.get(getAttachedObject(), R.id.rl_share_panel).getView();

                mask.setVisibility(View.INVISIBLE);
                share.setVisibility(View.INVISIBLE);

            }
        });

        startAnimation(R.id.rl_share_panel, R.anim.in_from_bottom, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }


    private void startAnimation(int viewId, int resAnim, Animation.AnimationListener al) {
        Animation anim = AnimationUtils.loadAnimation(getAttachedObject(), resAnim);
        anim.reset();
        if (null != al) {
            anim.setAnimationListener(al);
        }
        View l = ViewHolder.get(getAttachedObject(), viewId).getView();
        l.setVisibility(View.VISIBLE);
        l.clearAnimation();
        l.startAnimation(anim);
    }


	/*
	private void shareToFriend(String url,String msgTitle,String imgPath) {
        Intent intent = new Intent();
        ComponentName comp = new ComponentName("com.tencent.mm",
                        "com.tencent.mm.ui.tools.ShareImgUI");
        intent.setComponent(comp);
        intent.setAction("android.intent.action.SEND");

        if (imgPath == null || imgPath.equals("")) {
            intent.setType("text/plain"); // 纯文本
        } else {
            File f = new File(imgPath);
            if (f != null && f.exists() && f.isFile()) {
                intent.setType("image/*");
                Uri u = Uri.fromFile(f);
                intent.putExtra(Intent.EXTRA_STREAM, u);
            }
        }

        intent.putExtra(Intent.EXTRA_TEXT,getAttachedObject().getResources().getText(R.string.share_subject)+"\n"+msgTitle+"\n"+url);

        getAttachedObject().startActivity(intent);
	}
	private void shareToTimeLine(String url,String msgTitle,String imgPath) {
	        Intent intent = new Intent();
	        ComponentName comp = new ComponentName("com.tencent.mm",
	                        "com.tencent.mm.ui.tools.ShareToTimeLineUI");
	        intent.setComponent(comp);
	        intent.setAction("android.intent.action.SEND");
	        if (imgPath == null || imgPath.equals("")) {
	            intent.setType("text/plain"); // 纯文本
	        } else {
	            File f = new File(imgPath);
	            if (f != null && f.exists() && f.isFile()) {
	                intent.setType("image/*");
	                Uri u = Uri.fromFile(f);
	                intent.putExtra(Intent.EXTRA_STREAM, u);
	            }
	        }

	        intent.putExtra(Intent.EXTRA_TEXT,getAttachedObject().getResources().getText(R.string.share_subject)+"\n"+msgTitle+"\n"+url);

	        getAttachedObject().startActivity(intent);
	}

	private void ActionShareRichTextNews(String url, String msgTitle, String imgPath) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		if (imgPath == null || imgPath.equals("")) {
			intent.setType("text/plain"); // 纯文本
		} else {
			File f = new File(imgPath);
			if (f != null && f.exists() && f.isFile()) {
				intent.setType("image/png");
				Uri u = Uri.fromFile(f);
				intent.putExtra(Intent.EXTRA_STREAM, u);
			}
		}


		getAttachedObject().startActivity(Intent.createChooser(intent, getAttachedObject().getResources().getText(R.string.share_title)));

	}*/


    void ActionShowSetting() {
        View settingbtn = ViewHolder.get(getAttachedObject(), R.id.artile_action_bar_setting).getView();
        final MenuSetting menu = new MenuSetting(settingbtn, R.layout.article_setting_menu);
        menu.show();

        final ImageButton ib_small = (ImageButton) menu.findViewById(R.id.setting_font_small);
        final ImageButton ib_medium = (ImageButton) menu.findViewById(R.id.setting_font_medium);
        final ImageButton ib_large = (ImageButton) menu.findViewById(R.id.setting_font_large);

        fontStatus = configService.getFontSize();
        if (fontStatus == 0) {
            ib_small.setImageResource(R.drawable.news_more_fontsize_small_selected);
            ib_medium.setImageResource(R.drawable.news_more_fontsize_middle_normal);
            ib_large.setImageResource(R.drawable.news_more_fontsize_big_normal);
        } else if (fontStatus == 1) {
            ib_small.setImageResource(R.drawable.news_more_fontsize_small_normal);
            ib_medium.setImageResource(R.drawable.news_more_fontsize_middle_selected);
            ib_large.setImageResource(R.drawable.news_more_fontsize_big_normal);
        } else if (fontStatus == 2) {
            ib_small.setImageResource(R.drawable.news_more_fontsize_small_normal);
            ib_medium.setImageResource(R.drawable.news_more_fontsize_middle_normal);
            ib_large.setImageResource(R.drawable.news_more_fontsize_big_selected);
        }


        OnClickListener l = new OnClickListener() {

            @Override
            public void onClick(View v) {


                ib_small.setImageResource(R.drawable.news_more_fontsize_small_normal);
                ib_medium.setImageResource(R.drawable.news_more_fontsize_middle_normal);
                ib_large.setImageResource(R.drawable.news_more_fontsize_big_normal);
                if (v.getId() == R.id.setting_font_large) {
                    fontStatus = 2;
                    ((ImageButton) v).setImageResource(R.drawable.news_more_fontsize_big_selected);
                } else if (v.getId() == R.id.setting_font_medium) {
                    fontStatus = 1;
                    ((ImageButton) v).setImageResource(R.drawable.news_more_fontsize_middle_selected);
                } else if (v.getId() == R.id.setting_font_small) {
                    fontStatus = 0;
                    ((ImageButton) v).setImageResource(R.drawable.news_more_fontsize_small_selected);
                }


                configService.setFontSize(fontStatus);

                NewsItem item = dataService.getNewsById(dataService.getCurNid());


                if (item != null) {
                    String font = AnalysisUtil.FONT_MEDIUM;
                    if (fontStatus == 0)
                        font = AnalysisUtil.FONT_SMALL;
                    else if (fontStatus == 1)
                        font = AnalysisUtil.FONT_MEDIUM;
                    else if (fontStatus == 2)
                        font = AnalysisUtil.FONT_LARGE;
                    AnalysisUtil.recordArticleFont(item.get_id(), item.getTitle(), item.getType(), font);
                }

                if (item.getType().equals(NewsItem.NEWS_TYPE_IMAGE) || item.getType().equals(NewsItem.NEWS_TYPE_FEATURED)) {
                    binder.getAdapter().notifyDataSetChanged();
                } else {
                    WebView wv = ViewHolder.get(getAttachedObject(), R.id.wv_newscontent).getView();
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    wv.setLayoutParams(params);
                    if (wv != null)
                        wv.reload();
                }


            }
        };

        ib_small.setOnClickListener(l);
        ib_medium.setOnClickListener(l);
        ib_large.setOnClickListener(l);


        CheckBox cbNightMode = (CheckBox) menu.findViewById(R.id.cb_nightmode);

        if (NightModeUtil.getDayNightMode() == NightModeUtil.THEME_NIGHT)
            cbNightMode.setChecked(true);
        else
            cbNightMode.setChecked(false);

        cbNightMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    NightModeUtil.setDayNightMode(NightModeUtil.THEME_NIGHT);
                else
                    NightModeUtil.setDayNightMode(NightModeUtil.THEME_SUN);

                invalidateActivity();
            }
        });

    }

    private TextView tv_more_comment, tv_more_hot, main_comm_leng,tv_input;
    private LinearLayout article_linear,main_linear_layout;
    private RelativeLayout main_relative_layout,rl_mask;
    private EditText edit_input;
    private Button main_btn_send,btn_popup_send;
    private ListViewBinder binderMoreHot;
    private ListView listview_more_comment;

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getAttachedObject(), CommentActivity.class);
        intent.putExtra("nid", dataService.getCurNid());
        switch (v.getId()) {
            case R.id.article_main_btn_send:
                getAttachedObject().startActivity(intent);
                break;
            case R.id.article_main_tv_input:
                main_relative_layout.setVisibility(View.GONE);
                main_linear_layout.setVisibility(View.VISIBLE);
                edit_input.setHint(App.get().getResources().getString(R.string.comment_write));
                edit_input.setFocusableInTouchMode(true);
                edit_input.requestFocus();
                edit_input.setFocusable(true);
                edit_input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(255)});
                showSoftKeyboard(App.get().getCurrentContext(), edit_input);
                break;
            case R.id.btn_popup_send:
                addComment(intent);
                break;
            case R.id.tv_more_comment:
                getAttachedObject().startActivity(intent);
                break;
        }
    }

    private void addComment(Intent intent) {
        if (TextUtils.isEmpty(edit_input.getText().toString().trim())) {
            Toast.makeText(App.get().getCurrentContext(),
                    App.get().getResources().getString(R.string.comment_write_notnull), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!userService.isLogin()) {
            App.get().startActivity(LoginActivity.class);
        } else {
            NewsItem ni = dataService.getNewsById(curNid);
            if(ni!=null)
                AnalysisUtil.recordArticleComment(ni.get_id(),ni.getTitle(),ni.getType());

            String edString = edit_input.getText().toString();
            commentService.addNewsComment(curNid, edString, null, true);
            getAttachedObject().startActivity(intent);
            edit_input.setText("");
            main_relative_layout.setVisibility(View.VISIBLE);
            main_linear_layout.setVisibility(View.GONE);
        }
    }



    int commentCount;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onAttach(final CaseActivity target) {
        commentService.removeAllNewsComment();
        binderMoreHot = new ListViewBinder(hotBinder.build());

        binderMoreHot.bind(ViewHolder.get(getAttachedObject(), R.id.listview_more_comment), commentService.getMoreHotCommentList());
        binderMoreHot.getItemClickedEvent().addEventHandler(new IEventHandler<ItemEventArgs>() {
            @Override
            public void process(Object o, ItemEventArgs itemEventArgs) {
                Intent intent = new Intent(getAttachedObject(), CommentActivity.class);
                intent.putExtra("nid", dataService.getCurNid());
                getAttachedObject().startActivity(intent);
            }
        });
        main_btn_send = (Button) target.findViewById(R.id.article_main_btn_send);
        btn_popup_send = (Button) target.findViewById(R.id.btn_popup_send);
        tv_more_comment = (TextView) target.findViewById(R.id.tv_more_comment);
        main_comm_leng = (TextView) target.findViewById(R.id.main_comm_leng);
        tv_more_hot = (TextView) target.findViewById(R.id.tv_more_hot);
        article_linear = (LinearLayout) target.findViewById(R.id.article_linear);
        article_linear.setVisibility(View.INVISIBLE);
        main_relative_layout = (RelativeLayout) target.findViewById(R.id.main_relative_layout);
        rl_mask = (RelativeLayout) target.findViewById(R.id.rl_mask);
        main_linear_layout = (LinearLayout) target.findViewById(R.id.main_linear_layout);
        edit_input = (EditText) target.findViewById(R.id.edit_input);
        listview_more_comment = (ListView) target.findViewById(R.id.listview_more_comment);
        tv_input = (TextView) target.findViewById(R.id.article_main_tv_input);
        tv_more_comment.setOnClickListener(this);
        tv_input.setOnClickListener(this);
        main_btn_send.setOnClickListener(this);
        btn_popup_send.setOnClickListener(this);
        main_comm_leng.setText("0");
        edit_input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    if (TextUtils.isEmpty(edit_input.getText())) {
                        main_relative_layout.setVisibility(View.VISIBLE);
                        main_linear_layout.setVisibility(View.GONE);
                    }
                }

            }
        });
        final WeakReference<CaseActivity> refTarget = new WeakReference<>(target);
        dh = DBHelper.getInstance();

        ShowLoading(target, true);

        View adPanel = ViewHolder.get(target, R.id.fl_ad_content).getView();

        if (configService.isBannerAdEnable())
            adPanel.setVisibility(View.VISIBLE);
        else
            adPanel.setVisibility(View.GONE);




        Intent in = getAttachedObject().getIntent();
        Bundle b = in.getExtras();
        if (b != null) {


            String id = b.getString("nid");
            curNid = id;
            from = b.getInt("from");


            if (from != UiHelper.FROM_LAUCHER && from != UiHelper.FROM_PUSH) {
                String pname = b.getString("name");
                String pdomain = b.getString("domain");
                String title = b.getString("title");
                String body = b.getString("body");
                String source = b.getString("source");
                String type = b.getString("type");
                long time = b.getLong("releasetime");
                String mms = b.getString("mms");

                NewsItem item = new NewsItem();
                item.set_id(id);
                item.setSource(source);
                item.setBody(body);
                item.setPdomain(pdomain);
                item.setPname(pname);
                item.setTitle(title);
                item.set_cid("widget");
                item.setType(type);
                item.setReleaseTime(time);
                item.setEmo(new EmoVote());
                item.setCommentCount(commentCount);

                IDataNode node = DataHelper.parseJson(mms);
                IDataList<NewsImage> imgsToMerge = DataHelper.getBeanList(new DataNodeView(node, "*").getData(), NewsImage.class);
                item.setImgs((DataList<NewsImage>) imgsToMerge);


                dataService.addIntoNewsList(item);

            }

            dataService.setCurNid(id);



            if (from != UiHelper.FROM_LAUCHER) {


                dataService.initFavList();
                dataService.initPushList();
                dataService.initReadList();

            }

            if (from == UiHelper.FROM_PUSH)
                App.get().getService(IReportService.class).recordPushClick(id);


        }



        NewsItem item = dataService.getNewsById(curNid);
        if (item == null)
            return;


        initUiControls(target);
        doRemoteActions(item,false);
        getDataSet().setData(DataNames.CURRENT_NEWS, item);


        ViewPager vp = ViewHolder.get(target, R.id.news_img_vp).getView();
        WebView wv = ViewHolder.get(target, R.id.wv_newscontent).getView();
        WebView orgwv = ViewHolder.get(target, R.id.wv_orgcontent).getView();

        RelativeLayout vp_rl = ViewHolder.get(target, R.id.image_newscontent).getView();
        if (item != null) {
            if (item.getType().equals(NewsItem.NEWS_TYPE_IMAGE) || item.getType().equals(NewsItem.NEWS_TYPE_FEATURED)) {
                vp_rl.setVisibility(View.VISIBLE);
                wv.setVisibility(View.GONE);
                orgwv.setVisibility(View.GONE);
                ShowLoading(target, false);
                binder.bind(vp, item.getImgs(), (IIndexIndicator) ViewHolder.get(target, R.id.ciiCarousel).getView(), false);
                main_comm_leng.setText(item.getCommentCount() + "");
            } else {

                wv.setVisibility(View.VISIBLE);
                orgwv.setVisibility(View.VISIBLE);
                vp_rl.setVisibility(View.GONE);
            }
        }
//        hotBinder = new CommentItemBinder(getAttachedObject(),CommentItemBinder.TYPE_MORE_HOT);
//        hotBinder.build();
//        hotBinder.addSetter(hotSetter);
//        binderMoreHot = new ListViewBinder(hotBinder);
//        binderMoreHot.bind(ViewHolder.get(target, R.id.listview_more_comment), dataService.getMoreHotCommentList());
//        binderMoreHot.getItemClickedEvent().addEventHandler(new IEventHandler<ItemEventArgs>() {
//            @Override
//            public void process(Object o, ItemEventArgs itemEventArgs) {
//                Intent intent = new Intent(getAttachedObject(), CommentActivity.class);
//                intent.putExtra("nid", dataService.getCurNid());
//                getAttachedObject().startActivity(intent);
//            }
//        });

        // emo open button init
        View ivEmoBtn = ViewHolder.get(target, R.id.rl_article_emo).getView();
        ivEmoBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EmoMenu em = ViewHolder.get(getAttachedObject(), R.id.emo_menu).getView();
                int voteId = EmoVote.getVoteIdByName(dh.getCustomVote(dataService.getCurNid()));
                em.setMenuOpen(!em.isMenuOpen(), em.initVoteMenuData(emoVoteData, voteId), voteId);
            }
        });
        // emo menu init
        EmoMenu em = ViewHolder.get(getAttachedObject(), R.id.emo_menu).getView();
        em.setEmoListener(new IEmoListener() {

            @Override
            public void onClicked(int emoId) {
                String voteName = EmoVote.getVoteName(emoId);
                String nid = dataService.getCurNid();
                NewsItem item = getDataSet().getData(DataNames.CURRENT_NEWS);
                if (item == null)
                    item = dataService.getNewsById(nid);

                if (item != null)
                    AnalysisUtil.recordArticleMood(item.get_id(), item.getTitle(), item.getType(), voteName);

                reportService.recordEmo(nid, voteName);
                dataService.voteArticle(nid, voteName);


                dh.setCustomVote(nid, voteName);
                int curData = emoVoteData.get(emoId) + 1;
                emoVoteData.put(emoId, curData);
                curData = emoVoteData.get(R.id.emo_center_heart) + 1;
                emoVoteData.put(R.id.emo_center_heart, curData);
                EmoMenu em = ViewHolder.get(getAttachedObject(), R.id.emo_menu).getView();
                if (null != em) {
                    em.initVoteMenuData(emoVoteData,
                            EmoVote.getVoteIdByName(dh.getCustomVote(dataService.getCurNid())));
                }

                dataService.refreshFavList();
                dataService.refreshPushList();
                dataService.refreshReadList();


            }
        });

        // emo data init
        emoVoteData.clear();
        final String curVote = dh.getCustomVote(curNid);
        if (curVote != null) {
            int totalVote = dataService.getVoteNum(curNid, new IDataService.IVoteNum() {
                @Override
                public int onGetVote(String voteName, int num) {
                    if (voteName.equals(curVote) && 0 == num) {
                        num++;
                    }
                    emoVoteData.put(EmoVote.getVoteIdByName(voteName), num > 0 ? num : 0);
                    return num;
                }
            });
            emoVoteData.put(R.id.emo_center_heart, totalVote);

        }
        else
        {
            int totalVote = dataService.getVoteNum(curNid, new IDataService.IVoteNum() {
                @Override
                public int onGetVote(String voteName, int num) {
                    emoVoteData.put(EmoVote.getVoteIdByName(voteName), 0);
                    return 0;
                }
            });
            emoVoteData.put(R.id.emo_center_heart, totalVote);
        }

    }


    @Override
    public void onShow() {

        if (getAttachedObject() != null) {

            UiHelper.setStatusBarColor(getAttachedObject(), getAttachedObject().findViewById(R.id.statusBarBackground),
                    NightModeUtil.isNightMode() ? getAttachedObject().getResources().getColor(R.color.bg_red_night) : getAttachedObject().getResources().getColor(R.color.bg_red));


            NightModeUtil.setViewColor(getAttachedObject(), R.id.rl_loading,
                    resources.getColor(R.color.bg_white), resources.getColor(R.color.bg_black_night));

            NightModeUtil.setViewColor(getAttachedObject(), R.id.tv_loading,
                    resources.getColor(R.color.bg_black), resources.getColor(R.color.bg_white_night));

            NightModeUtil.setActionBarColor(getAttachedObject(), R.id.rl_article_action_bar);

            NightModeUtil.setViewColor(getAttachedObject(), R.id.sv_article,
                    resources.getColor(R.color.bg_article), resources.getColor(R.color.bg_article_night));

            ViewHolder.get(getAttachedObject(), R.id.linear_article_main).getView().setBackgroundColor(NightModeUtil.isNightMode() ? App.get().getResources().getColor(R.color.bg_black_night) :
                    App.get().getResources().getColor(R.color.bg_white));
            LinearLayout article_linear = ViewHolder.get(getAttachedObject(), R.id.article_linear).getView();
            TextView tv_more_hot = ViewHolder.get(getAttachedObject(), R.id.tv_more_hot).getView();
            TextView tv_more_comment = ViewHolder.get(getAttachedObject(), R.id.tv_more_comment).getView();
            if (NightModeUtil.isNightMode()){
                article_linear.setBackgroundDrawable(App.get().getResources().getDrawable(R.drawable.comment_more_shape_night));
                tv_more_hot.setBackgroundDrawable(App.get().getResources().getDrawable(R.drawable.comment_text_shape_night));
                tv_more_comment.setBackgroundColor(App.get().getResources().getColor(R.color.about_me_night));
                tv_more_comment.setTextColor(App.get().getResources().getColor(R.color.menu_item_background));
                main_relative_layout.setBackgroundColor(App.get().getResources().getColor(R.color.about_me_night));
                main_linear_layout.setBackgroundColor(App.get().getResources().getColor(R.color.about_me_night));
            } else {
                article_linear.setBackgroundDrawable(App.get().getResources().getDrawable(R.drawable.comment_more_shape));
                tv_more_hot.setBackgroundDrawable(App.get().getResources().getDrawable(R.drawable.comment_text_shape));
                tv_more_comment.setBackgroundColor(App.get().getResources().getColor(R.color.comment_more_bg));
                tv_more_comment.setTextColor(App.get().getResources().getColor(R.color.comment_more));
                main_relative_layout.setBackgroundColor(App.get().getResources().getColor(R.color.comment_write_bg));
                main_linear_layout.setBackgroundColor(App.get().getResources().getColor(R.color.comment_write_bg));
            }
        } else {
            Logger.error("SocialSetting not Attach!");
        }
        NewsItem item = dataService.getNewsById(curNid);
        if(commentService.getAllNewsCommentList()!= null && commentService.getAllNewsCommentList().getItemCount() > 0){
            main_comm_leng.setText(commentService.getAllNewsCommentList().getItemCount() + "");
        } else if (item != null){
            if (item.getCommentCount() == 0 && commentService.getAllNewsCommentList().getItemCount() > 0 && commentService.getAllNewsCommentList()!= null){
                main_comm_leng.setText(commentService.getAllNewsCommentList().getItemCount() + "");
            } else
            main_comm_leng.setText(item.getCommentCount() + "");
        }
        else
            main_comm_leng.setText("0");
        super.onShow();
    }

    private void ShowLoading(CaseActivity target, boolean visible) {


        ImageView iv = ViewHolder.get(target, R.id.iv_loading).getView();
        if (iv == null)
            return;

        iv.setBackgroundResource(R.drawable.loading_anim);
        AnimationDrawable loadAnim = (AnimationDrawable) iv.getBackground();
        loadAnim.setOneShot(false);

        if (visible) {
            RelativeLayout rl = ViewHolder.get(target, R.id.rl_loading).getView();
            rl.setVisibility(View.VISIBLE);
            loadAnim.start();
        } else {
            loadAnim.stop();
            RelativeLayout rl = ViewHolder.get(target, R.id.rl_loading).getView();
            rl.setVisibility(View.GONE);
        }
    }


    private void invalidateActivity() {
        boolean nightMode = (NightModeUtil.getDayNightMode() == NightModeUtil.THEME_NIGHT) ? true : false;
        CaseActivity target = getAttachedObject();
        View rlActionBar = ViewHolder.get(target, R.id.rl_article_action_bar).getView();
        TextView tv_more_comment = ViewHolder.get(target, R.id.tv_more_comment).getView();
        TextView tv_more_hot = ViewHolder.get(target, R.id.tv_more_hot).getView();
        LinearLayout article_linear = ViewHolder.get(getAttachedObject(), R.id.article_linear).getView();
        if (nightMode) {
            rlActionBar.setBackgroundColor(target.getResources().getColor(R.color.bg_red_night));
            NightModeUtil.setBrightness(100);
            tv_more_hot.setBackgroundDrawable(App.get().getResources().getDrawable(R.drawable.comment_text_shape_night));
            tv_more_comment.setBackgroundColor(App.get().getResources().getColor(R.color.about_me_night));
            tv_more_comment.setTextColor(App.get().getResources().getColor(R.color.menu_item_background));
            article_linear.setBackgroundDrawable(App.get().getResources().getDrawable(R.drawable.comment_more_shape_night));
            main_relative_layout.setBackgroundColor(App.get().getResources().getColor(R.color.about_me_night));
            main_linear_layout.setBackgroundColor(App.get().getResources().getColor(R.color.about_me_night));
        } else {
            rlActionBar.setBackgroundColor(target.getResources().getColor(R.color.bg_red));
            NightModeUtil.resetBrightness();
            tv_more_hot.setBackgroundDrawable(App.get().getResources().getDrawable(R.drawable.comment_text_shape));
            tv_more_comment.setBackgroundColor(App.get().getResources().getColor(R.color.comment_more_bg));
            tv_more_comment.setTextColor(App.get().getResources().getColor(R.color.comment_more));
            article_linear.setBackgroundDrawable(App.get().getResources().getDrawable(R.drawable.comment_more_shape));
            main_relative_layout.setBackgroundColor(App.get().getResources().getColor(R.color.comment_write_bg));
            main_linear_layout.setBackgroundColor(App.get().getResources().getColor(R.color.comment_write_bg));
        }
        NightModeUtil.setViewColor(getAttachedObject(),R.id.sv_article,
                resources.getColor(R.color.bg_article),resources.getColor(R.color.bg_article_night));
        ViewHolder.get(getAttachedObject(),R.id.linear_article_main).getView().setBackgroundColor(NightModeUtil.isNightMode() ? App.get().getResources().getColor(R.color.bg_black_night) :
                App.get().getResources().getColor(R.color.bg_white));

        UiHelper.setStatusBarColor(getAttachedObject(), getAttachedObject().findViewById(R.id.statusBarBackground),
                NightModeUtil.isNightMode() ? getAttachedObject().getResources().getColor(R.color.bg_red_night) : getAttachedObject().getResources().getColor(R.color.bg_red));

        WebView wv = ViewHolder.get(target, R.id.wv_newscontent).getView();
        wv.reload();
        if (binderMoreHot.getAdapter() != null){
            binderMoreHot.getAdapter().notifyDataSetChanged();
        }
    }


    private synchronized void initWebSettings(WebView wv) {
        WebSettings webSettings = wv.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            wv.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        } else {
            wv.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        }

        wv.setVerticalScrollBarEnabled(false);
        wv.setVerticalScrollbarOverlay(false);
        wv.setHorizontalScrollBarEnabled(false);
        wv.setHorizontalScrollbarOverlay(false);


		/*
		webSettings.setAllowContentAccess(true);
		webSettings.setAllowFileAccess(true);


		webSettings.setAppCacheEnabled(true);
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

		webSettings.setDatabaseEnabled(true);



		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDisplayZoomControls(false);

*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webSettings.setLoadsImagesAutomatically(true);
        } else {
            webSettings.setLoadsImagesAutomatically(false);
        }

        //webSettings.setBlockNetworkImage(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    }


    @Override
    public boolean onBack() {

        if (getAttachedObject() != null) {

            View mask = ViewHolder.get(getAttachedObject(), R.id.rl_mask).getView();
            View share = ViewHolder.get(getAttachedObject(), R.id.rl_share_panel).getView();

            if (share.getVisibility() == View.VISIBLE) {
                if (mask != null)
                    mask.setVisibility(View.INVISIBLE);
                if (share != null)
                    share.setVisibility(View.INVISIBLE);


                return true;
            }
        }


        DestroyAdAction();

        NewsItem item = getDataSet().getData(DataNames.CURRENT_NEWS);
        if (item == null)
            item = dataService.getNewsById(dataService.getCurNid());

        if (item != null)
            AnalysisUtil.endRecordArticleRead(item.get_id(), item.getTitle(), item.getType());

        item.setCommentCount(commentService.getAllNewsCommentList().getItemCount());
        commentService.removeAllNewsComment();

        return super.onBack();
    }

    private void DestroyAdAction() {
        AdHelper.destroyFacebookBannerAd();

    }


    private void initSharePanel() {
        ArrayList<IShare> shares = App.get().getService(IShareService.class).getCurShares();
        LinearLayout ll_share = (LinearLayout) ViewHolder.get(getAttachedObject(), R.id.ll_share).getView();


        if (shares != null && ll_share != null) {
            ll_share.removeAllViews();
            for (int i = 0; i < shares.size(); i++) {
                IShare share = shares.get(i);
                createShareView(ll_share, share.getControlDrawable(), share.getControlName(), share.getTitle());
            }
        }
    }


    private View.OnClickListener shareClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String tagName = (String) v.getTag();

            NewsItem ni = getDataSet().getData(DataNames.CURRENT_NEWS);
            String title = ni.getTitle();
            String webUrl = SHARE_TEMPLATE.replace("{aid}", ni.get_id());
            String imageUri = App.get().getService(IConfigService.class).getBaseImageUrl() + ni.getPreview() + "." + 240 + "x" + 152 + "t5";

            AnalysisUtil.recordArticleShare(ni.get_id(), ni.getTitle(), ni.getType(), tagName);
            App.get().getService(IShareService.class).share(tagName, title, webUrl, imageUri);

            View mask = ViewHolder.get(getAttachedObject(), R.id.rl_mask).getView();
            View share = ViewHolder.get(getAttachedObject(), R.id.rl_share_panel).getView();

            if (mask != null)
                mask.setVisibility(View.INVISIBLE);
            if (share != null)
                share.setVisibility(View.INVISIBLE);
        }
    };

    private void createShareView(ViewGroup parent, int drawable, String name, String title) {
        View share = getAttachedObject().getLayoutInflater().inflate(R.layout.share_item, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        //share.setLayoutParams(params);


        ((ImageView) share.findViewById(R.id.iv_share_icon)).setImageResource(drawable);
        ((TextView) share.findViewById(R.id.tv_share_title)).setText(title);

        share.setTag(name);
        share.setOnClickListener(shareClickListener);

        parent.addView(share, params);

    }


    public void copyFile(File oldfile, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;

            InputStream inStream = new FileInputStream(oldfile);
            FileOutputStream fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1024];
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread;
                fs.write(buffer, 0, byteread);
            }
            inStream.close();

        } catch (Exception e) {
            System.out.println("error  when copy files");
            e.printStackTrace();
        }
    }


    public File getAppDir(Context context, String category) {
        File appDir = null;
        if (appDir == null) {
            if (Environment.getExternalStorageState().equals("mounted")) {
                appDir = new File(Environment.getExternalStorageDirectory().getPath() + "/poponews/");
            } else {
                appDir = new File(Environment.getRootDirectory().getPath() + "/poponews/");
            }

            if (appDir != null && !appDir.exists()) {
                appDir.mkdirs();
            }
        }

        File fc = category == null ? appDir : new File(appDir, category);
        if (!fc.exists()) {
            fc.mkdir();
        }

        return fc;
    }


    private void startScaleAnimation(final View v) {
        if (getAttachedObject() != null) {
            Animation large = AnimationUtils.loadAnimation(getAttachedObject(), R.anim.image_long_click_large);
            large.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Animation small = AnimationUtils.loadAnimation(getAttachedObject(), R.anim.image_long_click_small);
                    v.clearAnimation();
                    v.startAnimation(small);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            v.clearAnimation();
            v.startAnimation(large);
        }


    }

    private void initUiControls(final CaseActivity target) {
        initSharePanel();
        final WeakReference<CaseActivity> refTarget = new WeakReference<CaseActivity>(target);



        final ImageButton ivFavBtn = ViewHolder.get(target, R.id.artile_action_bar_fav).getView();
        boolean fav = dh.getFav(curNid);
        Logger.error("Fav nid = "+curNid);
        if (fav)
            ivFavBtn.setImageResource(R.drawable.navigation_favority_selected);
        else
            ivFavBtn.setImageResource(R.drawable.navigation_favority_normal);


        ivFavBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (refTarget.get() != null) {

                    String nid = dataService.getCurNid();
                    NewsItem newsitem = dataService.getNewsById(nid);
                    boolean fav = dh.getFav(nid);


                    if (!fav) {
                        ivFavBtn.setImageResource(R.drawable.navigation_favority_selected);

                        dh.setFav(nid, true);
                        newsitem.setMyFav(NewsItem.NEWS_FAV_LIST);
                        int favnum = newsitem.getFav();
                        newsitem.setFav(favnum + 1);
                        dataService.addIntoFavList(newsitem);
                        reportService.recordFav(nid, NewsItem.NEWS_FAV_LIST);

                        Toast.makeText(refTarget.get(), R.string.add_favorite, Toast.LENGTH_SHORT).show();

                        AnalysisUtil.recordArticleFav(newsitem.get_id(), newsitem.getTitle(), newsitem.getType(), "true");
                    } else {
                        ivFavBtn.setImageResource(R.drawable.navigation_favority_normal);

                        dh.setFav(nid, false);
                        newsitem.setMyFav(NewsItem.NEWS_FAV_NONE);
                        int favnum = newsitem.getFav();
                        newsitem.setFav(favnum - 1 < 0 ? 0 : favnum - 1);

                        dataService.removeFromFavList(newsitem);
                        reportService.recordFav(nid, NewsItem.NEWS_FAV_NONE);

                        Toast.makeText(refTarget.get(), R.string.remove_favorite, Toast.LENGTH_SHORT).show();
                        AnalysisUtil.recordArticleFav(newsitem.get_id(), newsitem.getTitle(), newsitem.getType(), "false");
                    }
                    dataService.refreshItems(newsitem.get_cid());

                    dataService.refreshPushList();
                    dataService.refreshFavList();
                    dataService.refreshReadList();


                }
            }
        });


        final ImageButton ivShareBtn = ViewHolder.get(target, R.id.artile_action_bar_share).getView();

        ivShareBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (refTarget.get() != null) {
                    NewsItem ni = getDataSet().getData(DataNames.CURRENT_NEWS);
					/*ActionShareRichTextNews(configService.getShareUrl(i.get_id()),
							i.getTitle(), imageService.getCacheOf(i.getPreview()).getAbsolutePath());*/
                    ArrayList<String> mms = new ArrayList<String>();
                    for (int i = 0; i < ni.getImgs().getItemCount(); i++) {
                        String img = App.get().getService(IConfigService.class).getBaseImageUrl() + ni.getImgs().getItem(i).getFile() + ".200x200t5";
                        mms.add(img);
                    }
                    ActionShareNews(ni.getTitle(), ni.get_id(), ni.getPreview(), mms);
                }
            }
        });


        final ImageButton ivSettingBtn = ViewHolder.get(target, R.id.artile_action_bar_setting).getView();
        ivSettingBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (refTarget.get() != null) {
                    EmoMenu em = ViewHolder.get(getAttachedObject(), R.id.emo_menu).getView();
                    if (em!=null && em.isMenuOpen()) {
                        em.setMenuOpen(false, false, -1);
                    }
                    ActionShowSetting();
                }
            }
        });


        View ivBackBtn = ViewHolder.get(target, R.id.article_back).getView();

        ivBackBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                DestroyAdAction();

                if (refTarget.get() != null) {
                    Activity currActivity = App.get().getCurrentActivity();
                    if (currActivity != null)
                        currActivity.finish();
                }

                if (from != UiHelper.FROM_LAUCHER && !UiHelper.isMainAlive()) {
                    Intent mainIntent;
                    Context ctx = (getAttachedObject() != null) ? getAttachedObject() : App.get();

                    mainIntent = new Intent(ctx, SplashActivity.class);
                    mainIntent.putExtra(IntentNames.INTENT_EXTRA_FROM, UiHelper.FROM_PUSH);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(mainIntent);

                    remoteService.connect(false, true);

                }


            }
        });


    }

    private void doRemoteActions(NewsItem item, boolean alwaysGetBody) {

        if (!item.getType().equals(NewsItem.NEWS_TYPE_IMAGE)) {
            if (alwaysGetBody)
                remoteService.getBodyContent(item.get_id(), item.getBody());
            else {
                if (from != UiHelper.FROM_LAUCHER) {
                    remoteService.getBodyContent(item.get_id(), item.getBody());
                }
            }
        }

        reportService.recordPv(curNid);
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








