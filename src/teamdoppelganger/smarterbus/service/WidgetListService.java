package teamdoppelganger.smarterbus.service;

import java.util.HashMap;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.RemoteViewsService;

public class WidgetListService extends RemoteViewsService {

    static HashMap<Integer, WidgetListViewFactory> mHashMap = new HashMap<Integer, WidgetListViewFactory>();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        if (mHashMap.get(widgetId) == null) {
            WidgetListViewFactory widgetListViewFactory = new WidgetListViewFactory(this.getApplicationContext(), intent);
            mHashMap.put(widgetId, widgetListViewFactory);
        }

        return mHashMap.get(widgetId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        if (mHashMap.get(widgetId) == null) {
            onGetViewFactory(intent);
        }

        mHashMap.get(widgetId).refreshWidgetList(widgetId);
        stopSelf();

        return super.onStartCommand(intent, flags, startId);
    }
}