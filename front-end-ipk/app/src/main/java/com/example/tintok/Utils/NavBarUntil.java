package com.example.tintok.Utils;

import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavBarUntil {
    public static void removeItemsUnderline(BottomNavigationView bottomNavigationView) {
        for (int i = 0; i <  bottomNavigationView.getMenu().size(); i++) {
            MenuItem item = bottomNavigationView.getMenu().getItem(i);
            item.setTitle(item.getTitle().toString());
        }
    }

    public static void underlineMenuItem(MenuItem item) {
        SpannableString newTitle = new SpannableString(item.getTitle());
        newTitle.setSpan(new UnderlineSpan(), 0, newTitle.length(), 0);
        item.setTitle(newTitle);

    }
}
