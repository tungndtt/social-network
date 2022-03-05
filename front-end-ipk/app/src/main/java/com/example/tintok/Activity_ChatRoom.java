package com.example.tintok;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tintok.Adapters_ViewHolder.BaseAdapter;
import com.example.tintok.Adapters_ViewHolder.EmojiAdapter;
import com.example.tintok.Adapters_ViewHolder.EmojiViewHolder;
import com.example.tintok.Adapters_ViewHolder.MessageViewHolder;
import com.example.tintok.Adapters_ViewHolder.MessagesAdapter;
import com.example.tintok.CustomView.EditTextSupportIME;

import com.example.tintok.CustomView.NoSpaceRecyclerViewDecoration;
import com.example.tintok.DataLayer.DataRepositiory_Chatrooms;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.DataLayer.DataRepository_UserSimple;
import com.example.tintok.Model.EmojiModel;
import com.example.tintok.Model.MessageEntity;
import com.example.tintok.Model.UserSimple;
import com.example.tintok.Utils.AppNotificationChannelManager;
import com.example.tintok.Utils.CustomItemAnimator;
import com.example.tintok.Utils.EmoticonHandler;
import com.example.tintok.Utils.FileUtil;
import com.google.android.material.appbar.MaterialToolbar;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;

import android.util.Log;
import android.view.View;

import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class Activity_ChatRoom extends AppCompatActivity implements DataRepository_UserSimple.OnUserProfileChangeListener, DataRepositiory_Chatrooms.OnNewMessagesListener {
    //const
    private String roomID="";
    Activity_Chatroom_ViewModel mViewModel;
    MaterialToolbar toolbar;
    private TextView mProfileName;

    private final static int ALL_PERMISSIONS_RESULT = 107;
    private final static int PICK_FROM_GALLERY = 101;
    private final static int PICK_FROM_CAMERA = 102;
    //GUI
    RecyclerView emoji;
    RecyclerView messages;
    EditTextSupportIME nextMsg;
    ImageButton emojiButton, galleryImgButton, sendBtn, cameraBtn;
    ImageView profileImg;
    EmoticonHandler mEmoHandler;
    //Data

    BaseAdapter<MessageEntity, MessageViewHolder> msgAdapter;

    private ArrayList<String> permissionsToRequest = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        if (getIntent() != null) {
            this.roomID = getIntent().getStringExtra("roomID");
        }
        this.mViewModel = new ViewModelProvider(this).get(Activity_Chatroom_ViewModel.class);

        initComponents();
        initEmoji();
        initMessages();
        askpermission();


        mViewModel.getMessEntity(roomID).observe(this, messageEntities -> {
            msgAdapter.setItems(messageEntities);
            messages.smoothScrollToPosition(msgAdapter.getItemCount());
        });

        //Testing only

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    void initComponents() {
        emoji = findViewById(R.id.emojiView);
        messages = findViewById(R.id.messageView);
        nextMsg = findViewById(R.id.newMsg);
        emojiButton = findViewById(R.id.openEmojibtn);
        cameraBtn = findViewById(R.id.cameraBtn);
        galleryImgButton = findViewById(R.id.sendImgbtn);

        sendBtn = findViewById(R.id.sendButton);

        toolbar =  findViewById(R.id.chatroom_toolbar);
        toolbar.setNavigationOnClickListener(v -> {
                finish(); //onBackPressed();
        });

        profileImg = toolbar.findViewById(R.id.profileImg);
        mProfileName = toolbar.findViewById(R.id.chatroom_profile_name);
        nextMsg.requestFocus();

        profileImg.setOnClickListener(v -> {
            //TODO: goToProfilePage
        });

        emojiButton.setOnClickListener(v -> {
            if (emoji.getVisibility() == View.VISIBLE)
                emoji.setVisibility(View.INVISIBLE);
            else
                emoji.setVisibility(View.VISIBLE);
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImgfromCamera();
            }
        });

        galleryImgButton.setOnClickListener(v -> handleImgSfromGallery());

        sendBtn.setOnClickListener(v -> handleSendMessage());

        mEmoHandler = mViewModel.getEmoticonHandler(this, nextMsg);
        nextMsg.addTextChangedListener(mEmoHandler);
        nextMsg.setKeyBoardInputCallbackListener((inputContentInfo, flags, opts) -> {

            ArrayList<Uri> imgs = new ArrayList<>();
            Uri imgUri = inputContentInfo.getContentUri();
            imgs.add(imgUri);
            handleSendImg(imgs);
        });

    }


    void initMessages() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        messages.setLayoutManager(layoutManager);
        msgAdapter = new MessagesAdapter(this, new ArrayList<>());
        messages.setAdapter(msgAdapter);
        messages.setItemAnimator(new CustomItemAnimator());
        messages.addItemDecoration(new NoSpaceRecyclerViewDecoration());
    }

    void initEmoji() {


        //SampleData

        ArrayList<EmojiModel> emojis = EmojiModel.getEmojis(this);
        //endSampleData

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
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.closeChatRoom(this.roomID);
    }

    void handleImgfromCamera() {
        Intent imgIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (imgIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(imgIntent, PICK_FROM_CAMERA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<Uri> imgs = new ArrayList<>();
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
                    imgs.add(imageUri);
                }
            } else {
                Uri imageUri = data.getData();
                imgs.add(imageUri);
            }
            handleSendImg(imgs);
        } else if (requestCode == PICK_FROM_CAMERA) {
            if (data == null)
                return;
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            handleSendImgFromCamera(bitmap);
        }
    }


    void handleSendImg(ArrayList<Uri> imgs) {
        mViewModel.handleSendImg(this.roomID, imgs);
    }

    void handleSendImgFromCamera(Bitmap m) {
        mViewModel.handleSendImgFromCamera(this.roomID, m);
    }

    void handleSendMessage() {
        if (nextMsg.getText().toString().compareTo("") == 0)
            return;
        emoji.setVisibility(View.GONE);
        mViewModel.handleSendMessage(roomID);
        nextMsg.setText("");
    }


    @Override
    public void onProfileChange(UserSimple user) {
        if(mViewModel.getOtherUser().compareTo(user.getUserID())==0){
            Glide.with(this).load(user.getProfilePic().url)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(profileImg);
            mProfileName.setText(user.getUserName());
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewModel.registerUserSimpleListener(this);
        UserSimple user = mViewModel.getUserbyID(mViewModel.getOtherUser());
        Glide.with(this).load(user.getProfilePic().url)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(profileImg);
        mProfileName.setText(user.getUserName());
        if(!this.roomID.isEmpty())
            this.mViewModel.joinChatRoom(this.roomID);
        else{
            this.mViewModel.addOnNewMessageListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mViewModel.removeUserSimpleListener(this);
    }



    @Override
    protected void onStop() {
        super.onStop();
        if(!roomID .isEmpty())
            this.mViewModel.leaveChatRoom(this.roomID);
        else
            this.mViewModel.closeChatRoom(this.roomID);
    }

    @Override
    public void onNewMessage(String roomID, MessageEntity msg) {
        if(this.roomID.isEmpty()){
            if(mViewModel.getChatRoomByID(roomID).getMessageEntities().getValue().size() == 1)
                this.roomID = roomID;
        }
        this.mViewModel.joinChatRoom(this.roomID);
        mViewModel.removeOnNewMessageListener(this);
    }
}