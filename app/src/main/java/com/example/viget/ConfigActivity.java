package com.example.viget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class ConfigActivity extends AppCompatActivity {

    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;

    public final static String WIDGET_PREF = "widget_pref";
    public final static String WIDGET_CITY = "widget_city_";
    public final static String ACTION_UPDATE_WIDGET = "com.example.viget.ACTION_UPDATE_WIDGET";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        setResult(RESULT_CANCELED, resultValue);

        setContentView(R.layout.activity_config);
    }

    public void setCity(View view) {
        EditText etText = (EditText) findViewById(R.id.city);
        String city = etText.getText().toString().trim();
        if (city.isEmpty()) city = "London";

        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(WIDGET_CITY + widgetID, city);
        editor.apply();

        new CityPreference(this).setCity(city);

        Intent updateIntent = new Intent(this, AppWidget.class);
        updateIntent.setAction(ACTION_UPDATE_WIDGET);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }, 1000);

        setResult(RESULT_OK, resultValue);
        finish();
    }
}