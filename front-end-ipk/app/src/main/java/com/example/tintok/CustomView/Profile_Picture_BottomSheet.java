package com.example.tintok.CustomView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tintok.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class Profile_Picture_BottomSheet extends BottomSheetDialogFragment {

    private View view;
    private TextView viewPhoto, selectPhoto, addPhoto;
    OnTextViewClickListener mListener;

    public Profile_Picture_BottomSheet() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.profile_picture_bottom_sheet, container, false);
        viewPhoto = view.findViewById(R.id.bottom_sheet_view_photo);
        selectPhoto = view.findViewById(R.id.bottom_sheet_select_photo);
        addPhoto = view.findViewById(R.id.bottom_sheet_add_photo);
        viewPhoto.setOnClickListener(v -> {
            if(mListener != null)
                mListener.onTextViewClicked(0);
        });
        selectPhoto.setOnClickListener(v -> {
            Log.e("select", "clicked");
            if(mListener == null)
                Log.e("select", "isnull");
            if(mListener != null)
                mListener.onTextViewClicked(1);
        });
        addPhoto.setOnClickListener(v -> {
            if(mListener != null)
                mListener.onTextViewClicked(2);
        });
        return view;
    }

    public interface OnTextViewClickListener{
        void onTextViewClicked(int position);
    }
    public void setOnTextViewClickListener(OnTextViewClickListener mListener){
        this.mListener = mListener;
    }



}

