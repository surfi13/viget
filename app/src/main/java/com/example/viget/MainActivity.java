package com.example.viget;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setInfo();
    }

    private void setInfo() {
        String currentCity = new CityPreference(this).getCity();
        new ConnectFetch(this, currentCity, new ConnectFetch.OnConnectionCompleteListener() {
            @Override
            public void onSuccess(JSONObject response) {
                renderWeather(response);
            }

            @Override
            public void onFail(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderWeather(JSONObject json) {
        try {
            Glide.with(this).load(ConnectFetch.getIconUrl(json)).into((ImageView) findViewById(R.id.weather_icon));
            ((TextView) findViewById(R.id.city_field)).setText(ConnectFetch.getCityField(json));
            ((TextView) findViewById(R.id.updated_field)).setText(ConnectFetch.getLastUpdateTime(json));
            ((TextView) findViewById(R.id.details_field)).setText(ConnectFetch.getDetailsField(json));
            ((TextView) findViewById(R.id.current_temperature_field)).setText(ConnectFetch.getTemperatureField(json));
        } catch (Exception e) {
            Log.e("SimpleWeather", "Ошибка в MainActivity");
        }
    }
}