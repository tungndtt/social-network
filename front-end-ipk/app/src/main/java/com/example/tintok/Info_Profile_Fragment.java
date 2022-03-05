package com.example.tintok;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.tintok.DataLayer.DataRepository_Interest;
import com.example.tintok.DataLayer.ResponseEvent;
import com.example.tintok.Model.UserProfile;
import com.example.tintok.Model.UserSimple;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * This class gives the user the opportunity to view his stated information and to change those.
 * Those information encompass his birthday, gender and description and interests.
 * Birthday and gender must be stated while description can be empty.
 * Interests are saved separately, but are shown in this fragment
 * @see Interest_UpdateUser_Fragment
 *
 */
public class Info_Profile_Fragment extends Fragment {

    public static final String INTEREST_FRAGMENT = "interest_fragment";
    private MainPages_MyProfile_ViewModel mViewModel;
    private TextView mEmailTextView, mBirthdayTextView, mInterestsTV, interestBtn, mBirthdayBtn;
    private Spinner mGenderSpinner;
    private EditText mDescriptionEditText;
    View view;
    private ProgressBar mProgressBar;
    private MaterialButton saveBtn;
    private DatePickerDialog.OnDateSetListener  mOnDataSetListener;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private int day, year, month;
    private String interests;
    private DialogFragment interestFragment;


    public Info_Profile_Fragment() {
        // Required empty public constructor
    }

    public static Info_Profile_Fragment getInstance() {
        Info_Profile_Fragment fragment = new Info_Profile_Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Inflate the layout for this fragment and init views
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("INFO", "Creating view for info profile ...");
        view = inflater.inflate(R.layout.profile_info_fragment_test, container, false);
        mEmailTextView = view.findViewById(R.id.profile_email);
        mGenderSpinner = view.findViewById(R.id.profile_gender);
        mDescriptionEditText = view.findViewById(R.id.profile_description);
        mProgressBar = view.findViewById(R.id.profile_progressBar);
        saveBtn = view.findViewById(R.id.profile_edit_profile_button);
        mBirthdayTextView = view.findViewById(R.id.profile_birthday);
        mInterestsTV = view.findViewById(R.id.profile_interest);
        interestBtn = view.findViewById(R.id.profile_interest_addBtn);
        mBirthdayBtn = view.findViewById(R.id.profile_birthday_editBtn);
        return view;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mViewModel == null){
            mViewModel = new ViewModelProvider(requireParentFragment()).get(MainPages_MyProfile_ViewModel.class); //getParentFragment()
        }
        Log.e("MyInfo", mViewModel.toString());
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGenderSpinner.setAdapter(adapter);

        UserProfile user = mViewModel.getUserProfile().getValue();
        mEmailTextView.setText(user.getEmail());

        /*
            LiveData observer for current UserProfile.
            If the UserProfile changes, birthday, gender and description are set accordingly.
         */
        mViewModel.getUserProfile().observe(getViewLifecycleOwner(), userProfile -> {

            if(userProfile == null)
                return;
            mBirthdayTextView.setText(formatter.format(userProfile.getBirthday()));
            setCurrentGenderSpinner(userProfile);
            if(userProfile.getDescription() == null || userProfile.getDescription().isEmpty())
                mDescriptionEditText.setHint(R.string.inspirational_quote);
            else mDescriptionEditText.setText(userProfile.getDescription());
        });
        /*
            LiveData observer for boolean. Stays true while sending request and waiting for server response
            If true a progressbar is shown.
         */
        mViewModel.getIsUserUpdating().observe(getViewLifecycleOwner(), aBoolean ->  {
            if(aBoolean)
                mProgressBar.setVisibility(View.VISIBLE);
            else{mProgressBar.setVisibility(View.INVISIBLE);}
        });

        /*
            LiveData observer for ResponseEvent.
            If ResponseEvent type is USER_UPDATE then a Snackbar pops up containing an information if request was successful.
         */
        mViewModel.getNetworkResponse().observe(getViewLifecycleOwner(), responseEvent -> {
            if(responseEvent.getType() == ResponseEvent.Type.USER_UPDATE){
                String response = responseEvent.getContentIfNotHandled();
                if(response != null && response.equals("Created")){
                    mViewModel.resetLiveData();
                    Snackbar.make(view, "Updated", Snackbar.LENGTH_LONG).show();
                }

                if(response != null && response.equals("Forbidden"))
                    Snackbar.make(view, "Your changes could not be saved ", Snackbar.LENGTH_LONG).show(); // error textview
            }
        });
        /*
            LiveData observer for ArrayList<Integer>
            If user change his interests, then mInterestsTV shows new values.
         */
        mViewModel.getUserProfile().getValue().getUserInterests().observe(getViewLifecycleOwner(), integers -> {
            interests="";
            for(int i=0; i<integers.size();i++)
                interests += DataRepository_Interest.interests[integers.get(i)] + "\n";
            mInterestsTV.setText(interests.toLowerCase());
        });

        /*
            LiveData observer for SimpleUser
            If user changes any information, the save-button appears.
         */
        mViewModel.getEditedProfile().observe(getViewLifecycleOwner(), editedProfile -> {
            if(mViewModel.isUserEdited())
                saveBtn.setVisibility(View.VISIBLE);
            else saveBtn.setVisibility(View.GONE);
        });

        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int pos = position+1;
                if(mViewModel.getUserProfile().getValue().getGender() != null && mViewModel.getUserProfile().getValue().getGender().getI() != pos){
                    UserSimple user = mViewModel.getEditedProfile().getValue();
                    user.setGender(pos);
                    mViewModel.setEditedProfile(user);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        mOnDataSetListener = (view1, year, month, dayOfMonth) -> {
            String day = String.valueOf(dayOfMonth);
            String newMonth = String.valueOf(month +1);
            if(dayOfMonth < 10)
                day = "0"+day;
            if(month+1 < 10)
                newMonth = "0"+newMonth;
            Log.e("date", year + " " + day + " " +newMonth);
            String bDay = day + "." + newMonth  + "." + year;
            mBirthdayTextView.setText(bDay);

            UserSimple user1 = mViewModel.getEditedProfile().getValue();
            user1.setBirthday(LocalDate.parse(mBirthdayTextView.getText().toString(), formatter));
            mViewModel.setEditedProfile(user1);
        };

        mDescriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String desc = s.toString();
                UserSimple user = mViewModel.getEditedProfile().getValue();
                user.setDescription(desc);
                mViewModel.setEditedProfile(user);
            }
        });

        /*
            if clicked, a DatePickerDialog pops up to allow user change his birthday.
         */
        mBirthdayBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault()));//"UTC"
            if(mBirthdayTextView.getText().toString().isEmpty()){
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
            }else {
                year = mViewModel.getUserProfile().getValue().getBirthday().getYear();
                month = mViewModel.getUserProfile().getValue().getBirthday().getMonthValue();
                day = mViewModel.getUserProfile().getValue().getBirthday().getDayOfMonth();
            }
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getActivity(),
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    mOnDataSetListener, year, month-1, day);
            datePickerDialog.getDatePicker().setMinDate(1904);
            datePickerDialog.getDatePicker().setMaxDate(calendar.getTime().getTime());
            datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            datePickerDialog.show();
        });
        saveBtn.setOnClickListener(v -> handleInput());
        saveBtn.setVisibility(View.GONE);


        interestBtn.setOnClickListener(v -> {
            interestFragment = new Interest_UpdateUser_Fragment(mViewModel);
            interestFragment.show(getActivity().getSupportFragmentManager(), INTEREST_FRAGMENT);
        });

    }


    @Override
    public void onStart() {
        super.onStart();
      }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("Info", "onAct");
    }

    /**
     * Uses ViewModel to send new user information to the server.
     * It will be checked if the username is empty. If not, all values from the edited SimpleUser are taken over.
     * @return to if user is edited, but username is empty
     */
    private void handleInput() {

        UserSimple editedUser = mViewModel.getEditedProfile().getValue();
        if(editedUser.getUserName().isEmpty()){
            Snackbar.make(view, "Username cannot be empty",Snackbar.LENGTH_SHORT).show();
            return;
        }
        int newGender = editedUser.getGender().getI();//mGenderSpinner.getSelectedItemPosition() + 1;
        String newLocation= editedUser.getLocation();
        String newUsername = editedUser.getUserName();
        String newDescription = editedUser.getDescription();//mDescriptionEditText.getText().toString();
        LocalDate newDate = LocalDate.parse(mBirthdayTextView.getText().toString(), formatter);

        UserProfile updatedUser = new UserProfile();
        updatedUser.setUserID(mViewModel.getUserProfile().getValue().getUserID());
        updatedUser.setUserName(newUsername);
        updatedUser.setGender(newGender);
        updatedUser.setLocation(newLocation);
        updatedUser.setBirthday(newDate);
        updatedUser.setDescription(newDescription);

        mViewModel.updateUserInfo(updatedUser);
    }

    /*
        sets the position/gender of GenderSpinner based on the given UserProfile.
     */
    private void setCurrentGenderSpinner(UserProfile user){
        if(user.getGender() != null){
            switch (user.getGender()){
                case MALE: mGenderSpinner.setSelection(0);
                    break;
                case FEMALE: mGenderSpinner.setSelection(1);
                    break;
                case DIVERS: mGenderSpinner.setSelection(2);
                    break;

            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("INFO", "Destroying view for info profile ...");
    }


}
