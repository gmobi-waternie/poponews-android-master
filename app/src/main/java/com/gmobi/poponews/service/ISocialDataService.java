/*******************************************************************************
 * Copyright 2012 momock.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.gmobi.poponews.service;

import com.gmobi.poponews.model.SocialAccount;
import com.gmobi.poponews.model.SocialPost;
import com.momock.data.DataList;
import com.momock.data.IDataList;
import com.momock.service.IService;

public interface ISocialDataService extends IService {
	IDataList<SocialPost> getPostList();
	void syncPostData(String social_type);

	void removeItemFromPostList(SocialPost post);
	void removeItemFromAccList(SocialAccount account);
	void removeTypeFromPostList(String social_type);

	void removeAllFromPostList();
	void removeAllFromAccList(String social_type,boolean deletePost);



	/*
	* Hide post data of specified type,set them invisible instead of removing them from list
	* */
	void HideTypeInPostList(String type);

	/*
	* Show post data of specified type,,set them visible.
	* */
	void ShowTypeInPostList(String type);



	/*
	* Hide account data of specified type,set them invisible instead of removing them from list
	* */
	void HideTypeInAccList(String type);

	/*
	* Show account data of specified type,,set them visible.
	* */
	void ShowTypeInAccList(String type);



	void addIntoPostList(DataList<SocialPost> merge);
	void saveDataToCache(DataList<SocialPost> merge);
	void restoreDataFromCache(String social_type);



	void addIntoAccList(DataList<SocialAccount> merge);
	void saveAccountsToCache(DataList<SocialAccount> merge);
	void restoreAccountsFromCache(String social_type);

	IDataList<SocialAccount> getAccList(String social_type);
	SocialAccount findAccount(String social_type, String id);


	void setAllAccountsSelectStatus(String social_type,boolean select);
	boolean isAllAccountsSelect(String social_type);
	boolean isAllAccountsFinishUpdating(String social_type);



}
