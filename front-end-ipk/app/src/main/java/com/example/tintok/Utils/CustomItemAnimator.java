package com.example.tintok.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class CustomItemAnimator extends DefaultItemAnimator {
    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        holder.itemView.setAlpha(0);
        int duration = 500;
        int startDelay = (1)*200;
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(holder.itemView, "alpha" ,0f, 0.3f, 1f).setDuration(duration);
        ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(holder.itemView, "scaleX",0f, 0.5f, 1f).setDuration(duration);
        ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(holder.itemView, "scaleY",0f, 0.5f, 1f).setDuration(duration);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setStartDelay(startDelay);
        animatorSet.playTogether(animatorAlpha, animatorScaleX, animatorScaleY);
        animatorSet.start();
        return true;
    }

}
