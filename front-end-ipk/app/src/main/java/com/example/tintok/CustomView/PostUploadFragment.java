package com.example.tintok.CustomView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.Model.MediaEntity;
import com.example.tintok.Model.Post;
import com.example.tintok.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class PostUploadFragment extends MyDialogFragment {

    private EditTextSupportIME status;
    private ImageView image;
    private MaterialToolbar toolbar;
    private View view;
    MediaEntity chosenImage;

    // Code for get image from gallery
    private static final int IMAGE_PICK_CODE = 224;
    private static final int REQUEST_IMAGE = 1998;

    private static final int CAMERA_PICK_CODE = 225;
    private static final int REQUEST_CAMERA = 225;

    public PostUploadFragment(onNewPostListener mListner){
        this.mListner = mListner;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(android.content.DialogInterface dialog,
                                 int keyCode,android.view.KeyEvent event)
            {
                if ((keyCode ==  android.view.KeyEvent.KEYCODE_BACK))
                {
                    // To dismiss the fragment when the back-button is pressed.
                    onCancel();
                    return true;
                }
                // Otherwise, do nothing else
                else return false;
            }
        });
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()){
            @Override
            public void onBackPressed() {
                onCancel();
            }
        };
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.post_upload_fragment, container, false);
        status = view.findViewById(R.id.new_post_status);
        image = view.findViewById(R.id.new_post_image);
        toolbar = view.findViewById(R.id.new_post_toolbar);
        toolbar.setTitle("Create post");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        String[] colors = {"Gallery", "Camera"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Picking image from");
        builder.setItems(colors, (dialog, which) -> {
            if(which == 0)
                askForPermission(REQUEST_IMAGE);
            else
                askForPermission(REQUEST_CAMERA);
        });

        this.image.setOnClickListener(v -> builder.show());
        toolbar.setNavigationOnClickListener(v -> {
            onCancel();
        });

        Button postBtn = getView().findViewById(R.id.new_post_submit);
        postBtn.setOnClickListener(v -> {
           if(chosenImage != null){
               Post mPost = new Post("", status.getText().toString(),
                       DataRepositoryController.getInstance().getUser().getValue().getUserID(),
                       chosenImage, LocalDateTime.now());
               mListner.onNewPost(mPost);
               getDialog().dismiss();
           } else {
               Toast.makeText(getContext(), "No image chosen", Toast.LENGTH_LONG).show();
           }
        });

        status.setKeyBoardInputCallbackListener((inputContentInfo, flags, opts) -> {
            Uri imgUri = inputContentInfo.getContentUri();
            Glide.with(this.getContext()).load(imgUri).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(image);
            chosenImage = new MediaEntity(imgUri, "");
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void askForPermission(int requestCode){
        String permission = requestCode == REQUEST_IMAGE ? Manifest.permission.READ_EXTERNAL_STORAGE : Manifest.permission.CAMERA;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                String[] permissions = {permission};
                requestPermissions(permissions,requestCode);
            } else {
                if(requestCode == REQUEST_IMAGE)
                    pickImageFromGallery();
                else pickImageFromCamera();
            }
        } else {
            if(requestCode == REQUEST_IMAGE)
                pickImageFromGallery();
            else pickImageFromCamera();
        }
    }

    private void pickImageFromCamera(){
        Intent imgIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (imgIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(imgIntent, CAMERA_PICK_CODE);
        }
    }

    private void pickImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            if(requestCode == REQUEST_IMAGE)
                pickImageFromGallery();
            else pickImageFromCamera();
        else
            Toast.makeText(this.getContext(),"Permission denied...", Toast.LENGTH_LONG);
    }

    @Override
   public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null && resultCode == getActivity().RESULT_OK) {
            if (requestCode == IMAGE_PICK_CODE) {
                Uri imgUri= data.getData();
                Glide.with(this.getContext()).load(imgUri).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(image);
                chosenImage = new MediaEntity(imgUri, "");
            } else if(requestCode == CAMERA_PICK_CODE){
                if(data.getExtras() != null){
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    Glide.with(this.getContext()).load(bitmap).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(image);
                    chosenImage = new MediaEntity(bitmap);
                }
            } else{
                Log.i("Info", "Some things wrong happened");
            }
        }
    }

    onNewPostListener mListner;
    public interface onNewPostListener {
       public void onNewPost(Post newPost);

    }
    public void onCancel(){
        if(chosenImage != null || status == null || !status.getText().toString().isEmpty()){
            MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(getContext());
            alertBuilder.setCancelable(true)
                    .setMessage("Your post will be discarded. Do you want to continue?")
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.cancel();})
                    .setPositiveButton("Continue", (dialog, which) -> {
                        if(mListner != null)
                            mListner.onNewPost(null);
                        dismiss();
                    })
                    .show();
        }else if(mListner != null){
            mListner.onNewPost(null);
            dismiss();
        }else dismiss();
    }
}
