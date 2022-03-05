package com.example.tintok.Communication.RestAPI_model;

import com.example.tintok.DataLayer.DataRepository_MatchingPeople;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import retrofit2.http.Body;

public class PeopleFilterRequest {
    @SerializedName("name")
    public String name;
    @SerializedName("age")
    ArrayList<Integer> age;
    @SerializedName("interests")
    ArrayList<Integer> interests;
    @SerializedName("sex")
    ArrayList<Integer> sex;

    public PeopleFilterRequest(String name, ArrayList<Integer> age, ArrayList<Integer> interests, ArrayList<Integer> sex) {
        this.name = name;
        this.age = age;
        this.interests = interests;
        this.sex = sex;
    }


    public static PeopleFilterRequest fromFilterState(DataRepository_MatchingPeople.Filter e){

        ArrayList<Integer> age = new ArrayList<>();
        age.add(e.getMinAge());
        age.add(e.getMaxAge());
        ArrayList<Integer> interests = new ArrayList<>();
        for(int i = 0 ; i < e.getInterestBitmap().length;i++)
            if(e.getInterestBitmap()[i])
                interests.add(i);
        ArrayList<Integer> sex = new ArrayList<>();
        switch (e.getGender()){
            case MALE:
                sex.add(1);
                break;
            case FEMALE:
                sex.add(2);
                break;
            case DIVERS:
                sex.add(3);
                break;
            default:
                sex.add(1);
                sex.add(2);
                sex.add(3);
                break;
        }
        return new PeopleFilterRequest(e.getFilterName(), age, interests, sex);
    }
}
