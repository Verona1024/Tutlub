package com.verona1024.tutlub.fragments.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.interfaces.TabsStateListener;

import static com.facebook.FacebookSdk.getApplicationContext;

public class SupportAppFragment extends Fragment {
    private View view;
    private RewardedVideoAd mAd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate view.
        view = inflater.inflate(R.layout.fragment_support_app, container, false);

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

        mAd = MobileAds.getRewardedVideoAdInstance(getApplicationContext());
        mAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                ((Button)view.findViewById(R.id.buttonWatch)).setText(R.string.video_is_ready);
                view.findViewById(R.id.textViewNotification).setVisibility(View.VISIBLE);
            }

            @Override
            public void onRewardedVideoAdOpened() {
                Log.d("video ad", "onRewardedVideoAdOpened");
            }

            @Override
            public void onRewardedVideoStarted() {
                Log.d("video ad", "onRewardedVideoStarted");
            }

            @Override
            public void onRewardedVideoAdClosed() {
                loadRewardedVideoAd();
                Log.d("video ad", "onRewardedVideoAdClosed");
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                Log.d("video ad", "onRewarded");
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                Log.d("video ad", "onRewardedVideoAdLeftApplication");
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                if (i == 3){
                    ((Button)view.findViewById(R.id.buttonWatch)).setText(R.string.no_video);
                    view.findViewById(R.id.textViewNotification).setVisibility(View.GONE);
                }
                Log.d("video ad", "onRewardedVideoAdFailedToLoad + " + i);
            }
        });

        loadRewardedVideoAd();

        view.findViewById(R.id.buttonWatch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAd.isLoaded()) {
                    mAd.show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.video_not_ready, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void loadRewardedVideoAd() {
        if (mAd.isLoaded()) {
            ((Button)view.findViewById(R.id.buttonWatch)).setText(R.string.video_is_ready);
            view.findViewById(R.id.textViewNotification).setVisibility(View.VISIBLE);
        } else {
            mAd.loadAd(getString(R.string.video_ad_id), new AdRequest.Builder().build());
            ((Button)view.findViewById(R.id.buttonWatch)).setText(R.string.video_loading);
            view.findViewById(R.id.textViewNotification).setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
//        mAd.resume(getApplicationContext());
        super.onResume();
    }

    @Override
    public void onPause() {
//        mAd.pause(getApplicationContext());
        super.onPause();
    }

    @Override
    public void onDestroy() {
//        mAd.destroy(getApplicationContext());
        super.onDestroy();
    }
}
