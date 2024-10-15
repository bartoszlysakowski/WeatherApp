    package com.example.myapplication;

    import androidx.lifecycle.LiveData;
    import androidx.lifecycle.MutableLiveData;
    import androidx.lifecycle.ViewModel;

    import java.util.List;

    public class WeatherViewModel extends ViewModel {
        private final MutableLiveData<WeatherData> currentWeatherData = new MutableLiveData<>();
        private final MutableLiveData<List<Forecast>> forecastWeatherData = new MutableLiveData<>();

        public LiveData<WeatherData> getCurrentWeatherData() {
            return currentWeatherData;
        }

        public void setCurrentWeatherData(WeatherData data) {
            currentWeatherData.setValue(data);
        }

        public LiveData<List<Forecast>> getForecastWeatherData() {
            return forecastWeatherData;
        }

        public void setForecastWeatherData(List<Forecast> forecastList) {
            forecastWeatherData.setValue(forecastList);
        }
    }


