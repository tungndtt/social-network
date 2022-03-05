package com.example.tintok.DataLayer;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.tintok.Model.Interest;
import com.example.tintok.R;

import java.util.ArrayList;

public class DataRepository_Interest {
    public static String[] interests = {"GAMING", "READING", "TRAVELLING",
            "SPORT", "SHOPPING", "LEARNING","GOSSIP"
            /*,"a", "b","awe" , "we", "Wae","awe","aweaw", "1", "2",
            "2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2","2"*/
    };

    public static ArrayList<InterestTag> parseData(boolean[] bitmap){
        ArrayList<InterestTag> tags = new ArrayList<>();
        if(interests.length != bitmap.length)
            return null;
        for(int i = 0 ; i<bitmap.length;i++)
            tags.add(new InterestTag(interests[i], bitmap[i]));
        return tags;
    }

    public static class InterestTag{
        public String tag;
        public boolean isChecked;
        public InterestTag(String tag, boolean isChecked){
            this.tag = tag;
            this.isChecked = isChecked;
        }
    }
    public static ArrayList<Interest> interestArrayList = new ArrayList<Interest>();
    public static void initInterestArrayList(){
        if(interestArrayList.size() == 0) {
            interestArrayList.add(new Interest(0, R.drawable.ic_esports_24, interests[0]));          //GAMING
            interestArrayList.add(new Interest(1,R.drawable.ic_reading_24, interests[1]));    //READING
            interestArrayList.add(new Interest(2,R.drawable.ic_travel_24, interests[2]));         //TRAVELLING
            interestArrayList.add(new Interest(3,R.drawable.ic_sports_basketball_24, interests[3]));           //SPORT
            interestArrayList.add(new Interest(4,R.drawable.ic_shopping_24, interests[4]));     //SHOPPING
            interestArrayList.add(new Interest(5,R.drawable.ic_school_24, interests[5]));     //LEARNING
            interestArrayList.add(new Interest(6,R.drawable.ic_groups_24, interests[6]));     //GOSSIP
          //  interestArrayList.add(new Interest(6,R.drawable.ic_music_24, interests[6]));     //music
          //  interestArrayList.add(new Interest(6,R.drawable.ic_party_24, interests[6]));     //party

        }
    }
    public static ArrayList<Interest> getInterestArrayList() {
        return interestArrayList;
    }

    public static void setUserInterest(ArrayList<Integer> userInterest){
        for(Interest interest: interestArrayList){
            boolean tmp = false;
            for(int i = 0; i < userInterest.size(); i++ ){
                if(interest.getId() == userInterest.get(i)){
                    tmp = true;
                    break;
                }
            }
            interest.setSelected(tmp);
        }
    }
}
