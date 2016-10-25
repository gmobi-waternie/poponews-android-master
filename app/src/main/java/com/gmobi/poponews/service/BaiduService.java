package com.gmobi.poponews.service;

import android.util.Log;

import com.gmobi.poponews.app.CacheNames;
import com.gmobi.poponews.app.MessageTopics;
import com.gmobi.poponews.model.NewsCategory;
import com.gmobi.poponews.model.SocialAccount;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.model.SocialPost;
import com.gmobi.poponews.model.SocialSetting;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.BaiduNewsParser;
import com.gmobi.poponews.util.GoogleNewsParser;
import com.momock.data.DataList;
import com.momock.data.IDataView;
import com.momock.event.IEventHandler;
import com.momock.http.HttpSession;
import com.momock.service.ICacheService;
import com.momock.service.IHttpService;
import com.momock.service.IMessageService;
import com.momock.util.Logger;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by Administrator on 8/7 0007.
 */
public class BaiduService implements IBaiduService {
	@Inject
	IMessageService messageService;
	@Inject
	ICacheService cacheService;
	@Inject
	IHttpService httpService;
	@Inject
	ISocialDataService socialDataService;


	public NewsCategory ctg;

	private DataList<HttpSession> downList = new DataList<>();
	private IDataView<SocialAccount> accountView;
	private BaiduNewsParser parser = new BaiduNewsParser();

	private final String SERVICE_TYPE = SocialExtra.SOCIAL_TYPE_BAIDU;
	@Override
	public void setCategory(NewsCategory ctg) {
		this.ctg = ctg;
	}

	@Override
	public void remoteGetAccountList(int fromActivity) {
		if(ctg == null)
			return;




		SocialExtra se = (SocialExtra) ctg.getExtra();
		if(se== null)
			return;
		DataList<SocialAccount> accountList = new DataList<>();

		for(int i=0; i<se.getSources().size(); i++)
		{
			if(se.getSources().get(i).getName().equals(SERVICE_TYPE))
			{
				SocialExtra.SourcesEntity.ConfigEntity configs =  se.getSources().get(i).getConfig();

				for(int j=0; j<configs.getChannels().size(); j++)
				{
					SocialExtra.SourcesEntity.ConfigEntity.ChannelsEntity channel = configs.getChannels().get(j);
					SocialAccount acc = new SocialAccount();
					acc.setId(channel.getName());
					acc.setName(channel.getName());
					if(channel.getRss_url()!= null)
						acc.setRssurl(channel.getRss_url());
					else
						acc.setRssurl("");

					acc.setRssurls(channel.getRss_urls());
					acc.setNext("");
					acc.setVisible(1);
					acc.setSocialtype(SERVICE_TYPE);
					acc.setRole(SocialAccount.ROLE_BAIDU_CHANNEL);
					acc.setExtra("");


					boolean s = SocialSetting.getCategorySelect(SERVICE_TYPE, channel.getName());
					acc.setSelect(s);
					accountList.addItem(acc);
				}

				break;
			}
		}
		setFetchedList(true);
		socialDataService.addIntoAccList(accountList);

		if(fromActivity == ISocialService.FROM_MAIN)
			messageService.send(this, MessageTopics.GET_BAIDU_CHANNEL_MAIN);
		else
			messageService.send(this, MessageTopics.GET_BAIDU_CHANNEL_SETTING);
	}

	@Override
	public void remoteGetAccountPosts(SocialAccount sa, String nextUrl) {
		remoteGetChannelData();
	}




	@Override
	public boolean isLogged() {
		return true;
	}

	@Override
	public boolean isBinded() {
		return false;
	}


	@Override
	public String getCacheData() {
		return null;
	}

	@Override
	public void setCacheData(String data) {

	}

	@Override
	public void doLogin(Object extra, int from) {
		socialDataService.ShowTypeInAccList(SERVICE_TYPE);
		socialDataService.ShowTypeInPostList(SERVICE_TYPE);

		AnalysisUtil.recordSnsLogin(SocialExtra.SOCIAL_TYPE_BAIDU, AnalysisUtil.RESULT_SUCCESS);

		remoteGetAccountList(ISocialService.FROM_SETTING);
	}

	@Override
	public boolean doBind(Object extra) {
		return false;
	}

	@Override
	public void doUnbind(Object extra) {

	}

	@Override
	public void doLogout(Object extra) {
		socialDataService.HideTypeInPostList(SERVICE_TYPE);
		socialDataService.HideTypeInAccList(SERVICE_TYPE);

		AnalysisUtil.recordSnsLogin(SocialExtra.SOCIAL_TYPE_BAIDU, AnalysisUtil.RESULT_LOGOUT);

		setFetchedData(false);
		setFetchedList(false);
	}

	private static boolean hasFetchedData = false;
	private static boolean hasFetchedList = false;
	@Override
	public boolean hasFetchedData() {
		return hasFetchedData;
	}

	@Override
	public void setFetchedData(boolean f) {
		hasFetchedData = f;
	}

	@Override
	public boolean hasFetchedList() {
		return hasFetchedList;
	}

	@Override
	public void setFetchedList(boolean f) {
		hasFetchedList = f;
	}

	@Override
	public void syncData() {
		socialDataService.syncPostData(SERVICE_TYPE);
	}

	private static boolean enabled = false;
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean e) {
		enabled = e;
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

	private void removeAllDownloadSessions()
	{
		for(int i=0; i<downList.getItemCount(); i++)
		{
			HttpSession session = downList.getItem(i);
			session.stop();
		}
		downList.removeAllItems();
	}

	private void remoteGetChannelData()
	{
		removeAllDownloadSessions();
		DataList<SocialAccount> accountList = (DataList<SocialAccount>) socialDataService.getAccList(SERVICE_TYPE);

		for(int i=0; i<accountList.getItemCount(); i++)
		{
			if(!accountList.getItem(i).isSelected())
				continue;
			final SocialAccount sa = accountList.getItem(i);

			String uri = sa.getRssurl();
			List<String> uris = sa.getRssurls();

			if(uri!=null &&!uri.equals(""))
			{
				sa.setFetchStatus(SocialAccount.STATUS_UPDATING);
				downloadChannelData(sa,uri);
			}
			else if(uris != null)
			{
				sa.setFetchStatus(SocialAccount.STATUS_UPDATING);
				for(int j=0; j<uris.size();j++)
				{
					downloadChannelData(sa,uris.get(j));
				}
			}


		}


	}








	private static int updateStatus = NOT_UPDATE;
	@Override
	public int getUpdateStatus() {
		return updateStatus;
	}

	@Override
	public void setUpdateStatus(int u) {
		updateStatus = u;
	}


	private void downloadChannelData(final SocialAccount sa,String uri)
	{
		File f = cacheService.getCacheOf(CacheNames.UPDATE_CACHEDIR, uri);
		HttpSession rssSession = httpService.download(uri, f);

		Logger.debug("Start download baidu channel:"+uri);

		rssSession.start();
		rssSession.getStateChangedEvent().addEventHandler(new IEventHandler<HttpSession.StateChangedEventArgs>() {

			@Override
			public void process(Object sender,
								HttpSession.StateChangedEventArgs args) {
				if (args.getState() == HttpSession.STATE_FINISHED) {
					HttpSession self = args.getSession();

					if (self != null) {
						self.stop();
					}
					try {
						DataList<SocialPost> baidunewsList = parser.parse(self.getFile(),sa.getName());

						socialDataService.addIntoPostList(baidunewsList);
						sa.setFetchStatus(SocialAccount.STATUS_IDLE);
						Log.e("Baidu", "Baidu sa=" + sa.getName());
						AnalysisUtil.recordSnsFetch(SocialExtra.SOCIAL_TYPE_BAIDU, baidunewsList.getItemCount() + "");
						messageService.send(this, MessageTopics.GET_BAIDU_POST);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}

		});
		downList.addItem(rssSession);
	}


	private  String title;
	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

}
