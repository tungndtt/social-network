package com.example.tintok.Adapters_ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.tintok.Model.EmojiModel;
import com.example.tintok.R;

import java.util.ArrayList;

public class EmojiAdapter extends BaseAdapter<EmojiModel, EmojiViewHolder> {
    onEmojiChoosenListener emojiChoosenListener;
    public EmojiAdapter(Context context, ArrayList models, onEmojiChoosenListener mListener) {
        super(context, models);
        this.emojiChoosenListener = mListener;
    }

    @NonNull
    @Override
    public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emoji, parent, false);
        return new EmojiViewHolder(view, this, emojiChoosenListener );
    }

    public interface onEmojiChoosenListener{
        public void onEmojiChose(int position);
    }


}
