package com.example.tintok;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.example.tintok.CustomView.AfterRefreshCallBack;
import com.example.tintok.CustomView.DummyGestureDetectView;
import com.example.tintok.CustomView.Refreshable;
import com.example.tintok.DataLayer.DataRepositiory_Chatrooms;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.DataLayer.DataRepository_Notifications;
import com.example.tintok.Model.ChatRoom;
import com.example.tintok.Model.MessageEntity;
import com.example.tintok.Model.Notification;
import com.example.tintok.Utils.NavBarUntil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

public class Activity_AppMainPages extends AppCompatActivity implements DataRepositiory_Chatrooms.OnNewMessagesListener, DataRepository_Notifications.OnNewNotificationListener,
        SwipeRefreshLayout.OnRefreshListener, AfterRefreshCallBack, DialogInterface.OnDismissListener {

    Activity_AppMainPages_ViewModel mViewModel;
    BottomNavigationView navBar;
    NavigationView navView;
    DrawerLayout drawerLayout;
    GestureDetector mGestureDetector;
    Fragment mediaSurfing, notification, messages;
    DialogFragment peopleBrowsing, myHomepage, password_change_fragment, privacyFragment;
    boolean isOnDialogFragment;
    Fragment current;


    SwipeRefreshLayout refreshLayout;
    //GUI state
    int unseenNotifications;
    int unseenChatrooms;
    public static final String ITEM_PRIVACY = "privacy_policy_fragment";
    public static final String ITEM_MATCHING_PEOPLE = "matching_people";
    public static final String ITEM_POSTS = "posts";
    public static final String ITEM_NOTIFICATIONS = "notifications";
    public static final String ITEM_MESSENGER = "messages";
    public static final String ITEM_MYPROFILE = "profile";
    private boolean isLogoutPressed ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_main_pages);
        mViewModel = new ViewModelProvider(this).get(Activity_AppMainPages_ViewModel.class);
        initActivity();
        initActionBar();
        isLogoutPressed = false;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showNavView();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initActionBar() {
        setSupportActionBar(findViewById(R.id.actionBar));

        getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color=\"black\"><b>"+getString(R.string.app_name) + "</b></font>",HtmlCompat.FROM_HTML_MODE_LEGACY));
        //getSupportActionBar().setTitle(getString(R.string.app_name_tintok));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        ShapeableImageView matchingBtn, myprofileBtn;
        matchingBtn = findViewById(R.id.matchingBtn);
        myprofileBtn = findViewById(R.id.profileBtn);
        matchingBtn.setOnClickListener(v -> {
            if(!isOnDialogFragment){
                peopleBrowsing.show(getSupportFragmentManager(),"Matching");
                isOnDialogFragment = true;
            }

        });
        myprofileBtn.setOnClickListener(v -> {
            if(!isOnDialogFragment) {
                myHomepage.show(getSupportFragmentManager(), "MyProfile");
                isOnDialogFragment = true;
            }
        });
    }


    private void initActivity() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navBar = findViewById(R.id.navBar);
        navView = findViewById(R.id.naviagtion_view);
        refreshLayout = findViewById(R.id.refreshLayout);

        mGestureDetector = new GestureDetector(this, new SwipeGestureDetectorListener());
        drawerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                Log.e("Activi_MainP1", "I touched");
                return true;
            }
        });

        peopleBrowsing = new MainPages__PeopleBrowsing__Fragment();
        messages = new MainPages__Chatroom__Fragment();
        notification = new MainPages_Notification_Fragment();

        mediaSurfing = new MainPages_Posts_Fragment(true, true);
        myHomepage = new MainPages_MyProfile_Fragment();
        password_change_fragment = new Password_Change_Fragment();


        navBar.setOnNavigationItemSelectedListener(item -> {
            NavBarUntil.removeItemsUnderline(navBar);
            NavBarUntil.underlineMenuItem(item);
            switch (item.getItemId()) {
                case R.id.mediasurfing:
                    current = mediaSurfing;
                    break;
                case R.id.notification:
                    current = notification;
                    unseenNotifications = 0;
                    ShowBadgeForNavBar(ITEM_NOTIFICATIONS, unseenNotifications);
                    break;
                case R.id.messagers:
                    current = messages;
                    unseenChatrooms = 0;
                    ShowBadgeForNavBar(ITEM_MESSENGER, unseenChatrooms);
                    break;
                default:
                    return false;
            }
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.animation_in, R.anim.animation_out)
                    .addToBackStack(current.getTag()).replace(R.id.mainPageContent, current).commit();
            return true;
        });
        navBar.setSelectedItemId(R.id.mediasurfing);

        navView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.refresh:

                    break;
                case R.id.logout:
                    App.Logout(Activity_AppMainPages.this);
                    isLogoutPressed = true;
                    finish();
                    break;
                case R.id.change_password:
                    drawerLayout.closeDrawer(GravityCompat.START, false);
                    password_change_fragment.show(getSupportFragmentManager(), "PASSWORD_CHANGE_FRAGMENT");
                    break;
                case R.id.privacy_policy_item:
                    if(privacyFragment == null)
                        privacyFragment = new Privacy_Fragment();
                    privacyFragment.show(getSupportFragmentManager(), ITEM_PRIVACY);
                    break;

            }
            return true;
        });

        isOnDialogFragment = false;
        refreshLayout.setOnRefreshListener(this);
        ShowBadgeForNavBar(ITEM_NOTIFICATIONS, mViewModel.getUnseenNotifications());
        ShowBadgeForNavBar(ITEM_MESSENGER, mViewModel.getUnseenChatrooms());
    }

    public void ShowBadgeForNavBar(String navBarItem, int number) {
        int menuItemID;
        switch (navBarItem) {
            case ITEM_POSTS:
                menuItemID = R.id.mediasurfing;
                break;
            case ITEM_NOTIFICATIONS:
                menuItemID = R.id.notification;
                break;
            case ITEM_MESSENGER:
                menuItemID = R.id.messagers;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + navBarItem);
        }
        BadgeDrawable badge = navBar.getOrCreateBadge(menuItemID);
        if (number == 0) {
            badge.setVisible(false);
            badge.clearNumber();
        } else if (number == -1) {
            badge.setVisible(true);
            badge.clearNumber();
        } else {
            badge.setVisible(true);
            badge.setNumber(number);
        }
    }

    @Override
    public void onBackPressed() {
       /* if(isOnDialogFragment){
            if(myHomepage.isVisible()){
                if(!myHomepage.isCancelable())
                    return;
            }
        }*/

        if (navBar.getSelectedItemId() == R.id.mediasurfing)
            super.onBackPressed();
        else {
            current = mediaSurfing;
            navBar.setSelectedItemId(R.id.mediasurfing);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("navBarID", navBar.getSelectedItemId());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            int currentTab = intent.getIntExtra("currentTab", -1);
            if (currentTab != -1)
                this.navBar.setSelectedItemId(currentTab);
        }
    }


    @Override
    public void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        navBar.setSelectedItemId(savedInstanceState.getInt("navBarID"));
    }

    @Override
    protected void onStart() {
        super.onStart();

        mViewModel.addNewMessageListener(this);
        mViewModel.addNewNotificationListener(this);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.removeNewMessageListener(this);
        mViewModel.removeNewNotificationListener(this);
        if(!isLogoutPressed)
            App.Logout(this);
    }

    public void showNavView() {
        drawerLayout.openDrawer(GravityCompat.START);

    }

    public void hideNavView() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public void OnNotificationClicked(Notification noti) {

    }

    public void OnChatromClicked(ChatRoom r) {
    }


    @Override
    public void onNewMessage(String roomID, MessageEntity msg) {
        unseenChatrooms++;
        this.ShowBadgeForNavBar(ITEM_MESSENGER, unseenChatrooms);
    }

    @Override
    public void onNewNotification(Notification newNoti) {
        Log.e("Activity_Main", "at :" + newNoti.getPostID() + unseenNotifications);
        unseenNotifications++;
        this.ShowBadgeForNavBar(ITEM_NOTIFICATIONS, unseenNotifications);
    }

    @Override
    public void onRefresh() {
        try {
            ((Refreshable) current).onRefresh(this);
            refreshLayout.setRefreshing(true);
        } catch (Exception e) {
            Log.e("MainPage", "current Fragment cant be refresh");
            onRefreshingDone();
        }
    }

    @Override
    public void onRefreshingDone() {
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                Log.e("MainPage", "DoneRefreshing onThread");
                if(refreshLayout.isRefreshing())
                    refreshLayout.setRefreshing(false);
            }
        });
    }

    

    @Override
    public void onDismiss(DialogInterface dialog) {
        this.isOnDialogFragment = false;
        Log.e("AppMainPage", "dismiss");
    }


    public class SwipeGestureDetectorListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_DISTANCE_THRESHOLD = 70;
        private static final int SWIPE_VELOCITY_THRESHOLD = 70;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.e("AppMainPage", "onFling");
            if (Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD)
                if (velocityX > 0)
                    /*showNavView()*/;
                else
                    hideNavView();
            return true;
        }
    }


}