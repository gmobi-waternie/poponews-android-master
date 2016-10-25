package com.gmobi.poponews.util;

import android.content.res.Configuration;

import com.momock.app.App;

import java.util.Locale;

/**
 * Created by Administrator on 9/21 0021.
 */
public class LocaleHelper {


	private static String[][] localeMap = {
			{"en", "en", "US"},
			{"cn", "zh", "CN"},
			{"tw", "zh", "TW"},
			{"in", "", ""},
			{"id", "", ""},
			{"ru", "ru", "RU"},
			{"th", "th", "TH"},
			{"my", "ms", "MY"},

	};

	public static void setDefaultLocal(String lang) {
		Locale[] locales = Locale.getAvailableLocales();
		for(int i = 0; i< localeMap.length; i++)
		{
			if(localeMap[i][0].equals(lang))
			{
				Locale locale = null;
				if(localeMap[i][2].equals(""))
					locale= new Locale("in","ID");
				else
					locale= new Locale(localeMap[i][1],localeMap[i][2]);
				Locale.setDefault(locale);

				//Configuration config = new Configuration();
				Configuration config = App.get().getResources().getConfiguration();
				config.locale = locale;
				App.get().getResources().updateConfiguration(config, null);
			}
		}
	}
}
