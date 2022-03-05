package com.example.tintok;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import com.example.tintok.Communication.Communication;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Activity instantiate communication class and allows the user to either login, register or reset its password.
 * The default Fragment is Login_Fragment.
 * @see Login_Fragment
 * @see Sign_up_Fragment
 * @see Password_Reset_Fragment
 */
public class Activity_Login_Signup extends AppCompatActivity {
    public final String ID = "Login" ;
    private Login_SignUp_ViewModel viewModel;
    MaterialToolbar toolbar;

    public Activity_Login_Signup() {
    }

    /**
     * set toolbar, instantiate {@link Communication}, default page is Login-Fragment
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Activity_Login_Signup", "onCreate");
        setContentView(R.layout.activity_login_signup);
        toolbar =  findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        Communication.getInstance();
        this.viewModel = new ViewModelProvider(this).get(Login_SignUp_ViewModel.class);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, Login_Fragment.newInstance(viewModel)).commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.e("onBackPressed", "true");
        onBackPressed();
        return false;
    }
    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.e("onBackPressed", "YES");
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

     */


    public boolean isOnline() {
        boolean var = false;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if ( cm.getActiveNetworkInfo() != null ) {
            var = true;
        }
        return var;
    }

}