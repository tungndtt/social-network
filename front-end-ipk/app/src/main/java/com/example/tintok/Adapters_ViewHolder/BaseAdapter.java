package com.example.tintok.Adapters_ViewHolder;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.tintok.Model.Comment;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<T, VH extends BaseViewHolder<T>> extends RecyclerView.Adapter<VH> {
    Context context;
    ArrayList<T> items;


    public BaseAdapter(Context context, ArrayList<T> models){
        this.context = context;
        this.items = models;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {

        holder.bindData(items.get(position));


    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(T item) {
        items.add(item);
        notifyItemInserted(getItemCount() - 1);
    }
    public ArrayList<T> getItems() {
        return new ArrayList<>(items);
    }
    public void setItemAt(int position, T item){
        items.set(position, item);
        notifyItemChanged(position);
    }
    public T removeItem(int position) {
        T item = items.remove(position);
        notifyItemRemoved(position);
        return item;
    }

    public void setItems(ArrayList<T> items) {
        //notifyDataSetChanged();

        DiffUtil.DiffResult r =DiffUtil.calculateDiff(new MyDiffCallback<T>(this.items, items));
        this.items.clear();
        this.items.addAll(items);
        r.dispatchUpdatesTo(this);
        //this.items = items;
    }

    public Context getContext(){
        return this.context;
    }
}
