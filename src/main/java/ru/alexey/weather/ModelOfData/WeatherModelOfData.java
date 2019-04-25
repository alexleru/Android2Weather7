package ru.alexey.weather.ModelOfData;

import java.io.Serializable;

public class WeatherModelOfData implements Serializable {
    public long date;
    public String date_txt;
    public double temperature;
    public int humidity;
    public double wind;
    public int wind_of_direction;
    public double pressure;
    public String picture;
}
