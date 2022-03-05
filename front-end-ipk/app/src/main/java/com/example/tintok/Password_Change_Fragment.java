package com.example.tintok;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.tintok.CustomView.MyDialogFragment;
import com.example.tintok.DataLayer.ResponseEvent;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;

/**
 *  This class lets the user to reset its password.
 *  User needs to input current password and a new passwords with at least 6 characters and correctly retype the password.
 *  Current user password is compared to the password stored in the database.
 *  If all conditions pass, a forgot password request is send to server to save the new password.
 *  If not an error indicates the wrong input.
 */
public class Password_Change_Fragment extends MyDialogFragment {

    private View view;
    private EditText mCurrentPwEditText, mNewPwEditText, mRetypePWEditText;
    private MaterialButton mSaveBtn, mCancelBtn, mBackBtn;
    private TextView mCurrentPwError, mNewPwError, mRetypePwError;
    private ProgressBar mProgressBar;
    MainPages_MyProfile_ViewModel mViewModel;
    MaterialToolbar toolbar;

    public Password_Change_Fragment(){

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
        mCurrentPwError = view.findViewById(R.id.reset_pw_oldPWerror);
        mNewPwError = view.findViewById(R.id.reset_pw_newPWerror);
        mRetypePwError = view.findViewById(R.id.reset_pw_retypePWerror);
        toolbar = view.findViewById(R.id.reset_pw_toolbar);
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setTitle(R.string.change_password);
        toolbar.setNavigationOnClickListener(v -> {
            dismiss();
        });
        return view;
    }


    /**
     *  Observes a LiveData containing a ResponseEvent.
     *  If ResponseEvent is of type PASSWORD, it is checked if the request was successfully or the current password is wrong.
     *  It shows a corresponding feedback to the user.
     *  @see ResponseEvent
     *
     *  Observes a LiveData containing a boolean. Stays true from sending a request to server until server response.
     *  Sets the visibility of a progressbar accordingly to boolean.
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(mViewModel == null)
            mViewModel = new ViewModelProvider(this).get(MainPages_MyProfile_ViewModel.class);

        mViewModel.getNetworkResponse().observe(this, responseEvent -> {  // = null or string
            if(responseEvent.getType() == ResponseEvent.Type.PASSWORD){
                String response = responseEvent.getContentIfNotHandled(); // null or String
                if(response != null && response.equals("Created")){
                    resetUserInput();
                    Snackbar.make(getView(), "Updated", Snackbar.LENGTH_LONG).show();
                }
                if(response != null && response.equals("Forbidden")){
                    mCurrentPwError.setVisibility(View.VISIBLE);
                }
            }
        });

        mViewModel.getIsUserUpdating().observe(this, aBoolean -> {
            if(aBoolean)
                mProgressBar.setVisibility(View.VISIBLE);
            else mProgressBar.setVisibility(View.INVISIBLE);
        });

        mSaveBtn.setOnClickListener(v -> {
            mCurrentPwError.setVisibility(View.INVISIBLE);
            mNewPwError.setVisibility(View.INVISIBLE);
            mRetypePwError.setVisibility(View.INVISIBLE);
            handleInput();
        });
        mCancelBtn.setOnClickListener(v -> resetUserInput());
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Check if current password is not empty  and if new password is at least 6 characters long and correctly retyped.
     * If correct a password reset-request is send to the server. If not then an error indicates wrong user input.
     */
    private void handleInput() {
        String currentPW = mCurrentPwEditText.getText().toString();
        String newPW = mNewPwEditText.getText().toString();
        if(currentPW.isEmpty()){
            mCurrentPwError.setText(getResources().getString(R.string.error_password_empty));
            mCurrentPwError.setVisibility(View.VISIBLE);
            return;
        }
        if(newPW.length() <= 5){
            mNewPwError.setText(getResources().getString(R.string.error_password_length));
            mNewPwError.setVisibility(View.VISIBLE);
            return;
        }
        if(mCurrentPwEditText.getText().toString().equals(newPW)){
            mNewPwError.setText(getResources().getString(R.string.error_password_newPwMustDiffer));
            mNewPwError.setVisibility(View.VISIBLE);
            return;
        }
        if(!mRetypePWEditText.getText().toString().equals(newPW)){
            mRetypePwError.setText(getResources().getString(R.string.error_password_notMatching));
            mRetypePwError.setVisibility(View.VISIBLE);
            return;
        }
        List<String> passwords = Arrays.asList(currentPW, newPW);
        mViewModel.changePassword(passwords);
    }

    /**
     * makes all errors invisible and resets user input
     */
    private void resetUserInput(){
        mCurrentPwError.setVisibility(View.INVISIBLE);
        mNewPwError.setVisibility(View.INVISIBLE);
        mRetypePwError.setVisibility(View.INVISIBLE);
        mCurrentPwEditText.setText("");
        mNewPwEditText.setText("");
        mRetypePWEditText.setText("");
    }


}
