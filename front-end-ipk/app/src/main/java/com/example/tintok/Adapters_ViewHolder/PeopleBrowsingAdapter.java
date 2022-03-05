package com.example.tintok.Adapters_ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.tintok.Model.UserSimple;
import com.example.tintok.R;

import java.util.ArrayList;


public class PeopleBrowsingAdapter extends BaseAdapter<UserSimple, PeopleBrowsingViewHolder> {
    public PeopleBrowsingAdapter(Context context, ArrayList<UserSimple> models) {
        super(context, models);

    }

    @NonNull
    @Override
    public PeopleBrowsingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mainpages__peoplebrowsing, parent, false);
        return new PeopleBrowsingViewHolder(view, this);
    }

}
