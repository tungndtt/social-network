package com.example.tintok.DataLayer;

/**
 * A ResponseEvent can be exactly used once.
 * It is normally hold into LiveData and can be distinguished by type.
 * Contains the response message or rather https-status codes of the server.
 * Is used to give an LiveData-observer in a  fragment feedback if the request was successful or not.
 * based on SingleLiveEvent from Jose Alcerreca
 * https://gist.github.com/JoseAlcerreca/5b661f1800e1e654f07cc54fe87441af#file-event-kt
 */
public class ResponseEvent {

    private String response;
    private Type type;
    private boolean hasBeenHandled = false;

    /**
     * constructor
     * @param type of event that needs to be distinguished by observer
     * @param message http status code
     */
    public ResponseEvent(Type type, String message){
        this.type = type;
        this.response = message;
    }

    /**
     * returns the content and prevents its use again
     * @return null if ResponseEvent already has been handled or http status code if not
     */
    public String getContentIfNotHandled(){
        if(hasBeenHandled)
            return null;
        else {
            hasBeenHandled = true;
            return response;
        }
    }
    public Type getType(){
        return type;
    }

    public String peekContent(){
        return response;
    }

    public enum Type {PASSWORD, USER_UPDATE, PROFILE_PICTURE_UPDATE, PROFILE_PICTURE_UPLOAD, INTEREST_UPDATE};

}
