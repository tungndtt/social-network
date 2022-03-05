package com.example.tintok;


import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tintok.Adapters_ViewHolder.InterestAdapter;
import com.example.tintok.CustomView.MyDialogFragment;
import com.example.tintok.DataLayer.DataRepository_Interest;
import com.example.tintok.Model.Interest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;

/**
 * The class is used to determine user's interest. User can choose between several given interests.
 * It is possible that the user do no choose any interests, but without any interests he cannot register.
 * Chosen interests are saved in LiveData and can be used on sign up page.
 * @see DataRepository_Interest
 */
public class Interests_SignUp_Fragment extends MyDialogFragment {

    private View view;
    private RecyclerView recyclerView;
    private MaterialButton saveBtn;
    private Login_SignUp_ViewModel mViewModel;
    private InterestAdapter interestAdapter;
    private TextView errorTV;
    private MaterialToolbar toolbar;
    private ArrayList<Integer> result;

    DialogInterface.OnDismissListener dismissListener;
    public Interests_SignUp_Fragment(DialogInterface.OnDismissListener dismissListener){
        this.dismissListener = dismissListener;
    }

    public static Interests_SignUp_Fragment newInstance( DialogInterface.OnDismissListener dismissListener) {
        Interests_SignUp_Fragment fragment = new Interests_SignUp_Fragment(dismissListener);
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
        toolbar = view.findViewById(R.id.interest_toolbar);
        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if(dismissListener != null)
            dismissListener.onDismiss(dialog);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mViewModel == null)
            mViewModel = new ViewModelProvider(getActivity()).get(Login_SignUp_ViewModel.class);

        /*
            initialize interestAdapter with current chosen interests.
            selected item changes its background
         */
        DataRepository_Interest.setUserInterest(mViewModel.getChosenInterests().getValue());
        interestAdapter = new InterestAdapter(this.getContext(), DataRepository_Interest.getInterestArrayList());
        interestAdapter.setOnInterestClickListener(position -> {
            if(!saveBtn.isEnabled()){
                saveBtn.setEnabled(true);
            }
            interestAdapter.getItems().get(position).setSelected(!(interestAdapter.getItems().get(position).isSelected()));
        });
        /*
            dismiss dialog, if user has picked a interest
         */
        saveBtn.setOnClickListener(v -> {
           if(hasInterests())
               getDialog().dismiss();
        });

        recyclerView.setAdapter(interestAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setNestedScrollingEnabled(false);

        /*
            ask user if he wants to save his changes.
            saves changes if user has picked at least one interest
            else discard changes
         */
        toolbar.setTitle("Interests");
        toolbar.setNavigationOnClickListener(v -> {
            if(saveBtn.isEnabled()){
                MaterialAlertDialogBuilder alertDialog =  new MaterialAlertDialogBuilder(getActivity());
                alertDialog.setTitle("Warning")
                        .setMessage("Your current changes will be lost. Do you want to save?")
                        .setPositiveButton("Save", (dialog, which) -> {
                            if(hasInterests())
                                getDialog().dismiss();})
                        .setNegativeButton("Don't save", (dialog, which) -> {
                                getDialog().dismiss();})
                        .show();
            }else getDialog().dismiss();
        });
        saveBtn.setEnabled(false);
    }

    /**
     * Checks if user has chosen at least one interest.
     * If so chosen interest are saved in the ViewModel. If not, an error appears.
     * @return true if user has chosen at least one interest. False if user has not chosen any interest.
     */
    public boolean hasInterests() {
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
            errorTV.setText(R.string.interests_pleaseChose);
            errorTV.setVisibility(View.VISIBLE);
            return false;
        } else {
            mViewModel.setChosenInterests(result);
            return true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

}
