package com.example.tintok.DataLayer;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.tintok.Communication.Communication;
import com.example.tintok.Communication.CommunicationEvent;
import com.example.tintok.Communication.RestAPI;
import com.example.tintok.Communication.RestAPI_model.PeopleFilterRequest;
import com.example.tintok.Communication.RestAPI_model.UserForm;
import com.example.tintok.CustomView.FilterDialogFragment;
import com.example.tintok.Model.MediaEntity;
import com.example.tintok.Model.UserSimple;
import com.example.tintok.Utils.DataConverter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataRepository_MatchingPeople extends AbstractDataRepository {
    MutableLiveData<ArrayList<UserSimple>> matchingPeople;
    DataRepositoryController controller;

    public DataRepository_MatchingPeople(DataRepositoryController controller){
        this.matchingPeople = new MutableLiveData<>(new ArrayList<>());
        this.controller = controller;
    }

    public MutableLiveData<ArrayList<UserSimple>> getMatchingPeople(){
        return  this.matchingPeople;
    }

    public void setNewPeople(UserSimple... newPeople){
        ArrayList<UserSimple> mPeople =  this.matchingPeople.getValue();
        for(UserSimple p : newPeople){
            mPeople.add(p);
        }
        this.matchingPeople.setValue(mPeople);

    }
    public void postNewPeople(UserSimple... newPeople){
        ArrayList<UserSimple> mPeople = this.matchingPeople.getValue();
        for(UserSimple p : newPeople){
            mPeople.add(p);
        }
        this.matchingPeople.postValue(mPeople);

    }

    protected void submitNewData(ArrayList<UserForm> users) {
        ArrayList<UserSimple> current = this.matchingPeople.getValue();
        if(current == null)
            current = new ArrayList<>();
        current.clear();
        ArrayList<UserSimple> newData = DataConverter.ConvertFromUserFormToSimple(users);
        for(UserSimple user : newData){
            if(current.contains(user))
                current.remove(user);
            current.add(user);
        }
        this.matchingPeople.postValue(current);
    }

    @Override
    public void initData(){
        RestAPI api = Communication.getInstance().getApi();
        if(api != null){
            api.getRecommendedUsers().enqueue(new Callback<ArrayList<UserForm>>() {
                @Override
                public void onResponse(Call<ArrayList<UserForm>> call, Response<ArrayList<UserForm>> response) {
                    if(response.isSuccessful()){
                        ArrayList<UserForm> forms = response.body();
                       submitNewData(forms);
                        setReady();
                    } else {
                        Log.e("DataRepoMatching", "Cannot get users");
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<UserForm>> call, Throwable t) {
                    try {
                        throw t;
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            });
        } else {

        }
    }

    public void FindMatchingByFilter(Filter f){
        if( f == null){
            this.initData();
            return;
        }
        RestAPI api = Communication.getInstance().getApi();
        if(api != null){
            api.getFilteredUser(PeopleFilterRequest.fromFilterState(f)).enqueue(new Callback<ArrayList<UserForm>>() {
                @Override
                public void onResponse(Call<ArrayList<UserForm>> call, Response<ArrayList<UserForm>> response) {
                    if(response.isSuccessful()){
                        ArrayList<UserForm> forms = response.body();
                        submitNewData(forms);
                    } else {
                        Log.e("Info", "Cannot get users");
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<UserForm>> call, Throwable t) {

                }
            });
        }
    }

    public void submitPeopleReaction(UserSimple userSimple, boolean isLiked) {
        ArrayList<UserSimple> users = this.getMatchingPeople().getValue();
        users.remove(userSimple);
        this.getMatchingPeople().postValue(users);
        if(isLiked && !controller.isCurrentUserFollowUser(userSimple.getUserID()))
            controller.UserPressFollow(userSimple.getUserID());
        else if(!isLiked){
            Communication.getInstance().get_socket().emit(CommunicationEvent.UNLIKE_USER, controller.getUser().getValue().getUserID(), userSimple.getUserID());
        }
    }


    public interface Filter{
        public enum Gender{
            MALE, FEMALE, DIVERS,ALL
        }
        public String getFilterName();
        public int getMinAge();
        public int getMaxAge();
        public Gender getGender();
        public boolean[] getInterestBitmap();
    }
    //Server part
}
