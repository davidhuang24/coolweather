package com.dh.coolweather.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dh.coolweather.R;
import com.dh.coolweather.db.City;
import com.dh.coolweather.db.Country;
import com.dh.coolweather.db.Province;
import com.dh.coolweather.util.HttpUtil;
import com.dh.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 用于显示省、市、县的Fragment
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE=0;

    public static final int LEVEL_CITY=1;

    public static final int LEVEL_COUNTRY=2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String>adapter;

    private List<String> dataList=new ArrayList<>();//在listview里显示的数据

    private List<Province> provinceList;//省列表

    private List<City> cityList;//市列表

    private List<Country> countryList;//县列表

    private Province selectedProvince;//被选中的省

    private City selectedCity;//被选中的市

    private int currentLevel;//当前选中的级别


    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText=(TextView) view.findViewById(R.id.title_text);
        backButton=(Button) view.findViewById(R.id.back_button);
        listView=(ListView) view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCountries();
                }else if(currentLevel==LEVEL_COUNTRY){//选中县，跳转至天气显示界面
                    String weatherId=countryList.get(position).getWeatherId();
                    if(getActivity() instanceof  MainActivity){//Fragment的context是MainActivity
                        WeatherActivity.actionStart(getActivity(),weatherId);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity weatherActivity=(WeatherActivity)getActivity();
                        weatherActivity.drawerLayout.closeDrawers();
                        weatherActivity.swipeRefresh.setRefreshing(true);
                        weatherActivity.requestWeather(weatherId);
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTRY){
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    //查询(获取)全国所有的省,优先从数据库查询，如果没有查询到再去服务器上查询;显示查询结果
    private void queryProvinces(){
        titleText.setText(R.string.state);
        backButton.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);//查询所有的省记录
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else{
            String address=getResources().getString(R.string.pcc_china_Url);
            queryFromServer(address,"province");
        }
    }

    //查询(获取)省内所有的市,优先从数据库查询，如果没有查询到再去服务器上查询
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList= DataSupport.where("provinceId=?",String.valueOf
                (selectedProvince.getId())).find(City.class);//查询满足约束条件的城市记录
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());//装载数据到dataList
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            int provinceCode=selectedProvince.getProvinceCode();
            String address=getResources().getString(R.string.pcc_china_Url)+"/"+provinceCode;
            queryFromServer(address,"city");
        }

    }

    //查询(获取)市内所有的县（市区、区）,优先从数据库查询，如果没有查询到再去服务器上查询
    private void queryCountries(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countryList=DataSupport.where("cityId=?",String.valueOf
                (selectedCity.getId())).find(Country.class);//查询被选中的城市的所有县记录
        if(countryList.size()>0){
            dataList.clear();
            for (Country country:countryList){
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTRY;
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address=getResources().getString(R.string.pcc_china_Url)
                    +"/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"country");
        }
    }

/*    根据传入的地址和类型从服务器上查询省市县数据；
    1-6是一个用http在服务器查询(从服务器获取指定数据)完整的流程*/
    private void queryFromServer(String address,final String type){
        showProgressDialog();
        //1.发送http请求
        HttpUtil.sendRequestWithOkHttp(address, new Callback() {//回调接口
            @Override
            public void onFailure(Call call, IOException e) {//回调函数：服务器无响应
                getActivity().runOnUiThread(new Runnable() {//通知主线程更新“从服务器加载数据失败”的UI
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),
                                getResources().getString(R.string.load_from_pcc_server_failure),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
            //2.服务器返回数据
            @Override
            public void onResponse(Call call, Response response) throws IOException {//回调函数：服务器响应
                String responseText=response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    //3.解析服务器返回数据；4.将数据存入数据库
                    result= Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("country".equals(type)){
                    result=Utility.handleCountryResponse(responseText,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {//通知主线程更新UI
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                //5.在数据库中查询；6.显示查询结果
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("country".equals(type)){
                                queryCountries();
                            }
                        }
                    });
                }
            }
        });
    }

    //显示进度对话框
    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage(getResources().getString(R.string.load_from_pcc_server));
            progressDialog.setCanceledOnTouchOutside(false);//加载数据时不可交互
        }
        progressDialog.show();
    }
    //关闭进度对话框
    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
