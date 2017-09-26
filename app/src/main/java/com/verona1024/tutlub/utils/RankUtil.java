package com.verona1024.tutlub.utils;

import android.content.Context;

import com.verona1024.tutlub.R;

public class RankUtil {
    public static String getNameByPoints(Context context, int points){
        if (context == null){
            return "";
        }

        if (points <= 50){
            return context.getString(R.string.mode_prayer_newbie);
        } else if (points <= 200){
            return context.getString(R.string.mode_prayer_2class);
        } else if (points <= 500){
            return context.getString(R.string.mode_prayer_1class);
        } else if (points <= 1000){
            return context.getString(R.string.mode_prayer_warrior);
        } else {
            return context.getString(R.string.mode_prayer_king);
        }
    }

    public static int pointsToNextLevel(int points){
        if (points <= 50){
            return 50 - points;
        } else if (points <= 200){
            return 200 - points;
        } else if (points <= 500){
            return 500 - points;
        } else if (points <= 1000){
            return 100 - points;
        } else {
            return 0;
        }
    }

    public static int imageBigByPoints(int points){
        if (points <= 50){
            return R.drawable.level_big_newbie;
        } else if (points <= 200){
            return R.drawable.level_big_2nd;
        } else if (points <= 500){
            return R.drawable.level_big_1st;
        } else if (points <= 1000){
            return R.drawable.level_big_warrior;
        } else {
            return R.drawable.level_big_king;
        }
    }
}
