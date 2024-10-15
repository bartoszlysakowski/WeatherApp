package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesManager {

    private static final String SHARED_PREFERENCES_NAME = "WeatherAppPrefs";

    public static WeatherData loadCurrentWeatherDataForCity(Context context, String cityName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        // Pobranie zapisanego wcześniej JSON dla określonego miasta (cityName) z SharedPreferences. Używa klucza "current_weather_data_" + cityName
        String json = sharedPreferences.getString("current_weather_data_" + cityName, "");
        // Odczytany JSON jest następnie deserializowany za pomocą biblioteki Gson do obiektu typu WeatherData. Gson automatycznie mapuje JSON na odpowiedni obiekt klasy WeatherData
        return new Gson().fromJson(json, WeatherData.class);
    }

    public static void saveCurrentWeatherDataForCity(Context context, String cityName, WeatherData weatherData) {
        //Metoda rozpoczyna od uzyskania obiektu SharedPreferences.Editor, który służy do edytowania danych w SharedPreferences.
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        //Serializacja danych: Obiekt weatherData (typu WeatherData) jest konwertowany na format JSON za pomocą Gson za pomocą metody toJson(weatherData).
        String json = new Gson().toJson(weatherData);
        //JSON jest następnie zapisywany do SharedPreferences pod kluczem "current_weather_data_" + cityName.
        // W ten sposób dane są skojarzone z konkretnym miastem (cityName) i przechowywane w SharedPreferences.
        editor.putString("current_weather_data_" + cityName, json);
        //Zatwierdza zmiany
        editor.apply();
    }

    public static ForecastWeatherData loadForecastWeatherDataForCity(Context context, String cityName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("forecast_weather_data_" + cityName, "");
        return new Gson().fromJson(json, ForecastWeatherData.class);
    }

    public static void saveForecastWeatherDataForCity(Context context, String cityName, ForecastWeatherData forecastWeatherData) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        String json = new Gson().toJson(forecastWeatherData);
        editor.putString("forecast_weather_data_" + cityName, json);
        editor.apply();
    }

    public static void saveFavoriteCitiesToSharedPreferences(Context context, List<String> favoriteCities) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        String json = new Gson().toJson(favoriteCities);  // konwertuje listę na JSON. (SharedPreferences nie obsługuje zapisu złożonych obiektów jak List<String>)
        editor.putString("favorite_cities", json); // zapisuje JSON jako String.
        editor.apply();
    }

    public static ArrayList<String> loadFavoriteCitiesFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("favorite_cities", "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        } else {
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            // ArrayList<String> i ArrayList<Integer> sa w czasie wykonania traktowane jako ten sam typ- (ArrayList)
            // dlatego Gson nie może bezpośrednio określić rzeczywistego typu generycznego
            return new Gson().fromJson(json, type);
        }
    }

    public static void saveLastUsedCity(Context context, String cityName) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("last_used_city", cityName);
        editor.apply();
    }

    public static String loadLastUsedCity(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("last_used_city", "");
    }


    public static void removeCurrentWeatherDataForCity(Context context, String cityName) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.remove("current_weather_data_" + cityName);
        editor.apply();
    }

    public static void removeForecastWeatherDataForCity(Context context, String cityName) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.remove("forecast_weather_data_" + cityName);
        editor.apply();
    }

    public static List<String> loadFavoriteCities(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("favorite_cities", "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        } else {
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            return new Gson().fromJson(json, type);
        }
    }

    public static void saveUnitsPreference(Context context, boolean isMetric) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean("units", isMetric);
        editor.apply();
    }

    public static boolean loadUnitsPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("units", true);
    }


}

