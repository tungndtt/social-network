package com.example.tintok.CustomView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {

    DatePickerDialog.OnDateSetListener callbackListener;
    public DatePickerFragment(DatePickerDialog.OnDateSetListener mListener){
        this.callbackListener = mListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar c= Calendar.getInstance();
        int year = c.get(Calendar.YEAR),
                month = c.get(Calendar.MONTH),
                dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), callbackListener, year, month, dayOfMonth);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
        if (callbackListener instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) callbackListener).onDismiss(dialog);
        }
    }

}
