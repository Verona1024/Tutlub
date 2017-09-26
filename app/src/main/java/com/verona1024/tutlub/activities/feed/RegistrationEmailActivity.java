package com.verona1024.tutlub.activities.feed;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
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

public class RegistrationEmailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_email);

        final EditText editTextEmail = (EditText) findViewById(R.id.editEmail);
        final EditText editTextPassword = (EditText) findViewById(R.id.editPassword);

        findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextEmail.getText().toString().isEmpty()){
                    try {
                        DialogFragment newFragment = MessageDialog.newInstance("Please, fill in all fields!");
                        newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                    } catch (IllegalStateException e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Please, fill in all fields!", Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                if (editTextPassword.getText().toString().isEmpty()){
                    try {
                        DialogFragment newFragment = MessageDialog.newInstance("Please, fill in all fields!");
                        newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                    } catch (IllegalStateException e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Please, fill in all fields!", Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                UserUtil.EMAIL = editTextEmail.getText().toString();
                UserUtil.PASSWORD = editTextPassword.getText().toString();

                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                Log.e("refreshedToken ", "" + refreshedToken);

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(TutLubProvider.BASE_URL)
                        .build();
                TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                Call<ResponseBody> call =  trackerInternetProvider.registerUser(UserUtil.name, UserUtil.EMAIL, UserUtil.PASSWORD, "Android", UserUtil.COUNTRY, refreshedToken);
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
                                Retrofit retrofitLogin = new Retrofit.Builder()
                                        .baseUrl(TutLubProvider.BASE_URL)
                                        .build();
                                TutLubProvider trackerInternetProvider = retrofitLogin.create(TutLubProvider.class);
                                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                                Log.e("refreshedToken ","" + refreshedToken);
                                Call<ResponseBody> call =  trackerInternetProvider.loginUser(UserUtil.EMAIL, UserUtil.PASSWORD, refreshedToken);
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
                                            Log.e("123", body);
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
                                                UserUtil.picture = jsonObject.getJSONObject("data").getJSONObject("user").getString("picture");
                                                UserUtil.name = jsonObject.getJSONObject("data").getJSONObject("user").getString("name");
                                                UserUtil.description = jsonObject.getJSONObject("data").getJSONObject("user").getString("description");
                                                UserUtil.cover = jsonObject.getJSONObject("data").getJSONObject("user").getString("cover");
                                                UserUtil.education = jsonObject.getJSONObject("data").getJSONObject("user").getString("education");
                                                UserUtil.country = jsonObject.getJSONObject("data").getJSONObject("user").getString("country");
                                                UserUtil.points = jsonObject.getJSONObject("data").getJSONObject("user").getInt("points");
                                                UserUtil.saveUser(getApplicationContext(), UserUtil.EMAIL, UserUtil.PASSWORD);
                                                FriendsUtil.setFriends(jsonObject.getJSONObject("data").getJSONObject("user").getJSONObject("friendsList"));


                                                subscribePersons("7044321372");
                                                subscribePersons("5634401953");
                                                subscribePersons("4105101746");
                                                subscribePersons("1929124794");
                                                subscribePersons("6276649133");
                                                subscribePersons("1291463025");
                                                subscribePersons("5741288188");

                                                startActivity(new Intent(RegistrationEmailActivity.this, LetsGetStartedActivity.class));
                                                finish();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Throwable t) {
                                        t.printStackTrace();
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        });
    }

    private void subscribePersons(String personID){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TutLubProvider.BASE_URL)
                .build();
        TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);
        Call<ResponseBody> call;
        call = trackerInternetProvider.addFriend(personID, RequestUtil.OAUTH_HEADER);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RegistrationEmailActivity.this, RegistrationPhoneActivity.class));
        finish();
    }
}
