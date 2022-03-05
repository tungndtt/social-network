package com.example.tintok;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tintok.CustomView.DatePickerFragment;
import com.example.tintok.DataLayer.DataRepository_Interest;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * This fragment is used to register the user for the application.
 * The user must specify its name, email address, birthday, interests and password.
 * Only if all parameter are set by the user then the user can sign in and will receive an email for validation.
 */
public class Sign_up_Fragment extends Fragment implements Login_SignUp_ViewModel.requestListener, DialogInterface.OnDismissListener, DatePickerDialog.OnDateSetListener {

    public static final String INTERESTS_SIGN_UP = "interests_sign_up";
    public static final String DATE_PICKER = "Date Picker";
    private Button registerButton;
    private ProgressBar loadingBar;
    private TextView status, mInterestTV, day,month,year;
    private EditText name, email,password, retypepassword ;
    private Login_SignUp_ViewModel viewModel;
    private RadioGroup mGenderGroup;
    private String selectedInterest;
    private DialogFragment interestFragment, datePicker;
    private View view;

    public Sign_up_Fragment(){
    }

    public static Sign_up_Fragment newInstance(Login_SignUp_ViewModel viewModel) {
        return new Sign_up_Fragment(viewModel);
    }

    /**
     * creates a Sign_up_Fragment with given viewmodel
     * @param viewModel of Activity_Login_Signup
     */
    public Sign_up_Fragment(Login_SignUp_ViewModel viewModel){
        this.viewModel = viewModel;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Sign-Up", "onCreate");
    }

    /**
     * Inflates the layout of this fragment.
     * Initialization of views.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View of inflated layout with all views
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.e("Sign-UP", "onCreateView");
        view = inflater.inflate(R.layout.sign_up_fragment, container, false);
        registerButton =view.findViewById(R.id.sign_upButton);
        status = view.findViewById(R.id.status);
        loadingBar = view.findViewById(R.id.sign_up_progressBar);
        name = view.findViewById(R.id.nameInput);
        email = view.findViewById(R.id.emailInput);
        password = view.findViewById(R.id.passInput);
        retypepassword = view.findViewById(R.id.passInputConfirm);
        day = view.findViewById(R.id.dayofbirth_date);
        month = view.findViewById(R.id.dayofbirth_month);
        year = view.findViewById(R.id.dayofbirth_year);
        mGenderGroup = view.findViewById(R.id.register_gender_group);
        mInterestTV = view.findViewById(R.id.register_interests_inputTV);

        return view;
    }


    /**
     * Instantiation of Login_SignUp_ViewModel if it is null and of ArrayList<Interest>
     * Sets empty ArrayList for chosen users interests
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(viewModel == null){
            viewModel = new ViewModelProvider(this).get(Login_SignUp_ViewModel.class);
        }
        if(DataRepository_Interest.getInterestArrayList().size() == 0)
            DataRepository_Interest.initInterestArrayList();
        viewModel.setChosenInterests(new ArrayList<>());
    }


    /**
     * uses the Login_SignUp_ViewModel to observe the LiveData of chosen interests which are selected in Interest_SignUp_Fragment by the user
     * sets onClickListener for handling the given user input and for open Interests_SignUp_Fragment
     * @see Interests_SignUp_Fragment
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onStart() {
        super.onStart();
        this.onDismiss(null);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Registration");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mInterestTV.setText(selectedInterest);
        viewModel.getChosenInterests().observe(getViewLifecycleOwner(), integers -> {
            selectedInterest= "";
            for(int i=0; i<integers.size();i++)
                selectedInterest += DataRepository_Interest.interests[integers.get(i)] + " ";
            if(selectedInterest.isEmpty())
                selectedInterest = getResources().getString(R.string.interests_clickToChose);
            mInterestTV.setText(selectedInterest.toLowerCase());
            status.setVisibility(View.INVISIBLE);
        });

        registerButton.setOnClickListener(v -> HandleSignUp());

        mInterestTV.setOnClickListener(v -> {
            Log.e("Sign_up_Frag", "Interest click "+datePicker + interestFragment);
            if(datePicker == null && interestFragment == null) {
                interestFragment = Interests_SignUp_Fragment.newInstance(Sign_up_Fragment.this);
                interestFragment.show(getChildFragmentManager(), INTERESTS_SIGN_UP);
            }
        });



        onDateOfBirthClick datePickerClick = new onDateOfBirthClick();
        day.setOnClickListener(datePickerClick);
        month.setOnClickListener(datePickerClick);
        year.setOnClickListener(datePickerClick);

    }

    /**
     * Handles user inputs.
     * Checks if username and birth dates are not empty, if the email address is valid, if  passwords are identical and at least 6 characters long, if a gender and at least one interest are chosen.
     * If not, a corresponding error shows up.
     * If all conditions pass a sign up request is send to server using the viewmodel.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void HandleSignUp(){
        email.onEditorAction(EditorInfo.IME_ACTION_DONE);
        if(name.getText().toString().isEmpty()){
            setErrorStatus(R.string.error_username_empty);
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email.getText()).matches()){
            setErrorStatus(R.string.error_email_invalid);
            return;
        }
        if(password.getText().toString().length() < 6){
            setErrorStatus(R.string.error_password_length);
            return;
        }

        int dayInt, monthInt, yearInt;
        if(day.getText().toString().isEmpty() || month.getText().toString().isEmpty()||year.getText().toString().isEmpty()){
            setErrorStatus(R.string.error_birthday_invalid);
            return;
        }
        try {
            dayInt = Integer.parseInt(day.getText().toString());
            monthInt = Integer.parseInt(month.getText().toString());
            yearInt = Integer.parseInt(year.getText().toString());
        }catch (Exception e){
            setErrorStatus(R.string.error_birthday_invalid);
            return;
        }

        if(year.getText().toString().length()!=4 || (dayInt <=0 || dayInt >31)
            || (monthInt<=0 || monthInt > 12) || yearInt >= LocalDateTime.now().getYear()){
            setErrorStatus(R.string.error_birthday_invalid);
            return;
        }

        if(retypepassword.getText().toString().compareTo(password.getText().toString()) != 0){
           setErrorStatus(R.string.error_password_notMatching);
            return;
        }

        if(mGenderGroup.getCheckedRadioButtonId() == -1){
            setErrorStatus(R.string.error_gender_empty);
            return;
        }
        if(selectedInterest.equals(getResources().getString(R.string.interests_clickToChose))){
            setErrorStatus(R.string.error_interest_empty);
            return;
        }
        status.setVisibility(View.VISIBLE);
        status.setText(R.string.registration_signUp);
        status.setTextColor(Color.BLACK);
        loadingBar.setVisibility(View.VISIBLE);
        String birthday = dayInt+"/"+monthInt+"/"+yearInt;
        int gender = mGenderGroup.indexOfChild(getView().findViewById(mGenderGroup.getCheckedRadioButtonId())) + 1;
        ArrayList<Integer> chosenInterests = viewModel.getChosenInterests().getValue();
        this.viewModel.signUpRequest(name.getText().toString(), email.getText().toString(), birthday, password.getText().toString(), gender, chosenInterests, this);
    }


    /**
     * Makes the status visible as an red text based on given error code.
     * @param error Error as an ID for a String. If a condition in HandleSignUp do not pass, a specific error code is given to this function.
     */
    private void setErrorStatus(int error){
        status.setVisibility(View.VISIBLE);
        status.setText(error);
        status.setTextColor(Color.RED);
    }


    /**
     * shows a Snackbar that a verification mail is send and let the user go back to Login_Fragment
     */
    @Override
    public void requestSuccess() {
        loadingBar.setVisibility(View.INVISIBLE);
        Snackbar.make(getView(), R.string.registration_email_sent, Snackbar.LENGTH_LONG).show();
        getActivity().onBackPressed();
    }

    /**
     * shows an red error text that the registration failed
     */
    @Override
    public void requestFail(String reason) {
        status.setText(reason);
        loadingBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void connectionFail() {
        status.setVisibility(View.VISIBLE);
        status.setText(R.string.error_connection_failed);
        loadingBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.e("Sign_up_Frag", "on Dialog Fragment dismiss");
        this.interestFragment = null;
        this.datePicker = null;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.day.setText(String.valueOf(dayOfMonth));
        this.month.setText(String.valueOf(month+1));
        this.year.setText(String.valueOf(year));
        onDismiss(null);
    }

    public class onDateOfBirthClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Log.e("Sign_up_Frag", "Date click "+datePicker + interestFragment);
            if(datePicker == null && interestFragment == null){
                datePicker = new DatePickerFragment(Sign_up_Fragment.this);
                datePicker.show(getChildFragmentManager(), DATE_PICKER);
            }
        }
    }
}