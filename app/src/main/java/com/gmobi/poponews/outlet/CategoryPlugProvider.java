package com.gmobi.poponews.outlet;

import java.util.ArrayList;
import java.util.List;



import com.gmobi.poponews.R;
import com.gmobi.poponews.model.NewsCategory;
import com.momock.app.ICase;
import com.momock.data.DataChangedEventArgs;
import com.momock.data.IDataList;
import com.momock.data.IDataMutableList;
import com.momock.event.IEventHandler;
import com.momock.outlet.IPlug;
import com.momock.outlet.IPlugProvider;


public class CategoryPlugProvider implements IPlugProvider {
	List<IPlug> peerPlugs = new ArrayList<IPlug>();
	IDataList<IPlug> plugs = new IDataList<IPlug>(){
		
		@Override
		public boolean hasItem(IPlug item) {			
			return peerPlugs.contains(item);
		}

		@Override
		public IPlug getItem(int index) {
			return peerPlugs.get(index);
		}

		@Override
		public int getItemCount() {
			return peerPlugs.size();
		}

	};
	@Override
	public IDataList<IPlug> getPlugs() {
		return plugs;
	}
	IDataMutableList<NewsCategory> dataSource;
	ICase<?> kase;
	public CategoryPlugProvider(ICase<?> kase, IDataMutableList<NewsCategory> cats){
		this.kase = kase;
		dataSource = cats;
		dataSource.addDataChangedHandler(new IEventHandler<DataChangedEventArgs>(){

			@Override
			public void process(Object sender, DataChangedEventArgs args) {
				refreshPlugs();				
			}
			
		});
		refreshPlugs();
	}
	public void refreshPlugs(){
		peerPlugs.clear();
		for(int i = 0; i < dataSource.getItemCount(); i++){
			NewsCategory cat = dataSource.getItem(i);
			peerPlugs.add(new CategoryTabPlug(kase, cat,R.drawable.tab_selector,i));
		}
	}
	
	public IDataMutableList<NewsCategory> getDataSource(){
		return dataSource;
	}
}
