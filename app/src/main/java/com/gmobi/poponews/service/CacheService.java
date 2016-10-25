package com.gmobi.poponews.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;


import com.momock.util.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;

public class CacheService {
	File cacheDir;	
	Context mContext;

	private static CacheService ins;

	private CacheService(Context context) {
		this.mContext = context;
		if (getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			cacheDir = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? 
					getExternalCacheDir(mContext)
					: new File(getExternalStorageDirectory().getPath() + "/Android/data/" + mContext.getPackageName() + "/cache/");
		} else {
			cacheDir = mContext.getCacheDir();
		}
		if (cacheDir != null && !cacheDir.exists()) {
			cacheDir.mkdirs();
		}		
	}

	public static synchronized CacheService getInstance(Context context)
	{
		if(ins == null)
		{
			ins = new CacheService(context);
		}
		return ins;
	}


	public String getFilenameOf(String uri) {
			Logger.debug("uri = " + uri);
			return uri.replaceFirst("https?:\\/\\/", "").replaceAll("[^a-zA-Z0-9.]",
				"_");
	}

	public File getCacheDir(String category) {
		File fc = category == null ? cacheDir : new File(cacheDir, category);
		if (!fc.exists())
			fc.mkdir();
		return fc;
	}

	public File getCacheOf(String category, String uri) {
		return new File(getCacheDir(category), getFilenameOf(uri));
	}
	
	public String getCachePathOf(String category, String uri) {
		return getCacheDir(category)+"/"+getFilenameOf(uri);
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	File getExternalCacheDir(final Context context) {
		return context.getExternalCacheDir();
	}

	void clearDir(File dir){
		final File[] files = dir.listFiles();
		if (files == null)
			return;
		for (final File f : files) {
			if (f.isDirectory())
				clearDir(f);
			else
				f.delete();
		}
	}

	public void clear(String category) {
		if (cacheDir == null)
			return;
		clearDir(category == null ? cacheDir : new File(cacheDir, category));
	}
	
	public void saveBmp(Bitmap bmp,String path) throws IOException {
        File f = new File(path);
        f.createNewFile();
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
                e.printStackTrace();
        }
        bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
                fOut.flush();
        } catch (IOException e) {
                e.printStackTrace();
        }
        try {
                fOut.close();
        } catch (IOException e) {
                e.printStackTrace();
        }
	}



}
