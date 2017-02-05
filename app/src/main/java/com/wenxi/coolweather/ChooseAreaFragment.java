package com.wenxi.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.wenxi.coolweather.db.City;
import com.wenxi.coolweather.db.County;
import com.wenxi.coolweather.db.Province;
import com.wenxi.coolweather.util.HttpUtil;
import com.wenxi.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.litepal.crud.DataSupport.findAll;

/**
 * 展示各级数据页面
 * Created by Administrator on 2017/2/4 0004.
 */

public class ChooseAreaFragment extends Fragment implements View.OnClickListener {
    public static final int LEVEL_PROVINCE = 0;//省
    public static final int LEVEL_CITY = 1;//市
    public static final int LEVEL_COUNTY = 2;//县
    private ProgressDialog progressDialog;
    private ArrayAdapter<String> arrayAdapter;
    private List<String> datalist = new ArrayList<>();
    /**
     * 省级列表
     */
    private List<Province> provinceList;
    /**
     * 市级列表
     */
    private List<City> cityList;
    /**
     * 县级列表
     */
    private List<County> countyList;
    /**
     * 选中的省份
     */
    private Province selectedProince;
    /**
     * 选中的城市
     */
    private City selectedCity;
    /**
     * 当前选中的级别
     */
    private int currentLevel;
    private TextView title_tv;
    private Button back_btn;
    private ListView list_v;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        initView(view);
        arrayAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,datalist);
        list_v.setAdapter(arrayAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list_v.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //判断当前被点击的条目对应的数据
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProince = provinceList.get(i);
                    queryCitys();//查询选中的省份下所有的城市
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(i);
                    queryCountys();//查询选中的城市下所有的县
                }else if (currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(i).getWeatherId();
                    Intent intent = new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                }
            }
        });
        queryProvinces();//默认查询所有的省级数据
    }
    /**查询所有的县级数据*/
    private void queryCountys() {
        //标题设置为当前选中的城市名称
        title_tv.setText(selectedCity.getCityName());
        back_btn.setVisibility(View.VISIBLE);//县级目录显示返回键
        //根据选中的市获取县级列表
        countyList = DataSupport.where("cityId = ?",String.valueOf(selectedCity.getId())).find(County.class);
        //如果县级列表大于0，从数据列表中查询数据，否则从网络查询数据
        if (countyList.size() > 0){
            datalist.clear();
            for(County county:countyList){
                datalist.add(county.getCountyName());
            }
            arrayAdapter.notifyDataSetChanged();
            list_v.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+ provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    /**查询所有的城市数据*/
    private void queryCitys() {
        //标题设置为当前选中的省份名称
        title_tv.setText(selectedProince.getProvinceName());
        back_btn.setVisibility(View.VISIBLE);//市级列表返回键设置为显示
        //根据选中的省分获取市级列表
        cityList = DataSupport.where("provinceId = ?",String.valueOf(selectedProince.getId())).find(City.class);
        //如果市级列表有数据，就从数据库查询，否则就从网络查询
        if (cityList.size()>0){
            datalist.clear();
            for(City city:cityList){
                datalist.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            list_v.setSelection(0);//listv默认选中第0个条目
            currentLevel = LEVEL_CITY;//当前选中级别
        }else {
            int provinceCode = selectedProince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+ provinceCode;
            queryFromServer(address,"city");
        }
    }

    /**初始化控件*/
    private void initView(View view) {
        title_tv = (TextView) view.findViewById(R.id.title_tv);
        back_btn = (Button) view.findViewById(R.id.back_btn);
        list_v = (ListView) view.findViewById(R.id.list_v);
        back_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //点击返回键时，从新查询上级目录
            case R.id.back_btn:
                if (currentLevel == LEVEL_COUNTY){
                    queryCitys();
                }else if (currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
                break;
        }
    }
    /**查询所有的省级数据*/
    private void queryProvinces() {
        //展示省级名称，隐藏返回键，标题设置为中国
        title_tv.setText("中国");
        back_btn.setVisibility(View.GONE);
        //获取省级列表
         provinceList = findAll(Province.class);
        //如果省级列表大于0，先清空数据集合，遍历列表将列表中的所有省份名称添加到数据集合中
        if (provinceList.size() > 0){
            datalist.clear();
            for(Province province: provinceList) {
                datalist.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            list_v.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            //省级列表小于0，从服务器查询省级数据
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }
    /**根据传入的地址和数据类型从服务器上查询各省、市、县数据*/
    private void queryFromServer(String address, final String type) {
        //发送网络请求
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "网络加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                //根据不同的地址和不同的数据类型，解析和处理服务器返回的数据
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();//关闭进度条对话框
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCitys();
                            }else if ("county".equals(type)){
                                queryCountys();
                            }
                        }
                    });
                }
            }
        });
    }
    /**显示对话框*/
    private void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    /**关闭对话框*/
    private void closeProgressDialog() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
