package com.example.viget;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectFetch {

    private static final String OPEN_WEATHER_MAP_API =
            "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric";
    private static final String OPEN_WEATHER_ICON =
            "https://openweathermap.org/img/wn/%s@2x.png";

    public interface OnConnectionCompleteListener {
        void onSuccess(JSONObject response);
        void onFail(String message);
    }

    private OnConnectionCompleteListener listener;
    private Handler handler;

    public ConnectFetch(Context context, String city, OnConnectionCompleteListener listener) {
        this.listener = listener;
        this.handler = new Handler();
        updateWeatherData(city, context);
    }

    private void updateWeatherData(final String city, final Context context) {
        new Thread(new Runnable() {
            public void run() {
                final JSONObject json = ConnectFetch.getJSON(context, city);
                if (json == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            listener.onFail("Ошибка сети или города");
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            listener.onSuccess(json);
                        }
                    });
                }
            }
        }).start();
    }

    public static JSONObject getJSON(Context context, String city) {
        try {
            String apiKey = context.getString(R.string.weather_api_key).trim();
            String urlString = String.format(OPEN_WEATHER_MAP_API, city, apiKey);
            Log.d("SimpleWeather", "Fetching: " + urlString);

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() != 200) {
                Log.e("SimpleWeather", "HTTP error: " + connection.getResponseCode());
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return new JSONObject(sb.toString());
        } catch (Exception e) {
            Log.e("SimpleWeather", "Exception: " + e.getMessage());
            return null;
        }
    }

    public static String getIconUrl(JSONObject json) {
        try {
            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            return String.format(OPEN_WEATHER_ICON, details.getString("icon"));
        } catch (Exception e) { return ""; }
    }

    public static String getCityField(JSONObject json) {
        try {
            return json.getString("name").toUpperCase() + ", " + json.getJSONObject("sys").getString("country");
        } catch (Exception e) { return "NaN"; }
    }

    public static String getLastUpdateTime(JSONObject json) {
        try {
            java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance();
            return df.format(new java.util.Date(json.getLong("dt") * 1000));
        } catch (Exception e) { return "NaN"; }
    }

    public static String getDetailsField(JSONObject json) {
        try {
            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            return details.getString("description").toUpperCase() +
                    "\nВлажность: " + main.getString("humidity") + "%" +
                    "\nДавление: " + main.getString("pressure") + " hPa";
        } catch (Exception e) { return "NaN"; }
    }

    public static String getTemperatureField(JSONObject json) {
        try {
            return String.format("%.1f °C", json.getJSONObject("main").getDouble("temp"));
        } catch (Exception e) { return "-- °C"; }
    }
}