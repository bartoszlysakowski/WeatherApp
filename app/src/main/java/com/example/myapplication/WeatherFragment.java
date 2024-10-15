package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.util.Locale;

public class WeatherFragment extends Fragment {
    private TextView locationTextView;
    private TextView coordinatesTextView;
    private TextView temperatureTextView;
    private TextView pressureTextView;
    private TextView descriptionTextView;
    private TextView windInfo;
    private TextView visibilityInfo;
    private TextView humidity;
    private WeatherViewModel viewModel;
    private ImageView weatherIcon;
    private boolean isMetric;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_weather, container, false);

        locationTextView = view.findViewById(R.id.locationTextView);
        coordinatesTextView = view.findViewById(R.id.coordinatesTextView);
        temperatureTextView = view.findViewById(R.id.temperatureTextView);
        pressureTextView = view.findViewById(R.id.pressureTextView);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);

        windInfo = view.findViewById(R.id.windInfo);
        visibilityInfo = view.findViewById(R.id.visibilityInfo);
        humidity = view.findViewById(R.id.humidity);

        weatherIcon = view.findViewById(R.id.weatherIcon);

        Log.d("WeatherFragment", "onCreateView: TextViews initialized");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState); // służy do przechowywania stanu fragmentu. (np. przy obrocie)

        viewModel = new ViewModelProvider(requireActivity()).get(WeatherViewModel.class);
        isMetric = ((MainActivity) requireActivity()).isMetric;

        // obserwowanie zmian w livedata
        viewModel.getCurrentWeatherData().observe(getViewLifecycleOwner(), weatherData -> {
            updateWeatherData(weatherData);
        });
    }

    public void updateWeatherData(WeatherData weatherData) {
        if (weatherData != null) {
            Log.d("WeatherFragment", "updateWeatherData: WeatherData received: " + weatherData.toString());

            locationTextView.setText(weatherData.getName());
            coordinatesTextView.setText("Lat: " + weatherData.getCoord().getLat() + ", Lon: " + weatherData.getCoord().getLon());
            temperatureTextView.setText("Temp: " + convertTemperature(weatherData.getMain().getTemp()));
            pressureTextView.setText("Pressure: " + weatherData.getMain().getPressure() + " hPa");
            descriptionTextView.setText(weatherData.getWeather().get(0).getDescription());

            if (!weatherData.getWeather().isEmpty()) {
                String iconCode = weatherData.getWeather().get(0).getIcon();
                String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + "@2x.png";
                Glide.with(this).load(iconUrl).into(weatherIcon);
                Log.d("WeatherFragment", "updateWeatherData: Icon URL: " + iconUrl);
            }
            windInfo.setText("Wind: " + convertWindSpeed(weatherData.getWind().getSpeed()));
            visibilityInfo.setText("Visibility: " + weatherData.getVisibility() + " m");
            humidity.setText("Humidity: " + weatherData.getMain().getHumidity() + "%");
        } else {
            Log.d("WeatherFragment", "updateWeatherData: WeatherData is null");
        }
    }

    public void setUnits(boolean isMetric) {
        this.isMetric = isMetric;
        if (viewModel.getCurrentWeatherData().getValue() != null) {
            updateWeatherData(viewModel.getCurrentWeatherData().getValue());
        }
    }
    private String convertTemperature(double tempInKelvin) {
        if (isMetric) {
            double tempInCelsius = tempInKelvin - 273.15;
            return String.format(Locale.getDefault(), "%.1f°C", tempInCelsius);
        } else {
            double tempInFahrenheit = (tempInKelvin - 273.15) * 9/5 + 32;
            return String.format(Locale.getDefault(), "%.1f°F", tempInFahrenheit);
        }
    }
    private String convertWindSpeed(double speedInMetersPerSecond) {
        if (isMetric) {
            return String.format(Locale.getDefault(), "%.1f m/s", speedInMetersPerSecond);
        } else {
            double speedInMilesPerHour = speedInMetersPerSecond * 2.23694;
            return String.format(Locale.getDefault(), "%.1f mph", speedInMilesPerHour);
        }
    }
}
