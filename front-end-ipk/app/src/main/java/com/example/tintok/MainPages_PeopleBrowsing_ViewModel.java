package com.example.tintok;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.tintok.Communication.Communication;
import com.example.tintok.Communication.CommunicationEvent;
import com.example.tintok.CustomView.AfterRefreshCallBack;
import com.example.tintok.CustomView.FilterDialogFragment;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.Model.UserSimple;

import java.util.ArrayList;

public class MainPages_PeopleBrowsing_ViewModel extends AndroidViewModel {
    public MainPages_PeopleBrowsing_ViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<ArrayList<UserSimple>> getMatchingPeople(){
        return DataRepositoryController.getInstance().getMatchingPeople();
    }

    public void refreshData(AfterRefreshCallBack e) {
    }

    public void submitPeopleReaction(UserSimple userSimple, boolean isLiked) {
        DataRepositoryController.getInstance().submitPeopleReaction(userSimple, isLiked);
    }

    public void submitFilter(FilterDialogFragment.FilterState currentState) {
        if(currentState.equals(new FilterDialogFragment.FilterState())){
            currentState = null;
        }
        DataRepositoryController.getInstance().findPeoplewithFilter(currentState);
    }


    public void loadMoreMatchingPeople() {
        DataRepositoryController.getInstance().findMoreMatchingPeople();
    }
}
