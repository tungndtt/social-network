package com.example.tintok;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.Pair;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tintok.Communication.Communication;
import com.example.tintok.Communication.CommunicationEvent;
import com.example.tintok.DataLayer.DataRepositiory_Chatrooms;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.DataLayer.DataRepository_UserSimple;
import com.example.tintok.Model.ChatRoom;
import com.example.tintok.Model.MediaEntity;
import com.example.tintok.Model.MessageEntity;
import com.example.tintok.Model.UserSimple;
import com.example.tintok.Utils.EmoticonHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Activity_Chatroom_ViewModel extends AndroidViewModel {

    private EmoticonHandler mEmoiconHandler = null;
    private MutableLiveData<ArrayList<MessageEntity>> messageEntities = null;
    private ArrayList<String> participants;

    public Activity_Chatroom_ViewModel(@NonNull Application application) {
        super(application);
    }

    public EmoticonHandler getEmoticonHandler(Context mContext, EditText editText){
        if(mEmoiconHandler == null)
            mEmoiconHandler = new EmoticonHandler(mContext, editText);
        return mEmoiconHandler;
    }

    public MutableLiveData<ArrayList<MessageEntity>> getMessEntity(String roomID){
        if(messageEntities == null){
            ChatRoom r = this.getChatRoomByID(roomID);
            messageEntities = r.getMessageEntities();
            participants = r.getMemberIDs();
        }

        return messageEntities;
    }

    public String getOtherUser(){
        if(participants == null)
            return null;
        for(String s: participants){
            if(s.compareTo(DataRepositoryController.getInstance().getUser().getValue().getUserID())!= 0)
                return s;
        }
        return null;
    }

    public UserSimple getUserbyID(String id){
        return DataRepositoryController.getInstance().getUserSimpleProfile(id);
    }

    public void registerUserSimpleListener(DataRepository_UserSimple.OnUserProfileChangeListener mListener){
        DataRepositoryController.getInstance().AddUserProfileChangeListener(mListener);

    }

    public void removeUserSimpleListener(DataRepository_UserSimple.OnUserProfileChangeListener mListener){
        DataRepositoryController.getInstance().RemoveUserProfileChangeListener(mListener);

    }


    protected ChatRoom getChatRoomByID(String id){
        return DataRepositoryController.getInstance().getChatRoomByID(id);
    }

    void handleSendImg(String roomID, ArrayList<Uri> imgs) {
        Date now = Calendar.getInstance().getTime();
        for (Uri img : imgs) {
            DataRepositoryController.getInstance().emitNewMessage(getApplication(), roomID,
                    new MessageEntity(DataRepositoryController.getInstance().getUser().getValue().getUserID(), new MediaEntity(img, null), null), "" );
        }
    }

    void handleSendImgFromCamera(String roomID, Bitmap m) {
        DataRepositoryController.getInstance().emitNewMessage(getApplication(), roomID,
                new MessageEntity(DataRepositoryController.getInstance().getUser().getValue().getUserID(), new MediaEntity(m), null), "" );
    }

    void handleSendMessage(String roomID) {
        Pair<String, SpannableStringBuilder> newMsg = mEmoiconHandler.parseMessage();

        DataRepositoryController.getInstance().emitNewMessage(getApplication(), roomID,
                new MessageEntity(DataRepositoryController.getInstance().getUser().getValue().getUserID(), newMsg.second, null), newMsg.first );
    }

    public void joinChatRoom(String roomID) {
        Communication.getInstance().get_socket().emit(CommunicationEvent.JOIN_CHAT_ROOM, roomID);
    }

    public void leaveChatRoom(String roomID) {
        Communication.getInstance().get_socket().emit(CommunicationEvent.LEAVE_CHAT_ROOM, roomID);
    }

    public void closeChatRoom(String roomID) {
        DataRepositoryController.getInstance().closeChatRoom(roomID);
    }

    public void addOnNewMessageListener(DataRepositiory_Chatrooms.OnNewMessagesListener mListener){
        DataRepositoryController.getInstance().addNewMessageListener(mListener);
    }
    public void removeOnNewMessageListener(DataRepositiory_Chatrooms.OnNewMessagesListener mListener){
        DataRepositoryController.getInstance().removeNewMessageListener(mListener);
    }
}
