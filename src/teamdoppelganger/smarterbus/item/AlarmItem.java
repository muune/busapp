package teamdoppelganger.smarterbus.item;

import java.io.Serializable;

import teamdoppelganger.smarterbus.common.Constants;

public class AlarmItem implements Serializable {

    public int target;
    public int min;
    public String BusColor;
    public BusRouteItem busRouteItem;

}
