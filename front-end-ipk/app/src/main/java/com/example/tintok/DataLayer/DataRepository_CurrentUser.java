package com.example.tintok.DataLayer;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.example.tintok.Communication.Communication;
import com.example.tintok.Communication.CommunicationEvent;
import com.example.tintok.Communication.RestAPI;
import com.example.tintok.Communication.RestAPI_model.PostForm;
import com.example.tintok.Communication.RestAPI_model.UserForm;
import com.example.tintok.Model.MediaEntity;
import com.example.tintok.Model.Post;
import com.example.tintok.Model.UserProfile;
import com.example.tintok.Model.UserSimple;
import com.example.tintok.Utils.DataConverter;
import com.example.tintok.Utils.FileUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This class used RestAPI to send different requests to server that are related to the current user and receive the response.
 */
public class DataRepository_CurrentUser extends AbstractDataRepository  {
    MutableLiveData<UserProfile> currentUser;
    DataRepositoryController controller;
    public long lastSeen;
    MutableLiveData<Boolean> isUserUpdating;
    MutableLiveData<ResponseEvent>  networkStatus;

    public DataRepository_CurrentUser(DataRepositoryController controller){
        this.controller = controller;
        currentUser = new MutableLiveData<>();
        isUserUpdating = new MutableLiveData<>();
        networkStatus = new MutableLiveData<>();
    }


    /**
     * get the users relevant information after login from the server by sending a UserForm request
     * @see UserForm
     */
    public void initData() {
        RestAPI api = Communication.getInstance().getApi();
        api.getUser().enqueue(new Callback<UserForm>() {
            @Override
            public void onResponse(Call<UserForm> call, Response<UserForm> response) {
                if(response.isSuccessful()){
                    UserForm form = response.body();
                    UserProfile currUser = DataConverter.ConvertToUserProfile(form);
                    currentUser.postValue( currUser);
                    lastSeen = form.getTime();
                    Log.e("DataRepoUser", "lastSeen :"+ form.getTime());
                    setReady();
                } else {
                    Log.e("DataRepoUser", "Response fails");
                }
            }

            @Override
            public void onFailure(Call<UserForm> call, Throwable t) {
                try {
                    throw t;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                Log.e("Error", "Some errors occur");
            }
        });
    }

    /**
     * splits a created post into several RequestBodies and uploads the file to the server.
     * Receives a PostForm and updates LiveData of users posts if response is successful.
     * @param mContext
     * @param newPost the user created
     */
    public void submitNewPost(Context mContext, Post newPost) {
        RestAPI api = Communication.getInstance().getApi();
        if(api != null){
            MultipartBody.Part part = FileUtil.prepareImageFileBody(mContext, "upload", newPost.getImage());
            RequestBody user_id = RequestBody.create(MultipartBody.FORM, newPost.getAuthor_id());
            RequestBody status = RequestBody.create(MultipartBody.FORM, newPost.getStatus());
            String[] split = currentUser.getValue().getProfilePic().url.split("/");
            RequestBody profile_path = RequestBody.create(MultipartBody.FORM, split[split.length-1]);
            api.uploadFile(part, user_id, status, profile_path).enqueue(new Callback<PostForm>() {
                @Override
                public void onResponse(Call<PostForm> call, Response<PostForm> response) {
                    if(response.isSuccessful()){
                        // set id for the post
                        PostForm form = response.body();
                        newPost.setId(form.getId());
                        newPost.getImage().url = form.getImageUrl();
                        newPost.likers = new ArrayList<>();

                        // do something with newPost ...
                        ArrayList<Post> mPosts = currentUser.getValue().myPosts.getValue();
                        mPosts.add(0,newPost);
                        currentUser.getValue().myPosts.postValue(mPosts);

                    } else {
                        // Toast.makeText(getApplication(), "Fail to get response", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<PostForm> call, Throwable t) {
                    try {
                        throw t;
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            });
        } else {
           // Toast.makeText(this.getApplication(), "No file to upload", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Removes or add user's subscription to a post and updates LiveData
     * @param p post user subscribe or unsubscribe
     */
    public void UpdateSubcribedPost(Post p) {
        ArrayList<String> followingPost = this.currentUser.getValue().getFollowingPost().getValue();
        if(followingPost.contains(p.getId()))
            followingPost.remove(p.getId());
        else
            followingPost.add(p.getId());
        this.currentUser.getValue().getFollowingPost().postValue(followingPost);
    }

    /**
     * Converts UserForm into UserProfile. Only used for basic user information
     * @param userForm received from server
     * @return updated UserProfile
     */
    private UserProfile setUpdatedUserProfile(UserForm userForm){
        UserProfile userProfile = currentUser.getValue();
        userProfile.setUserName(userForm.getUsername());
        userProfile.setGender(userForm.getGender());
        userProfile.setBirthday(LocalDate.parse(userForm.getBirthday(),  DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        userProfile.setLocation(userForm.getLocation());
        userProfile.setDescription(userForm.getDescription());
        return userProfile;

    }

    /**
     * Updates basic user information.
     * Convert UserProfile into UserForm to send updated information to server.
     * If response is successful, the current user LiveData is updated.
     * @param userProfile edited userprofile
     */
    public void updateUserInfo(UserProfile userProfile){

        RestAPI api = Communication.getInstance().getApi();
        if(api != null){
            isUserUpdating.setValue(true);
            UserForm userForm = new UserForm(userProfile.getUserName(), "", "");
            userForm.setId(userProfile.getUserID());
            userForm.setBirthday(userProfile.getBirthday().toString());
            userForm.setLocation(userProfile.getLocation());
            userForm.setDescription(userProfile.getDescription());
            userForm.setGender(userProfile.getGender().getI());
            api.updateUserInfo(userForm).enqueue(new Callback<UserForm>() {
                @Override
                public void onResponse(Call<UserForm> call, Response<UserForm> response) {
                    if(response.isSuccessful()){
                        currentUser.postValue(setUpdatedUserProfile(response.body()));
                        currentUser.getValue().getMyPosts().postValue( currentUser.getValue().getMyPosts().getValue());
                       /*
                        UserSimple newUser = new UserSimple();
                        newUser.setUserID(currentUser.getValue().getUserID());
                        newUser.setUserName(currentUser.getValue().getUserName());
                        newUser.setProfilePic(new MediaEntity(currentUser.getValue().getProfilePic().url));
                        */
                        //TODO:
                      //  DataRepositoryController.getInstance().dataRepository_userSimple.updateUserSimpleInCache(newUser);
                        networkStatus.postValue(new ResponseEvent(ResponseEvent.Type.USER_UPDATE, response.message()));
                    }
                    isUserUpdating.postValue(false);
                }
                @Override
                public void onFailure(Call<UserForm> call, Throwable t) {
                    Log.e("response", "failed");
                    isUserUpdating.postValue(false);

                }
            });
        }

    }

    /**
     * Convert password-strings to UserForm.
     * Response tells user if password could be changed or the current password was wrong.
     * @param passwords current and new password
     */
    public void updateUserPassword(List<String> passwords){
        isUserUpdating.setValue(true);
        RestAPI api = Communication.getInstance().getApi();

        UserForm userForm = new UserForm("", currentUser.getValue().getEmail(), passwords.get(0));
        userForm.setId(currentUser.getValue().getUserID());
        userForm.setNew_password(passwords.get(1));

        if(api != null){
            api.updateUserPassword(userForm).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.e("onRe", "response " + response.message());
                    networkStatus.postValue(new ResponseEvent(ResponseEvent.Type.PASSWORD, response.message())); // Created or Unauthorized

                    isUserUpdating.postValue(false);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("onFail", "response " + t.getMessage());
                    isUserUpdating.postValue(false);
                }
            });

        }

    }

    /**
     * since post is the same as a profile picture the behaviour is like submitting a post, but includes to change the profile picture of the user.
     * @see #submitNewPost(Context, Post)
     * @param mContext
     * @param newPost
     */
    public void submitNewProfilePicture(Context mContext, Post newPost){
        RestAPI api = Communication.getInstance().getApi();
        if(api != null){
            isUserUpdating.setValue(true);
            MultipartBody.Part part = FileUtil.prepareImageFileBody(mContext, "upload", newPost.getImage());
            RequestBody user_id = RequestBody.create(MultipartBody.FORM, newPost.getAuthor_id());
            RequestBody status = RequestBody.create(MultipartBody.FORM, newPost.getStatus());
            String[] split = currentUser.getValue().getProfilePic().url.split("/");
            RequestBody profile_path = RequestBody.create(MultipartBody.FORM, split[split.length-1]);
            api.uploadImage(part, user_id, status, profile_path).enqueue(new Callback<PostForm>() {
                @Override
                public void onResponse(Call<PostForm> call, Response<PostForm> response) {
                    if(response.isSuccessful()) {
                        // set id for the post
                        PostForm form = response.body();
                        newPost.setId(form.getId());
                        newPost.getImage().url = form.getImageUrl();
                        newPost.likers = new ArrayList<>();

                        // do something with newPost ...
                        ArrayList<Post> mPosts = currentUser.getValue().myPosts.getValue();
                        mPosts.add(0, newPost);
                        currentUser.getValue().myPosts.postValue(mPosts);
                        // update UserProfile
                        MediaEntity tmpMediaEntity = new MediaEntity(form.getImageUrl());
                        UserProfile tmpUser = currentUser.getValue();
                        tmpUser.setProfilePic(tmpMediaEntity);
                        currentUser.postValue(tmpUser);


                        //TODO:
                        /*
                        UserSimple newUser = new UserSimple();
                        newUser.setUserID(tmpUser.getUserID());
                        newUser.setUserName(tmpUser.getUserName());
                        newUser.setProfilePic(tmpMediaEntity);
                        DataRepositoryController.getInstance().dataRepository_userSimple.updateUserSimpleInCache(newUser);
                         */
                    }
                    networkStatus.postValue(new ResponseEvent(ResponseEvent.Type.PROFILE_PICTURE_UPDATE, response.message()));
                    isUserUpdating.postValue(false);

                }

                @Override
                public void onFailure(Call<PostForm> call, Throwable t) {
                    try {
                        throw t;
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    //Toast.makeText(getApplication(), "Connection fails", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * updates the profile picture on server-side and sets new profile picture
     * @param url of an already created posts
     */
    public void updateUserProfilePicture(String url){
        RestAPI api = Communication.getInstance().getApi();
        if(api != null){
          //  isUserUpdating.setValue(true);
            UserForm userForm = new UserForm("", "", "");
            userForm.setId(currentUser.getValue().getUserID());
            String[] split = url.split("/");
            userForm.setImageUrl(split[split.length-1]);
            api.updateUserImage(userForm).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.isSuccessful()){
                        MediaEntity tmpMediaEntity = new MediaEntity(url);
                        UserProfile tmpUser = currentUser.getValue();
                        tmpUser.setProfilePic(tmpMediaEntity);
                        currentUser.postValue(tmpUser);
                        currentUser.getValue().getMyPosts().postValue( currentUser.getValue().getMyPosts().getValue());

                        //TODO:
                        /*
                        UserSimple newUser = new UserSimple();
                        newUser.setUserID(tmpUser.getUserID());
                        newUser.setUserName(tmpUser.getUserName());
                        newUser.setProfilePic(tmpMediaEntity);
                        DataRepositoryController.getInstance().dataRepository_userSimple.updateUserSimpleInCache(newUser);
                         */
                    }
                    networkStatus.postValue(new ResponseEvent(ResponseEvent.Type.PROFILE_PICTURE_UPDATE, response.message()));
                   // isUserUpdating.postValue(false);

                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("onFailure", "update failed");
                 //   isUserUpdating.postValue(false);

                }
            });

        }

    }

    /**
     * Convert list of integers into UserForm to send an update-request to server.
     * If response is successful, LiveData of interests are updated.
     * @param newInterests new chosen interests by user
     */
    public void updateUserInterests(ArrayList<Integer> newInterests){
        RestAPI api = Communication.getInstance().getApi();
        if(api != null){
            isUserUpdating.setValue(true);
            UserForm userForm = new UserForm("", "", "");
            userForm.setInterests(newInterests);
            api.updateUserInterests(userForm).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    if(response.isSuccessful())
                       currentUser.getValue().setUserInterests(newInterests);

                    networkStatus.postValue(new ResponseEvent(ResponseEvent.Type.INTEREST_UPDATE, response.message()));
                    isUserUpdating.postValue(false);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }
    }

    /**
     * Checks if other user is already a person the user follows or not.
     * In both cases a communication event is triggered
     * @param user other profile user wants to follow or unfollow
     */
    public void UserPressFollow(String user) {
        UserProfile currentUser = this.currentUser.getValue();
        ArrayList<String> currentFollowing = currentUser.getFollowing().getValue();
        Log.e("DataRepo_CurreUser","at " +currentFollowing);
        if (!currentFollowing.contains(user)) {
            Communication.getInstance().get_socket().emit(CommunicationEvent.FOLLOW_USER, user, currentUser.getUserID());
            currentFollowing.add(user);
        } else {
            Communication.getInstance().get_socket().emit(CommunicationEvent.UNFOLLOW_USER, user, currentUser.getUserID());
            currentFollowing.remove(user);
        }
        this.currentUser.getValue().getFollowing().postValue(currentFollowing);
    }

    /**
     * @param user user id of other user
     * @return true if user already follows the other user, else false
     */
    public boolean isCurrentUserFollowingUser(String user){
        UserProfile currentUser = this.currentUser.getValue();
        ArrayList<String> currentFollowing = currentUser.getFollowing().getValue();
        return currentFollowing.contains(user);
    }
}
