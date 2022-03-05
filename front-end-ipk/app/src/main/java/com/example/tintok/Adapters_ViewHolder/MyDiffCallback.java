package com.example.tintok.Adapters_ViewHolder;

import android.util.Log;

import androidx.recyclerview.widget.DiffUtil;

import java.util.ArrayList;

public class MyDiffCallback <T> extends DiffUtil.Callback {
    ArrayList<T> oldItem, newItem;
    public MyDiffCallback(ArrayList<T> oldItem, ArrayList<T> newItem){
        this.oldItem = oldItem;
        this.newItem = newItem;
    }
    @Override
    public int getOldListSize() {
        return this.oldItem.size();
    }

    @Override
    public int getNewListSize() {
        return this.newItem.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {

        return oldItem.get(oldItemPosition) ==  newItem.get(newItemPosition);

    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldItem.get(oldItemPosition) ==  newItem.get(newItemPosition);
    }

}
