package com.example.tintok;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tintok.CustomView.MyDialogFragment;
import com.example.tintok.CustomView.PostUploadFragment;
import com.example.tintok.CustomView.Profile_Picture_BottomSheet;
import com.example.tintok.CustomView.Profile_Picture_UploadFragment;
import com.example.tintok.DataLayer.ResponseEvent;
import com.example.tintok.Model.Post;
import com.example.tintok.Model.UserProfile;
import com.example.tintok.Model.UserSimple;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

/**
 * This class provides several features for the user .
 * In general, user can switch between {@link Info_Profile_Fragment} and {@link Image_Profile_Fragment}.
 * The {@link Info_Profile_Fragment} allows in combination with this fragment to change the basic user information: username, location, birthday, gender, description, interests.
 * The {@link Image_Profile_Fragment} shows all images from self uploaded posts and let the user select one of those images as a profile picture.
 * User can create a new post and see its own posts at the bottom of this fragment.
 * Furthermore, user can click on profile picture to choose between viewing, selecting or creating a new profile picture. A new profile picture is a new post.
 */
public class MainPages_MyProfile_Fragment extends MyDialogFragment implements PostUploadFragment.onNewPostListener, DialogInterface.OnDismissListener {

    private Fragment infoFragment, imageFragment, postFragment;
    private int selected;
    private ImageView profilePic, miniProfilePic;
    private View newPostBtn;
    private TextView followingNumber, followerNumber;
    private EditText username, location;
    private View view;
    private MaterialToolbar toolbar;
    BottomNavigationView profile_navigation_bar;
    Profile_Picture_UploadFragment profilePictureUploadFragment;
    View_Profile_Picture_Fragment viewProfilePictureFragment;
    PostUploadFragment post = null;
    MainPages_MyProfile_ViewModel mViewModel;
    View_Followers_Fragment viewFollowersFragment;

    private final static String NEW_POST = "New Post";
    private final static String NEW_PROFILE_PICTURE = "New Profile Picture";
    private final static String BOTTOM_SHEET = "profile picture bottom sheet";
    private final static String VIEW_PROFILE_PICTURE = "view profile picture";


    //TODO: change empty constructor
    @RequiresApi(api = Build.VERSION_CODES.O)
    public MainPages_MyProfile_Fragment() {
        Log.i("Init", "Initialize profile fragment...");
        this.selected = R.id.profile_info_item;
        this.infoFragment = Info_Profile_Fragment.getInstance();
        this.imageFragment = Image_Profile_Fragment.getInstance();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static MainPages_MyProfile_Fragment getInstance() {
        MainPages_MyProfile_Fragment fragment = new MainPages_MyProfile_Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("INFO", "Creating new fragment for profile...");
    }


    /**
     * setup of views, NavigationBar
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i("INFO", "Creating view for profile fragment ...");
        view = inflater.inflate(R.layout.mainpages_myprofile_fragment, container, false);
        //this.viewModel.setFragment(this);
        username = view.findViewById(R.id.profile_name);
        newPostBtn = view.findViewById(R.id.newPostBtn);
        profilePic = view.findViewById(R.id.post_profile);
        followingNumber = view.findViewById(R.id.followingsNumber);
        followerNumber = view.findViewById(R.id.follwersNumber);
        location = view.findViewById(R.id.profile_location);
        toolbar = view.findViewById(R.id.myProfile_toolbar).findViewById(R.id.toolbar);
        miniProfilePic = view.findViewById(R.id.mini_post_profile_picture);

        profile_navigation_bar = view.findViewById(R.id.profile_navigation_bar);
        profile_navigation_bar.setSelectedItemId(this.selected);

        /*
            enables/disables username and location, so that the user cannot edit any information if he is not at Info_Profile_Fragment
            Tests, if any basic user information is edited. If so, an alert pops up to ask user if he wants to continue and discard all changes or not.
         */
        profile_navigation_bar.setOnNavigationItemSelectedListener(item -> {
            selected = item.getItemId();
            if (item.getItemId() == R.id.profile_info_item){
                getChildFragmentManager().beginTransaction().replace(R.id.profile_sub_fragment, infoFragment).commit();
                username.setEnabled(true);
                location.setEnabled(true);
            }
            else if(mViewModel.isUserEdited() && item.getItemId() == R.id.profile_photo_item)
                getFragmentChangeAlertBuilder().show();
            else{
                location.setEnabled(false);
                username.setEnabled(false);
                getChildFragmentManager().beginTransaction().replace(R.id.profile_sub_fragment, imageFragment).commit();

            }
            return true;
        });
        setupFullscreen();
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mViewModel == null)
            mViewModel = new ViewModelProvider(this).get(MainPages_MyProfile_ViewModel.class);
        Log.e("MyProfile", mViewModel.toString());
        initPosts();
        /*
            sets editedSimpleUser values to current user values
         */
        UserSimple userSimple = new UserSimple();
        userSimple.setUserName(mViewModel.getUserProfile().getValue().getUserName());
        userSimple.setLocation(mViewModel.getUserProfile().getValue().getLocation());
        userSimple.setBirthday(mViewModel.getUserProfile().getValue().getBirthday());
        userSimple.setGender(mViewModel.getUserProfile().getValue().getGender().getI());
        userSimple.setDescription(mViewModel.getUserProfile().getValue().getDescription());
        mViewModel.setEditedProfile(userSimple);

        toolbar.setNavigationOnClickListener(v -> {
            if(mViewModel.isUserEdited()){
                getBackButtonAlertBuilder().show();
            }
            else getDialog().dismiss();
        });

        if (profile_navigation_bar.getSelectedItemId() == R.id.profile_info_item){
            location.setEnabled(true);
            username.setEnabled(true);
            getChildFragmentManager().beginTransaction().replace(R.id.profile_sub_fragment, infoFragment).commit();}
        else{
            location.setEnabled(false);
            username.setEnabled(false);
            getChildFragmentManager().beginTransaction().replace(R.id.profile_sub_fragment, imageFragment).commit();}

        /*
            notify data change to editedUserSimple of ViewModel
         */
        location.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                UserSimple user = mViewModel.getEditedProfile().getValue();
                user.setLocation(input);
                mViewModel.setEditedProfile(user);

            }
        });
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                String name = s.toString();
                UserSimple user = mViewModel.getEditedProfile().getValue();
                user.setUserName(name);
                mViewModel.setEditedProfile(user);
            }
        });

        /*
            Instantiate a new PostUploadFragment, so that the user can upload new content.
         */
        newPostBtn.setOnClickListener(v -> {
            if(post == null){
                post = new PostUploadFragment(MainPages_MyProfile_Fragment.this::onNewPost);
                post.show(getChildFragmentManager(), NEW_POST);
            }

        });
        /**
            If user clicks on followerNumber, he can see all of its followers
            {@link View_Followers_Fragment}
         */
        followerNumber.setOnClickListener(v -> {
            if(viewFollowersFragment == null){
                viewFollowersFragment = View_Followers_Fragment.newInstance(mViewModel.getUserProfile().getValue().followers.getValue());
                viewFollowersFragment.show(getChildFragmentManager(), "VIEW_FOLLOWING_FRAGMENT");
            }
        });
        /**
            If user clicks on followingNumber, he can see all of its persons he follows.
            {@link View_Followers_Fragment}
         */
        followingNumber.setOnClickListener(v -> {
            if(viewFollowersFragment == null){
                viewFollowersFragment = View_Followers_Fragment.newInstance(mViewModel.getUserProfile().getValue().following.getValue());
                viewFollowersFragment.show(getChildFragmentManager(), "VIEW_FOLLOWING_FRAGMENT");
            }

        });

        /*
            onClick, user gets redirected to either view his own profile picture,
            add a new profile picture or selecting one of its uploaded pictures as a profile picture.
         */
        profilePic.setOnClickListener(v -> {
            Profile_Picture_BottomSheet profilePictureBottomSheet = new Profile_Picture_BottomSheet();
            profilePictureBottomSheet.show(getActivity().getSupportFragmentManager(), BOTTOM_SHEET);
            profilePictureBottomSheet.setOnTextViewClickListener(position -> {
                switch (position){
                    case 0: // view profile picture
                        profilePictureBottomSheet.dismiss();
                        if(viewProfilePictureFragment == null)
                            viewProfilePictureFragment = new View_Profile_Picture_Fragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("name", mViewModel.getUserProfile().getValue().getUserName());
                        String status = "";
                        String url = mViewModel.getUserProfile().getValue().getProfilePic().url;
                        for(Post p: mViewModel.getUserProfile().getValue().getMyPosts().getValue()){
                            if(p.getImage().url.equals(url))
                                status = p.getStatus();
                        }
                        bundle.putString("status", status);
                        bundle.putString("url", url);
                        viewProfilePictureFragment.setArguments(bundle);
                        viewProfilePictureFragment.show(getChildFragmentManager(), VIEW_PROFILE_PICTURE);
                            break;
                    case 1: // select profile picture
                        profilePictureBottomSheet.dismiss();
                        if(mViewModel.isUserEdited() && profile_navigation_bar.getSelectedItemId() == R.id.profile_info_item){
                            getFragmentChangeAlertBuilder().show();
                        }else if (profile_navigation_bar.getSelectedItemId() == R.id.profile_info_item) {
                            profile_navigation_bar.setSelectedItemId(R.id.profile_photo_item);
                            getChildFragmentManager().beginTransaction().replace(R.id.profile_sub_fragment, imageFragment).commit();
                        }else
                            Snackbar.make(getView(), "Click on your picture", Snackbar.LENGTH_SHORT).show();
                        break;
                    case 2: // add profile picture
                        profilePictureBottomSheet.dismiss();
                        if(mViewModel.isUserEdited())
                            getBackButtonAlertBuilder().show();
                        if(!mViewModel.isUserEdited() && profilePictureUploadFragment == null){
                            profilePictureUploadFragment = new Profile_Picture_UploadFragment();
                            profilePictureUploadFragment.show(getChildFragmentManager(), NEW_PROFILE_PICTURE);
                            profilePictureUploadFragment.setOnNewProfilePictureListener(newPost -> {
                                mViewModel.submitNewProfilePicture(newPost);
                            });
                        }profilePictureUploadFragment = null;
                        break;
                }
            });
        });

    }



    /**
     * Initialisation of a new {@link MainPages_Posts_Fragment} to show all uploaded posts by the user.
     */
    void initPosts(){
        postFragment = new MainPages_Posts_Fragment(true,false);
        ((MainPages_Posts_Fragment)postFragment).setViewModel(this.mViewModel);
        getChildFragmentManager().beginTransaction().replace(R.id.my_posts, postFragment).commit();

    }

    /**
     * Setup observers of LiveData from the current user and from network response.
     * If UserProfile changes, then username, location, profile picture and number of follower and of following person are set accordingly.
     * If the a new profile picture is created or an old one is picked, then a snackbar gives the user feedback about the successfully request.
     *
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState ) {
        super.onActivityCreated(savedInstanceState);
        Log.i("Info", "MyProfile onActivityCreated");
        mViewModel.getUserProfile().observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile == null)
                return;
            username.setText(userProfile.getUserName().toUpperCase());
            if(userProfile.getLocation() == null || userProfile.getLocation().isEmpty())
                location.setHint(getResources().getString(R.string.location_hint).toUpperCase());
            else location.setText(userProfile.getLocation().toUpperCase());
            Glide.with(this.getContext()).load(userProfile.getProfilePic().url)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(profilePic);
            Glide.with(this.getContext()).load(userProfile.getProfilePic().url)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(miniProfilePic);
            followerNumber.setText(String.valueOf(userProfile.getFollowers().getValue().size()));
            followingNumber.setText(String.valueOf(userProfile.getFollowing().getValue().size()));
        });
        mViewModel.getNetworkResponse().observe(getViewLifecycleOwner(), responseEvent -> {
            if(responseEvent.getType() == ResponseEvent.Type.PROFILE_PICTURE_UPDATE || responseEvent.getType() == ResponseEvent.Type.PROFILE_PICTURE_UPLOAD){
                String response = responseEvent.getContentIfNotHandled();
                if(response != null && response.equals("Created"))
                    Snackbar.make(getView(), "Profile Picture Updated", Snackbar.LENGTH_LONG).show();
                if(response != null && response.equals("Ok"))
                    Snackbar.make(getView(), "Profile Picture Saved", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("INFO", "Destroying view of profile fragment ...");
    }

    @Override
    public void onNewPost(Post newPost) {
        post = null;
        if(newPost != null)
            mViewModel.submitNewPost(newPost);
    }

    /**
     * This dialog will be shown if the user edited his profile and pressed the BackButton.
     * User can either cancel the action and further edit his profile and save his changes or continue his action and thus discard his changes.
     * @return MaterialAlertDialogBuilder
     */
    private MaterialAlertDialogBuilder getBackButtonAlertBuilder(){
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(getContext());
        dialogBuilder.setCancelable(true)
                .setMessage("Your changes will be lost. Do you want to continue?")
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.cancel();})
                .setPositiveButton("Continue", (dialog, which) -> {
                   mViewModel.resetLiveData();
                   getDialog().dismiss();})
                .create();
        return dialogBuilder;
    }

    /**
     * Like {@link #getBackButtonAlertBuilder()}but is triggered,
     * if user wants to leave this fragment by choosing one option in BottomSheetFragment or selecting ImageFragment of profile navigation bar
     * @return
     */
    private MaterialAlertDialogBuilder getFragmentChangeAlertBuilder(){
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(getContext());
        dialogBuilder.setCancelable(true)
                .setMessage("Your changes will be lost. Do you want to continue?")
                .setNegativeButton("Cancel", (dialog, which) -> {
                    selected = R.id.profile_info_item;
                    profile_navigation_bar.setSelectedItemId(selected);
                    username.setEnabled(true);
                    location.setEnabled(true);
                    dialog.cancel();})
                .setPositiveButton("Continue", (dialog, which) -> {
                    mViewModel.resetLiveData();
                    UserProfile user = mViewModel.getUserProfile().getValue();
                    mViewModel.getUserProfile().setValue(user);
                    if(selected == R.id.profile_photo_item)
                        getChildFragmentManager().beginTransaction().replace(R.id.profile_sub_fragment, imageFragment).commit();
                    username.setEnabled(false);
                    location.setEnabled(false);
                  })
                .create();
        return dialogBuilder;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()){
            @Override
            public void onBackPressed() {

                if(mViewModel.isUserEdited()){
                    MaterialAlertDialogBuilder dialogBuilder = getBackButtonAlertBuilder();
                    dialogBuilder.show();
                }else super.onBackPressed();
            }
        };
    }

    @Override
    public boolean isCancelable() {
        return post == null;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void onCloseViewUserList(){
        this.viewFollowersFragment = null;
    }
}