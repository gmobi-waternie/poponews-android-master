package com.gmobi.poponews.util;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

import com.gmobi.poponews.app.CacheNames;
import com.gmobi.poponews.service.ConfigService;
import com.gmobi.poponews.service.NewsCacheService;
import com.momock.util.FileHelper;
import com.momock.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class AssetsHelper {
	Context mContext;
	
	public AssetsHelper(Context c)
	{
		this.mContext = c;
	}
	
	public void initHtmlRes()
	{
        try {
			InputStream inStream = mContext.getResources().getAssets().open("newsjs.zip");
			FileHelper.unzip(inStream, FileHelper.getCacheDir(mContext, CacheNames.NEWS_CONTENT_CACHEDIR));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<String> fetchHtmlResNames(Context ctx){
		List<String> fileNames = new ArrayList<String>();
		try {

			InputStream is = ctx.getAssets().open("newsjs.zip");
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				if (!ze.isDirectory()){
					fileNames.add(ze.getName());
				}
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
			is.close();
		}catch (Exception e){
			Logger.error(e);
		}
		return fileNames;
	}
	public void initStoreRes()
	{
        FileHelper fh = new FileHelper();
        
        try {
			InputStream inStream = mContext.getResources().getAssets().open("store.zip");
			FileHelper.unzip(inStream, ConfigService.getFileDir(mContext, CacheNames.STORE_CACHEDIR));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
