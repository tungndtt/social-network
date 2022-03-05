package com.example.tintok;
import android.app.Application;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.tintok.Communication.Communication;
import com.example.tintok.Communication.RestAPI;
import com.example.tintok.Communication.RestAPI_model.LoginResponseForm;
import com.example.tintok.Communication.RestAPI_model.UnknownUserForm;
import com.example.tintok.Communication.RestAPI_model.UserForm;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This ViewModel class is used to send either a login, sign up or forgot password request to the server via RestAPI.
 * Based on server response the requestListener is triggered.
 */
public class Login_SignUp_ViewModel extends AndroidViewModel {
    private RestAPI api;
    private MutableLiveData<ArrayList<Integer>> chosenInterests;

    public Login_SignUp_ViewModel(Application app){
        super(app);
        this.api = Communication.getInstance().getApi();
        chosenInterests = new MutableLiveData<>();
        chosenInterests.setValue(new ArrayList<Integer>());
    }

    /**
     * Getter and Setter for the chosen interests of user
     * @return List of integers that stand for interests chosen by user
     */
    public LiveData<ArrayList<Integer>> getChosenInterests(){
        return chosenInterests;
    }
    public void setChosenInterests(ArrayList<Integer> interests){
        chosenInterests.setValue(interests);
    }

    /**
     * Uses RestAPI to send login request to server containing an UnknownUserForm with provided email and password.
     * Sets token for communication if response is successful.
     * Triggers requestListener on successful response, on server response with client error  or on connection failures.
     * @see Login_Fragment
     * @param email user's typed in email address
     * @param password  user's typed in password
     * @param listener of Login_Fragment
     */
    public void loginRequest(String email, String password, requestListener listener){
        this.api.postLoginData(new UnknownUserForm("",email,password)).enqueue(new Callback<LoginResponseForm>() {
            @Override
            public void onResponse(Call<LoginResponseForm> call, Response<LoginResponseForm> response) {
                if(response.isSuccessful()){
                    LoginResponseForm body = response.body();
                    Communication.getInstance().setToken(body.getToken());
                    listener.requestSuccess();
                }
                else {
                    try {
                        JSONObject m = new JSONObject(response.errorBody().string());
                        listener.requestFail(m.getString("message"));
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<LoginResponseForm> call, Throwable t) {
                try {
                    throw t;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                listener.connectionFail();
            }
        });
    }

    /**
     * Uses RestAPI to send registration request to server containing an UnknownUserForm with provided username, email, password, birthday, gender and interests.
     * Triggers requestListener on successful response, on server response with client error or on connection failures.
     * @see Sign_up_Fragment
     * @param username user's typed in name
     * @param email user's email address
     * @param birthday user's birthday
     * @param password user's password
     * @param gender user's gender
     * @param interests user's interests
     * @param listener Sign_up_Fragment listener
     */
    public void signUpRequest(String username, String email, String birthday, String password, int gender, ArrayList<Integer> interests, requestListener listener){
        UnknownUserForm form = new UnknownUserForm(username, email, password);
        form.setBirthday(birthday);
        form.setGender(gender);
        form.setInterests(interests);
        this.api.postRegisterData(form).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful())
                    listener.requestSuccess();
                else listener.requestFail("Something wrong while sign up!");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                try {
                    throw t;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                listener.connectionFail();//("Some errors occur in sign up!");
            }
        });
    }

    /**
     * Uses RestAPI to send password forget request to server containing an UserForm with provided email, password
     * Triggers requestListener on successful response, on server response with client error  or on connection failures.
     * @see Password_Reset_Fragment
     * @param email user's email address
     * @param password user's password
     * @param listener Password_Reset_Fragment listener
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void resetPassword(String email, String password,  requestListener listener){
        this.api.resetPassword(new UserForm("",email,password)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    listener.requestSuccess();
                } else {
                    listener.requestFail("No user with given email available");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                try {
                    throw t;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                listener.connectionFail();
            }
        });
    }

    interface requestListener{
        void requestSuccess();
        void requestFail(String reason);
        void connectionFail();
    }
}
