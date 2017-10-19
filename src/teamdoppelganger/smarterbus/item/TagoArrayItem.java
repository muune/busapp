package teamdoppelganger.smarterbus.item;

import java.io.Serializable;
import java.util.ArrayList;

public class TagoArrayItem implements Serializable {

    public ArrayList<TagoItem> tagoArrayItem;

    public TagoArrayItem() {
        tagoArrayItem = new ArrayList<TagoItem>();
    }

}
