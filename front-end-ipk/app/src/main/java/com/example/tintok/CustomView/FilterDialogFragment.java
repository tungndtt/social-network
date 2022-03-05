package com.example.tintok.CustomView;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tintok.Adapters_ViewHolder.InterestTagAdapter;
import com.example.tintok.DataLayer.DataRepository_Interest;
import com.example.tintok.DataLayer.DataRepository_MatchingPeople;
import com.example.tintok.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class FilterDialogFragment extends MyDialogFragment {
    public FilterDialogFragment(FilterState filterState, onFilterApplyListener mListener){
        this.currentFilterState = filterState;
        this.mListener = mListener;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.peoplebrowsing_filter, container, false);
    }


    onFilterApplyListener mListener;
    MaterialCheckBox nameCheckBox;
    TextInputEditText name;
    RadioGroup genderGroup;
    RangeSlider ageSlider;
    RecyclerView interestTag;
    FilterState currentFilterState;
    MaterialButton cancelBtn, applyBtn, resetBtn;
    private MaterialToolbar toolbar;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nameCheckBox = view.findViewById(R.id.checkbox_byname);
        name = view.findViewById(R.id.filterByName);
        genderGroup = view.findViewById(R.id.gender_group);
        ageSlider = view.findViewById(R.id.rangeSlider);
        interestTag = view.findViewById(R.id.interestTag);
        applyBtn = view.findViewById(R.id.applyFilterBtn);
        toolbar = view.findViewById(R.id.filter_toolbar);
        toolbar.setTitle("Matching-Filter");
       // cancelBtn = view.findViewById(R.id.cancelFilterBtn);
        resetBtn = view.findViewById(R.id.resetFilterBtn);
        Log.e("filter","onCreateView");
        initComponents();

        resetBtn.setOnClickListener(v -> {
            resetFilter();
        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("filter","onActivView");
    }

    /*
    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        window.setGravity(Gravity.FILL);
        window.setWindowAnimations(R.style.MyAnimation_Window);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
    }

     */

    public void resetFilter(){
        currentFilterState = new FilterState();
        initComponents();
    }

    public void initComponents(){
        if(!currentFilterState.getFilterName().isEmpty()){
            nameCheckBox.setChecked(true);
            name.setText(currentFilterState.getFilterName());
        }
        else{
            nameCheckBox.setChecked(false);
        }




        ageSlider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int)(value+0.5));
            }
        });

        ageSlider.setValues((float)currentFilterState.getMinAge(), (float)currentFilterState.getMaxAge());

        switch (currentFilterState.getGender()){
            case MALE:
                genderGroup.check(R.id.option_male);
                break;
            case FEMALE:
                genderGroup.check(R.id.option_female);
                break;
            default:
                genderGroup.check(R.id.option_both);
                break;
        }

        InterestTagAdapter mAdapter = new InterestTagAdapter(this.getContext(), DataRepository_Interest.parseData(currentFilterState.getInterestBitmap()));
        mAdapter.setOnTagClickedListener(position -> {
            mAdapter.getItems().get(position).isChecked = !mAdapter.getItems().get(position).isChecked;
            currentFilterState.interestBitmap[position] = !currentFilterState.interestBitmap[position];
        });
        interestTag.setAdapter(mAdapter);
        GridLayoutManager manager = new GridLayoutManager(this.getContext(), 3);
        interestTag.setLayoutManager(manager);
        interestTag.setNestedScrollingEnabled(false);

        applyBtn.setOnClickListener(v -> {
            if(nameCheckBox.isChecked())
               currentFilterState.name = name.getText().toString();
            else
                currentFilterState.name = "";

            switch (genderGroup.getCheckedRadioButtonId()){
                case R.id.option_male:
                    currentFilterState.gender = DataRepository_MatchingPeople.Filter.Gender.MALE;
                    break;
                case R.id.option_female:
                    currentFilterState.gender = DataRepository_MatchingPeople.Filter.Gender.FEMALE;
                    break;
                case R.id.option_divers:
                    currentFilterState.gender = DataRepository_MatchingPeople.Filter.Gender.DIVERS;
                    break;
                default:
                    currentFilterState.gender = DataRepository_MatchingPeople.Filter.Gender.ALL;
                    break;
            }

            List<Float> values = ageSlider.getValues();
            currentFilterState.minAge = (int)(values.get(0).floatValue()+0.5);
            currentFilterState.maxAge = (int)(values.get(1).floatValue()+0.5);

            mListener.onFilterApplyListener(currentFilterState);
            getDialog().dismiss();
        });
        toolbar.setNavigationOnClickListener(v -> {
            Log.e("toolbar", "called");
            mListener.onFilterApplyListener(null);
            getDialog().dismiss();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(android.content.DialogInterface dialog,
                                 int keyCode,android.view.KeyEvent event)
            {
                if ((keyCode ==  android.view.KeyEvent.KEYCODE_BACK))
                {
                    // To dismiss the fragment when the back-button is pressed.
                    dismiss();
                    if(mListener != null)
                        mListener.onFilterApplyListener(null);
                    return true;
                }
                // Otherwise, do nothing else
                else return false;
            }
        });
    }
    public static class FilterState implements DataRepository_MatchingPeople.Filter{
        public String name;
        public int minAge, maxAge;
        public Gender gender;
        public boolean[] interestBitmap;

        public FilterState() {
            name = "";
            maxAge = 150;
            minAge = 0;
            gender = Gender.ALL;
            interestBitmap = new boolean[DataRepository_Interest.interests.length];
        }

        @Override
        public String getFilterName() {
            return name;
        }

        @Override
        public int getMinAge() {
            return minAge;
        }

        @Override
        public int getMaxAge() {
            return maxAge;
        }

        @Override
        public Gender getGender() {
            return gender;
        }

        @Override
        public boolean[] getInterestBitmap() {
            return interestBitmap;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            try{
                FilterState ob = (FilterState) obj;
                if(! (name.compareTo(ob.name) == 0 && minAge == ob.minAge && maxAge == ob.maxAge
                        && gender == ob.gender) ) return false;

                for(int i = 0; i< ob.interestBitmap.length;i++){
                    if(interestBitmap[i] != ob.getInterestBitmap()[i])
                        return false;
                }
                return true;
            }catch (Exception e){
                return false;
            }

        }
    }

    public interface onFilterApplyListener{
        public void onFilterApplyListener(FilterState state);
    }
}
