package com.gmobi.poponews.service;

import com.gmobi.poponews.model.SocialAccount;
import com.gmobi.poponews.model.SocialExtra;
import com.gmobi.poponews.model.SocialPost;
import com.gmobi.poponews.model.SocialSetting;
import com.gmobi.poponews.util.DBHelper;
import com.momock.app.App;
import com.momock.data.DataList;
import com.momock.data.DataListView;
import com.momock.data.IDataList;
import com.momock.data.IDataView;
import com.momock.data.IDataView.IFilter;
import com.momock.service.IUITaskService;
import com.momock.util.Logger;

/**
 * Created by Administrator on 8/10 0010.
 */
public class SocialDataService implements ISocialDataService{

	private DataList<SocialPost> postList= new DataList<SocialPost>();
	private IDataView<SocialPost> postView;

	private DataList<SocialAccount> accList= new DataList<SocialAccount>();
	private IDataView<SocialAccount> accView;

	private DBHelper dh;


	@Override
	public IDataList<SocialAccount> getAccList(final String social_type) {
		if (accView == null) {
			accView = new DataListView<SocialAccount>(accList);
		}
		accView.setFilter(new IFilter<SocialAccount>() {
			@Override
			public boolean check(SocialAccount socialAccount) {
				return (socialAccount.getSocialtype().equals(social_type) && socialAccount.getVisible() == 1);
			}
		});
		return accView.getData();
	}


	@Override
	public IDataList<SocialPost> getPostList() {
		if (postView == null) {
			postView = new DataListView<SocialPost>(postList);
			/*postView.setOrder(new IDataView.IOrder<SocialPost>() {
				@Override
				public int compare(SocialPost lhs, SocialPost rhs) {
					if (lhs.getReleasetime() > rhs.getReleasetime())
						return -1;
					else if (lhs.getReleasetime() < rhs.getReleasetime())
						return 1;
					else
						return 0;
				}
			});*/

		}

		postView.setFilter(new IFilter<SocialPost>() {
			@Override
			public boolean check(SocialPost socialPost) {
				return (socialPost.getVisible() == 1);
			}
		});
		return postView.getData();
	}

	@Override
	public void removeItemFromPostList(SocialPost post) {
		postList.beginBatchChange();
		postList.removeItem(post);
		postList.endBatchChange();
	}

	@Override
	public void removeItemFromAccList(SocialAccount account) {
		accList.beginBatchChange();
		accList.removeItem(account);
		accList.endBatchChange();
	}

	@Override
	public void HideTypeInPostList(String type) {
		postList.beginBatchChange();
		for(int i = 0; i < postList.getItemCount(); i++)
		{
			SocialPost sp = postList.getItem(i);
			if(sp.getSocialtype().equals(type))
				sp.setVisible(0);
		}
		postList.endBatchChange();
		postList.fireDataChangedEvent();
	}

	@Override
	public void ShowTypeInPostList(String type) {
		postList.beginBatchChange();
		for(int i = 0; i < postList.getItemCount(); i++)
		{
			if(postList.getItem(i).getSocialtype().equals(type))
				postList.getItem(i).setVisible(1);
		}
		postList.endBatchChange();
		postList.fireDataChangedEvent();
	}

	@Override
	public void HideTypeInAccList(String type) {

		for(int i = 0; i < accList.getItemCount(); i++)
		{
			SocialAccount sp = accList.getItem(i);

			if(sp.getSocialtype().equals(type))
				sp.setVisible(0);
		}

		accList.fireDataChangedEvent();
	}

	@Override
	public void ShowTypeInAccList(String type) {

		for(int i = 0; i < accList.getItemCount(); i++)
		{
			if(accList.getItem(i).getSocialtype().equals(type))
				accList.getItem(i).setVisible(1);
		}

		accList.fireDataChangedEvent();
	}

	@Override
	public void removeAllFromPostList() {
		postList.beginBatchChange();
		postList.removeAllItems();
		postList.endBatchChange();
	}

	@Override
	public void removeTypeFromPostList(String social_type) {
		DataList<SocialPost> delList= new DataList<>();
		for(int i=0;i<postList.getItemCount();i++)
		{
			SocialPost sp = postList.getItem(i);
			if(sp.getSocialtype().equals(social_type))
				delList.addItem(sp);
		}

		postList.beginBatchChange();
		for(int i=0;i<delList.getItemCount();i++)
		{
			postList.removeItem(delList.getItem(i));
		}
		postList.endBatchChange();
	}

	@Override
	public void removeAllFromAccList(String social_type,boolean deletePost) {
		DataList<SocialAccount> delList= new DataList<>();
		for(int i=0;i<accList.getItemCount();i++)
		{
			SocialAccount sa = accList.getItem(i);
			if(sa.getSocialtype().equals(social_type))
				delList.addItem(sa);
		}

		accList.beginBatchChange();
		for(int i=0;i<delList.getItemCount();i++)
		{
			accList.removeItem(delList.getItem(i));
		}
		accList.endBatchChange();


		if(deletePost)
			removeTypeFromPostList(social_type);
	}

	private boolean isPostExist(SocialPost sp)
	{
		String id = sp.getId();
		for(int i=0; i<postList.getItemCount(); i++)
		{
			if(id.equals(postList.getItem(i).getId()))
				return true;
		}
		return false;
	}

	private boolean isAccExist(SocialAccount sa)
	{
		String id = sa.getId();
		for(int i=0; i<accList.getItemCount(); i++)
		{
			if(id.equals(accList.getItem(i).getId()))
				return true;
		}
		return false;
	}

	@Override
	public void addIntoPostList(final DataList<SocialPost> merge) {

				postList.beginBatchChange();
				for (int i = 0; i < merge.getItemCount(); i++) {
					SocialPost sp = merge.getItem(i);
					if (!isPostExist(sp))
						postList.addItem(sp);
				}

				postList.endBatchChange();

				saveDataToCache(merge);

	}

	@Override
	public void saveDataToCache(DataList<SocialPost> merge) {
		for(int i=0; i<merge.getItemCount(); i++)
		{
			dh.savePostListToDB(merge.getItem(i));
		}
	}

	@Override
	public void restoreDataFromCache(String social_type) {
		DataList<SocialPost> list = dh.getPostListFromDB(social_type);
		postList.beginBatchChange();
		for(int i=0; i<list.getItemCount(); i++)
		{
			SocialPost sp = list.getItem(i);
			if(!isPostExist(sp)) {

				sp.setVisible(1);
				postList.addItem(sp);
			}
		}

		postList.endBatchChange();
	}



	@Override
	public void addIntoAccList(DataList<SocialAccount> merge) {
		accList.beginBatchChange();
		for(int i=0; i<merge.getItemCount(); i++)
		{
			SocialAccount sa = merge.getItem(i);
			if(!isAccExist(sa))
				accList.addItem(sa);
		}

		accList.endBatchChange();

		saveAccountsToCache(merge);
	}


	@Override
	public void saveAccountsToCache(DataList<SocialAccount> merge) {
		for(int i=0; i<merge.getItemCount(); i++)
		{
			dh.saveAccToDB(merge.getItem(i));
		}
	}

	@Override
	public void restoreAccountsFromCache(String social_type) {
		DataList<SocialAccount> list = dh.getAccfromDB(social_type);
		accList.beginBatchChange();
		for(int i=0; i<list.getItemCount(); i++)
		{
			SocialAccount sa = list.getItem(i);
			if(!isAccExist(sa)) {
				boolean s = SocialSetting.getCategorySelect(social_type, sa.getId());
				sa.setSelect(s);
				if(SocialSetting.getStatus(social_type))
					sa.setVisible(1);
				else
					sa.setVisible(0);
				sa.setFetchStatus(SocialAccount.STATUS_IDLE);
				accList.addItem(sa);
			}
		}

		accList.endBatchChange();
	}

	@Override
	public void setAllAccountsSelectStatus(String social_type,boolean select) {
		for(int i=0 ;i <accList.getItemCount(); i++)
		{
			SocialAccount sa = accList.getItem(i);
			if(sa.getSocialtype().equals(social_type))
				sa.setSelect(select);
		}
		accList.fireDataChangedEvent();

	}

	@Override
	public boolean isAllAccountsSelect(String social_type) {
		int count = 0;
		for(int i=0 ;i <accList.getItemCount(); i++)
		{
			SocialAccount sa = accList.getItem(i);
			if(sa.getSocialtype().equals(social_type))
			{
				count++;
				if(!sa.isSelected())
					return false;
			}

		}
		return count > 0;
	}

	@Override
	public boolean isAllAccountsFinishUpdating(String social_type)
	{
		for(int i=0; i<accList.getItemCount(); i++)
		{
			SocialAccount sa = accList.getItem(i);
			if(sa.getSocialtype().equals(social_type) && sa.isSelected() && sa.getFetchStatus()==SocialAccount.STATUS_UPDATING) {
				Logger.debug(social_type + ":NOT isAllAccountsFinishUpdating");
				return false;
			}
		}
		Logger.debug(social_type + ":isAllAccountsFinishUpdating");
		return true;
	}

	@Override
	public SocialAccount findAccount(String social_type, String id)
	{

		for(int i=0; i< accList.getItemCount(); i++)
		{
			SocialAccount sa = accList.getItem(i);
			if(sa.getId().equals(id) && sa.getSocialtype().equals(social_type))
				return sa;
		}
		return null;
	}


	@Override
	public void syncPostData(String social_type) {

		for(int i = 0; i<accList.getItemCount(); i++)
		{
			SocialAccount sa = accList.getItem(i);
			if(!sa.isSelected() && sa.getSocialtype().equals(social_type))
			{
				DataList<SocialPost> delList= new DataList<SocialPost>();
				DataList<SocialPost> postList = (DataList<SocialPost>)getPostList();
				for(int j=0; j<postList.getItemCount(); j++)
				{
					SocialPost fp = postList.getItem(j);
					if(fp.getFromid()!= null && fp.getFromid().equals(sa.getId()))
						delList.addItem(fp);
				}


				for(int j=0; j<delList.getItemCount(); j++)
				{
					removeItemFromPostList(delList.getItem(j));
				}

			}
		}
	}


	@Override
	public Class<?>[] getDependencyServices() {
		return new Class<?>[0];
	}

	@Override
	public void start() {
		dh = DBHelper.getInstance();
	}

	@Override
	public void stop() {

	}

	@Override
	public boolean canStop() {
		return false;
	}
}
