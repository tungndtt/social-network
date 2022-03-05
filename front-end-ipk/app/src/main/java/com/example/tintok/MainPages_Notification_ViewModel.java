package com.example.tintok;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.Model.Notification;

import java.util.ArrayList;


public class MainPages_Notification_ViewModel extends AndroidViewModel {
    MutableLiveData<ArrayList<Notification>> notifications = null;

    public MainPages_Notification_ViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<ArrayList<Notification>> getNotifications() {
        if(notifications == null){
            notifications = DataRepositoryController.getInstance().getNotifications();
        }
        return notifications;

    }

    // TODO: Implement the ViewModel
}