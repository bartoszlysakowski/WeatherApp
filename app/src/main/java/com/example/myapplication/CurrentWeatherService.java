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

public class CurrentWeatherService {
    private static final String BASE_URL = "https://api.openweathermap.org/";
    private final Retrofit retrofit;
    private final String apiKey;

    public CurrentWeatherService(String apiKey) {
        this.apiKey = apiKey;
        this.retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public void getCurrentWeather(String city_name, Callback<WeatherData> callback) {
        WeatherAPI weatherAPI = retrofit.create(WeatherAPI.class);
        Call<WeatherData> call = weatherAPI.getCurrentWeather(city_name, apiKey); //to reprezetnuje zapytanie
        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(call, response);
                } else {
                    callback.onFailure(call, new IOException("Failed to get current weather: " + response.message()));
                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    private interface WeatherAPI {
        @GET("data/2.5/weather")
        Call<WeatherData> getCurrentWeather(
                @Query("q") String name,
                @Query("appid") String apiKey
        );
    }
}
