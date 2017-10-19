package teamdoppelganger.smarterbus.util.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.SBWorkAddActivity;
import teamdoppelganger.smarterbus.SBWorkOnOffActivity;
import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseNewActivity;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.ArriveItem;
import teamdoppelganger.smarterbus.item.DepthItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.item.WorkDayItem;
import teamdoppelganger.smarterbus.item.WorkFavoriteItem;
import teamdoppelganger.smarterbus.item.WorkTimeItem;
import teamdoppelganger.smarterbus.service.SBWorkAlarmReceiver;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;

/**
 * Created by muune on 2017-10-13.
 */

public class WorkOnOffCommonFunction {


    public static void alarmAdd(Context $context, LocalDBHelper $localDB, boolean $isOn){ //출퇴근 알람을 실제 알람메니저에 등록함. 알람수정의 경우 alarmDel을 호출하고 난 다음에 호출할 것.
        WorkTimeItem item = $isOn ? $localDB.workOnGet() : $localDB.workOffGet();
        if(item == null) return;
        Log.i("bs", "alarmAdd:" + item.hour + ":" + item.min);
        AlarmManager am = ( (AlarmManager) $context.getSystemService( Context.ALARM_SERVICE ) );
        long time = getNextAlarm(item.hour, item.min, $localDB.workDayList());
        if(time == -1) return;

        Intent intent = new Intent( $context, SBWorkAlarmReceiver.class );
        intent.putExtra("pid", item.pid);
        intent.putExtra("type", $isOn ? "출근" : "퇴근");
        PendingIntent pi = PendingIntent.getBroadcast( $context, item.pid, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        Log.i("bs", "alarmAdd:" + (pi != null) + ":" + item.pid);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, time, pi);
        }else{
            am.set(AlarmManager.RTC_WAKEUP, time, pi);
        }
    }
    static AlarmManager _am;
    public static void alarmDel(Context $context, int $pid){
        Log.i("bs", "alarmDel pid :" + $pid);
        if( _am == null ) _am = (AlarmManager)$context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast( $context, $pid, new Intent( $context, SBWorkAlarmReceiver.class ), PendingIntent.FLAG_UPDATE_CURRENT);
        _am.cancel(sender);
        sender.cancel();
    }

    public static boolean isinday(List<WorkDayItem> $daylist, int $searchday){ //$searchday 1일, 2 월, 3화, 4수, 5목, 6금, 7토 해당 요일이 알람이 울리는 요일인지 검색
        boolean result = false;
        for (int j = 0; j < $daylist.size(); j++) {
            if($searchday == $daylist.get(j).dayofweekRowid){
                result = true;
            }
        }
        return result;
    }

    public static long getNextAlarm(int $hour, int $minute, List<WorkDayItem> $daylist){ //요일선택에따라 몇 밀리세컨드 후에 알람이 울려야 하는지를 리턴함.
        int startday,today,dayplus = 0;
        Calendar c = Calendar.getInstance(); // 현재 시간을 가져옴
        if( c.get( Calendar.HOUR_OF_DAY ) > $hour || ( c.get( Calendar.HOUR_OF_DAY ) == $hour && c.get( Calendar.MINUTE ) >= $minute ) ) dayplus = 1; //알람시작이 지금 시간보다 이전일경우
        c.set( c.get( Calendar.YEAR ), c.get( Calendar.MONTH ), c.get(Calendar.DAY_OF_MONTH), $hour, $minute, 0 );

        if( $daylist.size() == 0 ){ // 요일이 한개도 활성화 되어 있지 않으면
            return -1;
        }else{
            today = Calendar.getInstance().get( Calendar.DAY_OF_WEEK ); //오늘 요일을 가져옴. 1일,2월,3화,4수,5목,6금,7토
            startday = today + dayplus; //시작요일

            while( startday <= 7 ){ //시작요일부터 검사해서 토요일까지 검사했는데 안나오면 일요일부터 다시 검사해서 해당 알람시작요일을 찾음.
                if(isinday($daylist, startday)) break;
                if(startday == 7) startday = 0;
                startday++;
            }

            if( startday == today && dayplus == 1 ) dayplus = 7; //오늘과 같은 요일이 활성화됐는데 시간이 이전이면 다음주
            else dayplus = startday - today;
            if( dayplus < 0 ) dayplus += 7;
        }
        Log.i("bs", "getNextAlarm:" + c.getTimeInMillis());
        c.add( Calendar.DAY_OF_MONTH, dayplus );
        return c.getTimeInMillis();
    }



    public static class ListHolder extends RecyclerView.ViewHolder {
        public ImageView colorLayout, iconImgView, delImg;
        public TextView name, subMain, subMain2, arrive_1, arrive_2;
        public Button addBtn;
        public LinearLayout arriveLayout;
        public ListHolder(View itemView) {
            super(itemView);
            colorLayout = (ImageView)itemView.findViewById(R.id.colorLayout);
            iconImgView = (ImageView)itemView.findViewById(R.id.iconImg);
            delImg = (ImageView)itemView.findViewById(R.id.delImg);
            name = (TextView)itemView.findViewById(R.id.name);
            subMain = (TextView)itemView.findViewById(R.id.subMain);
            subMain2 = (TextView)itemView.findViewById(R.id.subMain2);
            addBtn = (Button)itemView.findViewById(R.id.addBtn);
            arriveLayout = (LinearLayout)itemView.findViewById(R.id.arriveLayout);
            arrive_1 = (TextView)itemView.findViewById(R.id.arrive_1);
            arrive_2 = (TextView)itemView.findViewById(R.id.arrive_2);
        }
    }


    public static int LIST_TYPE_SBWorkOnOffActivity = 1;
    public static int LIST_TYPE_SBWorkAddActivity = 2;
    public static int LIST_TYPE_SBWorkAlarmActivity = 3;

    public static String busArriveGet(ArriveItem arriveItem, Context context) {
        String resultStr = "";

        if (arriveItem.remainMin != -1) {
            if (arriveItem.remainMin != 0) {
                resultStr = arriveItem.remainMin + "분";
            }
        }

        if (arriveItem.remainSecond != -1) {
            resultStr = resultStr + arriveItem.remainSecond + "초";
        }

        if (arriveItem.remainStop != -1) {
            if (resultStr.equals("")) {
                resultStr = arriveItem.remainStop + "정류장 전";
            } else {
                resultStr = resultStr + "(" + arriveItem.remainStop
                        + "정류장 전)";
            }
        }

        if (arriveItem.state == Constants.STATE_PREPARE) {
            try {
                if (arriveItem.remainSecond == -9999) {
                    resultStr = arriveItem.remainMin + "시 " + ((arriveItem.remainStop == 0) ? arriveItem.remainStop + "0" : arriveItem.remainStop) + "분 출발";
                } else {
                    resultStr = context.getResources().getString(R.string.state_prepare);
                }
            } catch (Exception e) {
                resultStr = context.getResources().getString(R.string.state_prepare);
            }
        } else if (arriveItem.state == Constants.STATE_END) {
            resultStr = context.getResources().getString(R.string.state_end);
        } else if (arriveItem.state == Constants.STATE_PREPARE_NOT) {
            resultStr = context.getResources().getString(R.string.state_prepare_not);
        } else if (arriveItem.state == Constants.STATE_NEAR) {
            resultStr = context.getResources().getString(R.string.state_near);
        }
        return resultStr;
    }

    public static class FavoriteListAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private SBBaseNewActivity context;
        private List<WorkFavoriteItem> _c;
        private ArrayList<FavoriteAndHistoryItem> _bus;
        private int type;
        public FavoriteListAdapter(SBBaseNewActivity context, int type){
            this.context = context;
            this.type = type;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
            RecyclerView.ViewHolder viewHolder;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.workonoff_list_row, parent, false);
            viewHolder = new ListHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final WorkFavoriteItem obj = _c.get(position);
            final FavoriteAndHistoryItem item = _bus.get(position);

            ((ListHolder) holder).colorLayout.setVisibility(View.GONE);


            ((ListHolder) holder).delImg.setVisibility(type == LIST_TYPE_SBWorkOnOffActivity ? View.VISIBLE : View.GONE);
            if(type == LIST_TYPE_SBWorkOnOffActivity) ((ListHolder) holder).delImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{
                        SBWorkOnOffActivity act = (SBWorkOnOffActivity)context;
                        act.listDel(obj);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            ((ListHolder) holder).addBtn.setVisibility(type == LIST_TYPE_SBWorkAddActivity ? View.VISIBLE : View.GONE);
            if(type == LIST_TYPE_SBWorkAddActivity) ((ListHolder) holder).addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{
                        SBWorkAddActivity act = (SBWorkAddActivity)context;
                        act.listAdd(obj);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            ((ListHolder) holder).arriveLayout.setVisibility(type == LIST_TYPE_SBWorkAlarmActivity ? View.VISIBLE : View.GONE);
            if(type == LIST_TYPE_SBWorkAlarmActivity){
                Log.i("bs", "update:" + item.busRouteItem.arriveInfo.size());
                ((ListHolder) holder).arrive_1.setVisibility(item.busRouteItem.arriveInfo.size() > 0 ? View.VISIBLE : View.GONE);
                ((ListHolder) holder).arrive_2.setVisibility(item.busRouteItem.arriveInfo.size() > 1 ? View.VISIBLE : View.GONE);
                if(item.busRouteItem.arriveInfo.size() > 0) ((ListHolder) holder).arrive_1.setText(busArriveGet(item.busRouteItem.arriveInfo.get(0), context));
                if(item.busRouteItem.arriveInfo.size() > 1) ((ListHolder) holder).arrive_2.setText(busArriveGet(item.busRouteItem.arriveInfo.get(1), context));
            }

            ((ListHolder) holder).name.setText(item.busRouteItem.busRouteName);
            ((ListHolder) holder).subMain.setText(item.busRouteItem.busStopName);
            ((ListHolder) holder).iconImgView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_busstop));
            try {
                String color = ((SBInforApplication) context.getApplicationContext()).mBusTypeHash.get(item.busRouteItem.busType);
                ((ListHolder) holder).name.setTextColor(Color.parseColor("#" + color));
            } catch (Exception e) {
                ((ListHolder) holder).name.setTextColor(Color.parseColor("#000000"));
            }
        }

        @Override
        public int getItemCount() {
            return _c == null ? 0 : _c.size();
        }

        public void update(){
            Log.i("bs", "update");
            notifyDataSetChanged();
        }

        public void render(List<WorkFavoriteItem> $c) {
            _c = $c;
            _bus = FavoriteCommonFunction.getFavoriteAndHistoryItemList(context.getBusDbSqlite(), (SBInforApplication)context.getApplication(), _c);
            //Log.i("bs", "size:"+$c.size());
            notifyDataSetChanged();
        }
    }
}
