package com.verona1024.tutlub.activities.feed;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
import com.verona1024.tutlub.activities.main.MainActivity;
import com.verona1024.tutlub.dialogs.MessageDialog;
import com.verona1024.tutlub.utils.FriendsUtil;
import com.verona1024.tutlub.utils.RequestUtil;
import com.verona1024.tutlub.utils.UserUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class SplashActivity extends AppCompatActivity {
    TextView internetError;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        internetError = (TextView) findViewById(R.id.textViewInternetError);

        //Tutlub:
        MobileAds.initialize(getApplicationContext(), getString(R.string.ads));

        if (UserUtil.getUserLogin(getApplicationContext()).isEmpty() || UserUtil.getUserPass(getApplicationContext()).isEmpty()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, StartActivity.class));
                    finish();
                }
            }, 1500);
        } else {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(TutLubProvider.BASE_URL)
                    .build();
            TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

            String refreshedToken = FirebaseInstanceId.getInstance().getToken();

            Call<ResponseBody> call =  trackerInternetProvider.loginUser(UserUtil.getUserLogin(getApplicationContext()), UserUtil.getUserPass(getApplicationContext()), refreshedToken);
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
                        JSONObject jsonObject = new JSONObject(body);

                        try {
                            DialogFragment newFragment = MessageDialog.newInstance(jsonObject.getString("message"));
                            newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                        } catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "" + jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        }

                        if (jsonObject.getString("status").equals("success")){
                            RequestUtil.OAUTH_HEADER = jsonObject.getJSONObject("data").getString("token");
                            UserUtil.userId = jsonObject.getJSONObject("data").getJSONObject("user").getString("id");
                            UserUtil.user_id = jsonObject.getJSONObject("data").getJSONObject("user").getString("_id");
//                            Log.e("user _id", jsonObject.getJSONObject("data").getJSONObject("user").getString("_id"));
                            UserUtil.picture = jsonObject.getJSONObject("data").getJSONObject("user").getString("picture");
                            UserUtil.name = jsonObject.getJSONObject("data").getJSONObject("user").getString("name");
                            UserUtil.description = jsonObject.getJSONObject("data").getJSONObject("user").getString("description");
                            UserUtil.cover = jsonObject.getJSONObject("data").getJSONObject("user").getString("cover");
                            UserUtil.education = jsonObject.getJSONObject("data").getJSONObject("user").getString("education");
                            UserUtil.country = jsonObject.getJSONObject("data").getJSONObject("user").getString("country");
                            UserUtil.points = jsonObject.getJSONObject("data").getJSONObject("user").getInt("points");
                            FriendsUtil.setFriends(jsonObject.getJSONObject("data").getJSONObject("user").getJSONObject("friendsList"));
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(SplashActivity.this, StartActivity.class));
                            finish();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                    internetError.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
