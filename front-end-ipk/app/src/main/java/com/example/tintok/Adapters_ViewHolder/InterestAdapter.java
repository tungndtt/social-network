package com.example.tintok.Adapters_ViewHolder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.tintok.Model.Interest;
import com.example.tintok.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class InterestAdapter extends BaseAdapter<Interest, InterestAdapter.ViewHolder> {


    OnInterestClickListener mListener;

    public InterestAdapter(Context context, ArrayList<Interest> models) {
        super(context, models);
    }

    @NonNull
    @Override
    public InterestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_interest, parent, false);
        return new ViewHolder(view, this);
    }


    @Override
    public void setItems(ArrayList<Interest> items) {
        super.setItems(items);
    }

    public class ViewHolder extends BaseViewHolder<Interest> {

         ImageView interestIcon;
         TextView interestTV;
         MaterialCardView cardView;


        public ViewHolder (@NonNull View itemView, BaseAdapter mAdapter) {
            super(itemView, mAdapter);
            this.interestIcon = itemView.findViewById(R.id.item_interest_image);
            this.interestTV = itemView.findViewById(R.id.item_interest_tv);
            this.cardView = itemView.findViewById(R.id.item_interest_cardview);
        }
        public void updateColor(Interest interest){
            if(interest.isSelected()){
                cardView.setCardBackgroundColor(getContext().getColor(R.color.secondaryBlue));
            }
            else {
                cardView.setCardBackgroundColor(getContext().getColor(R.color.transparent));
                cardView.setCardElevation(0);
            }
        }
        @Override
        public void bindData(Interest itemData) {
            this.interestTV.setText(itemData.getInterest().toLowerCase());
            this.interestIcon.setImageResource(itemData.getImageResource());
            this.cardView.setOnClickListener(v -> {
                if(mListener != null){
                    mListener.onItemClick(getAdapterPosition());
                    updateColor(itemData);
                }
            });
            updateColor(itemData);

        }

    }

    public interface OnInterestClickListener {
        void onItemClick(int pos);
    }
    public void setOnInterestClickListener(OnInterestClickListener mListener){
        this.mListener = mListener;
    }



}

    /*
    HashMap<Integer, Interest> selectedItems;

    public InterestAdapter(Context context, ArrayList<Interest> models) {
        super(context, models);
        selectedItems = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_interest, parent, false);
        return new ViewHolder(view, this);
    }

    public HashMap<Integer, Interest> getSelectedItems() {
        return selectedItems;
    }
    public void clearSelectedItems(){
        selectedItems.clear();
    }


    @Override
    public void addItem(Interest item) {
        this.items.add(0,item);
        notifyItemInserted(0);
    }
    @Override
    public int getItemCount() {
        return this.items.size();
    }

    onCheckboxClickListener mListener;
    public void setOnCheckboxListner(onCheckboxClickListener mListener){
        this.mListener = mListener;
    }

    public interface onCheckboxClickListener{
         void onCheckboxClicked(int position);
    }

    public class ViewHolder extends BaseViewHolder<Interest> {

        private ImageView interestIcon;
        private TextView interestTV;
        private MaterialCheckBox checkBox;
        MaterialCardView cardView;


        public ViewHolder(@NonNull View itemView, BaseAdapter mAdapter) {
            super(itemView, mAdapter);
            this.interestIcon = itemView.findViewById(R.id.item_interest_image);
            this.interestTV = itemView.findViewById(R.id.item_interest_tv);
            this.checkBox = itemView.findViewById(R.id.item_interest_checkbox);
            this.cardView = itemView.findViewById(R.id.item_interest_cardview);
           // this.cardView.setBackgroundResource(R.drawable.post_background);



        }

        @Override
        public void bindData(Interest itemData) {
            this.interestTV.setText(itemData.getInterest().toLowerCase());
            this.interestIcon.setImageResource(itemData.getImageResource());
            this.checkBox.setChecked(itemData.isSelected());
            this.checkBox.setOnClickListener(v -> {
                if(mListener != null){
                    Log.e("onClick Interest Adapter", getItems().get(getAdapterPosition()).getInterest());
                    mListener.onCheckboxClicked(getAdapterPosition());
                    updateSelectedItems(itemData);
                }
                updateSelectedItems(itemData);


            });

        }
        public void updateSelectedItems(Interest interest){
            if(interest.isSelected()) {
                selectedItems.putIfAbsent(getItems().get(getAdapterPosition()).getId(), interest); //interest.getId()
                cardView.setBackgroundResource(R.drawable.interest_background);


               // Log.e("updatedSelect", getItems().get(getAdapterPosition()).getInterest());
               // if (!selectedItems.containsKey(getItems().get(getAdapterPosition()).getId())) {
               //     selectedItems.put(getItems().get(getAdapterPosition()).getId(), getItems().get(getAdapterPosition()));
               //     Log.e("updatedSelect", "not already in");
               // } else Log.e("updatedSelect", "already in");


            }
            else {
                selectedItems.remove(getItems().get(getAdapterPosition()).getId());  //interest.getId()
                cardView.setBackgroundResource(R.color.transparent);

            }
        }
    }


}
*/