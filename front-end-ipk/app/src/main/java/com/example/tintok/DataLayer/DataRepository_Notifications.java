package com.example.tintok.DataLayer;

import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.tintok.Activity_AppMainPages;
import com.example.tintok.Activity_ChatRoom;
import com.example.tintok.Communication.Communication;
import com.example.tintok.Communication.CommunicationEvent;
import com.example.tintok.Communication.RestAPI_model.NotificationForm;
import com.example.tintok.Model.ChatRoom;
import com.example.tintok.Model.MediaEntity;
import com.example.tintok.Model.Notification;
import com.example.tintok.Model.Post;
import com.example.tintok.Model.UserSimple;
import com.example.tintok.R;
import com.example.tintok.Utils.AppNotificationChannelManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataRepository_Notifications extends AbstractDataRepository {
    MutableLiveData<ArrayList<Notification>> notifications;
    DataRepositoryController controller;

    public DataRepository_Notifications(DataRepositoryController controller){
        notifications = new MutableLiveData<>(new ArrayList<>());
        this.controller = controller;
    }

    public MutableLiveData<ArrayList<Notification>> getNotifications(){
        return notifications;
    }

    public void setNewfeedPosts(Notification... newNoti){
        ArrayList<Notification> mNotifications = notifications.getValue();
        for(Notification p : newNoti){
            mNotifications.add(p);
        }
        notifications.setValue(mNotifications);

    }
    public void postNewfeedPosts(Notification... newNoti){
        ArrayList<Notification> mNotifications = notifications.getValue();
        for(Notification p : newNoti){
            mNotifications.add(p);
        }
        notifications.postValue(mNotifications);

    }

    public void SocketListner(){
        Socket socket = Communication.getInstance().get_socket();

        socket.on(CommunicationEvent.COMMENT_NOTIFICATION, args -> {
            String author_id = (String) args[0];
            String post_id = (String) args[1];
            String post_author_id = (String) args[2];
            String post_status = (String) args[3];
            String post_url= (String) args[4];
            Notification newNoti = new Notification(Notification.NotificationType.COMMENT_POST, LocalDateTime.now(), author_id ,
                    post_url, post_id,  post_author_id, post_status);
            ArrayList<Notification> notis = notifications.getValue();
            notis.add( newNoti);
            notifications.postValue(notis);
            if(newNotificationListeners != null){
                for(OnNewNotificationListener m :newNotificationListeners)
                    m.onNewNotification(newNoti);
            }
            AppNotificationChannelManager.getInstance().pushNotificationBasic("Notifications", "New unseen notifications", "Click to view unseen notifications", OpenNotificationIntent());
        });

        socket.on(CommunicationEvent.LIKE_NOTIFICATION, args -> {
            String author_id = (String) args[0];
            String post_id = (String) args[1];
            String post_author_id = (String) args[2];
            String post_status = (String) args[3];
            String post_url= (String) args[4];
            Notification newNoti = new Notification(Notification.NotificationType.LIKE_POST, LocalDateTime.now(), author_id ,
                    post_url, post_id,  post_author_id, post_status);
            ArrayList<Notification> notis = notifications.getValue();
            notis.add( newNoti);
            notifications.postValue(notis);
            if(newNotificationListeners != null){
                for(OnNewNotificationListener m :newNotificationListeners)
                    m.onNewNotification(newNoti);
            }
            AppNotificationChannelManager.getInstance().pushNotificationBasic("Notifications", "New unseen notifications", "Click to view unseen notifications", OpenNotificationIntent());
        });

        socket.on(CommunicationEvent.MATCHING_NOTIFICATION, args -> {
            String author_id = (String) args[0];
            Log.e("DataRepoNoti", "matching: "+author_id);
            Notification newNoti = new Notification(Notification.NotificationType.NEW_FRIEND, LocalDateTime.now(), author_id ,
                    null, null, null, null);
            ArrayList<Notification> notis = notifications.getValue();
            notis.add( newNoti);
            notifications.postValue(notis);
            if(newNotificationListeners != null){
                for(OnNewNotificationListener m :newNotificationListeners)
                    m.onNewNotification(newNoti);
            }
            AppNotificationChannelManager.getInstance().pushNotificationBasic("Notifications", "New unseen notifications", "Click to view unseen notifications", OpenNotificationIntent());
        });

    }

    public void initData(){
        Communication.getInstance().getApi().getAllNotifications().enqueue(new Callback<ArrayList<NotificationForm>>() {
            @Override
            public void onResponse(Call<ArrayList<NotificationForm>> call, Response<ArrayList<NotificationForm>> response) {
                ArrayList<NotificationForm> notis = response.body();
                Log.e("DataRepo_Noti", "len : "+notis.size());
                ArrayList<Notification> clientNoti  = new ArrayList<>();
                for(NotificationForm noti : notis){
                    Notification.NotificationType m = Notification.NotificationType.COMMENT_POST;
                    if(noti.getType().compareTo(NotificationForm.NEW_COMMENT) == 0)
                        m = Notification.NotificationType.COMMENT_POST;
                    else if(noti.getType().compareTo(NotificationForm.NEW_REACTION) == 0)
                        m = Notification.NotificationType.LIKE_POST;
                    else if(noti.getType().compareTo(NotificationForm.NEW_FRIEND)==0)
                        m = Notification.NotificationType.NEW_FRIEND;
                    Notification newNoti = new Notification(m, Instant.ofEpochMilli(noti.getDate()).atZone(ZoneId.systemDefault()).toLocalDateTime(), noti.getAuthor_id(),
                            noti.getUrl(), noti.getPostID(), noti.getPost_author_id(), noti.getStatus());
                    clientNoti.add(newNoti);
                }
                notifications.postValue(clientNoti);
                setReady();
            }

            @Override
            public void onFailure(Call<ArrayList<NotificationForm>> call, Throwable t) {

            }
        });
        this.SocketListner();
    }

    private Intent OpenNotificationIntent(){
        Intent t = new Intent(DataRepositoryController.applicationContext, Activity_AppMainPages.class);
        t.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        t.putExtra("currentTab", R.id.notification);
        Log.e("DataRepo_Noti", "at "+t.getIntExtra("currentTab", -1));
        //DataRepositoryController.applicationContext.startActivity(t);
        return t;
    }

    ArrayList<OnNewNotificationListener> newNotificationListeners;
    public void addNotificationListener(OnNewNotificationListener mListener){
        if(newNotificationListeners == null)
            newNotificationListeners = new ArrayList<>();
        if(!newNotificationListeners.contains(mListener))
            newNotificationListeners.add(mListener);
        Log.e("notifynoti","called "+ newNotificationListeners);
    }
    public void removeNotificationListener(OnNewNotificationListener mListener){
        if(newNotificationListeners == null)
            return;
        newNotificationListeners.remove(mListener);
    }

    public interface OnNewNotificationListener{
        public void onNewNotification(Notification newNoti);
    }
}
