package com.example.tintok.DataLayer;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.tintok.Communication.Communication;
import com.example.tintok.Communication.CommunicationEvent;
import com.example.tintok.CustomView.AfterRefreshCallBack;
import com.example.tintok.Model.ChatRoom;
import com.example.tintok.Model.Interest;
import com.example.tintok.Model.MessageEntity;
import com.example.tintok.Model.Notification;
import com.example.tintok.Model.Post;
import com.example.tintok.Model.UserProfile;
import com.example.tintok.Model.UserSimple;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class DataRepositoryController {
    public static Context applicationContext;
    private static DataRepositoryController instance;
    public static synchronized DataRepositoryController getInstance(){
        if(instance == null)
            instance = new DataRepositoryController();
        return instance;
    }

    DataRepositiory_Chatrooms dataRepositiory_chatrooms;
    DataRepository_CurrentUser dataRepository_currentUser;
    DataRepository_MatchingPeople dataRepository_matchingPeople;
    DataRepository_Notifications dataRepository_notifications;
    DataRepository_Posts dataRepository_posts;

    DataRepository_UserSimple dataRepository_userSimple;






    private DataRepositoryController(){
       dataRepository_userSimple = new DataRepository_UserSimple();

        dataRepositiory_chatrooms = new DataRepositiory_Chatrooms(this);
        dataRepository_currentUser = new DataRepository_CurrentUser(this);
        dataRepository_matchingPeople = new DataRepository_MatchingPeople(this);
        dataRepository_notifications = new DataRepository_Notifications(this);
        dataRepository_posts = new DataRepository_Posts(this);
    }

    public void initDataFromServer(){
        Communication.getInstance().initScoket();
        Log.e("DataInitRepo", "at: "+ Communication.getInstance().get_socket());
        dataRepository_currentUser.initData();
        dataRepositiory_chatrooms.initData();
        dataRepository_matchingPeople.initData();
        dataRepository_notifications.initData();
        dataRepository_posts.initData();
        DataRepository_Interest.initInterestArrayList();

        /*ArrayList<ChatRoom> mChatRooms = new ArrayList<>();
        ArrayList<MessageEntity> msgs = new ArrayList<>();
        MutableLiveData<ArrayList<MessageEntity>> msgsM = new MutableLiveData<>();
        msgsM.postValue(msgs);
        ChatRoom room = new ChatRoom();
        room.setChatRoomID("test");
        room.setMutableMessengerEntities(msgsM);
        mChatRooms.add(room);
        chatRooms.setValue(mChatRooms);*/



        //fetchData

        Log.e("DataRepo","InitDataDone ");

        dataRepository_userSimple.startAsyncTaskRecacheUser();
    }

    public boolean isDataReady(){
        return dataRepositiory_chatrooms.isReady() && dataRepository_currentUser.isReady() &&
                dataRepository_matchingPeople.isReady() && dataRepository_notifications.isReady && dataRepository_posts.isReady();
    }

   // public void ReFreshingData()

//region Chat Rooms

    public MutableLiveData<ArrayList<ChatRoom>> getChatRooms() {
        return dataRepositiory_chatrooms.getChatrooms();
    }
    public ChatRoom getChatRoomByID(String id){
        return dataRepositiory_chatrooms.getChatRoomByID(id);
    }
    public ChatRoom getChatRoomByUser(ArrayList<String> userIDs){
        return dataRepositiory_chatrooms.getChatRoomByUser(userIDs);
    }
    public void closeChatRoom(String roomID) {
        dataRepositiory_chatrooms.closeChatRoom(roomID);
    }

    public void emitNewMessage(Context mContext, String roomID, MessageEntity newMsg, String encoded){
        dataRepositiory_chatrooms.emitNewMessage( mContext, roomID, newMsg, encoded);
    }
    public void addNewMessageListener(DataRepositiory_Chatrooms.OnNewMessagesListener mListener){
        dataRepositiory_chatrooms.addNewMessageListener(mListener);
    }
    public void removeNewMessageListener(DataRepositiory_Chatrooms.OnNewMessagesListener mListener){
       dataRepositiory_chatrooms.removeNewMessageListener(mListener);
    }

    public int getNumberOfUnseenChatrooms(){
        int i = 0;
        for(ChatRoom r: getChatRooms().getValue()){
            ArrayList<MessageEntity> messages = r.getMessageEntities().getValue();
            if(messages!= null && !messages.isEmpty() && messages.get(messages.size()-1).datePosted.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() > getLastSeen()){
                Log.e("DataRepo_Control_mess", "lastseen: "+getLastSeen() + " time: "+messages.get(messages.size()-1).datePosted.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                i++;
            }
        }
        return i;
    }
//endregion



//region CurrentUser
    public MutableLiveData<UserProfile> getUser() {
        return  dataRepository_currentUser.currentUser;
    }
    public MutableLiveData<Boolean> getIsUserUpdating(){
        return dataRepository_currentUser.isUserUpdating;
    }
    public void updateUserInfo(UserProfile userProfile){
        this.dataRepository_currentUser.updateUserInfo(userProfile);
    }
    public MutableLiveData<ResponseEvent> getNetworkResponse(){
        return this.dataRepository_currentUser.networkStatus;
    }
    public void changePassword(List<String> passwords) {
        this.dataRepository_currentUser.updateUserPassword(passwords);
    }
    public void updateUserInterests(ArrayList<Integer> interests){
        this.dataRepository_currentUser.updateUserInterests(interests);
    }
    public void updateUserProfilePicture(String url){
        this.dataRepository_currentUser.updateUserProfilePicture(url);
    }
    public void submitNewProfilePicture(Context c, Post post){
        this.dataRepository_currentUser.submitNewProfilePicture(c, post);
    }
    public long getLastSeen(){
        return dataRepository_currentUser.lastSeen;
    }
    public boolean isThisUserLikedPost(Post p){
        return getUser().getValue().likedPosts.contains(p.getId());
    }
    public boolean isThisUserSubscribedPost(Post p){
        return getUser().getValue().getFollowingPost().getValue().contains(p.getId());
        //To Do
    }
    public void UpdateFollowingPost(Post p ){
        dataRepository_currentUser.UpdateSubcribedPost(p);
    }

    public void UserPressFollow(String user){
        dataRepository_currentUser.UserPressFollow(user);
    }
    public boolean isCurrentUserFollowUser(String user){
        return dataRepository_currentUser.isCurrentUserFollowingUser(user);
    }
//endregion

//region matchingPeople
    public MutableLiveData<ArrayList<UserSimple>> getMatchingPeople() {
        return dataRepository_matchingPeople.getMatchingPeople();
    }
    public void findPeoplewithFilter(DataRepository_MatchingPeople.Filter currentState) {
        dataRepository_matchingPeople.FindMatchingByFilter(currentState);
    }
    public void submitPeopleReaction(UserSimple userSimple, boolean isLiked) {
        dataRepository_matchingPeople.submitPeopleReaction(userSimple,isLiked);
    }

    public void findMoreMatchingPeople() {
        dataRepository_matchingPeople.initData();
    }
//endregion

    //region NewFeedPosts
    public MutableLiveData<ArrayList<Post>> getNewfeedPosts() {
        return dataRepository_posts.getNewfeedPosts();
    }

    public void submitNewPost(Context mContext, Post newPost) {
        this.dataRepository_currentUser.submitNewPost(mContext, newPost);
    }

    public void refreshPost(AfterRefreshCallBack e) {
        dataRepository_posts.refreshPost(e);
    }
    //endregion

    //region Notifications
    public MutableLiveData<ArrayList<Notification>> getNotifications() {
        return dataRepository_notifications.getNotifications();
    }
    public void addNotificationListener(DataRepository_Notifications.OnNewNotificationListener mListener){
       dataRepository_notifications.addNotificationListener(mListener);
    }
    public void removeNotificationListener(DataRepository_Notifications.OnNewNotificationListener mListener){
        dataRepository_notifications.removeNotificationListener(mListener);
    }

    public int getNumberOfUnseenNotifications(){
        int i = 0;
        for(Notification notification:getNotifications().getValue()){
            if(notification.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() > getLastSeen()){
                Log.e("DataRepo_Control_noti", "lastseen: "+getLastSeen() + " time: "+notification.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

                i++;
            }
        }
        return i;
    }
//endregion



//region UserSimpleCache
    public UserSimple getUserSimpleProfile(String id){
        if(getUser().getValue().getUserID().compareTo(id) == 0)
            return getUser().getValue();
        UserSimple result = dataRepository_userSimple.findUserSimpleinCahe(id);
        return result;
    }

    public void AddUserProfileChangeListener(DataRepository_UserSimple.OnUserProfileChangeListener newListener){
        dataRepository_userSimple.addListener(newListener);
    }

    public void RemoveUserProfileChangeListener(DataRepository_UserSimple.OnUserProfileChangeListener newListener){
        dataRepository_userSimple.removeListener(newListener);
    }

//endregion

    //Network, data update tasks


//region CustomObserver
    //should be called if new Data received
    public void notifyObserver(){
        for(DataObserver dataObserver:dataObservers)
            dataObserver.notifyDataChange(this);
    }

    ArrayList<DataObserver> dataObservers = new ArrayList<>();



    public interface DataObserver{
        public void notifyDataChange(DataRepositoryController dataRepositoryController);
    }
//endregion

    public void ClearRepository(){
        dataRepository_userSimple.mListeners.clear();
        dataRepository_userSimple.cancelRecacheUserTask();
        DataRepositoryController.instance = null;
    }
}

