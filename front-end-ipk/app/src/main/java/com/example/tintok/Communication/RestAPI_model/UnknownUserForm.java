package com.example.tintok.Communication.RestAPI_model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class UnknownUserForm {
    @SerializedName("username")
    private String username;
    @SerializedName("email")
    private String email;
    @SerializedName("password")
    private String password;
    @SerializedName("birthday")
    private String birthday;
    @SerializedName("sex")
    private int gender;
    @SerializedName("age")
    private int age;
    @SerializedName("new_password")
    private String new_password;

    @SerializedName("interests")
    private ArrayList<Integer> interests;

    public UnknownUserForm(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername(){
        return this.username;
    }
    public String getEmail() {
        return email;
    }
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    public String getBirthday() {
        return birthday;
    }
    public int getGender() {
        return gender;
    }
    public void setGender(int gender) {
        this.gender = gender;
    }
    public void setNew_password(String new_password) {
        this.new_password = new_password;
    }

    public ArrayList<Integer> getInterests() {
        return interests;
    }

    public void setInterests(ArrayList<Integer> interests) {
        this.interests = interests;
    }
    public int getAge() {
        return age;
    }
}
