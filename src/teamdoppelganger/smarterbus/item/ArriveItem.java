package teamdoppelganger.smarterbus.item;

import java.io.Serializable;

public class ArriveItem implements Serializable {

    public int remainStop = -1;
    public int remainMin = -1;
    public int remainSecond = -1;

    public String plainNum;
    public int state;

    public String elseStr = "";

}
