package com.example.tintok;

import androidx.annotation.RequiresApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This class is mainly used to either securely login the user or forward the user to a registration page.
 * User can login if he is already registers. Therefore, user must enter email and password.
 * If request is successful, then he will be logged in, else an error is shown.
 * Also, user can click on password forgot, on register now or on privacy policy to get redirect to the corresponding page.
 */
public class Login_Fragment extends Fragment implements Login_SignUp_ViewModel.requestListener {

    public Login_Fragment(){

    }
    public static Login_Fragment newInstance(Login_SignUp_ViewModel viewModel) {
        return new Login_Fragment(viewModel);
    }

    public Login_Fragment(Login_SignUp_ViewModel viewModel){
        this.viewModel = viewModel;
    }

    private Button loginButton, registerButton;
    private ProgressBar loadingBar;
    private TextView status;
    private EditText email,password;
    private TextView forget, privacy;
    private Login_SignUp_ViewModel viewModel;
    private Privacy_Fragment privacyFragment;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onStart() {
        super.onStart();
        init();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(HtmlCompat.fromHtml("<font color=\"black\"><b>"+getString(R.string.app_name) + "</b></font>",HtmlCompat.FROM_HTML_MODE_LEGACY));
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    }

    /**
     * Initialization of views.
     * Setting up onClickListener for login, registration, password forgot and privacy policy.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    void init(){
        loginButton = getView().findViewById(R.id.sign_inButton);
        registerButton = getView().findViewById(R.id.sign_upButton);
        status = getView().findViewById(R.id.status);
        loadingBar = getView().findViewById(R.id.login_progressBar);
        email = getView().findViewById(R.id.emailInput);
        password = getView().findViewById(R.id.passInput);
        forget = getView().findViewById(R.id.forget_account_text);
        privacy = getView().findViewById(R.id.login_privacy_policy);

        loginButton.setOnClickListener(v -> HandleLogin());
        registerButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().replace(R.id.fragment, Sign_up_Fragment.newInstance(viewModel)).addToBackStack("Login").commit());
        forget.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction().replace(R.id.fragment, Password_Reset_Fragment.newInstance(viewModel)).addToBackStack("Login").commit();
        });
        privacy.setOnClickListener(v -> {
            if(privacyFragment == null)
                privacyFragment = Privacy_Fragment.newInstance();
            privacyFragment.show(getActivity().getSupportFragmentManager(), "privacy_fragment");
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Checks if email is valid and password has at least 6 characters.
     * If so, email and password are passed to ViewModel to send a login request to server.
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void HandleLogin(){
        email.onEditorAction(EditorInfo.IME_ACTION_DONE);
        if(!Patterns.EMAIL_ADDRESS.matcher(email.getText()).matches() || password.getText().toString().length()<=5){
            status.setVisibility(View.VISIBLE);
            status.setText("Invalid Email or Password");
            return;
        }
        status.setVisibility(View.VISIBLE);
        status.setText("Signing in...");
        loadingBar.setVisibility(View.VISIBLE);

        viewModel.loginRequest(email.getText().toString(), password.getText().toString(), this);
    }


    /**
     * User is authenticated and will be logged in and activity loads user specific content
     * @see Activity_InitData
     */
    @Override
    public void requestSuccess() {
        loadingBar.setVisibility(View.INVISIBLE);
        Intent intent = new Intent(getActivity(), Activity_InitData.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.animation_in, R.anim.animation_out);
        getActivity().finish();
    }

    /**
     * request failed due to reason, e.g no valid email, incorrect password
     */
    @Override
    public void requestFail(String reason) {
        status.setVisibility(View.VISIBLE);
        status.setText("Signing in failed: "+ reason);
        loadingBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void connectionFail() {
        loadingBar.setVisibility(View.INVISIBLE);
        status.setVisibility(View.VISIBLE);
        status.setText(R.string.error_connection_failed);
    }
}