package com.verona1024.tutlub.activities.feed;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
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
import java.util.concurrent.TimeUnit;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

// TODO : DELETE IF NEED. NO MORE USED.
public class VerificationActivity extends AppCompatActivity {
    private String mVerificationId = "";
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        final EditText editTextCode = (EditText) findViewById(R.id.editCode);

        ((TextView) findViewById(R.id.textViewMessage)).setText("We sent a 6 digit number to\n" + UserUtil.PHONE_NUMBER + "\nEnter it below");

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null) {
                    registerUser();
                }
            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                UserUtil.PHONE_NUMBER,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                VerificationActivity.this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // This callback will be invoked in two situations:
                        // 1 - Instant verification. In some cases the phone number can be instantly
                        //     verified without needing to send or enter a verification code.
                        // 2 - Auto-retrieval. On some devices Google Play services can automatically
                        //     detect the incoming verification SMS and perform verificaiton without
                        //     user action.
                        Log.e("SMS", "onVerificationCompleted:" + credential);

                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        // This callback is invoked in an invalid request for verification is made,
                        // for instance if the the phone number format is not valid.
                        Log.e("SMS", "onVerificationFailed", e);

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            try {
                                DialogFragment newFragment = MessageDialog.newInstance("Invalid request");
                                newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                            } catch (IllegalStateException e1){
                                e1.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Invalid request", Toast.LENGTH_LONG).show();
                            }
                            // Invalid request
                            // ...
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            try {
                                DialogFragment newFragment = MessageDialog.newInstance("The SMS quota for TutLub has been exceeded");
                                newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                            } catch (IllegalStateException e1){
                                e1.printStackTrace();
                                Toast.makeText(getApplicationContext(), "The SMS quota for TutLub has been exceeded", Toast.LENGTH_LONG).show();
                            }
                            // The SMS quota for the project has been exceeded
                            // ...
                        }

                        try {
                            DialogFragment newFragment = MessageDialog.newInstance("Verification failed, unsupported error");
                            newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                        } catch (IllegalStateException e1){
                            e1.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Verification failed, unsupported error", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {
                        // The SMS verification code has been sent to the provided phone number, we
                        // now need to ask the user to enter the code and then construct a credential
                        // by combining the code with a verification ID.
                        Log.e("SMS", "onCodeSent:" + verificationId);

                        mVerificationId = verificationId;
                        mResendToken = token;
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(String s) {
                        super.onCodeAutoRetrievalTimeOut(s);
                        Log.e("SMS", "onCodeAutoRetrievalTimeOut:" + s);
                        try {
                            DialogFragment newFragment = MessageDialog.newInstance("Code auto retrieval timeout");
                            newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                        } catch (IllegalStateException e1){
                            e1.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Code auto retrieval timeout", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!mVerificationId.isEmpty()){
                    verifyCode(editTextCode.getText().toString().trim());
                    findViewById(R.id.buttonNext).setVisibility(View.GONE);
                } else {
                    try {
                        DialogFragment newFragment = MessageDialog.newInstance("Please, wait sms");
                        newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                    } catch (IllegalStateException e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Please, wait sms", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

        private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            try {
                                DialogFragment newFragment = MessageDialog.newInstance("Sms verification success!");
                                newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                            } catch (IllegalStateException e1){
                                e1.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Sms verification success!", Toast.LENGTH_LONG).show();
                            }
                            registerUser();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                try {
                                    DialogFragment newFragment = MessageDialog.newInstance("Invalid code");
                                    newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                                } catch (IllegalStateException e){
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Invalid code", Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        findViewById(R.id.buttonNext).setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void verifyCode(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void registerUser(){
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e("refreshedToken ", "" + refreshedToken);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TutLubProvider.BASE_URL)
                .build();
        TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);
        Call<ResponseBody> call =  trackerInternetProvider.registerUserUsePhone(UserUtil.name, UserUtil.PHONE_NUMBER, UserUtil.PASSWORD, "Android", UserUtil.COUNTRY, refreshedToken);
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

                    try {
                        DialogFragment newFragment = MessageDialog.newInstance(body);
                        newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                    } catch (IllegalStateException e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "" + body, Toast.LENGTH_LONG).show();
                    }

                    JSONObject jsonObject = new JSONObject(body);

                    if (jsonObject.getString("status").equals("success")){
                        Retrofit retrofitLogin = new Retrofit.Builder()
                                .baseUrl(TutLubProvider.BASE_URL)
                                .build();
                        TutLubProvider trackerInternetProvider = retrofitLogin.create(TutLubProvider.class);
                        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                        Log.e("refreshedToken ","" + refreshedToken);
                        Call<ResponseBody> call =  trackerInternetProvider.loginUser(UserUtil.PHONE_NUMBER, UserUtil.PASSWORD, refreshedToken);
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
                                        UserUtil.saveUser(getApplicationContext(), UserUtil.PHONE_NUMBER, UserUtil.PASSWORD);
                                        FriendsUtil.setFriends(jsonObject.getJSONObject("data").getJSONObject("user").getJSONObject("friendsList"));
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

                        startActivity(new Intent(VerificationActivity.this, LetsGetStartedActivity.class));
                        finish();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();

                    try {
                        DialogFragment newFragment = MessageDialog.newInstance("Unsupported registration error. ");
                        newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                    } catch (IllegalStateException e1){
                        e1.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Unsupported registration error.", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();

                try {
                    DialogFragment newFragment = MessageDialog.newInstance("Can not register now. Please, try later");
                    newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                } catch (IllegalStateException e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Can not register now. Please, try later", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(VerificationActivity.this, RegistrationPhoneActivity.class));
        finish();
    }
}
