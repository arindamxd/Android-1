package com.vacuum.app.cinema.Utility;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vacuum.app.cinema.Model.OpenloadResult;
import com.vacuum.app.cinema.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UploadOpenload {
    String OPENLOAD_API_Login,OPENLOAD_API_KEY;
    Retrofit retrofit;
    ApiInterface api;
    Context mContext;
    String id_,title;
    public UploadOpenload(Context mContext,String id,String title){

        this.mContext = mContext;
        this.id_ = id;
        this.title = title;

        String ROOT_URL = "https://mohamedebrahim.000webhostapp.com/";

        OPENLOAD_API_Login = mContext.getResources().getString(R.string.OPENLOAD_API_Login);
        OPENLOAD_API_KEY = mContext.getResources().getString(R.string.OPENLOAD_API_KEY);


        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(ROOT_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        api = retrofit.create(ApiInterface.class);


        Call<String> call_details = api.getSend("https://videospider.in/getvideo?key=Yz25qgFkgmtIjOfB&video_id="+id+"&tmdb=1");
        call_details.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.e("TAG","response.raw().request().url();"+response.raw().request().url());
                UploadOpenload2(response.raw().request().url().toString());

            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("TAG", t.toString());
            }
        });
    }

    private void UploadOpenload2(String raw) {
        Call<OpenloadResult> call_details =
                api.uploadOpenload("https://api.openload.co/1/remotedl/add?login="+OPENLOAD_API_Login+"&key="+OPENLOAD_API_KEY+"&url="+raw+"&folder=5457914");
        call_details.enqueue(new Callback<OpenloadResult>() {
            @Override
            public void onResponse(Call<OpenloadResult> call, Response<OpenloadResult> response) {

                if(response.body().getOpenload().getId() != null){
                    String x = response.body().getOpenload().getId();
                    getfileid(x);
                }

            }
            @Override
            public void onFailure(Call<OpenloadResult> call, Throwable t) {
                Log.e("tag", t.toString());
            }
        });
    }

    private void getfileid(final String id) {
        Call<ResponseBody> call_details =
                api.uploadOpenload_id("https://api.openload.co/1/remotedl/status?login="+OPENLOAD_API_Login+"&key="+OPENLOAD_API_KEY+"&id="+id);
        call_details.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                String remoteResponse= null;
                try {
                    remoteResponse = response.body().string();
                    Log.e("TAG", remoteResponse);
                    JSONObject response2 = new JSONObject(remoteResponse);
                    JSONObject result = response2.getJSONObject("result");
                    JSONObject arr = result.getJSONObject(id);
                    String file_id = arr.getString("extid");
                    new AddMovie(mContext,id_,title,file_id);

                } catch (JSONException e) {
                    Log.e("TAG:forecast", e.toString());
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("TAG", t.toString());
            }
        });
    }

}