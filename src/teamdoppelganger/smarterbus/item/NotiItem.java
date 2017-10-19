package teamdoppelganger.smarterbus.item;

import java.io.Serializable;

public class NotiItem implements Serializable {

    public String fileName;
    public boolean isDownNeed = false;
    public int dbVersion = 0;
    public String strEventBanner = "";

}
