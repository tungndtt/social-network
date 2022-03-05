package com.example.tintok;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.tintok.Adapters_ViewHolder.InterestAdapter;
import com.example.tintok.CustomView.MyDialogFragment;
import com.example.tintok.DataLayer.DataRepository_Interest;
import com.example.tintok.DataLayer.ResponseEvent;
import com.example.tintok.Model.Interest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;

/**
 * This class allows an user to change its interests.
 * Interests are saved separately and not as basic user information.
 * User has to choose at least one interest.
 * Nearly the same behaviour as
 * @see Interests_SignUp_Fragment
 */
public class Interest_UpdateUser_Fragment extends MyDialogFragment {

    private View view;
    private RecyclerView recyclerView;
    private MaterialButton saveBtn;
    private MainPages_MyProfile_ViewModel mViewModel;
    private InterestAdapter interestAdapter;
    private TextView errorTV;
    private ProgressBar mProgressBar;
    private ArrayList<Integer> result;
    private MaterialToolbar toolbar;

    public Interest_UpdateUser_Fragment(MainPages_MyProfile_ViewModel mViewModel) {
        this.mViewModel = mViewModel;
    }

    public static Interest_UpdateUser_Fragment newInstance(MainPages_MyProfile_ViewModel mViewModel) {
        Interest_UpdateUser_Fragment fragment = new Interest_UpdateUser_Fragment(mViewModel);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_interest, container, false);
        recyclerView = view.findViewById(R.id.interest_RV);
        saveBtn = view.findViewById(R.id.interest_saveBtn);
        errorTV = view.findViewById(R.id.interest_error);
        mProgressBar = view.findViewById(R.id.interest_progressBar);
        toolbar = view.findViewById(R.id.interest_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_backspace);
        toolbar.setTitle(getResources().getString(R.string.interests));
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mViewModel == null)
            mViewModel = new ViewModelProvider(this).get(MainPages_MyProfile_ViewModel.class);
        /*
            LiveData observer for boolean. Stays true while sending request and waiting for server response
            If true a progressbar is shown.
         */
        mViewModel.getIsUserUpdating().observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean)
                mProgressBar.setVisibility(View.VISIBLE);
            else mProgressBar.setVisibility(View.INVISIBLE);
        });
         /*
            LiveData observer for ResponseEvent.
            If ResponseEvent type is INTEREST_UPDATE then the dialog gets either dismissed and a Snackbar with Updated pops up or an error message appears.
         */
        mViewModel.getNetworkResponse().observe(getViewLifecycleOwner(), responseEvent -> {

            if(responseEvent.getType() == ResponseEvent.Type.INTEREST_UPDATE){
                String message = responseEvent.getContentIfNotHandled();
                if(message == null)
                    return;
                if(message.equals("Created")){
                    Snackbar.make(getActivity().getSupportFragmentManager().findFragmentByTag("MyProfile").getView(), "Updated", Snackbar.LENGTH_LONG).show();
                    getDialog().dismiss();
                }
                if(message.equals("forbidden"));
                    errorTV.setText("You can only change your interests every 30 minutes.");
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*
            initialize interestAdapter with current chosen interests.
            selected item changes its background
         */
        DataRepository_Interest.setUserInterest(mViewModel.getUserProfile().getValue().getUserInterests().getValue());
        interestAdapter = new InterestAdapter(getContext(), DataRepository_Interest.getInterestArrayList());
        interestAdapter.setOnInterestClickListener(position -> {
            if(!saveBtn.isEnabled()){
                saveBtn.setEnabled(true);
            }
            interestAdapter.getItems().get(position).setSelected(!(interestAdapter.getItems().get(position).isSelected()));
        });

        recyclerView.setAdapter(interestAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        saveBtn.setOnClickListener(v -> {
            hasInterests();
        });

        saveBtn.setEnabled(false);

        toolbar.setNavigationOnClickListener(v -> {
            if(saveBtn.isEnabled()){
                MaterialAlertDialogBuilder alertDialog =  new MaterialAlertDialogBuilder(getActivity());
                alertDialog.setTitle("Warning")
                        .setMessage("Your current changes will be lost. Do you want to save?")
                        .setPositiveButton("Save", (dialog, which) -> {
                            hasInterests();
                                })

                        .setNegativeButton("Don't save", (dialog, which) -> {
                            getDialog().dismiss();})
                        .show();
            }else getDialog().dismiss();
        });
    }

    /**
     * @see Interests_SignUp_Fragment#hasInterests()
     * uses {@link MainPages_MyProfile_ViewModel} instead for updating user interests
     */
    public void hasInterests() {
        result = new ArrayList<>();
        boolean isEmpty = true;
        ArrayList<Interest> newChosenInterests = interestAdapter.getItems();
        for (Interest interest : newChosenInterests) {
            if (interest.isSelected()) {
                result.add(interest.getId());
                isEmpty = false;
            }
        }
        if (isEmpty) {
            errorTV.setText(getResources().getString(R.string.interests_pleaseChose));
            errorTV.setVisibility(View.VISIBLE);
            return;
        } else {
            mViewModel.updateUserInterests(result);
        }
    }


}