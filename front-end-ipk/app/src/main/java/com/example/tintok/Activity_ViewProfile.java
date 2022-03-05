package com.example.tintok;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tintok.Model.Post;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

/**
 * Within this activity the user can see another user profile.
 * User can see other username, location (if stated), profile picture, number of followers and following person as well as all posts of this user.
 * In addition, the user can follow this person by clicking a button,
 * view the profile picture by clicking on the profile picture {@link View_Profile_Picture_Fragment}
 * and starting a chat with this person.
 * Furthermore,
 * @see ViewProfile_UserInfo_Fragment for additional user information and
 * @see ViewProfile_UserImages_Fragment are all images shown.
 *
 */
public class Activity_ViewProfile extends AppCompatActivity implements DialogInterface.OnDismissListener {

    private Activity_ViewProfile_ViewModel viewModel;

    private ImageView profile_pic;
    private Fragment infoFragment, imageFragment;
    private int selected;
    TextView username;
    private TextView followingNumber, followerNumber;
    private MaterialButton followBtn, messageBtn;
    BottomNavigationView profile_navigation_bar;
    private View_Profile_Picture_Fragment viewProfilePictureFragment;
    Fragment postFragment;
    MaterialToolbar toolbar;
    DialogFragment viewUserList = null;

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        // info of displayed user.
        username = findViewById(R.id.profile_name);
        followingNumber = findViewById(R.id.followingsNumber);
        followerNumber = findViewById(R.id.follwersNumber);
        followBtn = findViewById(R.id.followBtn);
        messageBtn = findViewById(R.id.messageBtn);
        toolbar = findViewById(R.id.view_profile_toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        /*
            to switch between info and image fragment
         */
        profile_navigation_bar = findViewById(R.id.profile_navigation_bar);
        profile_navigation_bar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selected = item.getItemId();
                if (item.getItemId() == R.id.profile_info_item)
                    getSupportFragmentManager().beginTransaction().replace(R.id.profile_sub_fragment, infoFragment).commit();
                else
                    getSupportFragmentManager().beginTransaction().replace(R.id.profile_sub_fragment, imageFragment).commit();
                return true;
            }
        });

        this.viewModel = new ViewModelProvider(this).get(Activity_ViewProfile_ViewModel.class);
        String author_id = getIntent().getStringExtra("author_id");
        this.viewModel.getUserProfile(author_id);
        Log.e("Act", viewModel.toString());
        infoFragment = ViewProfile_UserInfo_Fragment.getInstance();

        this.selected = R.id.profile_info_item;
        profile_navigation_bar.setSelectedItemId( this.selected);

        /*
            to show profile picture
         */
        profile_pic = findViewById(R.id.profile_picture);
        profile_pic.setOnClickListener(v -> {
            if(viewProfilePictureFragment == null)
                viewProfilePictureFragment = new View_Profile_Picture_Fragment();
            Bundle bundle = new Bundle();
            bundle.putString("name", viewModel.getProfile().getValue().getUserName());
            String status = "";
            String url = viewModel.getProfile().getValue().getProfilePic().url;
            for(Post p: viewModel.getProfile().getValue().getMyPosts().getValue()){
                if(p.getImage().url.equals(url))
                    status = p.getStatus();
            }
            bundle.putString("status", status);
            bundle.putString("url", url);
            viewProfilePictureFragment.setArguments(bundle);
            viewProfilePictureFragment.show(getSupportFragmentManager(), "VIEW_PROFILE_PICTURE");
        });


        /*
            sets username, location and profile picture, number of followers and following persons
         */
        viewModel.getProfile().observe(this, userProfile -> {
            if (userProfile == null)
                return;
            Log.e("ActivityVewProfile","at "+userProfile.getProfilePic().url);
            username.setText(userProfile.getUserName());
            Glide.with(this).load(userProfile.getProfilePic().url)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(profile_pic);
            imageFragment = ViewProfile_UserImages_Fragment.getInstance();
            initPosts();

            followingNumber.setText(String.valueOf(userProfile.getFollowing().getValue().size()));
            followerNumber.setText(String.valueOf(userProfile.getFollowers().getValue().size()));
            UpdateFollowBtn();

        });

        followBtn.setOnClickListener(v -> {
            viewModel.UserPressFollow();
            UpdateFollowBtn();
        });
        // to start chatroom
        messageBtn.setOnClickListener(v -> {
           App.startActivityChatroom(Activity_ViewProfile.this, viewModel.openChatRoomWithUser().getChatRoomID());
        });

        this.followerNumber.setOnClickListener(v -> {
            Log.e("Activity_ViewProfile","click follower: "+viewUserList);
            if(viewUserList == null){
                viewUserList = new View_Followers_Fragment(viewModel.getProfile().getValue().getFollowers().getValue());
                viewUserList.show(getSupportFragmentManager(), "Followers");
            }
        });

        this.followingNumber.setOnClickListener(v -> {
            Log.e("Activity_ViewProfile","click following: "+viewUserList);
            if(viewUserList == null){
                viewUserList = new View_Followers_Fragment(viewModel.getProfile().getValue().getFollowing().getValue());
                viewUserList.show(getSupportFragmentManager(), "Following");
            }
        });
    }

    /**
     *  if user follows this person then the follow button is blue else black
     */
    private void UpdateFollowBtn(){
        if(viewModel.isFollowing())
            followBtn.setTextColor(Color.parseColor("#03b5fc"));
        else
            followBtn.setTextColor(Color.BLACK);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * setup all posts of this person
     */
    void initPosts(){
        postFragment = new MainPages_Posts_Fragment(true, true);
        ((MainPages_Posts_Fragment)postFragment).setViewModel(this.viewModel);
        getSupportFragmentManager().beginTransaction().replace(R.id.my_posts, postFragment).commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("Activity_ViewProfile","on start called");
        if(infoFragment == null || imageFragment == null)
            return;
        if (profile_navigation_bar.getSelectedItemId() == R.id.profile_info_item)
            getSupportFragmentManager().beginTransaction().replace(R.id.profile_sub_fragment, infoFragment).commit();
        else
            getSupportFragmentManager().beginTransaction().replace(R.id.profile_sub_fragment, imageFragment).commit();



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("INFO", "Destroying view of profile fragment ...");
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        this.viewUserList = null;
    }
}