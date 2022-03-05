package com.example.tintok;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.tintok.DataLayer.DataRepository_Interest;
import java.time.format.DateTimeFormatter;

/**
 * Shows the user information age, gender, interests and description of another user
 */
public class ViewProfile_UserInfo_Fragment extends Fragment {

    private TextView  mAgeTV, mGenderTV, mInterestsTV, mDescriptionTV;
    View view;
    private String interests;
    Activity_ViewProfile_ViewModel mViewModel;


    public ViewProfile_UserInfo_Fragment(){}
    public static ViewProfile_UserInfo_Fragment getInstance(){
        ViewProfile_UserInfo_Fragment fragment = new ViewProfile_UserInfo_Fragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Inflates the layout of this fragment.
     * Initialization of views.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.view_profile_info_fragment, container, false);
        mAgeTV = view.findViewById(R.id.view_profile_age);
        mGenderTV = view.findViewById(R.id.view_profile_gender);
        mDescriptionTV = view.findViewById(R.id.view_profile_description);
        mInterestsTV = view.findViewById(R.id.view_profile_interest);
        return view;
    }

    /**
     * Instantiation of Activity_ViewProfile_ViewModel if it is null
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mViewModel == null)
            mViewModel = new ViewModelProvider(getActivity()).get(Activity_ViewProfile_ViewModel.class);
        Log.e("viewmodel", mViewModel.toString());

    }

    /** Uses the viewmodel to observe the chosen UserProfile.
     *  Initialize its user information age, gender, description and interests accordingly
     *  age, gender and interests cannot be null or empty
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.getProfile().observe(getViewLifecycleOwner(), userProfile -> {
            if(userProfile == null)
                return;
            Log.e("UserInfoFrag", "at "+userProfile.getBirthday());
            mAgeTV.setText(String.valueOf(userProfile.getAge()));
            mGenderTV.setText(userProfile.getGender().toString().toLowerCase());
            if(userProfile.getDescription() == null || userProfile.getDescription().isEmpty())
                mDescriptionTV.setVisibility(View.GONE);
            else mDescriptionTV.setText(userProfile.getDescription());
            interests = "";
            for(int i = 0; i < userProfile.getUserInterests().getValue().size(); i++){
                interests += DataRepository_Interest.interests[userProfile.getUserInterests().getValue().get(i)].toLowerCase() + "\n";
            }
            mInterestsTV.setText(interests);
        });


    }
}
