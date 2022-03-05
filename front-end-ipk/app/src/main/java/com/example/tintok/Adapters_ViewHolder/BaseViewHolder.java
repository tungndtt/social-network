package com.example.tintok.Adapters_ViewHolder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {
    BaseAdapter mAdapter;
    public BaseViewHolder(@NonNull View itemView, BaseAdapter mAdapter) {
        super(itemView);
        this.mAdapter = mAdapter;
    }

    public abstract void bindData(T itemData);
}
