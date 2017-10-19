package teamdoppelganger.smarterbus.item;

import java.io.Serializable;

public class FavoriteAndHistoryItem implements Serializable {

    public int key;
    public int id = -1;
    public int type; //버스 or 정류장 or 정류장과버스


    public String nickName, nickName2;
    public BusRouteItem busRouteItem;
    public BusStopItem busStopItem;
    public String color;

    public boolean isFavorite = false;


    public FavoriteAndHistoryItem() {
        busRouteItem = new BusRouteItem();
        busStopItem = new BusStopItem();
    }


    public FavoriteAndHistoryItem(FavoriteAndHistoryItem item) {
        key = item.key;
        id = item.id;
        type = item.type;
        nickName = item.nickName;
        busRouteItem = item.busRouteItem;
        busStopItem = item.busStopItem;
        color = item.color;
        isFavorite = item.isFavorite;

    }


}
