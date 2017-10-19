package teamdoppelganger.smarterbus.util.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.smart.lib.CommonConstants;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.util.common.Debug;

public class LocalDBHelper extends LocalDBBaseHelper {

    public final static String DB_NAME = "LOCALINFOR.db";
    private final static int DATABASE_VERSION = 14;

    public final static String TABLE_FAVORITE_NAME = "FAVORITE";
    public final static String TABLE_NAME2 = "HISTORY";
    public final static String TABLE_NAME3 = "SETTING";
    public final static String TABLE_NAME4 = "BUSINFOR";


    //_F  field의 의미
    public final static String TABLE_CITY_F = "city";
    //bus인지 정류장인지
    public final static String TABLE_TYPE_F = "type";
    //apiId
    public final static String TABLE_TYPEID_F = "typeID";
    //apidId2 가필요할시
    public final static String TABLE_TYPEID2_F = "typeID2";
    public final static String TABLE_TYPEID3_F = "typeID3";
    public final static String TABLE_TYPEID4_F = "typeID4";

    public final static String TABLE_TYPE_NICK = "NICKNAME";
    public final static String TABLE_TYPE_NICK2 = "NICKNAME2";

    public final static String TABLE_ID_F = "_id";
    public final static String TABLE3_LOCAL_F = "locals";
    public final static String TABLE_COLOR_F = "color";
    public final static String TABLE_ORDER = "favorite_order";

    public final static String TABLE_LOCATION_XY = "locationXY";
    public final static String TABLE_API_ID = "apiId";
    public final static String TABLE_LOCALNAME = "localName";

    public final static String TABLE_TEMP1 = "temp1";
    public final static String TABLE_TEMP2 = "temp2";


    public LocalDBHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
        String strSQL = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, %s INTEGER, %s INTEGER,%s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s Text, %s INTEGER, %s Text, %s Text,%s Text)"
                , TABLE_FAVORITE_NAME, TABLE_ID_F, TABLE_CITY_F, TABLE_TYPE_F, TABLE_TYPEID_F, TABLE_TYPEID2_F, TABLE_TYPEID3_F, TABLE_TYPEID4_F, TABLE_TYPE_NICK, TABLE_COLOR_F, TABLE_ORDER, TABLE_TYPE_NICK2, TABLE_TEMP1, TABLE_TEMP2);

        String strSQL2 = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, %s INTEGER, %s INTEGER,%s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s Text, %s Text, %s Text,%s Text)"
                , TABLE_NAME2, TABLE_ID_F, TABLE_CITY_F, TABLE_TYPE_F, TABLE_TYPEID_F, TABLE_TYPEID2_F, TABLE_TYPEID3_F, TABLE_TYPEID4_F, TABLE_TYPE_NICK, TABLE_COLOR_F, TABLE_TYPE_NICK2, TABLE_TEMP1, TABLE_TEMP2);

        String strSQL3 = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, %s INTEGER)"
                , TABLE_NAME3, TABLE_ID_F, TABLE3_LOCAL_F);

        String strSQL4 = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, %s TEXT, %s TEXT, %s TEXT)"
                , TABLE_NAME4, TABLE_ID_F, TABLE_API_ID, TABLE_LOCATION_XY, TABLE_LOCALNAME);


        db.execSQL(strSQL);
        db.execSQL(strSQL2);
        db.execSQL(strSQL3);
        db.execSQL(strSQL4);

    }

    public void writeFavoriteValue(String sql) {
        getWritableDatabase().execSQL(sql);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        if (oldVersion == 1) {
            try {
                String sql = String.format("DELETE FROM %s where %s='%s'", TABLE_FAVORITE_NAME, TABLE_CITY_F, CommonConstants.CITY_GWANG_JU._cityId);
                String sql2 = String.format("DELETE FROM %s where %s='%s'", TABLE_NAME2, TABLE_CITY_F, CommonConstants.CITY_GWANG_JU._cityId);

                db.execSQL(sql);
                db.execSQL(sql2);
            } catch (Exception e) {
            }

        }


        if (oldVersion < 3) {
            try {
                String sql = String.format("DELETE FROM %s where %s='%s'", TABLE_FAVORITE_NAME, TABLE_CITY_F, CommonConstants.CITY_BU_SAN._cityId);
                String sql2 = String.format("DELETE FROM %s where %s='%s'", TABLE_NAME2, TABLE_CITY_F, CommonConstants.CITY_BU_SAN._cityId);

                db.execSQL(sql);
                db.execSQL(sql2);


                String changeTableSql = String.format("ALTER TABLE %s ADD COLUMN %s TEXT", TABLE_FAVORITE_NAME, TABLE_TEMP1);
                String changeTableSql2 = String.format("ALTER TABLE %s ADD COLUMN %s TEXT", TABLE_FAVORITE_NAME, TABLE_TEMP2);
                String changeTableSql3 = String.format("ALTER TABLE %s ADD COLUMN %s TEXT", TABLE_NAME2, TABLE_TEMP1);
                String changeTableSql4 = String.format("ALTER TABLE %s ADD COLUMN %s TEXT", TABLE_NAME2, TABLE_TEMP2);

                db.execSQL(changeTableSql);
                db.execSQL(changeTableSql2);
                db.execSQL(changeTableSql3);
                db.execSQL(changeTableSql4);


            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        if (oldVersion < 4) {

            try {
                String sql = String.format("DELETE FROM %s where %s='%s'", TABLE_FAVORITE_NAME, TABLE_CITY_F, CommonConstants.CITY_GU_MI._cityId);
                db.execSQL(sql);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        if (oldVersion < 5) {

            try {

                String sql = String.format("DELETE FROM %s where %s='%s'", TABLE_FAVORITE_NAME, TABLE_CITY_F, CommonConstants.CITY_JIN_JU._cityId);
                String sql2 = String.format("DELETE FROM %s where %s='%s'", TABLE_NAME2, TABLE_CITY_F, CommonConstants.CITY_JIN_JU._cityId);
                String sql3 = String.format("DELETE FROM %s where %s='%s'", TABLE_FAVORITE_NAME, TABLE_CITY_F, CommonConstants.CITY_A_SAN._cityId);
                String sql4 = String.format("DELETE FROM %s where %s='%s'", TABLE_NAME2, TABLE_CITY_F, CommonConstants.CITY_A_SAN._cityId);

                db.execSQL(sql);
                db.execSQL(sql2);
                db.execSQL(sql3);
                db.execSQL(sql4);


            } catch (Exception e) {
                e.printStackTrace();
            }


        }


        if (oldVersion < 6) {
            try {

                String sql = String.format("DELETE FROM %s where %s='%s'", TABLE_FAVORITE_NAME, TABLE_CITY_F, CommonConstants.CITY_CHEONG_JU._cityId);
                String sql2 = String.format("DELETE FROM %s where %s='%s'", TABLE_NAME2, TABLE_CITY_F, CommonConstants.CITY_CHEONG_JU._cityId);

                db.execSQL(sql);
                db.execSQL(sql2);


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (oldVersion < 7) {
            try {

                String sql = String.format("DELETE FROM %s where %s='%s'", TABLE_FAVORITE_NAME, TABLE_CITY_F, CommonConstants.CITY_CHEONN_AN._cityId);
                String sql2 = String.format("DELETE FROM %s where %s='%s'", TABLE_NAME2, TABLE_CITY_F, CommonConstants.CITY_CHEONN_AN._cityId);

                db.execSQL(sql);
                db.execSQL(sql2);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (oldVersion < 8) {
            try {

                String sql = String.format("UPDATE %s SET NICKNAME='M6724' where _id=%s and typeId='90000450'", TABLE_FAVORITE_NAME, CommonConstants.CITY_SEO_UL._cityId);
                String sql2 = String.format("UPDATE %s SET NICKNAME='M6724' where _id=%s and typeId='90000450'", TABLE_NAME2, CommonConstants.CITY_SEO_UL._cityId);

                String sql3 = String.format("UPDATE %s SET NICKNAME='M6724' where _id=%s and typeId='165000381'", TABLE_FAVORITE_NAME, CommonConstants.CITY_IN_CHEON._cityId);
                String sql4 = String.format("UPDATE %s SET NICKNAME='M6724' where _id=%s and typeId='165000381'", TABLE_NAME2, CommonConstants.CITY_IN_CHEON._cityId);

                db.execSQL(sql);
                db.execSQL(sql2);
                db.execSQL(sql3);
                db.execSQL(sql4);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (oldVersion < 9) {
            try {

                String sql = String.format("DELETE FROM %s where %s='%s'", TABLE_FAVORITE_NAME, TABLE_CITY_F, CommonConstants.CITY_SEO_UL._cityId);
                String sql2 = String.format("DELETE FROM %s where %s='%s'", TABLE_NAME2, TABLE_CITY_F, CommonConstants.CITY_SEO_UL._cityId);

                db.execSQL(sql);
                db.execSQL(sql2);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (oldVersion < 10) {
            try {
                String sql = String.format("UPDATE %s SET NICKNAME='M6118' where _id=%s and typeId='165000335'", TABLE_FAVORITE_NAME, CommonConstants.CITY_SEO_UL._cityId);
                String sql2 = String.format("UPDATE %s SET NICKNAME='M6118' where _id=%s and typeId='165000335'", TABLE_NAME2, CommonConstants.CITY_SEO_UL._cityId);

                String sql3 = String.format("UPDATE %s SET NICKNAME='M6405' where _id=%s and typeId='165000215'", TABLE_FAVORITE_NAME, CommonConstants.CITY_SEO_UL._cityId);
                String sql4 = String.format("UPDATE %s SET NICKNAME='M6405' where _id=%s and typeId='165000215'", TABLE_NAME2, CommonConstants.CITY_SEO_UL._cityId);

                String sql5 = String.format("UPDATE %s SET NICKNAME='M6724' where _id=%s and typeId='165000381'", TABLE_FAVORITE_NAME, CommonConstants.CITY_SEO_UL._cityId);
                String sql6 = String.format("UPDATE %s SET NICKNAME='M6724' where _id=%s and typeId='165000381'", TABLE_NAME2, CommonConstants.CITY_SEO_UL._cityId);

                String sql7 = String.format("UPDATE %s SET NICKNAME='M6628' where _id=%s and typeId='165000409'", TABLE_FAVORITE_NAME, CommonConstants.CITY_SEO_UL._cityId);
                String sql8 = String.format("UPDATE %s SET NICKNAME='M6628' where _id=%s and typeId='165000409'", TABLE_NAME2, CommonConstants.CITY_SEO_UL._cityId);

                String sql9 = String.format("UPDATE %s SET NICKNAME='M6628' where _id=%s and typeId='165000409'", TABLE_FAVORITE_NAME, CommonConstants.CITY_IN_CHEON._cityId);
                String sql10 = String.format("UPDATE %s SET NICKNAME='M6628' where _id=%s and typeId='165000409'", TABLE_NAME2, CommonConstants.CITY_IN_CHEON._cityId);

                db.execSQL(sql);
                db.execSQL(sql2);
                db.execSQL(sql3);
                db.execSQL(sql4);
                db.execSQL(sql5);
                db.execSQL(sql6);
                db.execSQL(sql7);
                db.execSQL(sql8);
                db.execSQL(sql9);
                db.execSQL(sql10);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (oldVersion < 11) {
            try {
                String sql = String.format("DELETE FROM %s where %s='%s'", TABLE_FAVORITE_NAME, TABLE_CITY_F, CommonConstants.CITY_WON_JU._cityId);
                String sql2 = String.format("DELETE FROM %s where %s='%s'", TABLE_NAME2, TABLE_CITY_F, CommonConstants.CITY_WON_JU._cityId);

                db.execSQL(sql);
                db.execSQL(sql2);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (oldVersion < 12) {
            try {
                String sql = String.format("DELETE FROM %s where %s='%s'", TABLE_FAVORITE_NAME, TABLE_CITY_F, CommonConstants.CITY_SUN_CHEON._cityId);
                String sql2 = String.format("DELETE FROM %s where %s='%s'", TABLE_NAME2, TABLE_CITY_F, CommonConstants.CITY_SUN_CHEON._cityId);
                String sql3 = String.format("DELETE FROM %s where %s='%s'", TABLE_FAVORITE_NAME, TABLE_CITY_F, CommonConstants.CITY_GWANG_YANG._cityId);
                String sql4 = String.format("DELETE FROM %s where %s='%s'", TABLE_NAME2, TABLE_CITY_F, CommonConstants.CITY_GWANG_YANG._cityId);

                db.execSQL(sql);
                db.execSQL(sql2);
                db.execSQL(sql3);
                db.execSQL(sql4);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (oldVersion < 13) {
            try {
                String sql = String.format("DELETE FROM %s where %s='%s'", TABLE_FAVORITE_NAME, TABLE_CITY_F, CommonConstants.CITY_JE_JU._cityId);
                String sql2 = String.format("DELETE FROM %s where %s='%s'", TABLE_NAME2, TABLE_CITY_F, CommonConstants.CITY_JE_JU._cityId);

                db.execSQL(sql);
                db.execSQL(sql2);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (oldVersion < 14) {
            try {

                String sql = String.format("DELETE FROM %s where %s='%s'", TABLE_FAVORITE_NAME, TABLE_CITY_F, CommonConstants.CITY_CHIL_GOK._cityId);
                String sql2 = String.format("DELETE FROM %s where %s='%s'", TABLE_NAME2, TABLE_CITY_F, CommonConstants.CITY_CHIL_GOK._cityId);

                db.execSQL(sql);
                db.execSQL(sql2);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    public Cursor getFavoriteValue() {
        Cursor cursor = null;

        String sql = String.format("SELECT * FROM %s  order by %s asc, %s desc",
                TABLE_FAVORITE_NAME, TABLE_ORDER, TABLE_ID_F);
        cursor = getReadableDatabase().rawQuery(sql, null);

        return cursor;
    }


    public Cursor getFavoriteValue(int type) {
        Cursor cursor = null;

        String sql = String.format("SELECT * FROM %s WHERE %s=%s order by %s asc, %s desc",
                TABLE_FAVORITE_NAME, TABLE_TYPE_F, type, TABLE_ORDER, TABLE_ID_F);

        cursor = getReadableDatabase().rawQuery(sql, null);

        return cursor;
    }

    public Cursor getSettingValue() {
        Cursor cursor = null;

        String sql = String.format("SELECT * FROM %s",
                TABLE_NAME3, TABLE_TYPE_F);
        cursor = getReadableDatabase().rawQuery(sql, null);
        return cursor;
    }


    public Cursor getHistoryValue() {
        Cursor cursor = null;

        String sql = String.format("SELECT * FROM %s order by _id desc limit 5 ",
                TABLE_NAME2);
        cursor = getReadableDatabase().rawQuery(sql, null);

        return cursor;
    }


    public Cursor getHistoryValue(int type) {
        Cursor cursor = null;

        String sql = String.format("SELECT * FROM %s WHERE type=%s order by _id desc limit 5 ",
                TABLE_NAME2, type);
        cursor = getReadableDatabase().rawQuery(sql, null);

        return cursor;
    }

    public void updateFavorite(int id, String nickName, String nickName2, String color) {

        String sql = String.format("update %s set %s='%s', %s='%s',%s='%s' where %s=%s", TABLE_FAVORITE_NAME, TABLE_TYPE_NICK, nickName, TABLE_TYPE_NICK2, nickName2, TABLE_COLOR_F, color, TABLE_ID_F, id);
        getWritableDatabase().execSQL(sql);

    }

    public void inputFavorite(int type, int city, String typeID, String typeID2, String typeId3, String typeId4, String color, String nickName, String nickName2, String temp1, String temp2) {

        String selectSql = "", insertSql = "";

        if (typeID2 == null) {
            typeID2 = "0";
        }

        if (city == CommonConstants.CITY_BU_SAN._cityId) {
            selectSql = String.format("SELECT * from %s where type=%s and city=%s and typeId='%s'and typeId2='%s' and typeId3='%s' and typeId4='%s' and temp1='%s' "
                    , TABLE_FAVORITE_NAME, type, city, typeID, typeID2, typeId3, typeId4, temp1);
        } else {
            selectSql = String.format("SELECT * from %s where type=%s and city=%s and typeId='%s'and typeId2='%s' and typeId3='%s' and typeId4='%s' "
                    , TABLE_FAVORITE_NAME, type, city, typeID, typeID2, typeId3, typeId4);
        }


        Cursor cursor = getReadableDatabase().rawQuery(selectSql, null);
        if (cursor.getCount() == 0) {
            insertSql = String.format("INSERT INTO %s(city,type,typeID,typeID2,typeID3,typeID4,color,NICKNAME, NICKNAME2,temp1,temp2) VALUES(%s,%s,'%s','%s','%s','%s','%s','%s','%s','%s','%s')",
                    TABLE_FAVORITE_NAME, city, type, typeID, typeID2, typeId3, typeId4, color, nickName, nickName2, temp1, temp2);
            getWritableDatabase().execSQL(insertSql);

        }
        cursor.close();
    }


    public void inputHistory(int type, int city, String typeID, String typeID2, String typeID3, String typeId4, String color, String nickName, String nickName2, String temp1, String temp2) {
        String selectSql = "", insertSql = "";

        if (typeID2 == null) {
            typeID2 = "0";
        }


        if (city == CommonConstants.CITY_BU_SAN._cityId) {
            selectSql = String.format("SELECT * from %s where type=%s and city=%s and typeId='%s' and typeID2='%s' and typeID3='%s' and typeID4='%s' and temp1='%s'"
                    , TABLE_NAME2, type, city, typeID, typeID2, typeID3, typeId4, temp1);
        } else {
            selectSql = String.format("SELECT * from %s where type=%s and city=%s and typeId='%s' and typeID2='%s' and typeID3='%s' and typeID4='%s'"
                    , TABLE_NAME2, type, city, typeID, typeID2, typeID3, typeId4);
        }


        Cursor cursor = getReadableDatabase().rawQuery(selectSql, null);
        if (cursor.getCount() == 0) {
            insertSql = String.format("INSERT INTO %s(city,type,typeID,typeID2,typeID3,typeID4,color,NICKNAME,NICKNAME2,temp1,temp2) VALUES(%s,%s,'%s','%s','%s','%s','%s','%s','%s','%s','%s')",
                    TABLE_NAME2, city, type, typeID, typeID2, typeID3, typeId4, color, nickName, nickName2, temp1, temp2);
            getWritableDatabase().execSQL(insertSql);

        }
        cursor.close();


        selectSql = String.format("SELECT * from %s where type=%s order by _id asc", TABLE_NAME2, type);
        cursor = getReadableDatabase().rawQuery(selectSql, null);
        if (cursor.getCount() > 5) {
            if (cursor.moveToFirst()) {
                String delSql = String.format("Delete from %s where type=%s  and _id=%s", TABLE_NAME2, type, cursor.getInt(0));
                getWritableDatabase().execSQL(delSql);
            }
        }

        cursor.close();

    }

    public void insertSettingCity(int cityId) {

        String insertSql = String.format("INSERT Or Ignore INTO %s(locals) VALUES(%s)",
                TABLE_NAME3, cityId);
        getWritableDatabase().execSQL(insertSql);

    }

    public void deleteSettingCity(int cityId) {
        String delSql = String.format("DELETE FROM %s where locals=%s",
                TABLE_NAME3, cityId);
        getWritableDatabase().execSQL(delSql);
    }

    public void deleteHistory(int _id) {
        String delSql = String.format("DELETE FROM %s where _id=%s",
                TABLE_NAME2, _id);
        getWritableDatabase().execSQL(delSql);
    }

    public void deleteFavorite(int _id) {
        String delSql = String.format("DELETE FROM %s where _id=%s",
                TABLE_FAVORITE_NAME, _id);
        getWritableDatabase().execSQL(delSql);
    }


    public void insertLocationXY(String locationXY, String locationName, String apiId) {
        String insertSql = String.format("INSERT INTO %s(%s,%s,%s) VALUES('%s','%s','%s')",
                TABLE_NAME4, TABLE_LOCATION_XY, TABLE_API_ID, TABLE_LOCALNAME, locationXY, apiId, locationName);
        getWritableDatabase().execSQL(insertSql);
    }

    public Cursor getLocationXY(String locationName, String apiId) {
        String selectSql = String.format("SELECT * FROM %s where %s='%s' and %s='%s' ", TABLE_NAME4, TABLE_API_ID, apiId, TABLE_LOCALNAME, locationName);
        Cursor cursor = getReadableDatabase().rawQuery(selectSql, null);
        return cursor;
    }

}

