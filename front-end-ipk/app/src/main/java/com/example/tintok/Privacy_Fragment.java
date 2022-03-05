package com.example.tintok;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tintok.CustomView.MyDialogFragment;
import com.google.android.material.appbar.MaterialToolbar;

/**
 *  Privacy_Fragment shows privacy policy of this application.
 *  Loads the privacy policy from string value.
 */
public class Privacy_Fragment extends MyDialogFragment {

    private MaterialToolbar toolbar;
    private TextView mPrivacyPolicy;
    private View view;

    public Privacy_Fragment() {

    }

    public static Privacy_Fragment newInstance() {
        Privacy_Fragment fragment = new Privacy_Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Inflates the layout of this fragment.
     * Initialization of views.
     * loads privacy policy out of string.xml as html-format
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View of inflated layout with all views
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_privacy, container, false);
        toolbar = view.findViewById(R.id.privacy_toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            dismiss();
        });
        mPrivacyPolicy = view.findViewById(R.id.privacy_text);
        mPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
        mPrivacyPolicy.setText(Html.fromHtml(getString(R.string.privacy_policy), Html.FROM_HTML_MODE_LEGACY));

        return view;
    }
}