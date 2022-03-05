package com.example.tintok;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Application;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tintok.Communication.Communication;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.Utils.AppNotificationChannelManager;

public class App extends Application implements Application.ActivityLifecycleCallbacks {
    //activity
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        AppNotificationChannelManager.applicationContext = this;
        DataRepositoryController.applicationContext =this;
    }


    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPostStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    //region Activity Transaction Methods
    public static void startActivityComment(Context context, String postID){
        Intent mIntent = new Intent(context, Activity_Comment.class);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(context, R.anim.animation_in, R.anim.animation_out);
        mIntent.putExtra("post_id", postID);
        context.startActivity(mIntent,options.toBundle());
    }

    public static void startActivityChatroom(Context context, String roomID){
        Intent mIntent = new Intent(context, Activity_ChatRoom.class);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(context, R.anim.animation_in, R.anim.animation_out);
        mIntent.putExtra("roomID", roomID);
        context.startActivity(mIntent, options.toBundle());
    }

    public static void startActivityViewProfile(Context context, String profileID){
        String currentUserID = DataRepositoryController.getInstance().getUser().getValue().getUserID();
        if(profileID.compareTo(currentUserID) == 0)
            return;
        Intent mIntent = new Intent(context, Activity_ViewProfile.class);
        ActivityOptions options = ActivityOptions.makeCustomAnimation(context, R.anim.animation_in, R.anim.animation_out);
        mIntent.putExtra("author_id", profileID );
        context.startActivity(mIntent, options.toBundle());
    }

    public static void Logout(Context context){
        Intent mIntent = new Intent(context, Activity_Login_Signup.class);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Communication.getInstance().Close();
        DataRepositoryController.getInstance().ClearRepository();
        ActivityOptions options = ActivityOptions.makeCustomAnimation(context, R.anim.animation_in, R.anim.animation_out);
        context.startActivity(mIntent, options.toBundle());
    }
    //endregion
}
