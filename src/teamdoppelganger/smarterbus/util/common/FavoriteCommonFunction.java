package teamdoppelganger.smarterbus.util.common;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smart.lib.CommonConstants;

import java.util.ArrayList;
import java.util.List;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.bs.bsCursor;
import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.item.WorkFavoriteItem;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;

public class FavoriteCommonFunction {
    static public ArrayList<FavoriteAndHistoryItem> getFavoriteAndHistoryItemList(SQLiteDatabase busDb, LocalDBHelper localDb, SBInforApplication application, int[] favoriteIds) {
        List<WorkFavoriteItem> favoriteItemList = localDb.workFavoriteListFromIds(favoriteIds);
        return getFavoriteAndHistoryItemList(busDb, application, favoriteItemList);
    }
    static public ArrayList<FavoriteAndHistoryItem> getFavoriteAndHistoryItemList(SQLiteDatabase busDb, SBInforApplication application, List<WorkFavoriteItem> favoriteItemList) {
        ArrayList<FavoriteAndHistoryItem> mTmpFavoriteAndHistoryItem = new ArrayList<>();

        for(int i = 0, j = favoriteItemList.size(); i<j; i++){
            WorkFavoriteItem favoriteItem = favoriteItemList.get(i);

            int _id = favoriteItem.id;
            int cityId = favoriteItem.city;

            String type = favoriteItem.type+"";
            String typeId1 = favoriteItem.typeID;
            String typeId2 = favoriteItem.typeID2;
            String typeId3 = favoriteItem.typeID3;
            String typeId4 = favoriteItem.typeID4;

            String nick = favoriteItem.nickname;
            String nick2 = favoriteItem.nickname2;

            String color = favoriteItem.color;

            String temp1 = favoriteItem.temp1;
            String temp2 = favoriteItem.temp2;

            FavoriteAndHistoryItem item = new FavoriteAndHistoryItem();
            item.id = _id;
            item.nickName = nick;
            item.nickName2 = nick2;
            item.color = color;
            item.key = i;

            String cityEnName = ((SBInforApplication) application.getApplicationContext()).mHashLocation.get(cityId);

            if (type.equals(String.valueOf(Constants.BUS_TYPE))) {

                String sql;

                if (typeId2 == null || typeId2.equals("")) {

                    sql = String.format("Select * From %s where %s='%s'",
                            cityEnName + "_route", CommonConstants.BUS_ROUTE_ID1, typeId1);

                } else {

                    if (cityEnName.equals(CommonConstants.CITY_GU_MI._engName) || cityEnName.equals(CommonConstants.CITY_CHIL_GOK._engName)) {

                        sql = String.format("Select * From %s where %s='%s' and %s='%s' and %s='%s'",
                                cityEnName + "_route", CommonConstants.BUS_ROUTE_ID1, typeId1, CommonConstants.BUS_ROUTE_ID2, typeId2
                                , CommonConstants.BUS_ROUTE_NAME, nick2);
                    } else {

                        sql = String.format("Select * From %s where %s='%s' and %s='%s'",
                                cityEnName + "_route", CommonConstants.BUS_ROUTE_ID1, typeId1, CommonConstants.BUS_ROUTE_ID2, typeId2);

                    }

                }


                Cursor busCursor = busDb.rawQuery(sql, null);

                if (busCursor.moveToNext()) {
                    item.type = Constants.FAVORITE_TYPE_BUS;
                    item.busRouteItem.busRouteApiId = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID1));
                    item.busRouteItem.busRouteApiId2 = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID2));
                    item.busRouteItem.busRouteName = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_NAME));
                    item.busRouteItem.busRouteSubName = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_SUB_NAME));
                    item.busRouteItem.startStop = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_START_STOP_ID));
                    item.busRouteItem.endStop = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_END_STOP_ID));
                    item.busRouteItem.busType = busCursor.getInt(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));
                    item.busRouteItem._id = busCursor.getInt(busCursor.getColumnIndex(CommonConstants._ID));

                    item.busRouteItem.localInfoId = String.valueOf(cityId);
                    mTmpFavoriteAndHistoryItem.add(item);
                }
                busCursor.close();
            } else if (type.equals(String.valueOf(Constants.STOP_TYPE))) {

                String sql = "";

                int apiType = RequestCommonFuction.getApiTpye(cityId);


                if (apiType == Constants.API_TYPE_3) {
                    sql = String.format("SELECT * FROM %s_Stop where %s='%s' ", cityEnName,
                            CommonConstants.BUS_STOP_ARS_ID, typeId4);
                } else if (apiType == Constants.API_TYPE_1) {
                    sql = String.format("SELECT * FROM %s_Stop where %s='%s' ", cityEnName,
                            CommonConstants.BUS_STOP_API_ID, typeId3);
                } else if (apiType == Constants.API_TYPE_2) {


                    if (typeId3 == null || typeId3.trim().equals("") || typeId3.trim().equals("null")) {
                        typeId4 = String.valueOf(Integer.parseInt(typeId4));
                        sql = String.format(
                                "Select * From %s where %s='%s' ", cityEnName
                                        + "_stop", CommonConstants.BUS_STOP_ARS_ID,
                                typeId4);
                    } else if (typeId4 == null || typeId4.trim().equals("") || typeId4.trim().equals("null")) {
                        typeId3 = String.valueOf(Integer.parseInt(typeId3));
                        sql = String.format(
                                "Select * From %s where %s='%s' ", cityEnName
                                        + "_stop", CommonConstants.BUS_STOP_API_ID,
                                typeId3);

                    } else {
                        typeId4 = String.valueOf(Integer.parseInt(typeId4));
                        sql = String.format(
                                "Select * From %s where %s='%s' and %s='%s'", cityEnName
                                        + "_stop", CommonConstants.BUS_STOP_API_ID,
                                typeId3, CommonConstants.BUS_STOP_ARS_ID,
                                typeId4);
                    }

                } else if (apiType == Constants.API_TYPE_4) {

                    sql = String.format("SELECT * FROM %s_Stop where %s='%s' ", cityEnName,
                            CommonConstants.BUS_STOP_DESC, temp1);
                }


                Cursor busCursor = busDb.rawQuery(sql, null);
                if (busCursor.moveToNext()) {
                    item.type = Constants.FAVORITE_TYPE_STOP;
                    item.busStopItem.apiId = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    item.busStopItem.arsId = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    item.busStopItem.name = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    item.busStopItem._id = busCursor.getInt(busCursor.getColumnIndex(CommonConstants._ID));
                    item.busStopItem.tempId2 = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_DESC));

                    item.busStopItem.localInfoId = String.valueOf(cityId);


                    mTmpFavoriteAndHistoryItem.add(item);
                }
                busCursor.close();
            } else if (type.equals(String.valueOf(Constants.FAVORITE_TYPE_BUS_STOP))) {


                String sql = "";

                int apiType = RequestCommonFuction.getApiTpye(cityId);


                if (apiType == Constants.API_TYPE_3) {
                    sql = String.format("SELECT * FROM %s_Stop where %s='%s' ", cityEnName,
                            CommonConstants.BUS_STOP_ARS_ID, typeId4);
                } else if (apiType == Constants.API_TYPE_1) {
                    sql = String.format("SELECT * FROM %s_Stop where %s='%s' ", cityEnName,
                            CommonConstants.BUS_STOP_API_ID, typeId3);
                } else if (apiType == Constants.API_TYPE_2) {

                    if (typeId3 == null || typeId3.trim().equals("") || typeId3.trim().equals("null")) {
                        typeId4 = String.valueOf(Integer.parseInt(typeId4));
                        sql = String.format(
                                "Select * From %s where %s='%s' ", cityEnName
                                        + "_stop", CommonConstants.BUS_STOP_ARS_ID,
                                typeId4);
                    } else if (typeId4 == null || typeId4.trim().equals("") || typeId4.trim().equals("null")) {
                        typeId3 = String.valueOf(Integer.parseInt(typeId3));
                        sql = String.format(
                                "Select * From %s where %s='%s' ", cityEnName
                                        + "_stop", CommonConstants.BUS_STOP_API_ID,
                                typeId3);

                    } else {
                        typeId4 = String.valueOf(Integer.parseInt(typeId4));
                        sql = String.format(
                                "Select * From %s where %s='%s' and %s='%s'", cityEnName
                                        + "_stop", CommonConstants.BUS_STOP_API_ID,
                                typeId3, CommonConstants.BUS_STOP_ARS_ID,
                                typeId4);
                    }

                } else if (apiType == Constants.API_TYPE_4) {
                    sql = String.format("SELECT * FROM %s_Stop where %s='%s' ", cityEnName,
                            CommonConstants.BUS_STOP_DESC, temp1);
                }


                if (cityEnName.equals(CommonConstants.CITY_GU_MI._engName) || cityEnName.equals(CommonConstants.CITY_CHIL_GOK._engName)) {
                    if (typeId2.equals("null")) {
                        typeId2 = "";
                    }
                } else {
                    if (typeId2.equals("null") || typeId2.equals("0")) {
                        typeId2 = "";
                    }
                }

                String sql2 = null;

                if (cityEnName.equals(CommonConstants.CITY_GU_MI._engName) || cityEnName.equals(CommonConstants.CITY_CHIL_GOK._engName)) {

                    sql2 = String.format("Select * From %s where %s='%s' and %s='%s' and %s='%s'",
                            cityEnName + "_route", CommonConstants.BUS_ROUTE_ID1, typeId1, CommonConstants.BUS_ROUTE_ID2, typeId2
                            , CommonConstants.BUS_ROUTE_NAME, nick2);
                } else {

                    if (typeId2 == null || typeId2.equals("")) {

                        sql2 = String.format("Select * From %s where %s='%s'",
                                cityEnName + "_route", CommonConstants.BUS_ROUTE_ID1, typeId1);

                    } else {

                        sql2 = String.format("Select * From %s where %s='%s' and %s='%s'",
                                cityEnName + "_route", CommonConstants.BUS_ROUTE_ID1, typeId1, CommonConstants.BUS_ROUTE_ID2, typeId2);
                    }

                }

                Cursor busCursor2 = busDb.rawQuery(sql2, null);
                Cursor busCursor = busDb.rawQuery(sql, null);

                if (busCursor2.moveToNext() && busCursor.moveToNext()) {


                    item.type = Constants.FAVORITE_TYPE_BUS_STOP;
                    item.busRouteItem.busRouteApiId = busCursor2.getString(busCursor2.getColumnIndex(CommonConstants.BUS_ROUTE_ID1));
                    item.busRouteItem.busRouteApiId2 = busCursor2.getString(busCursor2.getColumnIndex(CommonConstants.BUS_ROUTE_ID2));
                    item.busRouteItem.busRouteName = busCursor2.getString(busCursor2.getColumnIndex(CommonConstants.BUS_ROUTE_NAME));
                    item.busRouteItem.busRouteSubName = busCursor2.getString(busCursor2.getColumnIndex(CommonConstants.BUS_ROUTE_SUB_NAME));
                    item.busRouteItem.startStop = busCursor2.getString(busCursor2.getColumnIndex(CommonConstants.BUS_ROUTE_START_STOP_ID));
                    item.busRouteItem.endStop = busCursor2.getString(busCursor2.getColumnIndex(CommonConstants.BUS_ROUTE_END_STOP_ID));
                    item.busRouteItem.busType = busCursor2.getInt(busCursor2.getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));

                    if (temp2 != null && temp2.trim().length() > 0) {
                        item.busRouteItem.direction = temp2;
                    }

                    item.busRouteItem.busStopApiId = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    item.busRouteItem.busStopArsId = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    item.busRouteItem.busStopName = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    item.busRouteItem.tmpId = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_DESC));

                    item.busStopItem._id = busCursor.getInt(busCursor.getColumnIndex(CommonConstants._ID));


                    item.busRouteItem.localInfoId = String.valueOf(cityId);
                    item.busStopItem.localInfoId = String.valueOf(cityId);

                    mTmpFavoriteAndHistoryItem.add(item);

                }


                busCursor.close();
                busCursor2.close();

            }
            favoriteItem.busRouteItem = item.busRouteItem;
            favoriteItem.busStopItem = item.busStopItem;
        }
        return mTmpFavoriteAndHistoryItem;
    }



}