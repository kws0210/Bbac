package world.picpic.www.bbac.util;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import world.picpic.www.bbac.HomeActivity;
import world.picpic.www.bbac.R;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.POWER_SERVICE;

/**
 * Created by Wonseob on 2016. 7. 25..
 */
public class CommonUtil {
    private static final String PREF_NAME = "world.picpic.www.bbac";

    /**
     * Has the Guide activity shown before. If shown, return true. Else, return false
     */
    public static boolean getHasShownGuide(Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        return prefs.getBoolean("hasShownGuide", false);
    }

    /**
     * Set true if the Guide activity has shown before. False else.
     */
    public static void setHasShownGuide(Context context, boolean hasShownGuide) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        prefs.edit().putBoolean("hasShownGuide", hasShownGuide).commit();
    }

    /**
     * Get User Phone number.
     */
    public static String getUserPhoneNo(Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        return prefs.getString("phoneNo", "");
    }

    /**
     * Set User Phone number.
     */
    public static void setUserPhoneNo(Context context, String phoneNo) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        prefs.edit().putString("phoneNo", phoneNo).commit();
    }

    /**
     * Get User FCM token.
     */
    public static String getUserFCMToken(Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        return prefs.getString("token", "");
    }

    /**
     * Set User FCM token.
     */
    public static void setUserFCMToken(Context context, String token) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        prefs.edit().putString("token", token).commit();
    }

    /**
     * Get User Phone number.
     */
    public static String getBadgeCount(Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        return prefs.getString("badgeCount", "");
    }

    /**
     * Set User Phone number.
     */
    public static void setBadgeCount(Context context, String badgeCount) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        prefs.edit().putString("badgeCount", badgeCount).commit();
    }

    /**
     * Get the default value which is with sms.
     */
    public static boolean getIsWithSms(Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        return prefs.getBoolean("isWithSms", true);
    }

    /**
     * Set the default value which is with sms.
     */
    public static void setIsWithSms(Context context, boolean hasShownGuide) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        prefs.edit().putBoolean("isWithSms", hasShownGuide).commit();
    }

    public static String formatMessageTargetNameAndNumber(String name, String number) {
        return name + " <" + number + ">";
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    /**
     * Using reflection to override default typeface
     * NOTICE: DO NOT FORGET TO SET TYPEFACE FOR APP THEME AS DEFAULT TYPEFACE WHICH WILL BE OVERRIDDEN
     *
     * @param context                    to work with assets
     * @param defaultFontNameToOverride  for example "monospace"
     * @param customFontFileNameInAssets file name of the font from assets
     */
    public static void overrideFont(Context context, String defaultFontNameToOverride, String customFontFileNameInAssets) {
        try {
            final Typeface customFontTypeface = Typeface.createFromAsset(context.getAssets(), customFontFileNameInAssets);

            final Field defaultFontTypefaceField = Typeface.class.getDeclaredField(defaultFontNameToOverride);
            defaultFontTypefaceField.setAccessible(true);
            defaultFontTypefaceField.set(null, customFontTypeface);
        } catch (Exception e) {
            Log.v("KWS", "Can not set custom font " + customFontFileNameInAssets + " instead of " + defaultFontNameToOverride);
            e.printStackTrace();
        }
    }

    public static String getTimeForThisApp(Context context, String strTime) {
        final String currentDatedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String resultString;

        if (currentDatedTime.substring(0, 9).equals(strTime.substring(0, 9))) {
            int currentMinutes = Integer.parseInt(currentDatedTime.substring(9, 10)) * 1440
                    + Integer.parseInt(currentDatedTime.substring(11, 13)) * 60
                    + Integer.parseInt(currentDatedTime.substring(14, 16));
            int itemMsgMinutes = Integer.parseInt(strTime.substring(9, 10)) * 1440
                    + Integer.parseInt(strTime.substring(11, 13)) * 60
                    + Integer.parseInt(strTime.substring(14, 16));

            int diff = currentMinutes - itemMsgMinutes;

            if (diff < 1) {
                resultString = context.getString(R.string.before_under_1_minute);
            } else if (diff < 60) {
                resultString = diff + context.getString(R.string.before_under_1_hour);
            } else if (diff < 1440) {
                resultString = (diff / 60) + context.getString(R.string.before_under_1_day);
            } else {
                resultString = strTime;
            }
        } else {
            resultString = strTime;
        }
        return resultString;
    }

    /**
     * Get the true value if the user will receive notification.
     */
    public static boolean getReceiveNotification(Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        return prefs.getBoolean("receiveNotification", true);
    }

    /**
     * Set the true value if the user will receive notification.
     */
    public static void setReceiveNotification(Context context, boolean receiveNotification) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        prefs.edit().putBoolean("receiveNotification", receiveNotification).commit();
    }

    /**
     * Get the true value if the user will use vibration.
     */
    public static boolean getUseNotificationSound(Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        return prefs.getBoolean("useSound", true);
    }

    /**
     * Set the true value if the user will use vibration.
     */
    public static void setUseNotificationSound(Context context, boolean useNotificationSound) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        prefs.edit().putBoolean("useSound", useNotificationSound).commit();
    }

    public static void sendNotification(Context context, String messageBody) {
        Intent pushIntent = new Intent("fromPush");
        context.sendBroadcast(pushIntent);

        String badgeCount = String.valueOf(Integer.parseInt(CommonUtil.getBadgeCount(context.getApplicationContext())) + 1 );
        CommonUtil.setBadgeCount(context.getApplicationContext(),  badgeCount);
        setBadge(context, badgeCount);



        if(getReceiveNotification(context)) {
            Intent intent = new Intent(context, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("isFromNoti", true);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_reverse)
                    .setColor(context.getResources().getColor(R.color.colorMint))
                    .setContentTitle(context.getResources().getString(R.string.noti_title))
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setTicker(messageBody)
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_HIGH);
            if(getUseNotificationSound(context))
                notificationBuilder.setSound(defaultSoundUri);


            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

//        int timeMillis = (int)(System.currentTimeMillis()&0xfffffff);
            notificationManager.notify(0, notificationBuilder.build());

            KeyguardManager km = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
            if (km.inKeyguardRestrictedInputMode()) {
                //it is locked
                PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, context.getString(R.string.app_name));
                wl.acquire();
            }
        }
    }

    public static void setBadge(Context context, String strCount) {
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count_package_name", "world.picpic.www.bbac");
        intent.putExtra("badge_count_class_name", "world.picpic.www.bbac.HomeActivity");
        intent.putExtra("badge_count", Integer.parseInt(strCount));
        context.sendBroadcast(intent);
    }

}
