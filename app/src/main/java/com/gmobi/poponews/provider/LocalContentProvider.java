package com.gmobi.poponews.provider;

import java.io.File;
import java.io.FileNotFoundException;

import com.gmobi.poponews.app.CacheNames;
import com.gmobi.poponews.service.IConfigService;
import com.momock.app.App;
import com.momock.service.ICacheService;
import com.momock.util.Logger;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class LocalContentProvider extends ContentProvider {

	private static String URI_PREFIX;

	public static String constructUri(String url) {
		URI_PREFIX = "content://"+App.get().getPackageName();
		Uri uri = Uri.parse(url);
		return uri.isAbsolute() ? url : URI_PREFIX + url;
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
		String url = uri.getPath();
		final ICacheService cas = App.get().getService(ICacheService.class);
		final IConfigService cos = App.get().getService(IConfigService.class);
		Logger.debug("openFile url = "+url);

		if(url.startsWith("/poponewsad"))

		{
			String path = uri.getPath().substring(11);

			File file = cas.getCacheOf(CacheNames.AD_IMAGE_CACHEDIR, path);
			Logger.debug("2.uri.getPath = " + file.getAbsolutePath());
			ParcelFileDescriptor parcel = ParcelFileDescriptor.open(file,
					ParcelFileDescriptor.MODE_READ_ONLY);
			return parcel;
		}
		else
		{
			String path = uri.getPath().substring(1);
			File file = cas.getCacheOf(CacheNames.MY_IMAGE_CACHEDIR, cos.getBaseImageUrl() + path);
			Logger.debug("2.uri.getPath = " + file.getAbsolutePath());
			ParcelFileDescriptor parcel = ParcelFileDescriptor.open(file,
					ParcelFileDescriptor.MODE_READ_ONLY);
			return parcel;
		}



	}

	@Override
	public AssetFileDescriptor openAssetFile(Uri uri, String mode)
			throws FileNotFoundException {
		URI_PREFIX = "content://"+App.get().getPackageName();
		AssetManager am = getContext().getAssets();
		String path = uri.getPath().substring(1);
		Log.e("path:", path);

		// sdcard里有没有
		File file = App.get().getService(ICacheService.class).getCacheOf(CacheNames.MY_IMAGE_CACHEDIR, path);
		String tpath = file.getAbsolutePath();

		if (file.exists()) {
			Log.e("path2:", tpath);
			Uri turi = Uri.parse(URI_PREFIX + tpath);
			return super.openAssetFile(turi, mode);
		}

		return super.openAssetFile(uri, mode);
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public int delete(Uri uri, String s, String[] as) {
		throw new UnsupportedOperationException(
				"Not supported by this provider");
	}

	@Override
	public String getType(Uri uri) {
		throw new UnsupportedOperationException(
				"Not supported by this provider");
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentvalues) {
		throw new UnsupportedOperationException(
				"Not supported by this provider");
	}

	@Override
	public Cursor query(Uri uri, String[] as, String s, String[] as1, String s1) {
		throw new UnsupportedOperationException(
				"Not supported by this provider");
	}

	@Override
	public int update(Uri uri, ContentValues contentvalues, String s,
			String[] as) {
		throw new UnsupportedOperationException(
				"Not supported by this provider");
	}

}
