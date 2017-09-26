package com.verona1024.tutlub.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
import com.verona1024.tutlub.activities.main.PersonProfileActivity;
import com.verona1024.tutlub.interfaces.TabsStateListener;
import com.verona1024.tutlub.models.FriendsInvites;
import com.verona1024.tutlub.models.SuggestedPeople;
import com.verona1024.tutlub.utils.FriendsUtil;
import com.verona1024.tutlub.utils.RankUtil;
import com.verona1024.tutlub.utils.RequestUtil;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.facebook.FacebookSdk.getApplicationContext;

public class FriendsFragment extends Fragment {
    private LinearLayout linearLayoutInvites, linearLayoutSuggests;
    private ArrayList<SuggestedPeople> suggestedPeoples;
    private ArrayList<FriendsInvites> invitedPeoples;
    private ImageLoader imgloader = ImageLoader.getInstance();
    private View view;
    private boolean showAllSuggested = false;
    private boolean showAllInvited = false;
    private AdView mAdView;

    @Override
    public void onResume() {
        super.onResume();
        Log.e("ads help", "onResume");
    }

    @Override
    public void onPause() {
        Log.e("ads help", "onPause");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.e("ads help", "onDestroy");
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate view.
        view = inflater.inflate(R.layout.fragment_friends, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.e("key down", "friends");
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (event.getAction() != KeyEvent.ACTION_DOWN) {
                        return true;
                    }
                    if (getActivity() instanceof TabsStateListener){
                        ((TabsStateListener) getActivity()).setFirstTab();
                    }
                    return true;
                }

                return false;
            }
        });

        linearLayoutSuggests = (LinearLayout) view.findViewById(R.id.linearSuggested);
        linearLayoutInvites = (LinearLayout) view.findViewById(R.id.linearInvites);

        mAdView = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.setVisibility(View.GONE);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.e("Ads", "onAdLoaded");
                mAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.e("Ads", "onAdFailedToLoad " + errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.e("Ads", "onAdOpened");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.e("Ads", "onAdLeftApplication");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
                Log.e("Ads", "onAdClosed");
            }
        });
        mAdView.loadAd(adRequest);

        final TextView textViewShowMoreSuggested = (TextView) view.findViewById(R.id.textViewShowMoreSuggested);
        textViewShowMoreSuggested.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showAllSuggested){
                    textViewShowMoreSuggested.setText(R.string.show_more);
                } else {
                    textViewShowMoreSuggested.setText(R.string.show_less);
                }
                showAllSuggested = !showAllSuggested;
                showSuggested();
            }
        });

        final TextView textViewShowMoreInvited = (TextView) view.findViewById(R.id.textViewShowMoreInvites);
        textViewShowMoreInvited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showAllInvited){
                    textViewShowMoreInvited.setText(R.string.show_more);
                } else {
                    textViewShowMoreInvited.setText(R.string.show_less);
                }
                showAllInvited = !showAllInvited;
                showInvited();
            }
        });

        view.findViewById(R.id.buttonInvite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.join_on_tutlub));
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        showSuggested();
        showInvited();
        getSuggestedAndInvited();

        return view;
    }

    private void getSuggestedAndInvited(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TutLubProvider.BASE_URL)
                .build();
        TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

        Call<ResponseBody> call =  trackerInternetProvider.suggested(RequestUtil.OAUTH_HEADER);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                try {
                    String body;
                    if (response.body() == null){
                        body = response.errorBody().string();
                    } else {
                        body = response.body().string();

                        FriendsUtil.setSuggestedFriends(body);
                        showSuggested();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getSuggestedAndInvited();
                            }
                        }, 10 * 1000);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });


        Retrofit retrofit1 = new Retrofit.Builder()
                .baseUrl(TutLubProvider.BASE_URL)
                .build();
        TutLubProvider trackerInternetProvider1 = retrofit1.create(TutLubProvider.class);

        Call<ResponseBody> call1 =  trackerInternetProvider1.getFriends(RequestUtil.OAUTH_HEADER);
        call1.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                try {
                    String body;
                    if (response.body() == null){
                        body = response.errorBody().string();
                    } else {
                        body = response.body().string();

                        FriendsUtil.setPendingFriends(body);
                        showInvited();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void showSuggested(){
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        linearLayoutSuggests.removeAllViews();

        int i = 0;
        for (final SuggestedPeople suggestedPeople : FriendsUtil.mySuggestedFriends){
            View view = inflater.inflate(R.layout.item_suggest_friend, null);
            imgloader.displayImage(suggestedPeople.picture, (ImageView) view.findViewById(R.id.profile_image));

            view.findViewById(R.id.profile_image).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), PersonProfileActivity.class);
                    intent.putExtra(PersonProfileActivity.PERSON_ID, "" + suggestedPeople.peopleId);
                    startActivity(intent);
                }
            });

            ((TextView)view.findViewById(R.id.textViewName)).setText(suggestedPeople.name);
            view.findViewById(R.id.imageViewAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(TutLubProvider.BASE_URL)
                            .build();
                    TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                    Call<ResponseBody> call =  trackerInternetProvider.acceptFriend(("" + suggestedPeople.peopleId) ,RequestUtil.OAUTH_HEADER);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                            getSuggestedAndInvited();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            });
            linearLayoutSuggests.addView(view);

            if (++i == 2 && !showAllSuggested){
                return;
            }
        }
    }

    private void showInvited(){
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        linearLayoutInvites.removeAllViews();
        int i = 0;
        for (final FriendsInvites friendsInvites : FriendsUtil.myInvitedFriends){
            View view = inflater.inflate(R.layout.item_invite_friend,null);
            imgloader.displayImage(friendsInvites.picture, (ImageView) view.findViewById(R.id.profile_image));
            view.findViewById(R.id.profile_image).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), PersonProfileActivity.class);
                    intent.putExtra(PersonProfileActivity.PERSON_ID, "" + friendsInvites.peopleId);
                    startActivity(intent);
                }
            });
            ((TextView)view.findViewById(R.id.textViewName)).setText(friendsInvites.name);
            ((TextView)view.findViewById(R.id.textViewType)).setText(RankUtil.getNameByPoints(getApplicationContext(), friendsInvites.points) + ", ");
            ((TextView)view.findViewById(R.id.textViewCountry)).setText(friendsInvites.country);
            view.findViewById(R.id.imageViewAccept).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(TutLubProvider.BASE_URL)
                            .build();
                    TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                    Call<ResponseBody> call =  trackerInternetProvider.acceptFriend(("" + friendsInvites.peopleId) ,RequestUtil.OAUTH_HEADER);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                            getSuggestedAndInvited();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            });
            view.findViewById(R.id.imageViewRefuse).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(TutLubProvider.BASE_URL)
                            .build();
                    TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                    Call<ResponseBody> call =  trackerInternetProvider.declineFriend(("" + friendsInvites.peopleId) ,RequestUtil.OAUTH_HEADER);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                            getSuggestedAndInvited();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            });
            linearLayoutInvites.addView(view);

            if (++i == 2 && !showAllInvited){
                return;
            }
        }
    }
}
