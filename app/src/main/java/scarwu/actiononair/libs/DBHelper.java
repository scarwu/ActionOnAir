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

    public DBHelper(Context context) {
        super(context, "ActionOnAir", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("AoA-SQLite", "Create");

        db.execSQL("CREATE TABLE cameras ("
            + "id INTEGER primary key autoincrement,"
            + "provider TEXT,"
            + "ssid TEXT,"
            + "pass TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("AoA-SQLite", "upgrade");

        db.execSQL("DROP TABLE IF EXISTS cameras");

        onCreate(db);
    }

    /**
     * Read Cameras
     *
     * @return
     */
    public Cursor readCameras() {
        Log.i("AoA-SQLite", "Read Cameras");

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query("cameras", null, null, null, null, null, null);
    }

    /**
     * Get Camera
     *
     * @return
     */
    public Cursor getCamera(int id) {
        Log.i("AoA-SQLite", "Get Camera");

        SQLiteDatabase db = this.getReadableDatabase();

        String[] args = {
            Integer.toString(id)
        };

        return db.query("cameras", null, "id=?", args, null, null, null);
    }

    /**
     * Add Camera
     *
     * @param provider
     * @param ssid
     * @param pass
     *
     * @return
     */
    public long addCamera(String provider, String ssid, String pass) {
        Log.i("AoA-SQLite", "Add Camera");

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("provider", provider);
        cv.put("ssid", ssid);
        cv.put("pass", pass);

        return db.insert("cameras", null, cv);
    }

    /**
     * Remove Camera
     *
     * @param id
     */
    public void removeCamera(int id) {
        Log.i("AoA-SQLite", "Remove Camera");

        SQLiteDatabase db = this.getWritableDatabase();

        String[] args = {
            Integer.toString(id)
        };

        db.delete("cameras", "id=?", args);
    }
}
