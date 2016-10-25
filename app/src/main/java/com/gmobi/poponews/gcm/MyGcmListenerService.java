/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmobi.poponews.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.gmobi.poponews.R;
import com.gmobi.poponews.activity.SplashActivity;
import com.gmobi.poponews.app.GlobalConfig;
import com.gmobi.poponews.cases.article.ArticleActivity;
import com.gmobi.poponews.service.IRemoteService;
import com.gmobi.poponews.service.IReportService;
import com.gmobi.poponews.util.AnalysisUtil;
import com.gmobi.poponews.util.DBHelper;
import com.gmobi.poponews.util.UiHelper;
import com.google.android.gms.gcm.GcmListenerService;
import com.momock.app.App;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String id = data.getString("itemId");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Itemid: " + data.toString());

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */
        /*DBHelper dh = DBHelper.getInstance();
        if(dh.getPush(id))
            return;*/
        App.get().getService(IReportService.class).recordPushRecv(id);
        AnalysisUtil.recordPushRecv(id);
        App.get().getService(IRemoteService.class).getSingleNews(id, message, GlobalConfig.SINGLE_NEWS);
        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        //sendNotification(id, message);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.*/


    int notificationId=0;


    private void sendNotification(String id, String message) {
        Intent in = new Intent(this, ArticleActivity.class);
        in.putExtra("nid", id);
        in.putExtra("from", UiHelper.FROM_LAUCHER);
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , in,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(message)
                .setContentText(id)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        notificationManager.notify(notificationId, notificationBuilder.build());

        if(notificationId == Integer.MAX_VALUE)
            notificationId=0;
        else
            notificationId ++;
    }
}
