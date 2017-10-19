package teamdoppelganger.smarterbus.item;

final public class WorkDayItem {
    public int dayofweekRowid; //1:일, 2:월, 3:화...
    public String name; //요일
    public WorkDayItem(int $dayofweekRowid, String $name){
        dayofweekRowid = $dayofweekRowid;
        name = $name;
    }
}
