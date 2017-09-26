package com.verona1024.tutlub.activities.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
import com.verona1024.tutlub.dialogs.MakingPostDialog;
import com.verona1024.tutlub.fragments.main.FriendsFragment;
import com.verona1024.tutlub.fragments.main.HomeFragment;
import com.verona1024.tutlub.fragments.main.NewsFragment;
import com.verona1024.tutlub.fragments.main.ProfileFragmet;
import com.verona1024.tutlub.fragments.main.ScoreboardFragment;
import com.verona1024.tutlub.fragments.main.SupportAppFragment;
import com.verona1024.tutlub.interfaces.TabsManager;
import com.verona1024.tutlub.interfaces.TabsStateListener;
import com.verona1024.tutlub.models.FriendObject;
import com.verona1024.tutlub.models.FriendsInvites;
import com.verona1024.tutlub.models.NotificationObject;
import com.verona1024.tutlub.services.RegistrationIntentService;
import com.verona1024.tutlub.utils.FriendsUtil;
import com.verona1024.tutlub.utils.RequestUtil;
import com.verona1024.tutlub.utils.UserUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;


public class MainActivity extends AppCompatActivity implements MakingPostDialog.NoticeDialogListener, TabsStateListener {
    public static final int REQUEST_CODE = 1;
    public static final int RESULT_CODE_OPEN_NEWS = 2;
    public static final int RESULT_CODE_DO_NOTHING = 3;
    private Fragment fragmentFriends, fragmentHome, fragmentProfile, fragmentNews, fragmentScoreboard, fragmentSuppostAd;
    private Fragment fragmentCurrent;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private BottomNavigationView navigation;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;

    private TextView notification;

    // Bottom Navigation View listener
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setFragment(fragmentHome);
                    return true;
                case R.id.navigation_scoreboard:
                    setFragment(fragmentScoreboard);
                    return true;
                case R.id.navigation_news:
//                    setFragment(fragmentNews);
                    setFragment(fragmentSuppostAd);
                    return true;
                case R.id.navigation_profile:
                    setFragment(fragmentProfile);
                    return true;
                case R.id.navigation_friends:
                    setFragment(fragmentFriends);
                    return true;
            }
            return false;
        }

    };

    @Override
    public void onDialogShareClick() {
        if (fragmentCurrent != null && fragmentCurrent instanceof MakingPostDialog.NoticeDialogListener){
            ((MakingPostDialog.NoticeDialogListener)fragmentCurrent).onDialogShareClick();
        }
    }

    @Override
    public void setFirstTab() {
        navigation.setSelectedItemId(R.id.navigation_home);
    }

    public static class BottomNavigationViewHelper {
        public static void disableShiftMode(BottomNavigationView view) {
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
            try {
                Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
                shiftingMode.setAccessible(true);
                shiftingMode.setBoolean(menuView, false);
                shiftingMode.setAccessible(false);
                for (int i = 0; i < menuView.getChildCount(); i++) {
                    BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                    // set once again checked value, so view will be updated
                    //noinspection RestrictedApi
                    item.setChecked(item.getItemData().isChecked());
                }
            } catch (Exception e) {
                Log.e("BNVHelper", "Unable to get shift mode field", e);
            }
        }
    }

    private void setFragment(final Fragment fragment){
        // Fragment should be created && shouldn't be added.
        if (fragment != null && !fragment.isAdded()) {

            // Check that the activity is using the layout version with the fragment_container.
            if (findViewById(R.id.frame_content) != null) {

                try {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            // Begin the transaction.
                            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                            // Replace whatever is in the fragment_container view with this fragment.
                            transaction.replace(R.id.frame_content, fragment);

                            // Commit the transaction.
                            transaction.commit();

                            fragmentCurrent = fragment;
                        }
                    });
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        } else if (fragment != null && fragment.isAdded() && fragment instanceof TabsManager){
            ((TabsManager) fragment).goToTheTop();
        }
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter("REGISTRATION_COMPLETE"));
            isReceiverRegistered = true;
        }
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentFriends = new FriendsFragment();
        fragmentHome = new HomeFragment();
        fragmentProfile = new ProfileFragmet();
        fragmentNews = new NewsFragment();
        fragmentScoreboard = new ScoreboardFragment();
        fragmentSuppostAd = new SupportAppFragment();

        // Init bottom menu
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_home);

        notification = (TextView) findViewById(R.id.textViewNotification);
        if (UserUtil.getNotificationCount() != 0){
            notification.setText("" + UserUtil.getNotificationCount());
            notification.setVisibility(View.VISIBLE);
        } else {
            notification.setVisibility(View.GONE);
        }

        findViewById(R.id.imageViewNotifications).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UserUtil.getShowMobileNotifications(getApplicationContext())) {
                    Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });

        findViewById(R.id.imageViewSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.editTextSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean("SENT_TOKEN_TO_SERVER", false);
                if (sentToken) {
                    Log.e(TAG, "gcm_send_message");
                } else {
                    Log.e(TAG, "token_error_message");
                }
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
        if (UserUtil.getNotificationCount() != 0 && UserUtil.getShowMobileNotifications(getApplicationContext())){
            notification.setText("" + UserUtil.getNotificationCount());
            notification.setVisibility(View.VISIBLE);
        } else {
            notification.setVisibility(View.GONE);
        }
        requestData();
        getNotifications();
    }

    private void getNotifications() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TutLubProvider.BASE_URL)
                .build();
        TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

        Call<ResponseBody> call = trackerInternetProvider.notifications(UserUtil.user_id, RequestUtil.OAUTH_HEADER);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                try {
                    String body;
                    if (response.body() == null){
                        body = response.errorBody().string();
                    } else {
                        body = response.body().string();
                    }

                    ArrayList<NotificationObject> notificationObjects = new ArrayList<NotificationObject>();
                    JSONObject jsonObject = new JSONObject(body);
                    JSONArray jsonArray = jsonObject.getJSONArray("notifications");
                    for (int i = 0; i < jsonArray.length(); i++){
                        try {
                            JSONObject object = jsonArray.getJSONObject(i);
                            NotificationObject notificationObject = new NotificationObject();
                            notificationObject.image = object.getString("image");
                            notificationObject.message = object.getString("message");
                            notificationObject.when = object.getString("when");
                            notificationObject.postid = object.getLong("postid");
                            notificationObject.seen = object.getBoolean("seen");
                            notificationObjects.add(notificationObject);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    UserUtil.notifications = notificationObjects;
                    notification = (TextView) findViewById(R.id.textViewNotification);
                    if (UserUtil.getNotificationCount() != 0){
                        notification.setText("" + UserUtil.getNotificationCount());
                        notification.setVisibility(View.VISIBLE);
                    } else {
                        notification.setVisibility(View.GONE);
                    }
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
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE){
            if (resultCode == RESULT_CODE_OPEN_NEWS){
//                setFragment(fragmentNews);
            }
        }
    }

    private void requestData(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TutLubProvider.BASE_URL)
                .build();
        TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

        Call<ResponseBody> call = trackerInternetProvider.getFriends(RequestUtil.OAUTH_HEADER);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                try {
                    String body;
                    if (response.body() == null){
                        body = response.errorBody().string();
                    } else {
                        body = response.body().string();
                    }

                    ArrayList<FriendObject> friendObjects = new ArrayList<FriendObject>();
                    JSONObject jsonObject = new JSONObject(body);
                    JSONArray jsonArray = jsonObject.getJSONObject("friends").getJSONArray("friends");
                    for (int i = 0; i < jsonArray.length(); i++){
                        try {
                            JSONObject object = jsonArray.getJSONObject(i);
                            FriendObject friendObject = new FriendObject();
                            friendObject.friendId = object.getString("id");
                            friendObject.name = object.getString("name");
                            friendObject.picture = object.getString("picture");
                            friendObject.country = object.getString("country") == null ? "" : object.getString("country");
                            friendObject.city = object.getString("city") == null ? "" : object.getString("city");
                            friendObject.points = object.getInt("points");
                            friendObjects.add(friendObject);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    FriendsUtil.myFriendObjects = friendObjects;
                } catch (IOException | JSONException e) {
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

                        JSONObject responseJson = new JSONObject(body);
                        JSONArray pendingInvitations = responseJson.getJSONObject("friends").getJSONArray("pendingInvitations");
                        ArrayList<FriendsInvites> invitedPeoples = new ArrayList<>();
                        for(int i=0; i < pendingInvitations.length(); i++) {
                            try {
                                FriendsInvites friendsInvites = new FriendsInvites();
                                friendsInvites.peopleId = pendingInvitations.getJSONObject(i).getString("id");
                                friendsInvites.name = pendingInvitations.getJSONObject(i).getString("name");
                                friendsInvites.picture = pendingInvitations.getJSONObject(i).getString("picture");
                                friendsInvites.country = pendingInvitations.getJSONObject(i).getString("country");
                                friendsInvites.city = pendingInvitations.getJSONObject(i).getString("city");
                                friendsInvites.points = pendingInvitations.getJSONObject(i).getInt("points");
                                invitedPeoples.add(friendsInvites);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        FriendsUtil.myInvitedFriends = invitedPeoples;
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
}
