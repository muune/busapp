package teamdoppelganger.smarterbus.item;

import java.util.ArrayList;

public class FindBusRouteItem {

    ArrayList<BusRouteItem> busRouteItem;
    ArrayList<String> descriptions;
    String time;
    String distance;
    String walkDistance;

    public FindBusRouteItem() {
        busRouteItem = new ArrayList<BusRouteItem>();
        descriptions = new ArrayList<String>();
    }

}
