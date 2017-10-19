package teamdoppelganger.smarterbus.item;

import java.io.Serializable;
import java.util.ArrayList;

public class TagoItem implements Serializable {

    public String mapId = null;

    public String totalDistance;
    public String totalBusDistance;
    public String totalTime;
    public String totalWalk;
    public String totalStationCount;


    public double startStopX, startStopY;
    public double endStopX, endStopY;

    public double startLocX, startLocY;
    public double endLocX, endLocY;

    public String srcName, dstName;
    public String srcStopName, dstStopName;
    public String srcStopId, endStopId;

    public String cityId;

    public int busId;
    public int gap;

    public int startIndex, endIndex;

    public String busName;
    public String busType;
    public String stopList;


    public boolean isInverse = false;

    public ArrayList<TagoBusItem> mTagoBusList;

    public TagoItem() {
        mTagoBusList = new ArrayList<TagoBusItem>();
    }

}
