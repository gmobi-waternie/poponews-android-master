package com.gmobi.poponews.outlet;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gmobi.poponews.R;
import com.gmobi.poponews.model.EditionList;
import com.gmobi.poponews.model.EditionList.EditionEntity;
import com.gmobi.poponews.service.CacheService;
import com.gmobi.poponews.util.ImageHelper;
import com.gmobi.poponews.util.PreferenceHelper;
import com.gmobi.poponews.util.WidgetDataHelper;


import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2/1 0001.
 */
public class EditionAdapter extends BaseAdapter{

	private List<EditionEntity> dataSrc;
	private Context mContext;


	public EditionAdapter(Context ctx, List<EditionEntity> src)
	{
		dataSrc = src;
		mContext = ctx;
	}


	public int getCount() {
		return dataSrc.size();
	}

	public Object getItem(int position) {
		return dataSrc.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		EditionEntity ee = dataSrc.get(position);

			convertView = LayoutInflater.from(mContext).inflate(R.layout.widget_setting_edition_item,null);

			CheckedTextView tv = (CheckedTextView) convertView.findViewById(R.id.ctv_name);
			tv.setText(ee.getName());

			if(ee.getId().equals(PreferenceHelper.getCurChannel(mContext)))
			{
				((ListView)parent).setItemChecked(position, true);
			}

			ImageView  iv = (ImageView) convertView.findViewById(R.id.iv_flag);
			String url  = WidgetDataHelper.getEditionBaseUrl(mContext) + ee.getIcon();
			Bitmap b = getBitmap(url);
			if(b != null)
				iv.setImageBitmap(b);



		return convertView;

	}


	private Bitmap getBitmap(final String fullUri) {
		Bitmap bitmap = null;
		final int expectedWidth = 0;
		final int expectedHeight = 0;
		if (fullUri == null) return null;

		CacheService cs = CacheService.getInstance(mContext);


		File bmpFile = cs.getCacheOf("PoponewsFlag", fullUri);
		if (bmpFile.exists()) {
			bitmap = ImageHelper.fromFile(bmpFile, expectedWidth, expectedHeight);
		}
		if (bitmap == null) {
			WidgetDataHelper.downloadFlag(mContext, fullUri);
		}


		return bitmap;
	}
}
