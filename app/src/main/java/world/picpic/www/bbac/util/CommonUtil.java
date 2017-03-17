package world.picpic.www.bbac.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import world.picpic.www.bbac.R;

/**
 * Created by Wonseob on 2016. 7. 25..
 */
public class CommonUtil {
    private static final String PREF_NAME = "world.picpic.www.bbac";

    /**
     * Has the Guide activity shown before. If shown, return true. Else, return false
     */
    public static boolean getHasShownGuide(Context context){
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        return prefs.getBoolean("hasShownGuide", false);
    }

    /**
     * Set true if the Guide activity has shown before. False else.
     */
    public static void setHasShownGuide(Context context, boolean hasShownGuide){
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        prefs.edit().putBoolean("hasShownGuide", hasShownGuide).commit();
    }

    /**
     * Get User Phone number.
     */
    public static String getUserPhoneNo(Context context){
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        return prefs.getString("phoneNo", "");
    }

    /**
     * Set User Phone number.
     */
    public static void setUserPhoneNo(Context context, String phoneNo){
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        prefs.edit().putString("phoneNo", phoneNo).commit();
    }

    /**
     * Get User FCM token.
     */
    public static String getUserFCMToken(Context context){
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        return prefs.getString("token", "");
    }

    /**
     * Set User FCM token.
     */
    public static void setUserFCMToken(Context context, String token){
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        prefs.edit().putString("token", token).commit();
    }

    /**
     * Get User Phone number.
     */
    public static String getBadgeCount(Context context){
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        return prefs.getString("badgeCount", "");
    }

    /**
     * Set User Phone number.
     */
    public static void setBadgeCount(Context context, String badgeCount){
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        prefs.edit().putString("badgeCount", badgeCount).commit();
    }

    /**
     * Get the default value which is with sms.
     */
    public static boolean getIsWithSms(Context context){
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
        return prefs.getBoolean("isWithSms", true);
    }

    /**
     * Set the default value which is with sms.
     */
    public static void setIsWithSms(Context context, boolean hasShownGuide){
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
     * @param context to work with assets
     * @param defaultFontNameToOverride for example "monospace"
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

    public static String getTimeForThisApp(Context context, String strTime){
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

}
