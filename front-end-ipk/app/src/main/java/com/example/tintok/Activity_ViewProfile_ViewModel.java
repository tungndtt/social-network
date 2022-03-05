package com.example.tintok;

import android.app.Application;
import android.os.Build;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;
import com.example.tintok.Communication.Communication;
import com.example.tintok.Communication.RestAPI;
import com.example.tintok.Communication.RestAPI_model.UserForm;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.Model.ChatRoom;
import com.example.tintok.Model.Post;
import com.example.tintok.Model.UserProfile;
import com.example.tintok.Utils.DataConverter;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The ViewModel is used to load the other user profile and its posts from the server, to open a chat between the user and the other profile.
 * In addition, the ViewModel handle the case when the user click on follow.
 */
public class Activity_ViewProfile_ViewModel extends MainPages_Posts_ViewModel {

    private MutableLiveData<UserProfile> profile;
    private RestAPI api;

    public Activity_ViewProfile_ViewModel(@NonNull Application application) {
        super(application);
        this.profile = new MutableLiveData<>(null);
        this.api = Communication.getInstance().getApi();
    }


    /**
     * Getter for UserProfile and its posts
     */
    public MutableLiveData<UserProfile> getProfile() {
        return profile;
    }
    @Override
    public MutableLiveData<ArrayList<Post>> getPosts() {
        if(this.profile == null)
            return null;
        return this.profile.getValue().getMyPosts();
    }

    /**
     * Fetches the profile from server based on its unique ID
     * @param id of the other user profile.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getUserProfile(String id){
        this.api.getUserProfile(id).enqueue(new Callback<UserForm>() {
            @Override
            public void onResponse(Call<UserForm> call, Response<UserForm> response) {
                if(response.isSuccessful()){
                    UserForm userForm = response.body();
                    UserProfile result = DataConverter.ConvertToUserProfile(userForm);
                    result.setUserID(id);
                    profile.postValue(result);
                }
                else{
                    Toast.makeText(Activity_ViewProfile_ViewModel.this.getApplication(), "Cannot get the user", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UserForm> call, Throwable t) {
                try {
                    throw t;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }


    /**
     * opens a chat with the other user
     * @return ChatRoom between current user and the other profile
     */
    public ChatRoom openChatRoomWithUser() {
        ArrayList<String> userIDs = new ArrayList<>(2);
        userIDs.add(this.profile.getValue().getUserID());
        userIDs.add(DataRepositoryController.getInstance().getUser().getValue().getUserID());
        return DataRepositoryController.getInstance().getChatRoomByUser(userIDs);
    }

    /**
     *  user can follow or not follow the other profile.
     *  The number of followers of the other user will be updated and saved on server-side
     * @see com.example.tintok.DataLayer.DataRepository_CurrentUser#UserPressFollow(String)
     */
    public void UserPressFollow() {
        String currentUser = getCurrentUserID();
        ArrayList<String> currentFollower = this.profile.getValue().getFollowers().getValue();
        if(!currentFollower.contains(currentUser)){
            currentFollower.add(currentUser);
            this.profile.getValue().getFollowers().postValue(currentFollower);
        }
        else{
            currentFollower.remove(currentUser);
            this.profile.getValue().getFollowers().postValue(currentFollower);
        }
        this.profile.postValue(this.profile.getValue());
        DataRepositoryController.getInstance().UserPressFollow(this.profile.getValue().getUserID());
    }

    /**
     * checks whether the current user is in the list of the followers of the other user or not.
     * @return true if the other user is following the current user
     */
    public boolean isFollowing(){
        return getProfile().getValue().getFollowers().getValue().contains(getCurrentUserID());
    }
}
