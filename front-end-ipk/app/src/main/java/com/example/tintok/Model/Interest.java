package com.example.tintok.Model;

/**
 * This class represents an interest that can be chosen by the user.
 */
public class Interest {

    private int id;
    private int imageResource;
    private String interest;
    private boolean isSelected;

    /**
     * Constructor
     * @param id unique identifier for an interest
     * @param imageResource url to set icon from resource
     * @param interest name of interest
     */
    public Interest(int id, int imageResource, String interest) {
        this.id = id;
        this.imageResource = imageResource;
        this.interest = interest;
        this.isSelected = false;
    }

    public boolean isSelected() {
        return isSelected;
    }
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public int getImageResource() {
        return imageResource;
    }
    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
    public String getInterest() {
        return interest;
    }
    public void setInterest(String interest) {
        this.interest = interest;
    }
    public int getId(){
        return id;
    }
}
