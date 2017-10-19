package teamdoppelganger.smarterbus.item;


import java.io.Serializable;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 버스 정류장에 따른 type 지정
 *
 * @author DOPPELSOFT4
 */

//투두 : 이름 변경
public class BusStopItem extends CommonItem implements Serializable {

    public String name;
    public String arsId;
    public String apiId;
    public String startTime;
    public String endTime;

    //지역
    public double latitude;
    public double longtitude;
    //지역 구분
    public String localInfoId;


    //현재역에 버스가 와있는지 없는지
    public boolean isExist = false;
    public String plainNum = "";

    public ArrayList<String> relatedRoutes;

    //추가적인 정보
    public String busStopNickName;
    public int plusParsingNeed = 0;

    public String tempId, tempId2;
    public String tmpString;

    public int index;
    public int _id = 0;
    public int apiType = 0;
    public int position = 2;

    public boolean isChecked = false;
    public boolean isTurn = false;

    public BusStopItem() {
        relatedRoutes = new ArrayList<String>();
    }

    public BusStopItem(Parcel in) {
        name = in.readString();
        arsId = in.readString();
        apiId = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        latitude = in.readDouble();
        longtitude = in.readDouble();
        localInfoId = in.readString();
        plusParsingNeed = in.readInt();
        _id = in.readInt();
        relatedRoutes = new ArrayList<String>();

    }


}
