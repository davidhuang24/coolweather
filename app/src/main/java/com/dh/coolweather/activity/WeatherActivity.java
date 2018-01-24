package com.dh.coolweather.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dh.coolweather.R;
import com.dh.coolweather.gson.ForeCast;
import com.dh.coolweather.gson.Weather;
import com.dh.coolweather.util.HttpUtil;
import com.dh.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT>=21){//5.0及以上沉浸模式,更简单实现方式
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//布局显示在状态栏上
            getWindow().setStatusBarColor(Color.TRANSPARENT);//状态栏颜色透明
        }

        setContentView(R.layout.activity_weather);
        //初始化各控件
        weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        forecastLayout=findViewById(R.id.forecast_layout);
        titleCity=(TextView) findViewById(R.id.title_city);
        titleUpdateTime=(TextView) findViewById(R.id.title_update_time);
        degreeText=(TextView) findViewById(R.id.degree_text);
        weatherInfoText=(TextView) findViewById(R.id.weather_info_text);
        aqiText=(TextView) findViewById(R.id.aqi_text);
        pm25Text=(TextView) findViewById(R.id.pm25_text);
        comfortText=(TextView) findViewById(R.id.comfort_text);
        carWashText=(TextView) findViewById(R.id.car_wash_text);
        sportText=(TextView) findViewById(R.id.sport_text);
        bingPicImg=(ImageView) findViewById(R.id.bing_pic_img);

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);//SharedPreferences实现缓存天气数据功能
        if(weatherString!=null){//有缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else{//没有缓存时从服务器获取天气数据
            String weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        String bingPic=prefs.getString("bing_pic",null);//图片URL缓存
        if(bingPic!=null){//从缓存获取URL,然后加载图片
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{//从服务器获取URL,然后加载图片
            loadBingPic();
        }


    }

    //根据weatherId向服务器请求获取天气数据
    public void requestWeather(final String weatherId){
        String weatherUrl=getResources().getString(R.string.weather_url_head)+weatherId
                +'&'+getResources().getString(R.string.hefeng_weather_key);

        HttpUtil.sendRequestWithOkHttp(weatherUrl, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather !=null&&"ok".equals(weather.status)){
                            //缓存服务器返回的天气数据(去头部，字符串化)
                            SharedPreferences.Editor editor=PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        loadBingPic();//每次更新天气同时更新背景图片
    }

    //加载必应每日一图：从郭神服务器获取必应图片的URL，用Glide加载图片
    private void loadBingPic(){
        String requestBingPic=getResources().getString(R.string.get_bingpicurl_url);
        HttpUtil.sendRequestWithOkHttp(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Toast.makeText(WeatherActivity.this, "加载背景图片失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //显示天气数据(Weathers实体中的数据)
    private void showWeatherInfo(Weather weather){
        if(weather.basic!=null){
            String cityName=weather.basic.cityName;
            String updateTime=weather.basic.update.updateTime.split(" ")[1];
            titleCity.setText(cityName);
            titleUpdateTime.setText(updateTime);
        }

        if(weather.now!=null){
            String degree=weather.now.temperature+"℃";
            String weatherInfo=weather.now.more.info;
            degreeText.setText(degree);
            weatherInfoText.setText(weatherInfo);
        }

        //LinearLayout动态添加子View实现列表效果
        forecastLayout.removeAllViews();
        for(ForeCast foreCast:weather.foreCastList){
            View view= LayoutInflater.from(this).inflate
                    (R.layout.forecast_item,forecastLayout,false);
            TextView dateText=(TextView) view.findViewById(R.id.date_text);
            TextView infoText=(TextView) view.findViewById(R.id.info_text);
            TextView maxText=(TextView) view.findViewById(R.id.max_text);
            TextView minText=(TextView) view.findViewById(R.id.min_text);
            dateText.setText(foreCast.date);
            infoText.setText(foreCast.more.info);
            maxText.setText(foreCast.temperature.max);
            minText.setText(foreCast.temperature.min);
            forecastLayout.addView(view);
        }


        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        if(weather.suggestion!=null){
            String comfort=getResources().getString(R.string.comfort)+weather.suggestion.comfort.info;
            String carWash=getResources().getString(R.string.car_wash)+weather.suggestion.carWash.info;
            String sport=getResources().getString(R.string.sport)+weather.suggestion.sport.info;
            comfortText.setText(comfort);
            carWashText.setText(carWash);
            sportText.setText(sport);
            weatherLayout.setVisibility(View.VISIBLE);
        }

    }

    public static void actionStart(Context context, String weatherId){
        Intent intent=new Intent(context,WeatherActivity.class);
        intent.putExtra("weather_id",weatherId);
        context.startActivity(intent);
    }
}
