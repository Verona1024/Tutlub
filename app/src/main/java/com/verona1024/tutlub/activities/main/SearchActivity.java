package com.verona1024.tutlub.activities.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
import com.verona1024.tutlub.fragments.main.search.SearchPeopleFragment;
import com.verona1024.tutlub.fragments.main.search.SearchPostsFragment;
import com.verona1024.tutlub.utils.RequestUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.verona1024.tutlub.utils.UserUtil.userId;

public class SearchActivity extends AppCompatActivity {

    private SearchPostsFragment searchPostsFragment;
    private SearchPeopleFragment searchPeopleFragment;

    public interface SearchShower{
        void onSearchUpdate(JSONArray jsonArray);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchPostsFragment = SearchPostsFragment.newInstance();
        searchPeopleFragment = SearchPeopleFragment.newInstance();
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        View newTab1 = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        ((TextView)newTab1.findViewById(R.id.textView)).setText("People"); //tab label txt
        ((ImageView)newTab1.findViewById(R.id.imageView)).setImageResource(R.drawable.ic_people_active);
        View newTab2 = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        ((TextView)newTab2.findViewById(R.id.textView)).setText("Post"); //tab label txt
        ((ImageView)newTab2.findViewById(R.id.imageView)).setImageResource(R.drawable.ic_post_active);
        tabLayout.getTabAt(0).setCustomView(newTab1);
        tabLayout.getTabAt(1).setCustomView(newTab2);

        final EditText editText = (EditText) findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(TutLubProvider.BASE_URL)
                        .build();
                TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);
                Call<ResponseBody> call;
                Log.e("user id", userId);
                call = trackerInternetProvider.search(editable.toString(), RequestUtil.OAUTH_HEADER);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                        try {
                            String body;
                            if (response.body() == null) {
                                body = response.errorBody().string();
                            } else {
                                body = response.body().string();
                            }
                            Log.e("1", body);

                            searchPostsFragment.onSearchUpdate(new JSONObject(body).getJSONArray("posts"));
                            searchPeopleFragment.onSearchUpdate(new JSONObject(body).getJSONArray("peoples"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {

                    }
                });
            }
        });

        findViewById(R.id.imageViewClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
            }
        });
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return searchPeopleFragment;
            } else if (position == 1) {
                return searchPostsFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.label_people);
                case 1:
                    return getString(R.string.label_posts);
            }
            return null;
        }
    }
}
