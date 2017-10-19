package teamdoppelganger.smarterbus.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.util.common.Debug;

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

public class WidgetProvider2x1 extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int i = 0; i < appWidgetIds.length; i++) {

            showResult(context, appWidgetManager, appWidgetIds[i], null, 0);

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


    public static void showResult(Context context, AppWidgetManager appWidgetManager, int widgetId,
                                  String[] resultTime, int state) {

        FileInputStream fis;
        FavoriteAndHistoryItem item = null;

        try {
            fis = context.openFileInput(String.valueOf(widgetId) + "_type1");
            ObjectInputStream os = new ObjectInputStream(fis);
            item = (FavoriteAndHistoryItem) os.readObject();
            os.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            context.deleteFile(String.valueOf(widgetId) + "_type1");
            AppWidgetHost host = new AppWidgetHost(context, 0);
            host.deleteAppWidgetId(widgetId);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (item == null)
            return;

        HashMap<Integer, String> busTypeHash = new HashMap<Integer, String>();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String dbName = pref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);
        SQLiteDatabase database = SQLiteDatabase.openDatabase(Constants.LOCAL_PATH + dbName, null, SQLiteDatabase.OPEN_READONLY);

        String verCheckQ = String.format("SELECT %s FROM %s", CommonConstants.VERSION_VERSION, CommonConstants.TBL_VERSION);
        String tmpSql = String.format("SELECT *FROM %s", CommonConstants.TBL_BUS_TYPE);

        Cursor cursor = database.rawQuery(verCheckQ, null);

        boolean isDel = false;
        if (item.busRouteItem.tmpId == null || item.busRouteItem.tmpId.equals("")) {
            isDel = true;
        }

        if (cursor.moveToFirst() || isDel) {
            //db설치보다 먼저 불림
            int dbVer = cursor.getInt(cursor.getColumnIndex(CommonConstants.VERSION_VERSION));

            if (dbVer <= 10) {
                if (item.busRouteItem.localInfoId.equals("401")) {

                    context.deleteFile(String.valueOf(widgetId) + "_type1");

                    AppWidgetHost host = new AppWidgetHost(context, 0);
                    host.deleteAppWidgetId(widgetId);
                    cursor.close();
                    return;
                }
            }
            if (dbVer <= 35) {
                if (item.busRouteItem.localInfoId.equals("301")) {

                    context.deleteFile(String.valueOf(widgetId) + "_type1");

                    AppWidgetHost host = new AppWidgetHost(context, 0);
                    host.deleteAppWidgetId(widgetId);
                    cursor.close();
                    return;
                }
                if (item.busRouteItem.localInfoId.equals("412")) {

                    context.deleteFile(String.valueOf(widgetId) + "_type1");

                    AppWidgetHost host = new AppWidgetHost(context, 0);
                    host.deleteAppWidgetId(widgetId);
                    cursor.close();
                    return;

                }
            }

        }

        cursor.close();

        cursor = database.rawQuery(tmpSql, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(CommonConstants._ID));
            String color = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_TYPE_COLOR));
            busTypeHash.put(id, color);
        }
        cursor.close();

        RemoteViews remote = new RemoteViews(context.getPackageName(), R.layout.widget_2x1);

        String resName = String.format("tag_%s", String.valueOf(item.color));
        int resID = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());

        remote.setImageViewResource(R.id.backColor, resID);
        remote.setImageViewResource(R.id.btn_image, R.drawable.ic_busstop);
        remote.setTextViewText(R.id.name1, item.busRouteItem.busRouteName);
        remote.setTextViewText(R.id.subname1, item.nickName);

        String color = busTypeHash.get(item.busRouteItem.busType);
        remote.setTextColor(R.id.name1, Color.parseColor("#" + color));

        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(Constants.INTENT_FAVORITEITEM, item);
        intent.putExtra("widgetId", widgetId);
        intent.putExtra("isType1", true);

        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        if (resultTime != null) {
            remote.setTextViewText(R.id.arrive_1, resultTime[0]);
            remote.setTextViewText(R.id.arrive_2, resultTime[1]);
        }

        remote.setOnClickPendingIntent(R.id.refreshBtn,
                PendingIntent.getService(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT));

        if (state == 1)
            remote.setViewVisibility(R.id.itemProgress, View.VISIBLE);
        else if (state == 2)
            remote.setViewVisibility(R.id.itemProgress, View.GONE);

        appWidgetManager.updateAppWidget(widgetId, remote);


        if (resultTime == null && state == 2)
            context.startService(intent);


    }

}
