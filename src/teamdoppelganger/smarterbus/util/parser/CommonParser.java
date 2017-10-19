package teamdoppelganger.smarterbus.util.parser;

import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.DepthAlarmItem;
import teamdoppelganger.smarterbus.item.DepthFavoriteItem;
import teamdoppelganger.smarterbus.item.DepthRouteItem;
import teamdoppelganger.smarterbus.item.DepthStopItem;

import android.database.sqlite.SQLiteDatabase;

/**
 * parser 상위 클래스
 *
 * @author DOPPELSOFT4
 */
public abstract class CommonParser {

    //각 지역별 파싱해오는 주소 셋팅
    String mStopUrl;
    String mLineUrl;
    SQLiteDatabase mSqliteDb;

    public CommonParser(SQLiteDatabase db) {
        mSqliteDb = db;
    }


    public void setUrl(String stopUrl, String lineUrl) {
        mStopUrl = stopUrl;
        mLineUrl = lineUrl;
    }

    public abstract DepthStopItem getStopList(BusRouteItem rItem);

    public abstract DepthRouteItem getLineList(BusStopItem sItem);

    public abstract DepthRouteItem getAlarmList(String... idStr);//String busStopApiId, String busRouteApidId, ord


    public abstract DepthRouteItem getLineDetailList(BusRouteItem busRouteItem);

    public abstract DepthStopItem getStopDetailList(BusStopItem busStopItem);


    public DepthRouteItem getDepthLineList(DepthRouteItem sItem) {
        return null;
    }

    public DepthStopItem getDepthStopList(DepthStopItem sItem) {
        return null;
    }

    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {
        return null;
    }

    public DepthFavoriteItem getDepthRefreshList(DepthFavoriteItem sItem) {

        if (sItem.favoriteAndHistoryItems.size() > 0) {
            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.clear();
        }

        return null;

    }

    protected String getStopUrl() {
        return mStopUrl;
    }

    protected String getLineUrl() {
        return mLineUrl;
    }

    public SQLiteDatabase getBusDb() {
        return mSqliteDb;
    }

}
