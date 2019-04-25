package ru.alexey.weather.Fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.alexey.weather.ActivityAboutWeather;
import ru.alexey.weather.Database.DataBaseHelper;
import ru.alexey.weather.Database.WeatherTable;
import ru.alexey.weather.Entities.ListModel;
import ru.alexey.weather.Entities.WeatherModel;
import ru.alexey.weather.Entities.WeatherRequestModel;
import ru.alexey.weather.ModelOfData.CityModelOfData;
import ru.alexey.weather.ModelOfData.WeatherModelOfData;
import ru.alexey.weather.R;
import ru.alexey.weather.Rest.OpenWeatherRepo;

public class FragmentSearch extends Fragment{
    public static final String ABOUT_WEATHER = "ABOUT_WEATHER";
    public static final String ABOUT_APP = "ABOUT_APP";
    public static final String FEEDBACK = "FEEDBACK";
    public final static String CITY = "CITY";
    public final static String ABOUT = "ABOUT";
    public final static String RESPONSE = "RESPONSE";
    private View view;
    private String cityName;
    private boolean isExitFragmentAboutWeather;
    private WeatherRequestModel responseSerializable;
    private SQLiteDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search, container, false);
        initView();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initDB();
    }

    private void initDB() {
        database = new DataBaseHelper(getContext()).getWritableDatabase();
    }
    //Определяем ориентацию эрана
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isExitFragmentAboutWeather =
                getResources().getConfiguration().orientation
                        == Configuration.ORIENTATION_LANDSCAPE;
    }

    private Intent getIntentAboutWeather() {
        Intent intent = new Intent(getActivity(), ActivityAboutWeather.class);
        intent.putExtra(CITY, cityName);
        intent.putExtra(ABOUT, ABOUT_WEATHER);
        intent.putExtra(RESPONSE, responseSerializable);
        return intent;
    }

    private Intent getIntentAboutApp() {
        Intent intent = new Intent(getActivity(), ActivityAboutWeather.class);
        intent.putExtra(ABOUT, ABOUT_APP);
        return intent;
    }

    private Intent getIntentFeedBack() {
        Intent intent = new Intent(getActivity(), ActivityAboutWeather.class);
        intent.putExtra(ABOUT, FEEDBACK);
        return intent;
    }

    private Bundle getBundleAboutWeather() {
        Bundle bundle = new Bundle();
        bundle.putString(CITY, cityName);
        bundle.putSerializable(RESPONSE, responseSerializable);
        return bundle;
    }

    private void initView(){
        CardView cardViewMoscow = view.findViewById(R.id.cardViewMoscow);
        cardViewMoscow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCellSearch(Objects.requireNonNull(getResources().getString(R.string.moscow)));
            }
        });
        CardView cardViewPeterburg = view.findViewById(R.id.cardViewSaintPeterburg);
        cardViewPeterburg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCellSearch(Objects.requireNonNull(getResources().getString(R.string.saint_petersburg)));
            }
        });
    }

    private void onClickCellSearch(String cityNameOfCard) {
        cityName = cityNameOfCard;
        requestRetrofit(cityName);
    }

    // В зависимости от ориентации экрана создаем фрагмент или новое активити
    private void chooseOrientationAndStart() {
        if(isExitFragmentAboutWeather){
            showFragmentAboutWeather();
        }
        else {
            startActivity(getIntentAboutWeather());
        }
    }

    private void requestRetrofit(final String cityName){
        OpenWeatherRepo.getSingleton().getAPI().loadWeather(cityName,
                "metric",
                16,
                "f3f2763fe63803beef4851d6365c83bc").enqueue(new Callback<WeatherRequestModel>() {
            @Override
            public void onResponse(@NonNull Call<WeatherRequestModel> call,
                                   @NonNull Response<WeatherRequestModel> response) {
                if(response.body() != null && response.isSuccessful()){
                    responseSerializable = response.body();
                    addElements(cityName, responseSerializable);
                    chooseOrientationAndStart();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherRequestModel> call,
                                  @NonNull Throwable t) {
                Toast.makeText(getContext(), getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addElements(@NonNull String cityName, @NonNull WeatherRequestModel response){
        WeatherModelOfData[] weatherModelOfDatas = new WeatherModelOfData[response.list.length];
        int i=0;
        for(ListModel list : response.list){
            WeatherModelOfData weatherModelOfData = new WeatherModelOfData();
            weatherModelOfData.date = list.dt;
            weatherModelOfData.date_txt = list.dt_txt;
            weatherModelOfData.temperature = list.main.temp;
            weatherModelOfData.humidity = list.main.humidity;
            weatherModelOfData.wind = list.wind.speed;
            weatherModelOfData.wind_of_direction = list.wind.deg;
            weatherModelOfData.pressure = list.main.pressure;
            weatherModelOfData.picture = list.weather[0].main;
            weatherModelOfDatas[i++] = weatherModelOfData;
        }
        CityModelOfData cityModelOfData = new CityModelOfData(cityName, weatherModelOfDatas);
        WeatherTable.insertElement(cityModelOfData, database);
    }


    public void onClickMenuAboutApp() {
        if(isExitFragmentAboutWeather){
            showFragmentAboutApp();
        }
        else{
            startActivity(getIntentAboutApp());
        }
    }

    public void onClickMenuFeedback() {
        if(isExitFragmentAboutWeather){
            showFragmentFeedback();
        }
        else{
            startActivity(getIntentFeedBack());
        }
    }

    private void showFragmentAboutWeather(){
        FragmentAboutWeather detail = FragmentAboutWeather.create(getBundleAboutWeather());
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_about_weather, detail);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            fragmentTransaction.commit();
    }

    private void showFragmentAboutApp() {
        FragmentAboutApp detail = new FragmentAboutApp();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_about_weather, detail);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }

    private void showFragmentFeedback() {
        FragmentFeedback detail = new FragmentFeedback();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_about_weather, detail);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }
}
