package qrcodescanner.masterteam.com.masterandroid.utils.util.database;

import android.content.Context;

import androidx.room.Database;

import qrcodescanner.masterteam.com.masterandroid.models.Code;
import qrcodescanner.masterteam.com.masterandroid.interfaces.CodeDao;
import qrcodescanner.masterteam.com.masterandroid.R;

@Database(entities = {Code.class},
        version = 1, exportSchema = false)
public abstract class QrCobaDatabase extends AppDatabase {

    private static volatile QrCobaDatabase sInstance;

    // Get a database instance
    public static synchronized QrCobaDatabase on() {
        return sInstance;
    }

    public static synchronized void init(Context context) {

        if (sInstance == null) {
            synchronized (QrCobaDatabase.class) {
                sInstance = createDb(context, context.getString(R.string.app_name), QrCobaDatabase.class);
            }
        }
    }

    public abstract CodeDao codeDao();
}
