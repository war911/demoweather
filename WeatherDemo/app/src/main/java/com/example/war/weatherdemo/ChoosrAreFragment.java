package com.example.war.weatherdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.war.weatherdemo.db.City;
import com.example.war.weatherdemo.db.County;
import com.example.war.weatherdemo.db.Province;
import com.example.war.weatherdemo.util.HttpUtil;
import com.example.war.weatherdemo.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChoosrAreFragment extends Fragment {
    private static final String TAG = "ChoosrAreFragment";
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provincesList = new LinkedList<>();
    private List<City> citiesList = new LinkedList<>();
    private List<County> countiesList = new LinkedList<>();
    //    选中的省
    private Province selectedPronvice;
    //    选中的市
    private City selectedCity;
    //    选中的县级别
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_are, container, false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(arrayAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedPronvice = provincesList.get(i);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = citiesList.get(i);
                    queryCounties();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provincesList = LitePal.findAll(Province.class);
        if (provincesList.size() > 0) {
            dataList.clear();
            for (Province province : provincesList) {
                dataList.add(province.getProvinceName());
            }
            Log.i(TAG, "queryProvinces: dataList "+dataList.toString());
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    private void queryCities() {
        titleText.setText(selectedPronvice.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        City city = LitePal.find(City.class, selectedPronvice.getId());
        if (city!= null){
            citiesList.add(city);
        }
        if (citiesList.size() > 0) {
            dataList.clear();
            Log.i(TAG, "queryCities: citiesList"+citiesList.toString());
            for (City city1 : citiesList) {
                dataList.add(city1.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedPronvice.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;

            queryFromServer(address,"city");
        }
    }

    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        County county1 = LitePal.find(County.class, selectedCity.getCityCode());
        if (county1 != null){
            countiesList.add(county1);
        }
        if (countiesList.size()>0) {
            dataList.clear();
            for (County county : countiesList) {
                dataList.add(county.getCountryName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedPronvice.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    private void queryFromServer(String address, final String type) {
        Log.i(TAG, "queryFromServer: address"+address+ "type"+type);
        showProGressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                closeProgressDialog();
                toastTest();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responeText = response.body().string();
                Log.i(TAG, "onResponse: responeText"+responeText);
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProninceResponse(responeText);
                }else if ("city".equals(type)){
                    result = Utility.handeCityResponse(responeText,selectedPronvice.getProvinceCode());
                }else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(responeText,selectedCity.getId());
                }else {
                    Log.e(TAG, "onResponse error type: "+type );
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }else {
                                Log.e(TAG, "runOnUiThread error type: "+type );
                            }
                        }
                    });
                }else {
                    Log.e(TAG, "result error : "+result );
                }
            }
        });
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


    private void showProGressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void toastTest() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "加载失败...", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }
}
