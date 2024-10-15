package com.example.myapplication;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastWeatherFragment extends Fragment {
    private RecyclerView recyclerView;
    private ForecastAdapter forecastAdapter;
    private WeatherViewModel viewModel;

    private boolean isMetric;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast_weather, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        forecastAdapter = new ForecastAdapter(new ArrayList<>());
        recyclerView.setAdapter(forecastAdapter);
        Log.d("ForecastWeatherFragment", "onCreateView: RecyclerView initialized");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("ForecastWeatherFragment", "onViewCreated: called");

        viewModel = new ViewModelProvider(requireActivity()).get(WeatherViewModel.class);

        isMetric = ((MainActivity) requireActivity()).isMetric;
        forecastAdapter.setUnits(isMetric);

        //obserwator nasłuchuje zmian danych w ViewModel i aktualizuje ui jakdane sie zmienia
        viewModel.getForecastWeatherData().observe(getViewLifecycleOwner(), forecastList -> {
            Log.d("ForecastWeatherFragment", "Data observed: " + forecastList);
            updateForecastData(forecastList);
        });
    }

    public void updateForecastData(List<Forecast> forecastList) {
        if (forecastList != null && recyclerView != null && isAdded()) {
            List<Forecast> filteredList = filterForecastsForNoon(forecastList);
            forecastAdapter.updateData(filteredList);
        } else {
            Log.e("ForecastWeatherFragment", "Cannot update data: Fragment not added or RecyclerView not initialized.");
        }
    }

    private List<Forecast> filterForecastsForNoon(List<Forecast> forecastList) {
        List<Forecast> filteredList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());

        for (Forecast forecast : forecastList) {
            String hour = sdf.format(new Date(forecast.getDt() * 1000L));
            if ("12".equals(hour) || "13".equals(hour) || "14".equals(hour)) {
                filteredList.add(forecast);
            }
        }

        return filteredList;
    }

    public void setUnits(boolean isMetric) {
        forecastAdapter.setUnits(isMetric);
    }



    private static class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {
        private List<Forecast> forecastList;
        private boolean isMetric;

        public ForecastAdapter(List<Forecast> forecastList) {
            this.forecastList = forecastList;
        }

        public void updateData(List<Forecast> forecastList) {
            this.forecastList = forecastList;
            notifyDataSetChanged();
        }

        public void setUnits(boolean isMetric) {
            this.isMetric = isMetric;
            notifyDataSetChanged(); //aktualizuje dane ui
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_forecast, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Forecast forecast = forecastList.get(position);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String date = sdf.format(new Date(forecast.getDt() * 1000L));

            holder.dateTextView.setText(date);
            holder.tempTextView.setText("Temp: " + convertTemperature(forecast.getMain().getTemp()) + "°");
            holder.descTextView.setText("Desc: " + forecast.getWeather().get(0).getDescription());

            String iconCode = forecast.getWeather().get(0).getIcon();
            String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + "@2x.png";
            Glide.with(holder.itemView).load(iconUrl).into(holder.weatherIcon);
            Log.d("WeatherFragment", "updateWeatherData: Icon URL: " + iconUrl);
        }

        @Override
        public int getItemCount() {
            return forecastList != null ? forecastList.size() : 0;
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

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView dateTextView;
            public TextView tempTextView;
            public TextView descTextView;
            private ImageView weatherIcon;

            public ViewHolder(View view) {
                super(view);
                dateTextView = view.findViewById(R.id.dateTextView);
                tempTextView = view.findViewById(R.id.tempTextView);
                descTextView = view.findViewById(R.id.descTextView);
                weatherIcon = view.findViewById(R.id.weatherIcon);
            }
        }
    }
}

