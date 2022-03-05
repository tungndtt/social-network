package com.example.tintok;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.Utils.AppNotificationChannelManager;
import com.example.tintok.Utils.AsynTaskRunner;
import com.example.tintok.Utils.CustomCallable;

/**
 * This activity is used to load the user specific data, e.g. user information, chatrooms or posts,
 * from the server and to instantiate a notification channel for notifications and for chat messages.
 * After the initialisation is done, the main activity will be started.
 * @see Activity_AppMainPages
 */
public class Activity_InitData extends AppCompatActivity implements CustomCallable<String> {

    private ProgressBar progressBar;
    int progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_data);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(100);

        AppNotificationChannelManager.getInstance().createNotificationChannel("Message");
        AppNotificationChannelManager.getInstance().createNotificationChannel("Notifications");

        DataRepositoryController.getInstance().initDataFromServer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        new AsynTaskRunner().executeTask(this);
    }

    @Override
    public void afterWorkDone(String result) {
        Intent intent = new Intent(this, Activity_AppMainPages.class);
        intent.addCategory(Intent.ACTION_MAIN);
        startActivity(intent);
        overridePendingTransition(R.anim.animation_in, R.anim.animation_out);
        finish();
    }

    @Override
    public void beforeWork() {

    }

    @Override
    public String call() throws Exception {
        do{
            Thread.sleep(200);
            for(int i = 0 ; i < 20; i++){
                progress += 5;
                progressBar.setProgress(progress);
                Thread.sleep(200);
            }
        }while(!DataRepositoryController.getInstance().isDataReady());
        return "Done";
    }

}