package com.gmobi.poponews.outlet;

import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmobi.poponews.R;
import com.gmobi.poponews.util.DipHelper;
import com.momock.binder.IComposedItemBinder;
import com.momock.binder.IContainerBinder;
import com.momock.binder.IItemBinder;
import com.momock.data.IDataList;
import com.momock.event.EventArgs;
import com.momock.holder.ViewHolder;
import com.momock.util.Convert;
import com.momock.util.Logger;

import java.util.ArrayList;

/**
 * Created by Administrator on 7/15 0015.
 */
public class RecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

	private IDataList<?> dataSource;
	private IItemBinder itemBinder;
	IContainerBinder containerBinder;
	RecyclerView.AdapterDataObserver observer;
	private OnRecyclerViewItemClickListener mOnItemClickListener = null;

	private static final int TYPE_ITEM = 0;
	private static final int TYPE_FOOTER = 10;

	@Override
	public void onClick(View v) {
		if (mOnItemClickListener != null) {
			mOnItemClickListener.onItemClick(v, Convert.toInteger(v.getTag()), dataSource.getItem(Convert.toInteger(v.getTag())));
		}
	}


	public interface OnRecyclerViewItemClickListener {
		void onItemClick(View view, int index, Object data);
	}

	public RecycleViewAdapter(IDataList<?> dataSource, IItemBinder binder, IContainerBinder containerBinder) {
		super();
		this.dataSource = dataSource;
		this.itemBinder = binder;
		this.containerBinder = containerBinder;
		observer = new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				//itemBinder.getDataChangedEvent().fireEvent(this, new EventArgs());

			}
		};
		registerAdapterDataObserver(observer);


	}


	public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
		this.mOnItemClickListener = listener;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		viewGroup.setBackgroundResource(R.color.bg_pinlist);
		/*if (viewType == TYPE_FOOTER) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(
					R.layout.footer_fresh_item, null);
			view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
					DipHelper.dip2px(81)));
			return new FooterViewHolder(view);
		} else
		*/
		{

			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_pin2,
					viewGroup, false);
			view.setOnClickListener(this);
			return new ItemViewHolder(view);
		}


	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
		if(viewHolder instanceof  ItemViewHolder) {
			viewHolder.itemView.setTag(i);
			itemBinder.onCreateItemView(viewHolder.itemView, i, containerBinder);
		}
		else if(viewHolder instanceof FooterViewHolder)
		{
			ImageView iv =((FooterViewHolder)viewHolder).iv_load;
			AnimationDrawable loadAnim = (AnimationDrawable) iv.getBackground();
			loadAnim.setOneShot(false);
			loadAnim.start();
		}
	}


	@Override
	public int getItemViewType(int position) {

		if (itemBinder instanceof IComposedItemBinder) {
			IComposedItemBinder cib = (IComposedItemBinder) itemBinder;
			return cib.getBinderIndex(dataSource.getItem(position));
		} else {
			return super.getItemViewType(position);
		}
	}


	@Override
	public long getItemId(int position) {
		return position;
	}


	@Override
	public int getItemCount() {
		return dataSource.getItemCount();// + 1;
	}

	class ItemViewHolder  extends RecyclerView.ViewHolder {
		TextView newsTitle;
		ImageView newsImage;


		public ItemViewHolder (View itemView) {

			super(itemView);
			newsTitle = (TextView) itemView.findViewById(R.id.news_title);
			newsImage = (ImageView) itemView.findViewById(R.id.news_img);

		}
	}

	class FooterViewHolder extends RecyclerView.ViewHolder {
		ImageView iv_load;
		public FooterViewHolder(View view) {
			super(view);

			iv_load = (ImageView) view.findViewById(R.id.iv_loading);

		}

	}


}
