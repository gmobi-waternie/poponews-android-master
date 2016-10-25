package com.gmobi.poponews.service;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.gmobi.poponews.view.ListRemoteViewsFactory;


/**
 * Created by Administrator on 10/9 0009.
 */
public class RemoteListviewService extends RemoteViewsService {
        @Override
        public RemoteViewsFactory onGetViewFactory(Intent intent) {
            return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
        }

        @Override
        public void onCreate() {
            // TODO Auto-generated method stub
            System.out.println("service in onCreate");
            super.onCreate();
        }

        @Override
        public void onDestroy() {
            // TODO Auto-generated method stub
            System.out.println("service in onDestory");
            super.onDestroy();
        }

        @Override
        public boolean onUnbind(Intent intent) {
            // TODO Auto-generated method stub
            System.out.println("service in onUnbind");
            return super.onUnbind(intent);
        }

        @Override
        public void onRebind(Intent intent) {
            // TODO Auto-generated method stub
            System.out.println("service in onRebind");
            super.onRebind(intent);
        }

        @Override
        public void onStart(Intent intent, int startId) {
            // TODO Auto-generated method stub
            System.out.println("service in onStart");
            super.onStart(intent, startId);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            // TODO Auto-generated method stub
            return super.onStartCommand(intent, flags, startId);
        }

}
