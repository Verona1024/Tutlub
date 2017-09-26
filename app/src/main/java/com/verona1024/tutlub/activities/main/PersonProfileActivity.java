package com.verona1024.tutlub.activities.main;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
import com.verona1024.tutlub.fragments.main.profile.PersonFriendsFragment;
import com.verona1024.tutlub.fragments.main.profile.PersonPostsFragment;
import com.verona1024.tutlub.utils.FriendsUtil;
import com.verona1024.tutlub.utils.PostsUtil;
import com.verona1024.tutlub.utils.RankUtil;
import com.verona1024.tutlub.utils.RequestUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class PersonProfileActivity extends AppCompatActivity {
    public static final String PERSON_ID = "person_id";
    private ImageLoader imgloader = ImageLoader.getInstance();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("");

        final ImageView imageViewProfile = (ImageView) findViewById(R.id.imageViewProfileImage);
        final ImageView imageViewWallpaper = (ImageView) findViewById(R.id.imageViewWallpaper);
        final ImageView imageViewFollowing = (ImageView) findViewById(R.id.imageViewFollowing);
        final ImageView imageViewFriends = (ImageView) findViewById(R.id.imageViewFriends);
        final ImageView imageViewRank = (ImageView) findViewById(R.id.imageViewRank);
        final TextView textViewName = (TextView) findViewById(R.id.textViewName);
        final TextView textViewAbout = (TextView) findViewById(R.id.textViewAboutMe);
        final TextView textViewLivein = (TextView) findViewById(R.id.textViewLiveIn);
        final TextView textViewStudyIn = (TextView) findViewById(R.id.textViewStudyIn);
        final TextView textViewFollowing = (TextView) findViewById(R.id.textViewFollowing);
        final TextView textViewFriends = (TextView) findViewById(R.id.textViewFriends);
        imageViewWallpaper.setVisibility(View.GONE);

        if (FriendsUtil.isFollowingFriend(getIntent().getStringExtra(PERSON_ID))){
            imageViewFollowing.setImageResource(R.drawable.ic_following);
            textViewFollowing.setText("Following");
            textViewFriends.setText("Invite sent");
        } else {
            imageViewFollowing.setImageResource(R.drawable.ic_following_none);
            textViewFollowing.setText("Follow");
        }

        if (FriendsUtil.isFriend(getIntent().getStringExtra(PERSON_ID))){
            textViewFriends.setText("Unfriend");
            imageViewFollowing.setImageResource(R.drawable.ic_following);
            textViewFollowing.setText("Following");
        } else {
            textViewFriends.setText("Add friend");
        }

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        findViewById(R.id.linearFollowing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FriendsUtil.isFollowingFriend(getIntent().getStringExtra(PERSON_ID))) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(TutLubProvider.BASE_URL)
                            .build();
                    TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);
                    Call<ResponseBody> call;
                    call = trackerInternetProvider.unfollowFriend(getIntent().getStringExtra(PERSON_ID), RequestUtil.OAUTH_HEADER);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                            try {
                                String body;
                                if (response.body() == null) {
                                    body = response.errorBody().string();
                                    Log.e("123", body);
                                } else {
                                    body = response.body().string();
                                }

                                imageViewFollowing.setImageResource(R.drawable.ic_following_none);
                                textViewFollowing.setText("Follow");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            t.printStackTrace();
                            Log.e("123", "onFailure");
                        }
                    });
                }
            }
        });

        findViewById(R.id.linearAddFriend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FriendsUtil.isFriend(getIntent().getStringExtra(PERSON_ID))) {
                    new AlertDialog.Builder(new ContextThemeWrapper(PersonProfileActivity.this, R.style.myDialog))
                            .setMessage("Are you sure you want to remove " + textViewName.getText().toString() + " as your friend?")
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Retrofit retrofit = new Retrofit.Builder()
                                            .baseUrl(TutLubProvider.BASE_URL)
                                            .build();
                                    TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);
                                    Call<ResponseBody> call;
                                    call = trackerInternetProvider.unfriendFriend(getIntent().getStringExtra(PERSON_ID), RequestUtil.OAUTH_HEADER);
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                                            try {
                                                String body;
                                                if (response.body() == null) {
                                                    body = response.errorBody().string();
                                                    Log.e("123", body);
                                                } else {
                                                    body = response.body().string();
                                                }
                                                textViewFriends.setText("Add friend");
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable t) {
                                            t.printStackTrace();
                                            Log.e("123", "onFailure");
                                        }
                                    });
                                    finish();
                                }

                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                } else {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(TutLubProvider.BASE_URL)
                            .build();
                    TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);
                    Call<ResponseBody> call;
                    call = trackerInternetProvider.addFriend(getIntent().getStringExtra(PERSON_ID), RequestUtil.OAUTH_HEADER);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                            try {
                                String body;
                                if (response.body() == null) {
                                    body = response.errorBody().string();
                                    Log.e("123", body);
                                } else {
                                    body = response.body().string();
                                }
                                imageViewFollowing.setImageResource(R.drawable.ic_following);
                                textViewFollowing.setText("Following");
                                textViewFriends.setText("Unfriend");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            t.printStackTrace();
                            Log.e("123", "onFailure");
                        }
                    });
                }
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TutLubProvider.BASE_URL)
                .build();
        TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);
        Call<ResponseBody> call;
        call = trackerInternetProvider.getUserById(getIntent().getStringExtra(PERSON_ID), RequestUtil.OAUTH_HEADER);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                try {
                    String body;
                    if (response.body() == null){
                        body = response.errorBody().string();
                        Log.e("123", body);
                    } else {
                        body = response.body().string();
                    }


                    JSONObject jsonObject = new JSONObject(body);
                    imgloader.displayImage(jsonObject.getJSONObject("profile").getString("picture"), imageViewProfile);
                    imgloader.displayImage(jsonObject.getJSONObject("profile").getString("cover"), imageViewWallpaper, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            imageViewWallpaper.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    });
                    textViewName.setText(jsonObject.getJSONObject("profile").getString("name"));
                    imageViewRank.setImageResource(RankUtil.imageBigByPoints(jsonObject.getJSONObject("profile").getInt("points")));
                    textViewStudyIn.setText(jsonObject.getJSONObject("profile").getString("education"));
                    textViewLivein.setText(jsonObject.getJSONObject("profile").getString("city"));
                    textViewAbout.setText(jsonObject.getJSONObject("profile").getString("description"));
                    setTitle(jsonObject.getJSONObject("profile").getString("name"));
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        FriendsUtil.personFriendObjects = new ArrayList<>();
        PostsUtil.personSupplications = new ArrayList<>();
        PostsUtil.personPosts = new ArrayList<>();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return PersonPostsFragment.newInstance(0, getIntent().getStringExtra(PERSON_ID));
            } else if (position == 1){
                return PersonPostsFragment.newInstance(1, getIntent().getStringExtra(PERSON_ID));
            } else if (position == 2){
                return PersonFriendsFragment.newInstance(getIntent().getStringExtra(PERSON_ID));
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
                    return getString(R.string.label_supplication);
                case 1:
                    return getString(R.string.label_posts);
                case 2:
                    return getString(R.string.label_friends);
            }
            return null;
        }
    }
}
