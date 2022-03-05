package com.example.tintok.Adapters_ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.tintok.DataLayer.DataRepository_Interest;
import com.example.tintok.R;

import java.util.ArrayList;

public class InterestTagAdapter extends BaseAdapter<DataRepository_Interest.InterestTag, InterestTagAdapter.ViewHolder> {
    public InterestTagAdapter(Context context, ArrayList<DataRepository_Interest.InterestTag> models) {
        super(context, models);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_interesttag,parent,false);
        return new ViewHolder(view, this);
    }

    public void setOnTagClickedListener(onTagClickedListener mListener) {
        this.mListener = mListener;
    }

    onTagClickedListener mListener;

    public interface onTagClickedListener{
        public void onTagClicked(int position);
    }
    public class ViewHolder extends BaseViewHolder<DataRepository_Interest.InterestTag>  {
        TextView tag ;
        public ViewHolder(@NonNull View itemView, BaseAdapter mAdapter) {
            super(itemView, mAdapter);
            tag = itemView.findViewById(R.id.tag);
        }

        @Override
        public void bindData(DataRepository_Interest.InterestTag itemData) {
            tag.setText(itemData.tag);
            tag.setOnClickListener(v -> {
                if(mListener != null)
                    mListener.onTagClicked(getAdapterPosition());
                updateColor(itemData);
            });
            updateColor(itemData);
        }

        public void updateColor(DataRepository_Interest.InterestTag itemData){
            if(itemData.isChecked)
                tag.setBackgroundResource(R.drawable.interesttag_selected);
            else
                tag.setBackgroundResource(R.drawable.interesttag_unselected);
        }
    }
}
