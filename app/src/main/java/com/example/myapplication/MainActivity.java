package com.example.myapplication;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private CurrentWeatherService currentWeatherService;
    private ForecastWeatherService forecastWeatherService;

    public WeatherViewModel viewModel;

    private String cityName = "warszawa";
    private String lastValidCityName = "warszawa";
    public boolean isMetric;

    private static final int REFRESH_INTERVAL_MS = 10000;
    private Handler handler;
    private Runnable refreshRunnable;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isTablet()) {
            setContentView(R.layout.activity_main_tablet);

            Toolbar toolbar = findViewById(R.id.toolbar_tablet);
            setSupportActionBar(toolbar);

            loadFragment(new WeatherFragment(), R.id.fragment_container_weather);
            loadFragment(new ForecastWeatherFragment(), R.id.fragment_container_forecast);
            loadFragment(new FavoriteCitiesFragment(), R.id.fragment_container_favorites);

        } else {
            setContentView(R.layout.activity_main);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            ViewPager2 viewPager = findViewById(R.id.viewPager);
            ViewPagerAdapter adapter = new ViewPagerAdapter(this);
            viewPager.setAdapter(adapter);
            // ViewPager2 używa adaptera zeby zarządzać fragmentami które mają być wyświetlane podczas przewijania
        }



        currentWeatherService = new CurrentWeatherService("f2f832c946e8acdf2a58902677e4375f");
        forecastWeatherService = new ForecastWeatherService("f2f832c946e8acdf2a58902677e4375f");

        viewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        cityName = SharedPreferencesManager.loadLastUsedCity(this);

        isMetric = SharedPreferencesManager.loadUnitsPreference(this);
        notifyFragmentsOfUnitChange();

        if (isNetworkAvailable()) {
            fetchWeatherData();
        } else {
            WeatherData weatherData = SharedPreferencesManager.loadCurrentWeatherDataForCity(MainActivity.this, cityName);
            viewModel.setCurrentWeatherData(weatherData);
            ForecastWeatherData forecastWeatherData = SharedPreferencesManager.loadForecastWeatherDataForCity(MainActivity.this, cityName);
            List<Forecast> forecastList = forecastWeatherData != null ? forecastWeatherData.getForecastList() : new ArrayList<>();
            viewModel.setForecastWeatherData(forecastList);

            Toast.makeText(MainActivity.this, "No internet connection. Data may be outdated.", Toast.LENGTH_LONG).show();
        }

        handler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (isNetworkAvailable()) {
                    fetchWeatherDataForCity(getCurrentCityName());
                }
                handler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.post(refreshRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(refreshRunnable);
    }



    private boolean isTablet() {
        return (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private void loadFragment(Fragment fragment, int containerId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(containerId, fragment);
        fragmentTransaction.commit();
        Log.d("MainActivity", "Fragment " + fragment.getClass().getSimpleName() + " loaded into container " + containerId);
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu); //tworzenie menu z pliku xml
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_refresh) {
            fetchWeatherData();
            return true;
        } else if (itemId == R.id.action_location) {
            showLocationDialog();
            return true;
        } else if (itemId == R.id.action_units) {
            toggleUnits();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchWeatherData() {
        currentWeatherService.getCurrentWeather(cityName, new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    viewModel.setCurrentWeatherData(response.body());
                    lastValidCityName = cityName;
                    SharedPreferencesManager.saveCurrentWeatherDataForCity(MainActivity.this, cityName, response.body());
                    SharedPreferencesManager.saveLastUsedCity(MainActivity.this, cityName); // Save last used city
                    removeOldCityDataIfNotFavorite(lastValidCityName);
                } else {
                    revertToLastValidCity();
                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error fetching weather data", Toast.LENGTH_SHORT).show());
                revertToLastValidCity();
            }
        });

        forecastWeatherService.getForecastWeather(cityName, new Callback<ForecastWeatherData>() {
            @Override
            public void onResponse(Call<ForecastWeatherData> call, Response<ForecastWeatherData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    viewModel.setForecastWeatherData(response.body().getForecastList());
                    SharedPreferencesManager.saveForecastWeatherDataForCity(MainActivity.this, cityName, response.body());
                }
            }

            @Override
            public void onFailure(Call<ForecastWeatherData> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error fetching forecast data", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void removeOldCityDataIfNotFavorite(String newCity) {
        String previousCity = SharedPreferencesManager.loadLastUsedCity(this);
        List<String> favoriteCities = SharedPreferencesManager.loadFavoriteCities(this);

        if (!favoriteCities.contains(previousCity) && !previousCity.equals(newCity)) {
            SharedPreferencesManager.removeCurrentWeatherDataForCity(this, previousCity);
            SharedPreferencesManager.removeForecastWeatherDataForCity(this, previousCity);
        }
    }

    private void revertToLastValidCity() { // gdy sie nie uda zaladowac dla miasta np wpisanego to bedzie dla wczesniejszego
        cityName = lastValidCityName;
        fetchWeatherData();
    }

    private void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Location");

        final EditText input = new EditText(this);
        input.setHint("Enter city name");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newCityName = input.getText().toString();
            if (!newCityName.isEmpty()) {
                if (isNetworkAvailable()) {
                    fetchWeatherDataForCity(newCityName);
                } else {
                    Toast.makeText(MainActivity.this, "No internet connection. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "City name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void toggleUnits() {
        isMetric = !isMetric;
        SharedPreferencesManager.saveUnitsPreference(this, isMetric);
        notifyFragmentsOfUnitChange();
    }

    private void notifyFragmentsOfUnitChange() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof ForecastWeatherFragment) {
                ((ForecastWeatherFragment) fragment).setUnits(isMetric);
            }
            if (fragment instanceof WeatherFragment) {
                ((WeatherFragment) fragment).setUnits(isMetric);
            }
        }
    }

    public void fetchWeatherDataForCity(String newCityName) {
        cityName = newCityName;
        if (isNetworkAvailable()) {
            fetchWeatherData();
        } else {
            WeatherData weatherData = SharedPreferencesManager.loadCurrentWeatherDataForCity(MainActivity.this, cityName);
            viewModel.setCurrentWeatherData(weatherData);
            ForecastWeatherData forecastWeatherData = SharedPreferencesManager.loadForecastWeatherDataForCity(MainActivity.this, cityName);
            List<Forecast> forecastList = forecastWeatherData != null ? forecastWeatherData.getForecastList() : new ArrayList<>();
            viewModel.setForecastWeatherData(forecastList);
        }
    }

    public String getCurrentCityName() {
        return cityName;
    }
}


// odswieznaie co jakis czas
// jednostki maja sie zapisywac po zamknieciu aplikacji