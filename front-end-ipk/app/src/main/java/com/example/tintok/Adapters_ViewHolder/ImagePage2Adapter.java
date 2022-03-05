package com.example.tintok.Adapters_ViewHolder;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.Model.MediaEntity;
import com.example.tintok.Model.Post;

import java.util.ArrayList;

import com.example.tintok.Model.UserProfile;
import com.example.tintok.R;

public class ImagePage2Adapter extends BaseAdapter<Post, ImagePage2Adapter.ViewHolder> {

    OnImageClickListener mListener;
    public ImagePage2Adapter(Context context, ArrayList<Post> items) {
        super(context, items);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_image_item,parent,false);
        return new ViewHolder(view, this);
    }

    @Override
    public void addItem(Post item) {
        this.items.add(0,item);
        notifyItemInserted(0);
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public class ViewHolder extends BaseViewHolder<Post>{
        private ImageView img;
        String url;
        public ViewHolder(@NonNull View itemView, BaseAdapter baseAdapter) {
            super(itemView, baseAdapter);
            this.img = itemView.findViewById(R.id.image_on_profile_page);
        }

        @Override
        public void bindData(Post itemData) {
            this.url = itemData.getImage().url;
            Glide.with(mAdapter.getContext()).load(itemData.getImage().url).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(img);
            img.setLongClickable(true);
            img.setOnLongClickListener(v -> {
                if(mListener != null){
                    mListener.onImageClick(this.url);
                }

                /*
                String[] colors = {"Choose this image as Avatar"};
                AlertDialog.Builder builder = new AlertDialog.Builder(mAdapter.getContext());
                builder.setCancelable(true);
                builder.setTitle("Options");
                builder.setItems(colors, (dialog, which) -> {
                    if(which == 0){
                       UserProfile user = DataRepositoryController.getInstance().getUser().getValue();
                        user.setProfilePic(new MediaEntity(this.url));
                        DataRepositoryController.getInstance().getUser().postValue(user);
                    }
                });
                builder.show();

                 */
                return true;
            });
        }
    }

    public interface OnImageClickListener{
        void onImageClick(String url);
    }
    public void setOnImageClickListener(OnImageClickListener mListener){
        this.mListener = mListener;
    }

}