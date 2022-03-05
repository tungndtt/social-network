package com.example.tintok.Adapters_ViewHolder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.tintok.Model.EmojiModel;
import com.example.tintok.R;

public class EmojiViewHolder extends BaseViewHolder<EmojiModel> implements View.OnClickListener {

    EmojiAdapter.onEmojiChoosenListener mListener;
    ImageView img;
    EmojiViewHolder(@NonNull View itemView, BaseAdapter mAdapter, EmojiAdapter.onEmojiChoosenListener emojiChoosenListener) {
        super(itemView, mAdapter);
        img = itemView.findViewById(R.id.image);
        mListener = emojiChoosenListener;

        itemView.setOnClickListener(this);
    }

    @Override
    public void bindData(EmojiModel itemData) {
        img.setImageDrawable(itemData.getResource());
    }

    @Override
    public void onClick(View v) {
        mListener.onEmojiChose(getAdapterPosition());
    }
}
