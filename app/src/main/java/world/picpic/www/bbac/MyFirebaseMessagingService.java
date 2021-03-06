package world.picpic.www.bbac; /**
 * Copyright 2016 Google Inc. All Rights Reserved.
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

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

import world.picpic.www.bbac.GuideActivity;
import world.picpic.www.bbac.R;
import world.picpic.www.bbac.common.BaseActivity;
import world.picpic.www.bbac.fragments.MsgListFragment;
import world.picpic.www.bbac.util.CommonUtil;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final int REQ_CODE_GET_NOTIFICATION = 20;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
//            sendNotification(remoteMessage.getData().get("message"));
            CommonUtil.sendNotification(getApplicationContext(), remoteMessage.getData().get("message"));
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
//    private void sendNotification(String messageBody) {
//        Intent pushIntent = new Intent("fromPush");
//        sendBroadcast(pushIntent);
//
//        String badgeCount = String.valueOf(Integer.parseInt(CommonUtil.getBadgeCount(getApplicationContext())) + 1 );
//        CommonUtil.setBadgeCount(getApplicationContext(),  badgeCount);
//        setBadgeCount(badgeCount);
//
//
//
//
//        Intent intent = new Intent(this, HomeActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        intent.putExtra("isFromNoti", true);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQ_CODE_GET_NOTIFICATION, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_reverse)
//                .setColor(getResources().getColor(R.color.colorMint))
//                .setContentTitle(getResources().getString(R.string.noti_title))
//                .setContentText(messageBody)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setTicker(messageBody)
//                .setContentIntent(pendingIntent)
//                .setPriority(Notification.PRIORITY_HIGH);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
////        int timeMillis = (int)(System.currentTimeMillis()&0xfffffff);
//        notificationManager.notify(0, notificationBuilder.build());
//
//        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
//        if( km.inKeyguardRestrictedInputMode()) {
//            //it is locked
//            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
//            PowerManager.WakeLock wl=pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, getString(R.string.app_name));
//            wl.acquire();
//        }
//    }
//
//    private void setBadgeCount(String strCount) {
//        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
//        intent.putExtra("badge_count_package_name", "world.picpic.www.bbac");
//        intent.putExtra("badge_count_class_name", "world.picpic.www.bbac.HomeActivity");
//        intent.putExtra("badge_count", Integer.parseInt(strCount));
//        sendBroadcast(intent);
//    }
}