/*******************************************************************************
 * Copyright 2012 momock.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.gmobi.poponews.outlet;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.gmobi.poponews.R;
import com.momock.binder.ContainerBinder;
import com.momock.binder.IComposedItemBinder;
import com.momock.binder.IItemBinder;
import com.momock.data.DataChangedEventArgs;
import com.momock.data.IDataChangedAware;
import com.momock.data.IDataList;
import com.momock.event.EventArgs;
import com.momock.event.IEventHandler;
import com.momock.event.ItemEventArgs;
import com.momock.util.Logger;

public class RecyclerViewBinder<T extends RecyclerView> extends ContainerBinder<T> {
	
	public RecyclerViewBinder(IItemBinder binder) {
		super(binder);
	}

	RecycleViewAdapter adapter = null;
	public RecycleViewAdapter getAdapter(){
		return adapter;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void onBind(T view, final IDataList<?> dataSource) {
		if (view != null) {


			adapter = new RecycleViewAdapter(dataSource,getItemBinder(),this);
			adapter.setOnItemClickListener(new RecycleViewAdapter.OnRecyclerViewItemClickListener() {
				@Override
				public void onItemClick(View view, int index, Object data) {
					ItemEventArgs args = new ItemEventArgs(view, index,
							dataSource.getItem(index));
					itemClickedEvent.fireEvent(view, args);
				}

			});
			view.setAdapter(adapter);
			if (dataSource instanceof IDataChangedAware)
				((IDataChangedAware)dataSource).addDataChangedHandler(new IEventHandler<DataChangedEventArgs>(){
	
					@Override
					public void process(Object sender, DataChangedEventArgs args) {
						adapter.notifyDataSetChanged();
					}
					
				});
		}
	}
}
