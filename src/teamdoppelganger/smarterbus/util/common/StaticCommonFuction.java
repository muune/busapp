package teamdoppelganger.smarterbus.util.common;

import java.io.UnsupportedEncodingException;

import teamdoppelganger.smarterbus.util.db.LocalDBHelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Xml.Encoding;

import com.smart.lib.CommonConstants;

public class StaticCommonFuction {

    public void inputFavorteAndRecent(SQLiteDatabase db, LocalDBHelper localDb, boolean isFavorite, String cityName, int typeId, String apiId, String apiId2
            , String apiId3, String apiId4, String color, String nickName, String nickName2, String temp1, String temp2) {

        String subStr = null;

        String sql = String.format("SELECT * FROM %s where %s='%s'",
                CommonConstants.TBL_CITY, CommonConstants.CITY_ID, cityName);
        Cursor locationCur = db.rawQuery(sql, null);

        if (locationCur.moveToNext()) {
            int cityId = locationCur.getInt(locationCur
                    .getColumnIndex(CommonConstants.CITY_ID));

            if (isFavorite) {
                localDb.inputFavorite(typeId, cityId, apiId, apiId2, apiId3, apiId4, color, nickName, nickName2, temp1, temp2);
            } else {
                localDb.inputHistory(typeId, cityId, apiId, apiId2, apiId3, apiId4, color, nickName, nickName2, temp1, temp2);
            }
        }

        locationCur.close();

    }

    public static int getBusType(SQLiteDatabase db, String busRouteApiId, String localEnName) {

        int result = 0;

        String sql = String.format("SELECT *FROM %s WHERE %s='%s'", localEnName + "_Route"
                , CommonConstants.BUS_ROUTE_ID1, busRouteApiId);


        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));
        }

        cursor.close();

        return result;
    }


    public static String unicodeToKo(String str) {

        String result = "";
        String uniCodeStr;

        uniCodeStr = new String(str.getBytes());
        String[] rawUnicode = uniCodeStr.replace("\\", "//").split("//u");

        if (rawUnicode.length > 0) {
            result = rawUnicode[0];
        }
        result = rawUnicode[0];


        for (int i = 1; i < rawUnicode.length; i++) {

            String splitStr = null;

            if (rawUnicode[i].length() > 4) {
                splitStr = rawUnicode[i].substring(0, 4);
                char c = (char) Integer.parseInt(splitStr, 16);
                result = result + c + rawUnicode[i].substring(4, rawUnicode[i].length() - 1);

            } else {
                char c = (char) Integer.parseInt(rawUnicode[i], 16);
                result = result + c;
            }
        }

        return result;

    }


    public static boolean isNullOrTrash(String str) {
        if (str == null) return true;
        if (str.toString().trim().length() == 0) return true;

        return false;
    }


}
