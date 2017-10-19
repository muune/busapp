package teamdoppelganger.smarterbus.item;

import java.io.Serializable;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * BUS넘버에 따른 type 지정
 *
 * @author DOPPELSOFT4
 */

public class BusRouteItem extends CommonItem implements Serializable {

    public String busRouteArsId;
    public String busRouteApiId;
    public String busRouteApiId2 = "";
    public String busRouteName;
    public String busRouteSubName = "";
    public String busRouteInitialName;

    public String startStop;
    public String endStop;
    public String localInfoId;
    public String nextDirctionName;

    public ArrayList<ArriveItem> arriveInfo;

    public int busType = 0;
    public int firstTime;
    public int lastTime;

    //추가적인 정보
    public String busRouteNickName;
    public String busAlarmDivideName; //알람을 위한 추가 설정
    public String stopApiOfRoute;   //알람을 위한 추가 설정

    public String busStopArsId; //어느 정류장에서 검색했는지 알기위해
    public String busStopName;
    public String busStopApiId;
    public String direction; //1,0 정방향 /역방향
    public String relateStop;

    //서울 역 순서정보 추가
    public String stOrd;

    //서울쪽때문에 추가 정보 들어가
    public ArrayList<String> plusInforAry;


    public int plusParsingNeed = 0;
    public int index = 0;
    public int _id;
    public int _stopId;
    public String tmpId;

    public boolean isChecked = false;
    public boolean isSection = false;
    public boolean isAlarmAble = false;
    public boolean isTimeTable = false;
    public boolean isEnd = false;  //정보 완료 체크를 위한 변수


    public BusRouteItem() {
        arriveInfo = new ArrayList<ArriveItem>();
        plusInforAry = new ArrayList<String>();

    }

    public BusRouteItem(Parcel in) {
        busRouteArsId = in.readString();
        busRouteApiId = in.readString();
        busRouteApiId2 = in.readString();
        busRouteName = in.readString();
        busRouteSubName = in.readString();
        busRouteInitialName = in.readString();
        nextDirctionName = in.readString();
        relateStop = in.readString();

        startStop = in.readString();
        endStop = in.readString();
        localInfoId = in.readString();

        busType = in.readInt();
        firstTime = in.readInt();
        lastTime = in.readInt();
        plusParsingNeed = in.readInt();
        index = in.readInt();
        _id = in.readInt();
        _stopId = in.readInt();

    }


}
