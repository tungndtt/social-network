package com.example.tintok;

import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

/**
 *  This class lets the user to reset its password.
 *  User needs to input a valid email address and a new passwords with at least 6 characters and correctly retype the password.
 *  If all conditions pass, a forgot password request is send to server.
 *  If not an error indicates the wrong input.
 *  Only accessible from {@link Activity_Login_Signup}
 */
public class Password_Reset_Fragment extends Fragment implements Login_SignUp_ViewModel.requestListener {

    private View view;
    private EditText mCurrentPwEditText, mNewPwEditText, mRetypePWEditText;
    private MaterialButton mSaveBtn, mCancelBtn;
    private TextView mEmailError, mNewPwError, mRetypePwError;
    private ProgressBar mProgressBar;
    private Login_SignUp_ViewModel viewModel;


    public Password_Reset_Fragment(Login_SignUp_ViewModel viewModel){
        this.viewModel = viewModel;

    }

    public static Password_Reset_Fragment newInstance(Login_SignUp_ViewModel viewModel){
        Password_Reset_Fragment fragment = new Password_Reset_Fragment(viewModel);
        return fragment;
    }

    /**
     * Inflates the layout of this fragment.
     * Initialization of views.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View of inflated layout with all views
     */
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.reset_password_fragment, container, false);
        mCurrentPwEditText = view.findViewById(R.id.reset_pw_oldPW);
        mNewPwEditText = view.findViewById(R.id.reset_pw_newPW);
        mRetypePWEditText = view.findViewById(R.id.reset_pw_retypePW);
        mCancelBtn = view.findViewById(R.id.reset_pw_cancelBtn);
        mSaveBtn = view.findViewById(R.id.reset_pw_saveBtn);
        mProgressBar = view.findViewById(R.id.reset_pw_progressBar);
        mEmailError = view.findViewById(R.id.reset_pw_oldPWerror);
        mNewPwError = view.findViewById(R.id.reset_pw_newPWerror);
        mRetypePwError = view.findViewById(R.id.reset_pw_retypePWerror);
        return view;
    }


    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(viewModel == null)
            viewModel = new ViewModelProvider(this).get(Login_SignUp_ViewModel.class);
        mCurrentPwEditText.setHint(R.string.email);
        mCurrentPwEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mCurrentPwEditText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email_black_24, 0, 0, 0);


        /*
            if clicked: make all errors invisible and call handleInput to check if user input is correct.
         */
        mSaveBtn.setOnClickListener(v -> {
            mEmailError.setVisibility(View.INVISIBLE);
            mNewPwError.setVisibility(View.INVISIBLE);
            mRetypePwError.setVisibility(View.INVISIBLE);

            handleInput();
        });
        /*
            if clicked,  make all errors invisible and reset user input
         */
        mCancelBtn.setOnClickListener(v -> {

            mEmailError.setVisibility(View.INVISIBLE);
            mNewPwError.setVisibility(View.INVISIBLE);
            mRetypePwError.setVisibility(View.INVISIBLE);
            mCurrentPwEditText.setText("");
            mNewPwEditText.setText("");
            mRetypePWEditText.setText("");

        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Reset Password");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    /**
     * Check if email-EditText is not empty and contains a valid email address and if password is at least 6 characters long and correctly retyped.
     * If correct a password reset-request is send to the server. If not then an error indicates wrong user input.
     */
    private void handleInput() {

        String email = mCurrentPwEditText.getText().toString();
        String newPW = mNewPwEditText.getText().toString();
        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            mEmailError.setText(getResources().getString(R.string.error_email_invalid));
            mEmailError.setVisibility(View.VISIBLE);
            return;
        }
        if(newPW.length() < 6){
            mNewPwError.setText(getResources().getString(R.string.error_password_length));
            mNewPwError.setVisibility(View.VISIBLE);
            return;
        }
        if(!mRetypePWEditText.getText().toString().equals(newPW)){
            mRetypePwError.setText(getResources().getString(R.string.error_password_notMatching));
            mRetypePwError.setVisibility(View.VISIBLE);
            return;
        }

        viewModel.resetPassword(email, newPW, this);

    }


    @Override
    public void requestSuccess() {
        Snackbar.make(getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment).getView(), R.string.confirmation_email_sent, Snackbar.LENGTH_LONG).show();
        getParentFragmentManager().popBackStack();
    }

    @Override
    public void requestFail(String reason) {
        mEmailError.setText(reason);
        mEmailError.setVisibility(View.VISIBLE);
    }

    @Override
    public void connectionFail() {
        Snackbar.make(getView(), "Some errors occur in reset password!", Snackbar.LENGTH_LONG).show();
    }
}
