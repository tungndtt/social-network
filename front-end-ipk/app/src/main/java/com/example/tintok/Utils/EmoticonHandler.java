package com.example.tintok.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.Pair;
import android.widget.EditText;

import java.io.IOException;
import java.util.ArrayList;

public class EmoticonHandler implements TextWatcher {
    EditText mEditText;
    Context mContext;
    private final ArrayList<ImageSpan> mEmoticonsToRemove = new ArrayList<ImageSpan>();
    private ArrayList<Pair<ImageSpan, String>> spanInsMSg = new ArrayList<>();

    public EmoticonHandler(Context context, EditText text) {
        this.mEditText = text;
        this.mContext = context;
    }

    public static SpannableStringBuilder parseMessageFromString(Context mContext, String message) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int start = 0;
        int end = 0;
        int previousSpanPos = 0;
        for (int i = 0; i < message.length(); i++) {
            if (message.charAt(i) == '$')
                start = i;
            else if (message.charAt(i) == '}') {
                end = i;
                String emoji = message.substring(start + 2, end);
                String beforespan = message.substring(previousSpanPos, start);
                Drawable drawable = null;
                try {
                    drawable = Drawable.createFromStream(mContext.getAssets().open("Emojis/"+emoji), null);
                    drawable.setBounds(0, 0, 60, 60);
                    ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
                    builder.append(beforespan).append(" ");
                    builder.setSpan(span, builder.length() - 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.append(" ");
                    previousSpanPos = end + 1;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (i == message.length() - 1)
                builder.append(message.substring(previousSpanPos, i + 1));
        }
        return builder;
    }

    public Pair<String, SpannableStringBuilder> parseMessage() {
        Editable message = mEditText.getEditableText();
        for (UnderlineSpan span : message.getSpans(0, message.length(), UnderlineSpan.class))
            message.removeSpan(span);
        SpannableStringBuilder builder = new SpannableStringBuilder(message);

        for (Pair<ImageSpan, String> icon : spanInsMSg) {
            ImageSpan img = icon.first;
            String imgName = icon.second;
            int start = message.getSpanStart(img);
            int end = message.getSpanEnd(img);
            message.removeSpan(img);
            message.replace(start, end, "${" + imgName + "}");
        }
        spanInsMSg.clear();
        return new Pair(message.toString(), builder);
    }

    //replace the text within start and and with img speicify by resource, speci
    public void insertEmoji(int start, int end, String resource) {
        Drawable drawable = null;
        try {
            drawable = Drawable.createFromStream(mContext.getAssets().open("Emojis/"+resource), null);
            drawable.setBounds(0, 0, 60, 60);
            ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
            String placeholder = "  [img]  ";
            Editable msg = mEditText.getEditableText();
            msg.replace(start, end, placeholder);
            msg.setSpan(span, start + 1, start + placeholder.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanInsMSg.add(new Pair(span, resource));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertEmoji(String resource) {
        int start = mEditText.getSelectionStart();
        int end = mEditText.getSelectionEnd();
        this.insertEmoji(start, end, resource);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (count > 0) {
            int end = start + count;
            Editable message = mEditText.getEditableText();
            ImageSpan[] list = message.getSpans(start, end, ImageSpan.class);

            for (ImageSpan span : list) {
                int spanStart = message.getSpanStart(span);
                int spanEnd = message.getSpanEnd(span);
                if ((spanStart < end) && (spanEnd > start)) {
                    mEmoticonsToRemove.add(span);
                }
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        Editable message = mEditText.getEditableText();

        // Commit the emoticons to be removed.
        for (ImageSpan span : mEmoticonsToRemove) {
            int start = message.getSpanStart(span);
            int end = message.getSpanEnd(span);
            // Remove the span
            message.removeSpan(span);
            spanInsMSg.remove(span);

            // Remove the remaining emoticon text.
            if (start != end) {
                message.delete(start, end);
            }
        }
        mEmoticonsToRemove.clear();
    }
}