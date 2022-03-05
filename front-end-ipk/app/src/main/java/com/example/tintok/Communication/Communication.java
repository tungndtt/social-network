package com.example.tintok.Communication;

import android.os.Build;


import android.util.Log;

import androidx.annotation.RequiresApi;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.IOException;

import java.net.URISyntaxException;
import java.security.KeyManagementException;

import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.Socket;
import io.socket.client.IO;
import io.socket.emitter.Emitter;

import io.socket.engineio.client.transports.WebSocket;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class Communication {
    private static Communication instance;
    //ublic static arrayList<String> registeredEvent
    public static final String myLink ="https://iptk-p.herokuapp.com"; //https://192.168.1.116:3000, https://iptk-p.herokuapp.com/

    public OkHttpClient getHttpsClient() {
        return httpsClient;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    private Retrofit retrofit;

    private RestAPI api;

    private OkHttpClient httpsClient;

    public Socket get_socket() {
        return _socket;
    }

    private Socket _socket;

    private Communication() {
        try{
            HostnameVerifier myHostnameVerifier = (hostname, session) -> true;
            SSLContext mySSLContext = SSLContext.getInstance("TLS");
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }


                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) {
                }


                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) {
                }
            }};
            mySSLContext.init(null, trustAllCerts, null);


            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();

                            Request request = original.newBuilder()
                                    .header("Accept", "application/json")
                                    .header("Cookie", "X-Authorization "+ Communication.getInstance().getToken())
                                    .method(original.method(), original.body())
                                    .build();
                            return chain.proceed(request);
                        }
                    })
                    /*.retryOnConnectionFailure(true)
                    .connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                    .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                    .readTimeout(5, TimeUnit.MINUTES)// read timeout
                    .connectionPool(new ConnectionPool(0, 1, TimeUnit.NANOSECONDS))*/
                    .hostnameVerifier(myHostnameVerifier)
                    .sslSocketFactory(mySSLContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);


            httpsClient = clientBuilder.build();
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(myLink)
                    .client(httpsClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            api = retrofit.create(RestAPI.class);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

    }



    public static Communication getInstance(){
        if(instance == null)
            instance = new Communication();
        return instance;
    }

    public RestAPI getApi(){
        return this.api;
    }

    public boolean initScoket(){
        try {
            if (_socket == null) {
                Log.e("Object", "called");



                IO.Options opts = new IO.Options();
                opts.transports = new String[]{WebSocket.NAME};

                opts.forceNew = false;
                opts.reconnection = true;
                opts.callFactory = httpsClient;
                opts.webSocketFactory = httpsClient;
                opts.query = "X-Authorization" + "R3YKZFKBVi";
                opts.path = "/socket.io";


                this._socket = IO.socket(Communication.myLink, opts);
                _socket.connect();
                //_socket.io().timeout(60L);
                String TAG = "Error";
                _socket.emit("request_key",SecureConnection.getInstance().getPubKeyClient() );

               /* _socket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Transport transport = (Transport) args[0];
                        //Log.e("My transport", "Transport error " + args[1].toString());
                        transport.on(Transport.EVENT_ERROR, new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                Exception e = (Exception) args[0];
                                Log.e(TAG, "Transport error " + e);
                                e.printStackTrace();
                                e.getCause().printStackTrace();
                            }
                        });

                        transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                Log.v(TAG, "Caught EVENT_REQUEST_HEADERS after EVENT_TRANSPORT, adding headers");
                                Map<String, List<String>> mHeaders = (Map<String, List<String>>) args[0];
                                //Log.e("my Header before", mHeaders.toString());
                                mHeaders.put("Authorization", Arrays.asList("Basic bXl1c2VyOm15cGFzczEyMw=="));
                                mHeaders.put("'x-user-id'", Arrays.asList("MYUSER"));
                                //Log.e("my Header after", mHeaders.toString());
                            }
                        });

                        transport.on(Transport.EVENT_RESPONSE_HEADERS, new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                @SuppressWarnings("unchecked")
                                Map<String, List<String>> headers = (Map<String, List<String>>) args[0];
                                // access response headers
                               //String cookie = headers.get("set-Authorization").get(0);
                            }
                        });
                    }
                });*/


                _socket.on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {

                        /*JSONObject data = new JSONObject();
                        try {
                            data.put("user-id", "myID");
                            Log.e("TAG", "Reconnect");
                            //_socket.emit("vloz", data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/

                    }
                }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.e("socket error:",args[0].toString());
                    }
                }).on("GotAuthen", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        JSONObject data = (JSONObject)(args[0]);
                        Log.d("Got Data", data.toString());
                    }
                });


            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return true;
    }

    public void setToken(String token){
        PacketFactory.getInstance().mToken = token;
    }
    public String getToken(){
        return PacketFactory.getInstance().mToken;
    }

    public void Close(){
        if(getInstance().get_socket() != null)
            getInstance().get_socket().disconnect();
        instance = null;
    }
    /*
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void LoginRequest(JsonObject jo, RestAPI_Entity.RestApiListener mListener){
        Call<RestAPI_Entity.StatusResponseEntity> res= loginService.login(jo);
        res.enqueue(new Callback<RestAPI_Entity.StatusResponseEntity>() {
            @Override
            public void onResponse(Call<RestAPI_Entity.StatusResponseEntity> call, retrofit2.Response<RestAPI_Entity.StatusResponseEntity> response) {
                RestAPI_Entity.StatusResponseEntity res = response.body();
                mListener.onSuccess(res);
            }
            @Override
            public void onFailure(Call<RestAPI_Entity.StatusResponseEntity> call, Throwable t) {
                mListener.onFailure();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void SignUpRequest(JsonObject jo, RestAPI_Entity.RestApiListener mListener){
        //String data = PacketFactory.getInstance().createPacket(jo);
        Call<RestAPI_Entity.StatusResponseEntity> res= signUpService.sign_up(jo);
        res.enqueue(new Callback<RestAPI_Entity.StatusResponseEntity>() {
            @Override
            public void onResponse(Call<RestAPI_Entity.StatusResponseEntity> call, retrofit2.Response<RestAPI_Entity.StatusResponseEntity> response) {
                RestAPI_Entity.StatusResponseEntity res = response.body();
                mListener.onSuccess(res);
            }

            @Override
            public void onFailure(Call<RestAPI_Entity.StatusResponseEntity> call, Throwable t) {
                mListener.onFailure();
            }
        });
    }
     */
}
