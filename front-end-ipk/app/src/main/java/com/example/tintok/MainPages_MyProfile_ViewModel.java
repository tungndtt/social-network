package com.example.tintok;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.DataLayer.ResponseEvent;
import com.example.tintok.Model.Post;
import com.example.tintok.Model.UserProfile;
import com.example.tintok.Model.UserSimple;
import java.util.ArrayList;
import java.util.List;

/**
 *  This class is responsible for preparing and managing data for several fragments that are related to the current user.
 *  {@link Password_Change_Fragment} {@link Interest_UpdateUser_Fragment} {@link Info_Profile_Fragment}
 *  {@link View_Followers_Fragment} {@link MainPages_MyProfile_Fragment} {@link Password_Change_Fragment}
 *  The class survives configuration chance and expose information via LiveData to fragments.
 *
 */
public class MainPages_MyProfile_ViewModel extends MainPages_Posts_ViewModel {
    private MutableLiveData<UserSimple> editedUserProfile;

    public MainPages_MyProfile_ViewModel(@NonNull Application application) {
        super(application);
        editedUserProfile = new MutableLiveData<>();
    }



    /**
     * Checks whether the user edited its information or not.
     * Compares username, location, birthday, gender and description.
     * @return true if user edited its information else false.
     */
    public boolean isUserEdited(){
        UserProfile currUser = getUserProfile().getValue();
        UserSimple editedUser = editedUserProfile.getValue();
        if(currUser.getUserName().toUpperCase().equals(editedUser.getUserName().toUpperCase()) &&
                currUser.getLocation().toUpperCase().equals(editedUser.getLocation().toUpperCase()) &&
                currUser.getBirthday().isEqual(editedUser.getBirthday()) &&
                currUser.getGender().getI() == editedUser.getGender().getI() &&
                currUser.getDescription().equals(editedUser.getDescription()))
            return false;
        else return true;
    }

    /**
     *  UserSimple value in editedUserProfile is reset to current user information
     */
    public void resetLiveData(){
        UserProfile user = getUserProfile().getValue();
        UserSimple userSimple = editedUserProfile.getValue();
        userSimple.setUserName(user.getUserName());
        userSimple.setLocation(user.getLocation());
        userSimple.setGender(user.getGender().getI());
        userSimple.setBirthday(user.getBirthday());
        userSimple.setDescription(user.getDescription());
        setEditedProfile(userSimple);
    }


    /**
     *  Getter and Setter of MutableLiveData
     *
     */
    public LiveData<UserSimple> getEditedProfile(){
        return editedUserProfile;
    }
    public void setEditedProfile(UserSimple profile){
        editedUserProfile.setValue(profile);
    }
    public MutableLiveData<ResponseEvent> getNetworkResponse(){
        return DataRepositoryController.getInstance().getNetworkResponse();
    }
    public MutableLiveData<UserProfile> getUserProfile(){
        return DataRepositoryController.getInstance().getUser();
    }
    @Override
    public MutableLiveData<ArrayList<Post>> getPosts() {
        return getUserProfile().getValue().getMyPosts();
    }
    public MutableLiveData<Boolean> getIsUserUpdating(){
        return DataRepositoryController.getInstance().getIsUserUpdating();
    }

    /**
     * Those methods forwarding user input/updates (basic user information, password, interests) and new posts to DataRepository_CurrentUser to send request to server.
     * @see com.example.tintok.DataLayer.DataRepository_CurrentUser
     */
    public void submitNewPost(Post newPost) {
        DataRepositoryController.getInstance().submitNewPost(getApplication(), newPost);
    }
    public void submitNewProfilePicture(Post newPost){
        DataRepositoryController.getInstance().submitNewProfilePicture(getApplication(), newPost);
    }
    public void updateUserInfo(UserProfile userProfile){
        DataRepositoryController.getInstance().updateUserInfo(userProfile);
    }
    public void changePassword(List<String> passwords){
        DataRepositoryController.getInstance().changePassword(passwords);
    }
    public void updateUserInterests(ArrayList<Integer> interests){
        DataRepositoryController.getInstance().updateUserInterests(interests);
    }
    public void updateUserProfilePicture(String url){
        DataRepositoryController.getInstance().updateUserProfilePicture(url);
    }

}
