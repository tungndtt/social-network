package com.example.tintok;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tintok.DataLayer.DataRepositoryController;
import com.example.tintok.Model.ChatRoom;
import com.example.tintok.Model.UserSimple;
import java.util.ArrayList;

/**
 * This ViewModel is used to get all known (available) chatrooms for the user and to search for specific chatpartner by name
 */
public class MainPages_Chatroom_ViewModel extends ViewModel {


    /**
     * Getter
     * @return LiveData of ArrayList with all available chatrooms
     */
    public MutableLiveData<ArrayList<ChatRoom>> getChatrooms(){
        return DataRepositoryController.getInstance().getChatRooms();
    }

    /**
     * search for a (previous) chat partner  at user's available chatrooms
     * @param filter name or part of the name the user is looking for
     * @return list of chatsrooms that match the given user input
     */
    public ArrayList<ChatRoom> filterByName(String filter){
        if(filter.isEmpty())
            return this.getChatrooms().getValue();
        DataRepositoryController data = DataRepositoryController.getInstance();
        String thisUserID = data.getUser().getValue().getUserID();
        ArrayList<ChatRoom> result = new ArrayList<>();
        ArrayList<ChatRoom> original = this.getChatrooms().getValue();
        for(ChatRoom room:original){
            UserSimple another = null;
            for(String id : room.getMemberIDs()){
                if(id.compareTo(thisUserID)!= 0){
                    another = data.getUserSimpleProfile(id);
                    break;
                }
            }
            if(another!=null){
                if(another.getUserName().contains(filter))
                    result.add(room);
            }
        }
        return result;
    }
}
