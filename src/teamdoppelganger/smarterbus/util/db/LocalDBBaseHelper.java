package teamdoppelganger.smarterbus.util.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import teamdoppelganger.smarterbus.bs.bsCursor;
import teamdoppelganger.smarterbus.bs.bsSQLite;
import teamdoppelganger.smarterbus.item.BusOffItem;
import teamdoppelganger.smarterbus.item.BusOnItem;
import teamdoppelganger.smarterbus.item.WorkDayItem;
import teamdoppelganger.smarterbus.item.WorkFavoriteItem;
import teamdoppelganger.smarterbus.item.WorkTimeItem;

class LocalDBBaseHelper extends bsSQLite {
    LocalDBBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, version, null);
        //SQL을 Assert에서 로드함
        queryLoad("db_create.sql", "db_work.sql", "db_busonoff.sql");

        //테이블 생성. 이미 있으면 무시됨.
        String keys[] = {
                "dayofweek_create", "dayofweek_add", "member_create", "member_default_add", "work_create", "work_default_add",
                "workday_create", "workon_create", "workoff_create", "workonfavorite_create",  "workofffavorite_create", "workonactive_create", "workoffactive_create", "alarmpid_create",
                "buson_create", "busoff_create"
        };
        for (String k: keys){
            exec(k);
            query(k, null); //이제 필요없으므로 삭제
        }
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    private int fieldInt(bsCursor $c, String $k){
        int i = $c.getColumnIndex($k);
        if(i == -1) throw new RuntimeException("Not Exist db table key. Input key is " + $k);
        return $c.getInt(i);
    }
    private float fieldFloat(bsCursor $c, String $k){
        int i = $c.getColumnIndex($k);
        if(i == -1)  throw new RuntimeException("Not Exist db table key. Input key is " + $k);
        return $c.getFloat(i);
    }
    private String fieldStr(bsCursor $c, String $k){
        int i = $c.getColumnIndex($k);
        if(i == -1) throw new RuntimeException("Not Exist db table key. Input key is " + $k);
        return $c.getString(i);
    }
    //출근시간 활성화
    public void workOnActive(){
        exec("alarmpid_add");
        exec("workon_default_set");
        exec("workonactive_add");
    }
    //출근시간 비활성화
    public void workOnUnactive(){
        exec("workonactive_del");
    }
    //퇴근시간 활성화
    public void workOffActive(){
        exec("alarmpid_add");
        exec("workoff_default_set");
        exec("workoffactive_add");
    }
    //퇴근시간 비활성화
    public void workOffUnactive(){
        exec("workoffactive_del");
    }
    //출근시간 활성화인가?
    public boolean workOnIsactive(){
        Cursor c = select("workon_isactive");
        if( c == null ) return false;
        if( c.getCount() == 0 ) {
            c.close();
            return false;
        }
        bsCursor _c = new bsCursor(c);
        boolean isactive = fieldInt(_c, "cnt") == 1;
        c.close();
        return isactive;
    }
    //출근시간 활성화인가?
    public boolean workOffIsactive(){
        Cursor c = select("workoff_isactive");
        if( c == null ) return false;
        if( c.getCount() == 0 ) {
            c.close();
            return false;
        }
        bsCursor _c = new bsCursor(c);
        boolean isactive = fieldInt(_c, "cnt") == 1;
        c.close();
        return isactive;
    }
    //출근시간 설정
    public void workonSet(int $hour, int $min){
        exec("workon_set", "hour", $hour + "", "min", $min + "");
        exec("alarmpid_add");
        exec("workonactive_add"); //새 alarm pid 교체
    }
    //퇴근시간 설정
    public void workOffSet(int $hour, int $min){
        exec("workoff_set", "hour", $hour + "", "min", $min + "");
        exec("alarmpid_add");
        exec("workoffactive_add"); //새 alarm pid 교체
    }
    //출근시간 얻기
    @Nullable
    public WorkTimeItem workOnGet(){
        return workItem("workon_get");
    }
    //퇴근시간 얻기
    @Nullable
    public WorkTimeItem workOffGet(){
        return workItem("workoff_get");
    }
    @Nullable
    private WorkTimeItem workItem(String $k){
        Cursor c = select($k);
        if( c == null ) return null;
        if( c.getCount() == 0 ) {
            c.close();
            return null;
        }
        bsCursor _c = new bsCursor(c);
        WorkTimeItem item = new WorkTimeItem(fieldInt(_c, "rowid"), fieldInt(_c, "hour"), fieldInt(_c, "min"), fieldInt(_c, "pid"));
        c.close();
        return item;
    }
    //근무요일 설정
    public void workDaySet(boolean[] $dayList){
        String dayofweek_rowids = "100"; //모두 삭제 처리
        if($dayList.length != 7)  throw new RuntimeException("workDaySet $dayList.length != 7");
        else{
            dayofweek_rowids = "";
            for(int i = 0; i < 7; i++){
                if($dayList[i]) dayofweek_rowids += (i+1) + ","; // 1일, 2월, 3화, 4수, 5목, 6금, 7토
            }
            if(dayofweek_rowids.length() > 0) dayofweek_rowids = dayofweek_rowids.substring(0, dayofweek_rowids.length()-1);
        }
        exec("workday_del", "dayofweek_rowids", dayofweek_rowids);
        exec("workday_add", "dayofweek_rowids", dayofweek_rowids);
    }
    //근무요일 가져오기
    public List<WorkDayItem> workDayList(){
        List<WorkDayItem> list = new ArrayList<>();
        Cursor c = select("workday_list");
        if(c == null) return list;
        if(c.getCount() == 0) {
            c.close();
            return list;
        }
        bsCursor _c = new bsCursor(c);
        if(_c.moveToFirst()){
            do {
                list.add(new WorkDayItem(fieldInt(_c, "dayofweek_rowid"), fieldStr(_c, "name")));
            } while (_c.moveToNext());
        }
        c.close();
        return list;
    }
    //id로 부터 즐겨찾기 리스트 가져오기
    public List<WorkFavoriteItem> workFavoriteListFromIds(int[] favoriteIds){
        String favorite_ids = "";
        Cursor c;
        if(favoriteIds.length == 0){
            c = null;
        }else{
            for(int i = 0; i < favoriteIds.length; i++){
                favorite_ids += favoriteIds[i];
                if(i != favoriteIds.length - 1) favorite_ids += ",";
            }
            c = select("workfavorite_list_from_ids", "favorite_ids", favorite_ids);
        }
        return workFavoriteList(c);
    }
    //출근길 선택 대상 목록
    public List<WorkFavoriteItem> workonfavoriteSelectList(){
        Cursor c = select("workonfavorite_select_list");
        return workFavoriteList(c);
    }
    //퇴근길 선택 대상 목록
    public List<WorkFavoriteItem> workofffavoriteSelectList(){
        Cursor c = select("workofffavorite_select_list");
        return workFavoriteList(c);
    }
    //출근길 목록
    public List<WorkFavoriteItem> workonfavoriteList(){
        Cursor c = select("workonfavorite_list");
        return workFavoriteList(c);
    }
    //퇴근길 목록
    public List<WorkFavoriteItem> workofffavoriteList(){
        Cursor c = select("workofffavorite_list");
        return workFavoriteList(c);
    }
    private List<WorkFavoriteItem> workFavoriteList(final Cursor $c){
        List<WorkFavoriteItem> list = new ArrayList<>();
        if($c == null) return list;
        if($c.getCount() == 0) {
            $c.close();
            return list;
        }
        bsCursor _c = new bsCursor($c);
        if(_c.moveToFirst()){
            do {
                WorkFavoriteItem item = new WorkFavoriteItem();
                item.rowid = fieldInt(_c, "rowid");
                item.id = fieldInt(_c, "_id");
                item.city = fieldInt(_c, "city");
                item.type = fieldInt(_c, "type");
                item.typeID = fieldStr(_c, "typeID");
                item.typeID2 = fieldStr(_c, "typeID2");
                item.typeID3 = fieldStr(_c, "typeID3");
                item.typeID4 = fieldStr(_c, "typeID4");
                item.nickname = fieldStr(_c, "NICKNAME");
                item.nickname2 = fieldStr(_c, "NICKNAME2");
                item.color = fieldStr(_c, "color");
                //item.order = fieldInt(_c, "favorite_order");
                item.temp1 = fieldStr(_c, "temp1");
                item.temp2 = fieldStr(_c, "temp2");
                list.add(item);
            } while (_c.moveToNext());
        }
        $c.close();
        return list;
    }
    //출근길 추가
    public void workonfavoriteAdd(int $favoriteId){
        exec("workonfavorite_add", "favorite_id", $favoriteId + "");
    }
    //퇴근길 추가
    public void workofffavoriteAdd(int $favoriteId){
        exec("workofffavorite_add", "favorite_id", $favoriteId + "");
    }
    //출근길 삭제
    public void workonfavoriteDel(int $rowid){
        exec("workonfavorite_del", "workonfavorite_rowid", $rowid + "");
    }
    //퇴근길 삭제
    public void workofffavoriteDel(int $rowid){
        exec("workofffavorite_del", "workofffavorite_rowid", $rowid + "");
    }
    //승차알람 등록
    public void busonAdd(int $cityId, int $routeId1, int $routeId2, @NonNull String $stopArsId, @NonNull String $stopApiId, @NonNull String $busNum, int $min){
        exec("buson_add", "cityId", $cityId + "", "routeId1", $routeId1 + "", "routeId2", $routeId2 + "", "stopArsId", $stopArsId, "stopApiId", $stopApiId, "busNum", $busNum, "min", $min + "");
    }
    //승차알람 삭제
    public void busonDel(){
        exec("buson_del");
    }
    //승차알람 얻기
    public BusOnItem busonGet(){
        Cursor c = select("buson_get");
        if( c == null ) return null;
        if( c.getCount() == 0 ) {
            c.close();
            return null;
        }
        bsCursor _c = new bsCursor(c);
        BusOnItem item = new BusOnItem();
        item.cityId = fieldInt(_c, "cityId");
        item.routeId1 = fieldStr(_c, "routeId1");
        item.routeId2 = fieldStr(_c, "routeId2");
        item.stopArsId = fieldStr(_c, "stopArsId");
        item.stopApiId = fieldStr(_c, "stopApiId");
        item.busNum =  fieldStr(_c, "busNum");
        item.min = fieldInt(_c, "min");
        c.close();
        return item;
    }
    //승차알람 등록
    public void busoffAdd(int $cityId, int $routeId1, int $routeId2, @NonNull String $stopArsId, @NonNull String $stopApiId){
        exec("busoff_add", "cityId", $cityId + "", "routeId1", $routeId1 + "", "routeId2", $routeId2 + "", "stopArsId", $stopArsId, "stopApiId", $stopApiId);
    }
    //승차알람 삭제
    public void busoffDel(){
        exec("busoff_del");
    }
    //승차알람 얻기
    public BusOffItem busoffGet(){
        Cursor c = select("busoff_get");
        if( c == null ) return null;
        if( c.getCount() == 0 ) {
            c.close();
            return null;
        }
        bsCursor _c = new bsCursor(c);
        BusOffItem item = new BusOffItem();
        item.cityId = fieldInt(_c, "cityId");
        item.routeId1 = fieldStr(_c, "routeId1");
        item.routeId2 = fieldStr(_c, "routeId2");
        item.stopArsId = fieldStr(_c, "stopArsId");
        item.stopApiId = fieldStr(_c, "stopApiId");
        c.close();
        return item;
    }
}
