package com.example.tintok;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tintok.Adapters_ViewHolder.BaseAdapter;
import com.example.tintok.Adapters_ViewHolder.CommentAdapter;
import com.example.tintok.Adapters_ViewHolder.EmojiAdapter;
import com.example.tintok.Adapters_ViewHolder.EmojiViewHolder;
import com.example.tintok.Adapters_ViewHolder.MessageViewHolder;
import com.example.tintok.Adapters_ViewHolder.MessagesAdapter;
import com.example.tintok.Adapters_ViewHolder.PostAdapter;
import com.example.tintok.Communication.Communication;
import com.example.tintok.CustomView.EditTextSupportIME;
import com.example.tintok.CustomView.NoSpaceRecyclerViewDecoration;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.Model.Comment;
import com.example.tintok.Model.EmojiModel;
import com.example.tintok.Model.MediaEntity;
import com.example.tintok.Model.MessageEntity;
import com.example.tintok.Model.Post;
import com.example.tintok.Model.UserSimple;
import com.example.tintok.Utils.AppNotificationChannelManager;
import com.example.tintok.Utils.EmoticonHandler;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class Activity_Comment extends AppCompatActivity implements View.OnClickListener{
    private String postID="";
    Activity_Comment_ViewModel mViewModel;

    private final static int ALL_PERMISSIONS_RESULT = 107;
    private final static int PICK_FROM_GALLERY = 101;
    private final static int PICK_FROM_CAMERA = 102;
    //GUI
    RecyclerView emoji;
    RecyclerView messages;
    EditTextSupportIME nextMsg;
    ImageButton emojiButton, galleryImgButton, sendBtn;
    MaterialCardView commentImg;
    ImageView choosenImage;
    ImageButton deleteChoosenImg;
    EmoticonHandler mEmoHandler;
    MaterialToolbar toolbar;

    private TextView nComment, nLike, status, author;
    private ImageView iv, notificationIcon, profile;

    MaterialButton likeBtn;
    MaterialCardView cardView;
    GestureDetectorCompat mGestureDetector;

    //Data
    Uri currentCmtImg;
    BaseAdapter<Comment, CommentAdapter.ViewHolder> mAdapter;

    private ArrayList<String> permissionsToRequest = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        this.mViewModel = new ViewModelProvider(this).get( Activity_Comment_ViewModel.class);
        if (getIntent() != null) {
            this.postID = getIntent().getStringExtra("post_id");
        }

        initComponents();
        initEmoji();
        mViewModel.getComments(postID);
        initMessages();
        initPostComponents();
        askpermission();
        final Intent intent = new Intent(this, Activity_AppMainPages.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

        mViewModel.getListComments().observe(this, commentEntities -> {
            mAdapter.setItems(commentEntities );
        });

        //Testing only

    }

    private void initPostComponents() {
        MainPages_Posts_Fragment postFragment = new MainPages_Posts_Fragment(false,true);
        ((MainPages_Posts_Fragment)postFragment).setViewModel(this.mViewModel);
        getSupportFragmentManager().beginTransaction().replace(R.id.post_part, postFragment).commit();
    }


    void initComponents() {
        emoji = findViewById(R.id.emojiView);
        messages = findViewById(R.id.list_comment);
        nextMsg = findViewById(R.id.comment_field);
        emojiButton = findViewById(R.id.openEmojibtn);
        galleryImgButton = findViewById(R.id.sendImgbtn);
        sendBtn = findViewById(R.id.send_comment);
        commentImg = findViewById(R.id.comment_image);
        choosenImage = findViewById(R.id.choosenImage);
        deleteChoosenImg = findViewById(R.id.imageRemoveBtn);

        deleteChoosenImg.setOnClickListener(v -> onImageRemove());

        toolbar = findViewById(R.id.comment_toolbar);
        toolbar.setTitle("Comments");
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        nextMsg.requestFocus();


        emojiButton.setOnClickListener(v -> {
            if (emoji.getVisibility() == View.VISIBLE)
                emoji.setVisibility(View.GONE);
            else
                emoji.setVisibility(View.VISIBLE);
        });



        galleryImgButton.setOnClickListener(v -> handleImgSfromGallery());

        sendBtn.setOnClickListener(v -> handleSendComment());

        mEmoHandler = mViewModel.getEmoticonHandler(this, nextMsg);
        nextMsg.addTextChangedListener(mEmoHandler);
        nextMsg.setKeyBoardInputCallbackListener((inputContentInfo, flags, opts) -> {

            Uri imageUri = inputContentInfo.getContentUri();
            onImageChoosen(imageUri);
        });


    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, Activity_ViewProfile.class);
        intent.putExtra("author_id", mViewModel.getPosts().getValue().get(0).getAuthor_id());
        v.getContext().startActivity(intent);
    }

    void initMessages() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        messages.setLayoutManager(layoutManager);
        ArrayList<Comment>  comments = new ArrayList<>();
        mAdapter = new CommentAdapter(this, comments);
        Log.e("activity CMT start:" , "items "+mAdapter.getItems());
        messages.setAdapter(mAdapter);
        messages.addItemDecoration(new NoSpaceRecyclerViewDecoration());
    }

    void initEmoji() {


        ArrayList<EmojiModel> emojis = EmojiModel.getEmojis(this);

        BaseAdapter<EmojiModel, EmojiViewHolder> emojidapter = new EmojiAdapter(this, emojis, position -> mEmoHandler.insertEmoji(emojis.get(position).getResourceImgName()));
        emoji.setAdapter(emojidapter);

        GridLayoutManager gridView = new GridLayoutManager(this, 5, RecyclerView.HORIZONTAL, false);
        emoji.setLayoutManager(gridView);

    }

    void askpermission() {
        if (permissions.isEmpty()) {
            permissions.add(Manifest.permission.CAMERA);
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        for (String perm : permissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(perm) == PackageManager.PERMISSION_DENIED) ;
                permissionsToRequest.add(perm);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ALL_PERMISSIONS_RESULT) {
            permissionsToRequest.clear();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                    permissionsToRequest.add(permissions[i]);
            }

            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
    }

    void handleImgSfromGallery() {
        Intent imgIntent = new Intent(Intent.ACTION_GET_CONTENT);
        imgIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(imgIntent, "Select Gallery"), PICK_FROM_GALLERY);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK) {
            ClipData imgData = data.getClipData();

            if (imgData != null) {
                int n = imgData.getItemCount();
                for (int i = 0; i < n; i++) {
                    Uri imageUri = imgData.getItemAt(i).getUri();
                    /*try {
                        InputStream is = getContentResolver().openInputStream(imageUri);
                        imgs.add(BitmapFactory.decodeStream(is));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    onImageChoosen(imageUri);
                }
            } else {
                Uri imageUri = data.getData();
                onImageChoosen(imageUri);
            }
        } /*else if (requestCode == PICK_FROM_CAMERA) {
            if (data == null)
                return;
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            handleSendImgFromCamera(bitmap);
        }*/
    }

    void onImageChoosen(Uri imageUri){
        this.currentCmtImg = imageUri;
        commentImg.setVisibility(View.VISIBLE);
        choosenImage.setImageURI(this.currentCmtImg);
        Log.e("Activity_Cmt", "Called");
    }
    void onImageRemove(){
        this.currentCmtImg = null;
        commentImg.setVisibility(View.GONE);
        choosenImage.setImageURI(null);
    }

    void handleSendComment() {
        if (nextMsg.getText().toString().compareTo("") == 0 && currentCmtImg == null)
            return;
        mViewModel.sendComment(this.postID, currentCmtImg );
        Log.e("Activity_Comment ","at "+ postID);
        nextMsg.setText("");
        currentCmtImg = null;
        commentImg.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.mViewModel.joinPost(this.postID);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messages.setAdapter(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.mViewModel.leavePost(this.postID);
    }
}