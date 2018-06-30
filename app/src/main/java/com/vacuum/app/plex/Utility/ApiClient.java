package com.vacuum.app.plex.Utility;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.ads.internal.gmsg.HttpClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vacuum.app.plex.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.Certificate;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Home on 2/20/2018.
 */

public class ApiClient {

    public static final String BASE_URL = "https://api.themoviedb.org/3/";
    public static final String BASE_URL2 = "https://mohamedebrahim.000webhostapp.com/";


    //private static Retrofit retrofit = null;
    static Context mContext;


    public static Retrofit getClient(Context mContext2,String ROOT_URL) {
            mContext = mContext2;





            Gson gson = new GsonBuilder()
                .setLenient()
                .create();


            //===========================================================

        /*Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL2)
                .client(getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();*/
        //====================================================================


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ROOT_URL)
                .client(getUnsafeOkHttpClient2())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();


        return retrofit;

    }




    private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            okhttp3.Response originalResponse = chain.proceed(chain.request());
            if (Utils.isNetworkAvailable(mContext)) {
                int maxAge = 10*60; // read from cache for 1 minute
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build();
            } else {
                int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }
        }
    };



    public static OkHttpClient getUnsafeOkHttpClient2() {
        File httpCacheDirectory = new File(mContext.getCacheDir(), "responses");
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            builder.cache(cache);
            builder.addInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR);
            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}