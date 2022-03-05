package com.example.tintok;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.tintok.DataLayer.DataRepositiory_Chatrooms;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.DataLayer.DataRepository_Notifications;

public class Activity_AppMainPages_ViewModel extends AndroidViewModel {
    public Activity_AppMainPages_ViewModel(@NonNull Application application) {
        super(application);
    }

    public void addNewMessageListener(DataRepositiory_Chatrooms.OnNewMessagesListener listener) {
        DataRepositoryController.getInstance().addNewMessageListener(listener);
    }

    public void removeNewMessageListener(DataRepositiory_Chatrooms.OnNewMessagesListener listener) {
        DataRepositoryController.getInstance().removeNewMessageListener(listener);
    }

    public void addNewNotificationListener(DataRepository_Notifications.OnNewNotificationListener listener) {
        DataRepositoryController.getInstance().addNotificationListener(listener);
    }

    public void removeNewNotificationListener(DataRepository_Notifications.OnNewNotificationListener listener) {
        DataRepositoryController.getInstance().removeNotificationListener(listener);
    }

    public int getUnseenChatrooms() {
        return DataRepositoryController.getInstance().getNumberOfUnseenChatrooms();
    }

    public int getUnseenNotifications() {
        return DataRepositoryController.getInstance().getNumberOfUnseenNotifications();
    }
}
