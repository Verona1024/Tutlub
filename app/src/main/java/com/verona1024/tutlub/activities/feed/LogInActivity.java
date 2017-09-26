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

public class LogInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText editTextLogin = (EditText) findViewById(R.id.editTextPhone);
        final EditText editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editTextLogin.getText().toString().isEmpty()){
                    DialogFragment newFragment = MessageDialog.newInstance(getString(R.string.forgot_fill_in_all));
                    newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                    return;
                }

                if (editTextPassword.getText().toString().isEmpty()){
                    DialogFragment newFragment = MessageDialog.newInstance(getString(R.string.forgot_fill_in_all));
                    newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                    return;
                }

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(TutLubProvider.BASE_URL)
                        .build();
                TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                String refreshedToken = FirebaseInstanceId.getInstance().getToken();

                Call<ResponseBody> call =  trackerInternetProvider.loginUser(editTextLogin.getText().toString(), editTextPassword.getText().toString(), refreshedToken);
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
                                UserUtil.picture = jsonObject.getJSONObject("data").getJSONObject("user").getString("picture");
                                UserUtil.name = jsonObject.getJSONObject("data").getJSONObject("user").getString("name");
                                UserUtil.description = jsonObject.getJSONObject("data").getJSONObject("user").getString("description");
                                UserUtil.cover = jsonObject.getJSONObject("data").getJSONObject("user").getString("cover");
                                UserUtil.education = jsonObject.getJSONObject("data").getJSONObject("user").getString("education");
                                UserUtil.country = jsonObject.getJSONObject("data").getJSONObject("user").getString("country");
                                UserUtil.points = jsonObject.getJSONObject("data").getJSONObject("user").getInt("points");
                                UserUtil.saveUser(getApplicationContext(), editTextLogin.getText().toString(), editTextPassword.getText().toString());
                                FriendsUtil.setFriends(jsonObject.getJSONObject("data").getJSONObject("user").getJSONObject("friendsList"));
                                Log.e("token", RequestUtil.OAUTH_HEADER);
                                startActivity(new Intent(LogInActivity.this, MainActivity.class));
                                finish();
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
        });

        findViewById(R.id.textViewForgot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LogInActivity.this, ForgotPasswordActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(LogInActivity.this, StartActivity.class));
        finish();
    }
}
