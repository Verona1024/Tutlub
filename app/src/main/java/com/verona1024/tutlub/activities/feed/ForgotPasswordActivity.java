package com.verona1024.tutlub.activities.feed;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
import com.verona1024.tutlub.activities.main.MainActivity;
import com.verona1024.tutlub.dialogs.MessageDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_password);

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

                Call<ResponseBody> call =  trackerInternetProvider.forgotUser(editTextLogin.getText().toString(), editTextPassword.getText().toString());
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
                            DialogFragment newFragment = MessageDialog.newInstance(jsonObject.getString("message"));
                            newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");

                            if (jsonObject.getString("status").equals("success")){
                                startActivity(new Intent(ForgotPasswordActivity.this, MainActivity.class));
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ForgotPasswordActivity.this, LogInActivity.class));
        finish();
    }
}
