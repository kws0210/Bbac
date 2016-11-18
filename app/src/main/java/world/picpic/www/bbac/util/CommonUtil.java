package world.picpic.www.bbac.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import java.lang.reflect.Field;

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

}
