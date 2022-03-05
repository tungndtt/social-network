package com.example.tintok;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.Pair;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tintok.Communication.Communication;
import com.example.tintok.Communication.CommunicationEvent;
import com.example.tintok.Communication.RestAPI;
import com.example.tintok.Communication.RestAPI_model.CommentForm;
import com.example.tintok.Communication.RestAPI_model.PostForm;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.Model.Comment;
import com.example.tintok.Model.MediaEntity;
import com.example.tintok.Model.MessageEntity;
import com.example.tintok.Model.Post;
import com.example.tintok.Model.UserSimple;
import com.example.tintok.Utils.EmoticonHandler;
import com.example.tintok.Utils.FileUtil;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.socket.emitter.Emitter;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Activity_Comment_ViewModel extends MainPages_Posts_ViewModel{

    public Activity_Comment_ViewModel(@NonNull Application application) {
        super(application);
        this.api = Communication.getInstance().getApi();
        this.listComments = new MutableLiveData<>(new ArrayList<>());
        this.currentPost = new MutableLiveData<>();

        Communication.getInstance().get_socket().on(CommunicationEvent.NEW_COMMENT, args -> {
            String author_id = (String) args[0];
            String comment = (String) args[1];
            String post_id = (String) args[2];
            String image_path = (String) args[3];
            ArrayList<Comment> comments = listComments.getValue();
            Log.e("Act_Comment_ViewModel", "new cmt content: "+ comment);
            if(!image_path.isEmpty())
                comments.add(0,new Comment("some id", author_id, EmoticonHandler.parseMessageFromString(getApplication().getBaseContext(), comment),
                    new MediaEntity(image_path), LocalDateTime.now()));
            else
                comments.add(0,new Comment("some id", author_id, EmoticonHandler.parseMessageFromString(getApplication().getBaseContext(), comment),
                        null, LocalDateTime.now()));

            listComments.postValue(comments);
        });
    }

    private RestAPI api;
    private MutableLiveData<ArrayList<Comment>> listComments;



    private EmoticonHandler mEmoiconHandler = null;

    public EmoticonHandler getEmoticonHandler(Context mContext, EditText editText){
        if(mEmoiconHandler == null)
            mEmoiconHandler = new EmoticonHandler(mContext, editText);
        return mEmoiconHandler;
    }

    public MutableLiveData<ArrayList<Comment>> getListComments() {
        return listComments;
    }

    public void getComments(String post_id) {
        api.getComments(post_id).enqueue(new Callback<PostForm>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<PostForm> call, Response<PostForm> response) {
                if(response.isSuccessful()){
                    ArrayList<CommentForm> comments = response.body().getComments();
                   // Log.e("Comment_Activity_ViewModel, Length", ""+comments.get(0).getComment()+" "+comments.get(0).getAuthor_name());
                            ArrayList<Comment> mComments = listComments.getValue();
                    for(CommentForm c : comments){
                        Comment m = null;
                        if(!c.getImage_path().isEmpty())
                            m = new Comment(
                                    c.getId(),
                                    c.getAuthor_id(),
                                    EmoticonHandler.parseMessageFromString(getApplication().getBaseContext(), c.getComment()),
                                    new MediaEntity(c.getImage_path()), Instant.ofEpochMilli(c.getDate()).atZone(ZoneId.systemDefault()).toLocalDateTime()
                            );
                        else{
                            m = new Comment(
                                    c.getId(),
                                    c.getAuthor_id(),
                                    EmoticonHandler.parseMessageFromString(getApplication().getBaseContext(), c.getComment()),
                                    null,
                                    Instant.ofEpochMilli(c.getDate()).atZone(ZoneId.systemDefault()).toLocalDateTime()
                            );
                        }
                        mComments.add(m);
                        UserSimple newUser = new UserSimple();
                        newUser.setUserID(c.getAuthor_id());
                        newUser.setUserName(c.getAuthor_name());

                        // set user profile

                    }
                    listComments.postValue(mComments);
                    PostForm p = response.body();
                    Post post = new Post(post_id, p.getStatus(), p.getAuthor_id(), new MediaEntity(p.getImageUrl()),
                            Instant.ofEpochMilli(p.getDate()).atZone(ZoneId.systemDefault()).toLocalDateTime());
                    post.likers = p.getLikes() == null?new ArrayList<>():p.getLikes();
                    ArrayList<Post> posts = new ArrayList<>();
                    post.comments = listComments;
                    posts.add(post);

                    currentPost.postValue(posts);
                } else {
                    Toast.makeText(Activity_Comment_ViewModel.this.getApplication(),"Request fails", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PostForm> call, Throwable t) {
                try {
                    throw t;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }

    public void sendComment(String post_id, Uri uri){
        Pair<String, SpannableStringBuilder> newComment = mEmoiconHandler.parseMessage();
        String comment = newComment.first;
        if(uri == null){
            Communication.getInstance()
                    .get_socket()
                    .emit(
                            CommunicationEvent.SEND_COMMENT,
                            post_id,
                            DataRepositoryController.getInstance().getUser().getValue().getUserID(),
                            comment
                    );
        }
        else {
            MultipartBody.Part part = FileUtil.prepareImageFileBody(this.getApplication().getBaseContext(), "upload", new MediaEntity(uri));
            RequestBody sender_id = RequestBody.create(MultipartBody.FORM, DataRepositoryController.getInstance().getUser().getValue().getUserID());
            RequestBody postId = RequestBody.create(MultipartBody.FORM, post_id);
            RequestBody cmt = RequestBody.create(MultipartBody.FORM, comment);
            this.api.uploadFileFromComment(part,postId,sender_id,cmt).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(!response.isSuccessful()){
                        Log.e("Activity_Comment", "Sending comment fails");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        throw t;
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            });
        }
    }

    public void joinPost(String post_id){
        Communication.getInstance().get_socket().emit(CommunicationEvent.JOIN_POST, post_id);
    }

    public void leavePost(String post_id){
        Communication.getInstance().get_socket().emit(CommunicationEvent.LEAVE_POST, post_id);
    }

    MutableLiveData<ArrayList<Post>> currentPost ;

    @Override
    public MutableLiveData<ArrayList<Post>> getPosts() {
        Log.e("VM_Ac_Cmt","GetCalled");
        return currentPost;
    }
}
