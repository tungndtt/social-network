package com.example.tintok;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tintok.Adapters_ViewHolder.ImagePage2Adapter;
import com.example.tintok.Model.Post;

/**
 * Shows all uploaded posts and profile pictures from another user.
 * Allows the user to click on an image and let it show in another fragment
 */
public class ViewProfile_UserImages_Fragment extends Fragment {


    private ImagePage2Adapter adapter;
    private ViewPager2 vp2;
    final int offScreenPageLimit = 3;
    private View view;
    private Activity_ViewProfile_ViewModel mViewModel;
    private View_Profile_Picture_Fragment  viewProfilePictureFragment;


    public  ViewProfile_UserImages_Fragment(){
    }
    public static ViewProfile_UserImages_Fragment getInstance(){
        ViewProfile_UserImages_Fragment fragment = new  ViewProfile_UserImages_Fragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("INFO", "Creating new profile image...");
    }

    /**
     * Inflates the layout for this fragment.
     * Initialization of views
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("INFO", "Creating view for displaying photo profile ...");
        view = inflater.inflate(R.layout.profile_image_fragment, container, false);
        vp2 = view.findViewById(R.id.profile_image_list_page);
        return view;
    }


    /**
     * Instantiation of Activity_ViewProfile_ViewModel if it is null and ImagePage2Adapter.
     * Shows all uploaded content in a ViewPager2.
     * The user can click on an item to show the item at View_Profile_Picture_Fragment
     * @see View_Profile_Picture_Fragment
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
       // super.onStart();
        Log.e("IMAGE", "ON ACTIVITY");
        if (mViewModel == null)
            mViewModel = new ViewModelProvider(getActivity()).get(Activity_ViewProfile_ViewModel.class);
        Log.e("viewmodel", mViewModel.toString());

        /*
            Setting up ImagePage2Adapter with all available content of the other user
            and an onImageClickListener to show to picture if the user clicks on it
         */
        adapter = new ImagePage2Adapter(this.getContext(), mViewModel.getPosts().getValue());
        adapter.setOnImageClickListener(url -> {
            if(viewProfilePictureFragment == null)
                viewProfilePictureFragment = new View_Profile_Picture_Fragment();
            Bundle bundle = new Bundle();
            bundle.putString("name", mViewModel.getProfile().getValue().getUserName());
            String status = "";
            for(Post p:  mViewModel.getPosts().getValue()){
                if(p.getImage().url.equals(url))
                    status = p.getStatus();
            }
            bundle.putString("status", status);
            bundle.putString("url", url);
            viewProfilePictureFragment.setArguments(bundle);
            viewProfilePictureFragment.show(getActivity().getSupportFragmentManager(), "VIEW_PROFILE_PICTURE");
        });

        vp2.setAdapter(adapter);
        vp2.setOffscreenPageLimit(offScreenPageLimit);
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(10));
        transformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                /*Log.e("transform: at" + page.getId() , String.valueOf(position));
                float x = 1-Math.abs(position);
                page.setScaleY( 0.8f+x*0.2f);*/
                int pageWidth = page.getWidth();
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
        vp2.setPageTransformer(transformer);

    }




}
