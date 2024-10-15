package com.example.myapplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class FavoriteCitiesFragment extends Fragment {

    private ArrayList<String> favoriteCities;
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_cities, container, false);

        ListView listView = view.findViewById(R.id.listView);
        Button addButton = view.findViewById(R.id.addButton);

        //wczytanie listy z shared pref
        favoriteCities = new ArrayList<>(SharedPreferencesManager.loadFavoriteCitiesFromSharedPreferences(requireContext()));
        if (favoriteCities == null) {
            favoriteCities = new ArrayList<>();
        }

        adapter = new ArrayAdapter<String>(requireContext(), R.layout.list_item_city, R.id.cityNameTextView, favoriteCities) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Button deleteButton = view.findViewById(R.id.deleteButton);

                deleteButton.setFocusable(false);
                deleteButton.setFocusableInTouchMode(false);

                deleteButton.setOnClickListener(v -> {
                    String cityToRemove = favoriteCities.get(position);
                    favoriteCities.remove(position);
                    notifyDataSetChanged();

                    SharedPreferencesManager.saveFavoriteCitiesToSharedPreferences(requireContext(), favoriteCities);

                    String currentCity = ((MainActivity) requireActivity()).getCurrentCityName();
                    if (!cityToRemove.equals(currentCity)) {
                        SharedPreferencesManager.removeCurrentWeatherDataForCity(requireContext(), cityToRemove);
                        SharedPreferencesManager.removeForecastWeatherDataForCity(requireContext(), cityToRemove);
                    }
                });


                return view;
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedCity = favoriteCities.get(position);
            if (isNetworkAvailable()) {
                ((MainActivity) requireActivity()).fetchWeatherDataForCity(selectedCity);
            } else {
                WeatherData weatherData = SharedPreferencesManager.loadCurrentWeatherDataForCity(requireContext(), selectedCity);
                ForecastWeatherData forecastWeatherData = SharedPreferencesManager.loadForecastWeatherDataForCity(requireContext(), selectedCity);

                if (weatherData != null && forecastWeatherData != null) {
                    ((MainActivity) requireActivity()).viewModel.setCurrentWeatherData(weatherData);
                    ((MainActivity) requireActivity()).viewModel.setForecastWeatherData(forecastWeatherData.getForecastList());
                } else {
                    Toast.makeText(requireContext(), "No offline data available for " + selectedCity, Toast.LENGTH_SHORT).show();
                }
            }
        });



        addButton.setOnClickListener(v -> showAddCityDialog());

        return view;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showAddCityDialog() {
        final EditText input = new EditText(requireContext()); //pole tekstowe
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        //tworzy okno dialogowe
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Add City")
                .setMessage("Enter the name of the city:")
                .setView(input)
                .setPositiveButton("Add", (dialog, whichButton) -> {
                    String city = input.getText().toString();
                    if (!city.isEmpty()) {
                        favoriteCities.add(city);
                        adapter.notifyDataSetChanged();
                        if (favoriteCities != null) {
                            SharedPreferencesManager.saveFavoriteCitiesToSharedPreferences(requireContext(), favoriteCities);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
