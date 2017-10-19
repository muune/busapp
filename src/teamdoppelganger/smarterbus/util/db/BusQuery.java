package teamdoppelganger.smarterbus.util.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 각각 cursor를 리턴 하는 함수들은 사용하는 곳에서 cursor를 닫아 주어야 된다.
 *
 * @author DOPPELSOFT4
 */
public class BusQuery {

    SQLiteDatabase mDatabase;

    public BusQuery(SQLiteDatabase database) {
        // TODO Auto-generated constructor stub
        mDatabase = database;
    }


    public Cursor getCBusLocation() {
        if (mDatabase == null) return null;
        Cursor cursor = mDatabase.rawQuery("SELECT *FROM %s", null);
        return cursor;
    }


    public Cursor getCDetailLocation() {
        if (mDatabase == null) return null;
        Cursor cursor = mDatabase.rawQuery("SELECT *FROM DetailLocation", null);
        return cursor;

    }


}
