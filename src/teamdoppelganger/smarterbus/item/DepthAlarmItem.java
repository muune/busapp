package teamdoppelganger.smarterbus.item;

import java.util.ArrayList;

public class DepthAlarmItem extends DepthItem {

    public ArrayList<ArriveItem> busAlarmItem = new ArrayList<ArriveItem>();
    public int state = -1;  //state =0 인 경우는 결과 값 null 인상태

}
