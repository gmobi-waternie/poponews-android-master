package com.gmobi.poponews.cases.setting;


import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmobi.poponews.BuildConfig;
import com.gmobi.poponews.R;
import com.gmobi.poponews.model.EditionData;
import com.gmobi.poponews.service.IConfigService;
import com.gmobi.poponews.service.IOfflineService;
import com.gmobi.poponews.util.AdHelper;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.AssetsHelper;
import com.gmobi.poponews.util.NightModeUtil;
import com.gmobi.poponews.util.SizeHelper;
import com.gmobi.poponews.widget.PopoDialog;
import com.momock.app.App;
import com.momock.data.DataList;
import com.momock.service.IAsyncTaskService;
import com.momock.service.ICacheService;
import com.momock.service.IUITaskService;
import com.momock.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class MainSettingFragment extends PreferenceFragment {

	Preference pfClean;
	long cacheFileSize = 0;
	IUITaskService uiTaskSvc;
	IAsyncTaskService asyncTaskSvc;
	ICacheService cacheSvc;
	IOfflineService offlineSvc;
	IConfigService configSvc;
	PopoDialog waitPop;

	Preference pfFeed;
	Preference pfEdition;
	Preference pVideo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(
				NightModeUtil.isNightMode() ? R.xml.pref_main_night : R.xml.pref_main);


		uiTaskSvc = App.get().getService(IUITaskService.class);
		cacheSvc = App.get().getService(ICacheService.class);
		asyncTaskSvc = App.get().getService(IAsyncTaskService.class);
		offlineSvc = App.get().getService(IOfflineService.class);
		configSvc = App.get().getService(IConfigService.class);


		String key = getResources().getString(R.string.key_clean);
		pfClean = getPreferenceManager().findPreference(key);
		if (null != pfClean) {
			pfClean.setSummary("-");
			pfClean.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					waitPop.show();
					asyncTaskSvc.run(new Runnable() {
						@Override
						public void run() {
							offlineSvc.stopDownloadCategory(false);
							List<String> excepts = AssetsHelper.fetchHtmlResNames(App.get().getApplicationContext());
							cacheFileSize = delFilesInFolder(cacheSvc.getCacheDir(null), excepts);
							uiTaskSvc.runDelayed(new Runnable() {
								@Override
								public void run() {
									cacheFileSize = 0;
									pfClean.setSummary(SizeHelper.getSize(cacheFileSize));
									waitPop.dismiss();
								}
							}, 1000);
						}
					});
					AnalysisUtil.recordMeClear();

					return true;
				}
			});
		}

		key = getResources().getString(R.string.key_video);
		pVideo = getPreferenceManager().findPreference(key);
		if (null != pVideo) {

			pVideo.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					configSvc.setVideoPlayAuto((Boolean) newValue);
					AdHelper.getInstance(getActivity()).setAutoplayFlag((Boolean) newValue);
					Logger.error("Set AutoPlay Falg:" + (Boolean) newValue);
					AnalysisUtil.recordMeAutoplay(newValue.toString());
					return true;
				}
			});
		}


		asyncTaskSvc.run(new Runnable() {
			@Override
			public void run() {
				cacheFileSize = getFileSize(cacheSvc.getCacheDir(null));
				uiTaskSvc.run(new Runnable() {
					@Override
					public void run() {
						pfClean.setSummary(SizeHelper.getSize(cacheFileSize));
					}
				});
			}
		});
		waitPop = new PopoDialog(getActivity(), R.style.PopoDialogStyle,
				R.layout.dialog_wait, false, 0, null, 0, null);

		if (!setEditionVisible()) {
			key = getResources().getString(R.string.key_edition);
			PreferenceScreen pfMenu = (PreferenceScreen) getPreferenceManager().findPreference("main_menu");
			pfEdition = pfMenu.findPreference(key);
			pfMenu.removePreference(pfEdition);
		}


		key = getResources().getString(R.string.key_feed);
		if (!BuildConfig.SUPPORT_PUSH) {
			PreferenceScreen pfMenu = (PreferenceScreen) getPreferenceManager().findPreference("main_menu");
			pfFeed = pfMenu.findPreference(key);
			pfMenu.removePreference(pfFeed);
		}
		else {

			pfFeed = getPreferenceManager().findPreference(key);
			if (pfFeed != null) {
				pfFeed.setDefaultValue(true);
				if (null != pfFeed) {
					pfFeed.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							configSvc.setNewsFeedFlag((Boolean) newValue);
							return true;
						}
					});
				}
			}
		}

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		container.setBackgroundResource(NightModeUtil.isNightMode() ? R.color.actionbar_sys_bg_color_night : R.color.actionbar_sys_bg_color);
		return super.onCreateView(inflater, container, savedInstanceState);
	}


	@Override
	public void onResume() {
		super.onResume();
	}

	private long getFileSize(File f) {
		long size = 0;
		try {
			File flist[] = f.listFiles();
			for (int i = 0; i < flist.length; i++) {
				if (flist[i].isDirectory()) {
					size = size + getFileSize(flist[i]);
				} else {
					size = size + flist[i].length();
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		return size;
	}

	private long delFilesInFolder(File file, List<String> exceptSuffix) {
		long ret = 0;
		File[] childFiles = file.listFiles();
		if (childFiles != null && childFiles.length > 0) {
			File tmpFile;
			for (int i = 0; i < childFiles.length; i++) {
				tmpFile = childFiles[i];
				if (tmpFile.isDirectory()) {
					ret += delFilesInFolder(tmpFile, exceptSuffix);
				} else {
					if (null != exceptSuffix) {
						boolean pass = false;
						for (String sf : exceptSuffix) {
							if (tmpFile.getName().endsWith(sf)) {
								pass = true;
								break;
							}
						}
						if (pass) {
							ret += tmpFile.length();
							continue;
						}
					}
				}
				tmpFile.delete();
			}
		}
		return ret;
	}

	private boolean setEditionVisible() {
		JSONObject dataSrc = configSvc.getEditionList();
		DataList<EditionData> out = new DataList<>();

		try {
			String baseUrl = dataSrc.getString("baseUrl");
			JSONArray ja = dataSrc.getJSONArray("data");
			JSONObject tmpJo;
			for (int i = 0; i < ja.length(); i++) {
				tmpJo = ja.getJSONObject(i);
				EditionData ed = new EditionData(baseUrl + tmpJo.getString("icon"),
						tmpJo.getString("name"),
						tmpJo.getString("id"), tmpJo.getString("lang"));
				out.addItem(ed);
			}
		} catch (Exception e) {
			return false;
		}

		return out.getItemCount() > 1;

	}
}
