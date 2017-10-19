package teamdoppelganger.smarterbus.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.item.WidgetStopItem;
import teamdoppelganger.smarterbus.util.common.Debug;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import com.smart.lib.CommonConstants;

@SuppressLint("NewApi")
public class WidgetProvider4x2 extends AppWidgetProvider {

    static final int DIM_NOTHING = 0;
    static final int DIM_VISIBLE = 1;
    static final int DIM_GONE = 2;


    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; i++) {
            showResult(context, appWidgetIds[i], DIM_NOTHING, null);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    public static void showResult(Context context, int widgetId, int dimVisible, HashMap<String, String[]> hash) {

        WidgetStopItem item = null;

        try {
            FileInputStream fis = context.openFileInput(String.valueOf(widgetId) + "_type2");
            ObjectInputStream os = new ObjectInputStream(fis);
            item = (WidgetStopItem) os.readObject();
            os.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            context.deleteFile(String.valueOf(widgetId) + "_type2");
            AppWidgetHost host = new AppWidgetHost(context, 0);
            host.deleteAppWidgetId(widgetId);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (item == null)
            return;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String dbName = pref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);
        SQLiteDatabase mDatabase = SQLiteDatabase.openDatabase(
                Constants.LOCAL_PATH + dbName, null, SQLiteDatabase.OPEN_READONLY);

        String verCheckQ = String.format("SELECT %s FROM %s", CommonConstants.VERSION_VERSION, CommonConstants.TBL_VERSION);
        Cursor cursor = mDatabase.rawQuery(verCheckQ, null);

        boolean isDel = false;
        if (item.favoriteAndHistoryItem.busStopItem.tempId2 == null
                || item.favoriteAndHistoryItem.busStopItem.tempId2.equals("")) {
            isDel = true;
        }

        if (cursor.moveToFirst() || isDel) {
            int dbVer = cursor.getInt(cursor.getColumnIndex(CommonConstants.VERSION_VERSION));

            if (dbVer <= 10) {
                if (item.favoriteAndHistoryItem.busStopItem.localInfoId.equals("401")) {
                    context.deleteFile(String.valueOf(widgetId) + "_type2");

                    AppWidgetHost host = new AppWidgetHost(context, 0);
                    host.deleteAppWidgetId(widgetId);
                    cursor.close();
                    return;
                }
            }
            if (dbVer <= 35) {
                if (item.favoriteAndHistoryItem.busStopItem.localInfoId.equals("301")) {
                    context.deleteFile(String.valueOf(widgetId) + "_type2");

                    AppWidgetHost host = new AppWidgetHost(context, 0);
                    host.deleteAppWidgetId(widgetId);
                    cursor.close();
                    return;
                }
                if (item.favoriteAndHistoryItem.busStopItem.localInfoId.equals("412")) {
                    context.deleteFile(String.valueOf(widgetId) + "_type2");

                    AppWidgetHost host = new AppWidgetHost(context, 0);
                    host.deleteAppWidgetId(widgetId);
                    cursor.close();
                    return;

                }
            }

        }

        cursor.close();
        if (android.os.Build.VERSION.SDK_INT < 11) {


            int page = pref.getInt(String.valueOf(widgetId) + "widget_page", 0);
            int maxIndex = page * 4 + 4;

            Intent intent = new Intent(context, WidgetService.class);

            intent.putExtra("widgetId", widgetId);
            intent.putExtra("isType1", false);
            intent.putExtra("favoriteItemArry", item);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));


            HashMap<Integer, String> mBusTypeHash = new HashMap<Integer, String>();

            String tmpSql = String.format("SELECT *FROM %s", CommonConstants.TBL_BUS_TYPE);
            cursor = mDatabase.rawQuery(tmpSql, null);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(CommonConstants._ID));
                String color = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_TYPE_COLOR));
                mBusTypeHash.put(id, color);
            }

            RemoteViews remote = new RemoteViews(context.getPackageName(), R.layout.widget_4x2_low_sdk_ver);

            String resName = String.format("widget_top%s", String.valueOf(item.favoriteAndHistoryItem.color));
            int resID = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());

            remote.setImageViewResource(R.id.widget_text_back, resID);

            remote.setTextViewText(R.id.widget_type2_stop_name, item.favoriteAndHistoryItem.nickName);

            if (page == 0)
                remote.setViewVisibility(R.id.widget_type2_scroll_btn1_dim, View.VISIBLE);
            else
                remote.setViewVisibility(R.id.widget_type2_scroll_btn1_dim, View.GONE);

            if (item.busRouteArray.size() <= maxIndex) {

                remote.setViewVisibility(R.id.widget_type2_scroll_btn2_dim, View.VISIBLE);

                for (int i = 0; i < (maxIndex) - item.busRouteArray.size(); i++) {
                    remote.setTextViewText(context.getResources().getIdentifier("@id/widget_type2_bus_name_" + (3 - i),
                            "id", context.getPackageName()), "");
                }
            } else
                remote.setViewVisibility(R.id.widget_type2_scroll_btn2_dim, View.GONE);

            for (int i = 0; i < item.busRouteArray.size(); i++) {

                if (i >= maxIndex || i < page * 4)
                    continue;

                int layoutNum = (i - page * 4);

                remote.setTextViewText(context.getResources().getIdentifier("@id/widget_type2_bus_name_" + layoutNum,
                        "id", context.getPackageName()), item.busRouteArray.get(i).busRouteName);

                String color = mBusTypeHash.get(item.busRouteArray.get(i).busType);

                remote.setTextColor(context.getResources().getIdentifier("@id/widget_type2_bus_name_" + layoutNum,
                        "id", context.getPackageName()), Color.parseColor("#" + color));

                if (hash == null) {
                    remote.setTextViewText(context.getResources().getIdentifier("@id/widget_type2_remain_first_" + layoutNum,
                            "id", context.getPackageName()), "");
                    remote.setTextViewText(context.getResources().getIdentifier("@id/widget_type2_remain_second_" + layoutNum,
                            "id", context.getPackageName()), "");
                    remote.setViewVisibility(context.getResources().getIdentifier("@id/widget_type2_divider_" + layoutNum,
                            "id", context.getPackageName()), View.GONE);
                } else {

                    String[] resultTime = hash.get(item.busRouteArray.get(i).busRouteName);

                    if (resultTime != null) {
                        remote.setTextViewText(context.getResources().getIdentifier("@id/widget_type2_remain_first_" + layoutNum,
                                "id", context.getPackageName()), resultTime[0]);

                        remote.setTextViewText(context.getResources().getIdentifier("@id/widget_type2_remain_second_" + layoutNum,
                                "id", context.getPackageName()), resultTime[1]);

                        int visibility = (resultTime[1] == null) ? View.GONE : View.VISIBLE;

                        remote.setViewVisibility(context.getResources().getIdentifier("@id/widget_type2_divider_" + layoutNum,
                                "id", context.getPackageName()), visibility);
                    }
                }

            }

            if (dimVisible == DIM_VISIBLE)
                remote.setViewVisibility(R.id.widget_type2_linear_layout_dim, View.VISIBLE);

            else if (dimVisible == DIM_GONE)
                remote.setViewVisibility(R.id.widget_type2_linear_layout_dim, View.GONE);


            remote.setOnClickPendingIntent(R.id.widget_type2_btn_refresh,
                    PendingIntent.getService(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT));


            Intent listUpIntent = new Intent(context, WidgetService.class);
            listUpIntent.putExtra("widgetId", widgetId);
            listUpIntent.putExtra("isType1", false);
            listUpIntent.putExtra("favoriteItemArry", item);
            listUpIntent.putExtra("list_state", -1);
            listUpIntent.setData(Uri.parse(listUpIntent.toUri(Intent.URI_INTENT_SCHEME)));

            remote.setOnClickPendingIntent(R.id.widget_type2_scroll_btn1,
                    PendingIntent.getService(context, widgetId, listUpIntent, PendingIntent.FLAG_UPDATE_CURRENT));

            Intent listDownIntent = new Intent(context, WidgetService.class);
            listDownIntent.putExtra("widgetId", widgetId);
            listDownIntent.putExtra("isType1", false);
            listDownIntent.putExtra("favoriteItemArry", item);
            listDownIntent.putExtra("list_state", 1);
            listDownIntent.setData(Uri.parse(listDownIntent.toUri(Intent.URI_INTENT_SCHEME)));

            remote.setOnClickPendingIntent(R.id.widget_type2_scroll_btn2,
                    PendingIntent.getService(context, widgetId, listDownIntent, PendingIntent.FLAG_UPDATE_CURRENT));

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            manager.updateAppWidget(widgetId, remote);

            return;
        }

        Intent intent = new Intent(context, WidgetListService.class);

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews remote = new RemoteViews(context.getPackageName(), R.layout.widget_4x2_list);

        String resName = String.format("widget_top%s", String.valueOf(item.favoriteAndHistoryItem.color));
        int resID = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());

        remote.setImageViewResource(R.id.widget_text_back, resID);

        remote.setTextViewText(R.id.widget_type2_stop_name, item.favoriteAndHistoryItem.nickName);
        remote.setViewVisibility(R.id.widget_type2_list_layout, View.VISIBLE);

        if (dimVisible == DIM_VISIBLE)
            remote.setViewVisibility(R.id.widget_type2_dim, View.VISIBLE);

        else if (dimVisible == DIM_GONE)
            remote.setViewVisibility(R.id.widget_type2_dim, View.GONE);

        remote.setRemoteAdapter(R.id.widget_type2_list, intent);

        remote.setOnClickPendingIntent(R.id.widget_type2_btn_refresh,
                PendingIntent.getService(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT));

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(widgetId, remote);

        if (dimVisible == 0)
            context.startService(intent);
    }

}
