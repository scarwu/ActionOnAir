/**
 * DB Helper
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

package scarwu.actiononair.libs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "AoA-" + DBHelper.class.getSimpleName();

    public DBHelper(Context context) {
        super(context, "ActionOnAir", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Create");

        db.execSQL("CREATE TABLE camera ("
            + "ssid TEXT primary key,"
            + "pass TEXT,"
            + "provider TEXT)"
        );

        db.execSQL("CREATE TABLE sns ("
            + "provider TEXT primary key,"
            + "token TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrade");

        db.execSQL("DROP TABLE IF EXISTS camera");
        db.execSQL("DROP TABLE IF EXISTS sns");

        onCreate(db);
    }

    /**
     * Get Camera List
     *
     * @return
     */
    public Cursor getCameraList() {
        Log.i(TAG, "Get Camera List");

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query("camera", null, null, null, null, null, null);
    }

    /**
     * Get Camera Item
     *
     * @param ssid
     *
     * @return
     */
    public Cursor getCameraItem(String ssid) {
        Log.i(TAG, "Get Camera Item");

        SQLiteDatabase db = this.getReadableDatabase();

        String[] args = {
            ssid
        };

        return db.query("camera", null, "ssid=?", args, null, null, null);
    }

    /**
     * Add Camera Item
     *
     * @param ssid
     * @param pass
     * @param provider
     *
     * @return
     */
    public long addCameraItem(String ssid, String pass, String provider) {
        Log.i(TAG, "Add Camera Item");

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("ssid", ssid);
        cv.put("pass", pass);
        cv.put("provider", provider);

        return db.insert("camera", null, cv);
    }

    /**
     * Remove Camera Item
     *
     * @param ssid
     */
    public void removeCameraItem(String ssid) {
        Log.i(TAG, "Remove Camera Item");

        SQLiteDatabase db = this.getWritableDatabase();

        String[] args = {
            ssid
        };

        db.delete("camera", "ssid=?", args);
    }

    /**
     * Get SNS List
     *
     * @return
     */
    public Cursor getSNSList() {
        Log.i(TAG, "Get SNS List");

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query("sns", null, null, null, null, null, null);
    }

    /**
     * Get SNS Item
     *
     * @param provider
     *
     * @return
     */
    public Cursor getSNSItem(String provider) {
        Log.i(TAG, "Get SNS Item");

        SQLiteDatabase db = this.getReadableDatabase();

        String[] args = {
            provider
        };

        return db.query("sns", null, "provider=?", args, null, null, null);
    }

    /**
     * Add SNS Item
     *
     * @param provider
     * @param token
     *
     * @return
     */
    public long addSNSItem(String provider, String token) {
        Log.i(TAG, "Add SNS Item");

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("provider", provider);
        cv.put("token", token);

        return db.insert("sns", null, cv);
    }

    /**
     * Remove SNS Item
     *
     * @param provider
     */
    public void removeSNSItem(String provider) {
        Log.i(TAG, "Remove SNS Item");

        SQLiteDatabase db = this.getWritableDatabase();

        String[] args = {
            provider
        };

        db.delete("sns", "provider=?", args);
    }
}
