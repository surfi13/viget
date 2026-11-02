package com.example.viget;

import android.content.Context;
import android.content.SharedPreferences;

public class CityPreference {
    private SharedPreferences prefs;

    public CityPreference(Context context) {
        prefs = context.getSharedPreferences("city_prefs", Context.MODE_PRIVATE);
    }

    public String getCity() {
        return prefs.getString("city", "London");
    }

    public void setCity(String city) {
        prefs.edit().putString("city", city).apply();
    }
}