package teamdoppelganger.smarterbus.item;

//workon, workoff db 테이블 value object
final public class WorkTimeItem {
    public int rowid;
    public int hour; //0~23
    public int min; //0~59
    public int pid; //alarm용 pid

    public WorkTimeItem(int $rowid, int $hour, int $min, int $pid){
        rowid = $rowid;
        hour = $hour;
        min = $min;
        pid = $pid;
    }
}
