package com.example.viget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

public class AppWidget extends AppWidgetProvider {

    static final String LOG_TAG = "myLogs";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // Ловим наше сообщение об обновлении
        if (ConfigActivity.ACTION_UPDATE_WIDGET.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                SharedPreferences sp = context.getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
                updateAppWidget(context, sp, AppWidgetManager.getInstance(context), appWidgetId);
            }
        }
    }

    static void updateAppWidget(final Context context,
                                SharedPreferences sharedPreferences,
                                AppWidgetManager appWidgetManager,
                                final int appWidgetId) {

        String widgetCity = sharedPreferences.getString(ConfigActivity.WIDGET_CITY + appWidgetId, null);
        if (widgetCity == null || widgetCity.isEmpty()) {
            widgetCity = new CityPreference(context).getCity();
        }

        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        remoteViews.setTextViewText(R.id.city_field, widgetCity);
        remoteViews.setTextViewText(R.id.details_field, "Загрузка...");
        pushWidgetUpdate(context, remoteViews);

        final String finalCity = widgetCity;

        new Thread(() -> {
            try {
                JSONObject json = ConnectFetch.getJSON(context, finalCity);
                if (json == null) {
                    remoteViews.setTextViewText(R.id.details_field, "Ошибка сети");
                    pushWidgetUpdate(context, remoteViews);
                    return;
                }

                String temp = ConnectFetch.getTemperatureField(json);
                String iconUrl = ConnectFetch.getIconUrl(json);

                remoteViews.setTextViewText(R.id.details_field, temp);

                if (iconUrl != null && !iconUrl.isEmpty()) {
                    try {
                        Bitmap bitmap = Glide.with(context.getApplicationContext())
                                .asBitmap()
                                .load(iconUrl)
                                .submit()
                                .get();

                        if (bitmap != null) {
                            remoteViews.setImageViewBitmap(R.id.weather_icon, bitmap);
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Ошибка картинки: " + e.getMessage());
                    }
                }

                pushWidgetUpdate(context, remoteViews);

            } catch (Exception e) {
                Log.e(LOG_TAG, "Ошибка виджета: " + e.getMessage());
                remoteViews.setTextViewText(R.id.details_field, "Ошибка");
                pushWidgetUpdate(context, remoteViews);
            }
        }).start();
    }

    public static void pushWidgetUpdate(Context context, RemoteViews rv) {
        ComponentName myWidget = new ComponentName(context, AppWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, rv);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        SharedPreferences sp = context.getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, sp, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        SharedPreferences.Editor editor = context
                .getSharedPreferences(ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE)
                .edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(ConfigActivity.WIDGET_CITY + widgetID);
        }
        editor.apply();
    }
}