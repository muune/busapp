package teamdoppelganger.smarterbus.item;

import java.util.ArrayList;

public class NotificationItem {

    public int id;
    public String title;
    public String contents;
    public int buttonType;
    public String link;
    public int minVersion;
    public int maxVersion;
    public String buttonName;

    public ArrayList<LocalNotiItem> localNotiItem;

    public NotificationItem() {
        localNotiItem = new ArrayList<LocalNotiItem>();
    }


}
