package teamdoppelganger.smarterbus;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.List;

import teamdoppelganger.smarterbus.common.SBBaseNewActivity;
import teamdoppelganger.smarterbus.item.WorkDayItem;
import teamdoppelganger.smarterbus.item.WorkTimeItem;
import teamdoppelganger.smarterbus.util.common.WorkOnOffCommonFunction;

/**
 * Created by muune on 2017-09-28.
 */

public class SBWorkTimeSetActivity extends SBBaseNewActivity implements View.OnClickListener {

    private SBBaseNewActivity context;
    private TimePickerDialog timePickerDialog;
    private AlertDialog.Builder workofweekDialog;
    private String timeType = "";
    private Switch.OnCheckedChangeListener onOffListener;
    private Switch slideBtn1;
    private Switch slideBtn2;
    private TextView timeSet1;
    private TextView timeSet2;
    private TextView workofweek;
    final String[] week = new String[]{"일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일"};
    final boolean[] dialog_week_checked = new boolean[7];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workonoff_timesetting);
        context = this;
        actionBarInit("알림 시간 설정");

        onOffListener = new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                boolean isOn = compoundButton.getId() == R.id.slideBtn1; //출근인가
                Log.i("bs", "ison:"+(compoundButton.getId() == R.id.slideBtn1)+">"+isChecked);
                WorkTimeItem item = isOn ? getLocalDBHelper().workOnGet() : getLocalDBHelper().workOffGet();
                if(item != null) WorkOnOffCommonFunction.alarmDel(context, item.pid);
                if(isOn){
                    if(isChecked) getLocalDBHelper().workOnActive();
                    else getLocalDBHelper().workOnUnactive();
                }else{
                    if(isChecked) getLocalDBHelper().workOffActive();
                    else getLocalDBHelper().workOffUnactive();
                }
                if(isChecked) WorkOnOffCommonFunction.alarmAdd(context, getLocalDBHelper(), isOn);
            }
        };

        timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener(){
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                alarmTimeSet(timeType.equals("출근"), hourOfDay, minute);
            }
        }, 0, 0, false);


        slideBtn1 = (Switch) findViewById(R.id.slideBtn1);
        slideBtn1.setOnCheckedChangeListener(onOffListener);
        slideBtn2 = (Switch)findViewById(R.id.slideBtn2);
        slideBtn2.setOnCheckedChangeListener(onOffListener);
        timeSet1 = (TextView)findViewById(R.id.timeSet1);
        timeSet2 = (TextView)findViewById(R.id.timeSet2);
        workofweek = (TextView)findViewById(R.id.workofweek);

        findViewById(R.id.timeSet1).setOnClickListener(this);
        findViewById(R.id.timeSet2).setOnClickListener(this);
        findViewById(R.id.workofweek).setOnClickListener(this);

        workofweekDialog = new AlertDialog.Builder(this);
        workofweekDialog.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getLocalDBHelper().workDaySet(dialog_week_checked);
                render();
            }
        }).setNegativeButton("취소", null);

        render();
        this.setAds();
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.timeSet1 : popOpen_timeset((TextView)view, "출근"); break;
            case R.id.timeSet2 : popOpen_timeset((TextView)view, "퇴근"); break;
            case R.id.workofweek : popOpen_dayset(); break;
        }
    }


    private void render(){
        if(slideBtn1 != null) slideBtn1.setChecked(getLocalDBHelper().workOnIsactive());
        if(slideBtn2 != null) slideBtn2.setChecked(getLocalDBHelper().workOffIsactive());
        //Log.i("bs", DAO.workOnIsactive() ? "onon":"onoff");
        WorkTimeItem t1 = getLocalDBHelper().workOnGet();
        WorkTimeItem t2 = getLocalDBHelper().workOffGet();
        timeSet1.setText(t1 != null ? String.format("%02d:%02d", t1.hour, t1.min) : "09:00");
        timeSet2.setText(t2 != null ? String.format("%02d:%02d", t2.hour, t2.min) : "18:00");

        List<WorkDayItem> list = getLocalDBHelper().workDayList();
        String str = "";
        int i = 0;
        while(i < list.size()) str += list.get(i++).name + ",";
        str = str.length() > 0 ? str.substring(0, str.length()-1) : "선택 없음";
        workofweek.setText(str);
    }

    private void popOpen_timeset(TextView $textView, String $type){
        if($type == "출근" && !getLocalDBHelper().workOnIsactive()) return;
        if($type == "퇴근" && !getLocalDBHelper().workOffIsactive()) return;
        timeType = $type;
        String time = $textView.getText().toString();
        if(time.length() == 5) timePickerDialog.updateTime(Integer.parseInt(time.substring(0,2)), Integer.parseInt(time.substring(3,5)));
        if(timePickerDialog != null) timePickerDialog.show();
    }

    private void popOpen_dayset(){
        if(!getLocalDBHelper().workOnIsactive() && !getLocalDBHelper().workOffIsactive()) return;
        if(workofweekDialog != null){
            dialog_week_checked_Set();
            workofweekDialog.setMultiChoiceItems(week, dialog_week_checked, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                    dialog_week_checked[i] = b ? true : false;
                }
            });
            workofweekDialog.show();
        }
    }
    private void dialog_week_checked_Set(){
        List<WorkDayItem> list = getLocalDBHelper().workDayList();
        int i = 0;
        while(i < 7)  dialog_week_checked[i] = WorkOnOffCommonFunction.isinday(list, ++i) ? true : false;
    }

    private void alarmTimeSet(boolean $isOn, int $hourOfDay, int $minute){
        WorkTimeItem item = $isOn ? getLocalDBHelper().workOnGet() : getLocalDBHelper().workOffGet();
        if(item != null) WorkOnOffCommonFunction.alarmDel(context, item.pid);
        if($isOn) getLocalDBHelper().workonSet($hourOfDay, $minute);
        else getLocalDBHelper().workOffSet($hourOfDay, $minute);
        WorkOnOffCommonFunction.alarmAdd(context, getLocalDBHelper(), $isOn);
        render();
    }
}
