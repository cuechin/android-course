package qrcodescanner.masterteam.com.masterandroid.utils.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefUtil {
    private static SharedPreferences preferences;

    private SharedPrefUtil() {
    }

    public static void init(Context context) {
        if (preferences == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    public static boolean readBooleanDefaultTrue(String key) {
        return preferences.getBoolean(key, true);
    }
}