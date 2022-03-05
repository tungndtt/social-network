package com.example.tintok;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tintok.Adapters_ViewHolder.BaseAdapter;
import com.example.tintok.Adapters_ViewHolder.PeopleBrowsingAdapter;


import java.util.ArrayList;

//TODO: DELETE?
public class StackViewPager {
    ViewPager2 viewPager2;
    int offScreenPageLimit;
    public void initFragment(){
       // viewPager2 = getView().findViewById(R.id.viewPager);

       // BaseAdapter adapter = new PeopleBrowsingAdapter(this.getContext(), models);
       // viewPager2.setAdapter(adapter);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager2.setOverScrollMode(ViewPager2.OVER_SCROLL_IF_CONTENT_SCROLLS);
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(10));
        transformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                /*Log.e("transform: at" + page.getId() , String.valueOf(position));
                float x = 1-Math.abs(position);
                page.setScaleY( 0.8f+x*0.2f);*/
                int pageWidth = page.getWidth();
                Log.e("pagewithd by "+position, "at "+(-pageWidth/1.2f *position));
                page.setElevation(-Math.abs(position));

                float DEFAULT_TRANSLATION_X = .0f;
                float DEFAULT_TRANSLATION_FACTOR = 1.2f;

                float SCALE_FACTOR = .14f;
                float DEFAULT_SCALE = 1f;

                float ALPHA_FACTOR = .3f;
                float DEFAULT_ALPHA = 1f;

                float scaleFactor = -SCALE_FACTOR * position + DEFAULT_SCALE;
                float alphaFactor = -ALPHA_FACTOR * position + DEFAULT_ALPHA;
                if(position<=0){
                    page.setTranslationX(DEFAULT_TRANSLATION_X);
                    page.setScaleX(DEFAULT_SCALE);
                    page.setScaleY(DEFAULT_SCALE);
                    page.setAlpha(DEFAULT_ALPHA+position);
                }
                else if(position <= offScreenPageLimit ){
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);
                    page.setTranslationX(-pageWidth/DEFAULT_TRANSLATION_FACTOR*position);
                    page.setAlpha(alphaFactor);
                }
                else{
                    page.setTranslationX(DEFAULT_TRANSLATION_X);
                    page.setScaleX(DEFAULT_SCALE);
                    page.setScaleY(DEFAULT_SCALE);
                    page.setAlpha(DEFAULT_ALPHA);
                }
            }
        });
        viewPager2.setPageTransformer(transformer);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        });
        viewPager2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }

        });
    }
}
