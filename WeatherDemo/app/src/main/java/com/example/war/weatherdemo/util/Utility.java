package com.example.war.weatherdemo.util;

import android.text.TextUtils;
import android.util.Log;

import com.example.war.weatherdemo.db.City;
import com.example.war.weatherdemo.db.County;
import com.example.war.weatherdemo.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    private static final String TAG = "Utility";

    /**
     * 解析服务器返回的省数据
     */
    public static boolean handleProninceResponse(String resopne){
        if (!TextUtils.isEmpty(resopne)){
            try {
                JSONArray allPrinces = new JSONArray(resopne);
                for (int i = 0; i < allPrinces.length(); i++) {
                    JSONObject provinceObject = allPrinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                Log.e(TAG,"provinc resopne is null :"+e);
                e.printStackTrace();
            }
        }
        return false;
    }

//     市级数据
    public static boolean handeCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCitires = new JSONArray(response);
                for (int i = 0; i < allCitires.length(); i++) {
                    JSONObject cityObject = allCitires.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                    return true;
                }
            } catch (JSONException e) {
                Log.e(TAG,"citry resopne is null :"+e);
                e.printStackTrace();
            }
        }
        return false;
    }

   /*
   *县级数据
   */
   public static boolean handleCountyResponse(String resopne,int cityId){
       if (!TextUtils.isEmpty(resopne)) {
           try {
               JSONArray allCounties = new JSONArray(resopne);
               for (int i = 0; i < allCounties.length(); i++) {
                   JSONObject countyObject = allCounties.getJSONObject(i);
                   County county = new County();
                   county.setCountryName(countyObject.getString("name"));
                   county.setWeatherId(countyObject.getString("weather_id"));
                   county.save();
               }
               return true;
           } catch (JSONException e) {
               Log.e(TAG,"county resopne is null :"+e);
               e.printStackTrace();
           }
       }
       return false;
   }
}
