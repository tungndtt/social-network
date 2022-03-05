package com.example.tintok.Communication;

import com.google.gson.annotations.SerializedName;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class RestAPI_Entity {
    /*
    public interface LoginService{
        @POST("/login")
        Call<StatusResponseEntity> login(@Body JsonObject data);
    }

    public interface SignUpService{

        @POST("/sign_up")
        Call<StatusResponseEntity> sign_up(@Body JsonObject data);
    }

    public interface GetKeyService{
        @POST("/PublicKeyRequest")
        Call<KeyResponseEntiy> requestKey(@Body JsonObject data);
    }

    public interface RestApiListener{
        public void onSuccess(AbstractResponseEntity response);
        public void onFailure();
    }




    public class StatusResponseEntity extends AbstractResponseEntity{
        @SerializedName("status")
        public boolean status;
        @SerializedName("token")
        public String mToken;
        @SerializedName("reason")
        public String reason;
    }

    public class KeyResponseEntiy extends AbstractResponseEntity{
        @SerializedName("serverKey")
        public  String serverKey;
    }
    public abstract class AbstractResponseEntity{

    }

     */
}
