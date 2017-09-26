package com.verona1024.tutlub.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.verona1024.tutlub.models.NotificationObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class UserUtil {
    private static final String PREF_TUTLUB = "TutUser";
    private static final String PREF_TUTLUB_FIRST_TIME = "firsttime";
    private static final String PREF_TUTLUB_MOBILE_NOTIF = "mobilenotification";
    private static final String PREF_TUTLUB_PUSH_NOTIF = "pushnotification";
    private static final String PREF_TUTLUB_LOGIN = "login";
    private static final String PREF_TUTLUB_PASS = "pass";

    public static String COUNTRY;
    public static String EMAIL;
    public static String PHONE_NUMBER;
    public static String VERIFICATION_NUMBER;
    public static String PASSWORD;
    public static String userId;
    public static String user_id;
    public static String picture;
    public static String name;
    public static String description;
    public static String cover;
    public static String education;
    public static String country;
    public static int points;
    public static ArrayList<NotificationObject> notifications = new ArrayList<>();

    /**
     * Get notifications count.
     * @return - number of notifications.
     */
    public static int getNotificationCount(){
        int count = 0;

        for (NotificationObject notificationObject : notifications){
            if (!notificationObject.seen){
                count++;
            }
        }

        return count;
    }

    /**
     * Mark notifications as seen.
     * @param position - position of notification.
     */
    public static void removeNotification(int position){
        if (notifications.get(position) == null){
            return;
        }

        notifications.get(position).seen = true;
    }

    /**
     * If user start app first time.
     * @param context - Application context
     * @return - frag.
     */
    @NonNull
    public static Boolean getUserFirstTime(Context context){
        if (context == null){
            return false;
        }

        SharedPreferences preferences = context.getSharedPreferences(PREF_TUTLUB, MODE_PRIVATE);
        return preferences.getBoolean(PREF_TUTLUB_FIRST_TIME, true);
    }

    /**
     * Mark if user start app before.
     * @param context - Application context.
     */
    public static void setUserFirstTimeDone(Context context){
        if (context == null){
            return;
        }

        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_TUTLUB, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_TUTLUB_FIRST_TIME, false);
        editor.apply();
    }

    /**
     * If show mobile notification.
     * @param context - Application context.
     * @return - flag.
     */
    @NonNull
    public static Boolean getShowMobileNotifications(Context context){
        if (context == null){
            return false;
        }

        SharedPreferences preferences = context.getSharedPreferences(PREF_TUTLUB, MODE_PRIVATE);
        return preferences.getBoolean(PREF_TUTLUB_MOBILE_NOTIF, true);
    }

    /**
     * If show mobile notification.
     * @param context - Application context.
     * @param b - flag.
     */
    public static void setShowMobileNotifications(Context context, boolean b){
        if (context == null){
            return;
        }

        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_TUTLUB, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_TUTLUB_MOBILE_NOTIF, b);
        editor.apply();
    }

    /**
     * If show in-app notification.
     * @param context - Application context.
     * @return - flag.
     */
    @NonNull
    public static Boolean getShowPushNotifications(Context context){
        if (context == null){
            return false;
        }

        SharedPreferences preferences = context.getSharedPreferences(PREF_TUTLUB, MODE_PRIVATE);
        return preferences.getBoolean(PREF_TUTLUB_PUSH_NOTIF, true);
    }

    /**
     * If show in-app notification.
     * @param context - Application context.
     * @param b - flag.
     */
    public static void setShowPushNotifications(Context context, boolean b){
        if (context == null){
            return;
        }

        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_TUTLUB, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_TUTLUB_PUSH_NOTIF, b);
        editor.apply();
    }

    /**
     * Get user login.
     * @param context - Application context.
     * @return - user login.
     */
    public static String getUserLogin(Context context){
        if (context == null){
            return "";
        }

        SharedPreferences preferences = context.getSharedPreferences(PREF_TUTLUB, MODE_PRIVATE);
        return preferences.getString(PREF_TUTLUB_LOGIN, "");
    }

    /**
     * Get user pass.
     * @param context - Application context.
     * @return - user pass.
     */
    public static String getUserPass(Context context){
        if (context == null){
            return "";
        }

        SharedPreferences preferences = context.getSharedPreferences(PREF_TUTLUB, MODE_PRIVATE);
        return preferences.getString(PREF_TUTLUB_PASS, "");
    }

    /**
     * Save user info.
     * @param context - Application context.
     * @param login - user login.
     * @param pass - user pass.
     */
    public static void saveUser(Context context, String login, String pass){
        if (context == null){
            return;
        }

        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_TUTLUB, MODE_PRIVATE).edit();
        editor.putString(PREF_TUTLUB_LOGIN, login);
        editor.putString(PREF_TUTLUB_PASS, pass);
        editor.apply();
    }
}
