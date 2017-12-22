package com.mustache.myhttpapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Set;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private OkHttpClient client;

    Button getBt;

    private static final String url = "https://192.168.3.34:8443/gw/api/china?a=b23123&b=1231";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TMSServer.init();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init okhttp
        Cache cache = new Cache(new File(this.getCacheDir() + "/get_cache"), 10*1024*1024);
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.cache(cache);
        builder.addInterceptor(new NetCacheInterceptor());
        builder.addNetworkInterceptor(new RewriteResponseInterceptor());
        builder.sslSocketFactory(createSSLSocketFactory(), new TrustAllManager());
        builder.hostnameVerifier(new TrustAllHostnameVerifier());
        client = builder.build();

        //init view
        getBt = findViewById(R.id.get);
        getBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Request request = new Request.Builder().url(url).build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i(TAG, "onFailure");
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.i(TAG, "onResponse: response body =" + response.body().string());
                    }
                });
            }
        });

        Button tmsupload = findViewById(R.id.tmsupload);
        tmsupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadReq req = new UploadReq();
                req.setId("1");
                req.setVersion("20170910");
                TMSServer.api.upload(req, "IJO!OIJDI@!OIJOIJD+/d1").enqueue(new
                                                                                   retrofit2.Callback<UploadResult>() {
                    @Override
                    public void onResponse(retrofit2.Call<UploadResult> call, retrofit2.Response<UploadResult> response) {
                        Log.i(TAG, "upload.onResponse:" + response.body());
                    }

                    @Override
                    public void onFailure(retrofit2.Call<UploadResult> call, Throwable t) {
                        Log.i(TAG, "upload.onFailure:" + t.getMessage());

                    }
                });

            }
        });

        Button tmsquery = findViewById(R.id.tmsquery);
        tmsquery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TMSServer.api.query("IJO!OIJDI@!OIJOIJD+/d1").enqueue(new retrofit2.Callback<AgreeResult>() {
                    @Override
                    public void onResponse(retrofit2.Call<AgreeResult> call, retrofit2.Response<AgreeResult> response) {
                        Log.i(TAG, "query.onResponse:" + response.body());
                    }

                    @Override
                    public void onFailure(retrofit2.Call<AgreeResult> call, Throwable t) {
                        Log.i(TAG, "query.onFailure:" + t.getMessage());

                    }
                });
            }
        });
    }

    /**
     * 缓存拦截器
     */
    private static class NetCacheInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Log.i(TAG, "intercept: intercept in net cache.");
            Response originalResponse = chain.proceed(chain.request());

            Log.i(TAG, "intercept: decrypt response body.");
            String encryptBody = new String(originalResponse.body().bytes());
            Log.i(TAG, "intercept: decrypt response base64body="+encryptBody);
            byte[] responseBytes = Base64.decode(encryptBody, Base64.DEFAULT);
            byte[] newResponseBytes = (new String(responseBytes)).getBytes(Charset
                    .forName("utf-8"));
            InputStream origBytes = new ByteArrayInputStream(newResponseBytes);

            //重新设置头长度
            Headers bodyHeader = resetContentLength(originalResponse.headers(), newResponseBytes.length);
            return originalResponse.newBuilder().removeHeader("pragma")
                    .header("Cache-Control", "max-age=10")//设置10秒
                    .header("Cache-Control", "max-stale=30").build();
        }
    }

    private static Headers resetContentLength(Headers headers, int length)
    {
        return headers.newBuilder().set("Content-Length", String.valueOf(length)).build();
    }

    private static class RewriteResponseInterceptor implements Interceptor
    {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Log.i(TAG, "intercept: intercept in RewriteResponse.");
            Response originalResponse = chain.proceed(chain.request());

            //begin encrypt body
            byte[] responseBytes = originalResponse.body().bytes();
            Log.i(TAG, "intercept: encrypt before="+Arrays.toString(responseBytes));
            String baseBody = Base64.encodeToString(responseBytes, Base64.DEFAULT);
            Log.i(TAG, "intercept: encrypt response body=" + baseBody);
            InputStream origBytes = new ByteArrayInputStream(baseBody.getBytes(Charset
                    .forName("utf-8")));

            //begin encrypt url
            HttpUrl url = originalResponse.request().url();
            String encodeQuery = url.encodedQuery();
            Log.i(TAG, "intercept: encodeQuery="+encodeQuery);

            Response.Builder builder = originalResponse.newBuilder();
            if (encodeQuery != null) {
                HttpUrl.Builder urlBuilder = url.newBuilder();
                String newQuery = Base64.encodeToString(encodeQuery.getBytes(), Base64.DEFAULT);
                urlBuilder.encodedQuery(newQuery);
                HttpUrl newUrl = urlBuilder.build();
                Log.i(TAG, "intercept: encrypt url=" + newUrl);
                Request newRequest = originalResponse.request().newBuilder().url(newUrl).build();
                builder.request(newRequest);
            }
            Headers headers = originalResponse.headers();
            String newContentLength = String.valueOf(baseBody.length());
            Headers newHeaders = headers.newBuilder().set("Content-Length", newContentLength)
                    .build();

            Response response = builder.header("Content-Length", newContentLength).build();
            return response;
        }
    }

    private static SSLSocketFactory createSSLSocketFactory() {

        SSLSocketFactory sSLSocketFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustAllManager[]{new TrustAllManager()}, new SecureRandom());
            sSLSocketFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return sSLSocketFactory;
    }

    private static class TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)

                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }


}
