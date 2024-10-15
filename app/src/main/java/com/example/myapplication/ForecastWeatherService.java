package com.example.myapplication;

import android.util.Log;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class ForecastWeatherService {
    private static final String TAG = "ForecastWeatherService";
    private static final String BASE_URL = "https://api.openweathermap.org/";
    private final Retrofit retrofit;
    private final String apiKey;

    public ForecastWeatherService(String apiKey) {
        this.apiKey = apiKey;
        this.retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public void getForecastWeather(String city_name, Callback<ForecastWeatherData> callback) {
        WeatherAPI weatherAPI = retrofit.create(WeatherAPI.class);
        Call<ForecastWeatherData> call = weatherAPI.getForecastWeather(city_name, apiKey);
        call.enqueue(new Callback<ForecastWeatherData>() {
            @Override
            public void onResponse(Call<ForecastWeatherData> call, Response<ForecastWeatherData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Forecast data: " + response.body().toString());
                    callback.onResponse(call, response);
                } else {
                    Log.e(TAG, "Failed to get forecast weather: " + response.message());
                    callback.onFailure(call, new IOException("Failed to get forecast weather: " + response.message()));
                }
            }

            @Override
            public void onFailure(Call<ForecastWeatherData> call, Throwable t) {
                Log.e(TAG, "Error: ", t);
                callback.onFailure(call, t);
            }
        });
    }


    private interface WeatherAPI {
        @GET("data/2.5/forecast")
        Call<ForecastWeatherData> getForecastWeather(
                @Query("q") String city_name,
                @Query("appid") String apiKey
        );
    }
}
