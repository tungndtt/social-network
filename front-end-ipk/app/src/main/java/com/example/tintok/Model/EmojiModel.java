package com.example.tintok.Model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class represents a Emoji.
 * Existing of name and drawable
 */
public class EmojiModel {

    private final String resourceImgName;
    private final Drawable resource;

    public EmojiModel(String resourceImgName, Drawable resource) {
        this.resourceImgName = resourceImgName;
        this.resource = resource;
    }

    public String getResourceImgName() {
        return resourceImgName;
    }
    public Drawable getResource() {
        return resource;
    }

    /**
     * Loads all Emoji from assets folder into ArrayList
     * @param context
     * @return ArrayList of all available Emojis at assets folder
     */
    public static ArrayList<EmojiModel> getEmojis(Context context){
        ArrayList<EmojiModel> emojis = new ArrayList<>();
        String dataname = "emoticon (";
        Drawable emoji = null;
        final int numEmoji = 3248;
        int i = 1;
        do {
            String imgName = dataname + i+").png";
            try {
                emoji = Drawable.createFromStream(context.getAssets().open("Emojis/"+imgName), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (emoji == null)
                continue;
            emojis.add(new EmojiModel(imgName, emoji));
            i++;
        } while (i <= numEmoji);
        return emojis;
    }
}
