package com.verona1024.tutlub.fragments.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.activities.main.EditUserInfoActivity;
import com.verona1024.tutlub.activities.main.SettingsActivity;
import com.verona1024.tutlub.fragments.main.profile.PersonFriendsFragment;
import com.verona1024.tutlub.fragments.main.profile.PersonPostsFragment;
import com.verona1024.tutlub.interfaces.TabsStateListener;
import com.verona1024.tutlub.utils.UserUtil;

public class ProfileFragmet extends Fragment {

    private ImageLoader imgloader = ImageLoader.getInstance();
    private TextView name, id, aboutMe, liveIn, education;
    private ImageView profileImage, wallpaper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (getActivity() instanceof TabsStateListener){
                        ((TabsStateListener) getActivity()).setFirstTab();
                    }
                    return true;
                }

                return false;
            }
        });

        wallpaper = (ImageView) view.findViewById(R.id.imageViewWallpaper);
        profileImage = (ImageView) view.findViewById(R.id.imageViewProfileImage);
        name = (TextView) view.findViewById(R.id.textViewName);
        id = (TextView) view.findViewById(R.id.textViewId);
        aboutMe = (TextView) view.findViewById(R.id.textViewAboutMe);
        liveIn = (TextView) view.findViewById(R.id.textViewLiveIn);
        education = (TextView) view.findViewById(R.id.textViewStudyIn);
        ImageView imageViewSettings = (ImageView) view.findViewById(R.id.imageViewSettings);
        imageViewSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.buttonEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditUserInfoActivity.class);
                startActivity(intent);
            }
        });

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        name.setText(UserUtil.name);
        id.setText(UserUtil.userId);
        aboutMe.setText(UserUtil.description);
        liveIn.setText(UserUtil.country);
        education.setText(UserUtil.education);
        wallpaper.setVisibility(View.GONE);
        imgloader.displayImage(UserUtil.cover, wallpaper, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                wallpaper.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
        imgloader.displayImage(UserUtil.picture, profileImage);
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return PersonPostsFragment.newInstance(0);
            } else if (position == 1){
                return PersonPostsFragment.newInstance(1);
            } else if (position == 2){
                return PersonFriendsFragment.newInstance();
            }

            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_supplication);
                case 1:
                    return getString(R.string.tab_posts);
                case 2:
                    return getString(R.string.tab_friends);
            }
            return null;
        }
    }
}
