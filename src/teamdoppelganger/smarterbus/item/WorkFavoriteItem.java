package teamdoppelganger.smarterbus.item;


final public class WorkFavoriteItem {
    public int rowid;
    public int id;
    public int city;
    public int type;
    public String typeID;
    public String typeID2;
    public String typeID3;
    public String typeID4;
    public String nickname;
    public String nickname2;
    public String color;
    public int order;
    public String temp1;
    public String temp2;

    //아래 것은 FavoriteCommonFunction.getFavoriteAndHistoryItemList 에서 셋팅된다.
    public BusRouteItem busRouteItem;
    public BusStopItem busStopItem;
}
