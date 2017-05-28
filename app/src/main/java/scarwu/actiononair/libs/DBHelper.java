/**
 * Main Activity
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

public class DBHelper extends SQLiteOpenHelper {

	public DBHelper(Context context) {
		super(context, "ActionOnAir", null, 4);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE cameras ("
			+ "id INTEGER primary key autoincrement,"
			+ "provider TEXT,"
			+ "ssid TEXT,"
			+ "pass TEXT)"
		);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS cameras");

		onCreate(db);
	}

	/**
	 * Read Cameras
	 *
	 * @return
	 */
	public Cursor readCameras() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query("cameras", null, null, null, null, null, null);
		
		return cursor;
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
		SQLiteDatabase db = this.getWritableDatabase();

		String[] args = { Integer.toString(id) };

		db.delete("cameras", "id=?", args);
	}
}
