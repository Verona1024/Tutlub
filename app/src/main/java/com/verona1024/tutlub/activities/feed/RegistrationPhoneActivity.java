package com.verona1024.tutlub.activities.feed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.verona1024.tutlub.R;
import com.verona1024.tutlub.dialogs.MessageDialog;
import com.verona1024.tutlub.utils.UserUtil;

public class RegistrationPhoneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phone);

        final EditText editTextEmail = (EditText) findViewById(R.id.editEmail);
        final EditText editTextPassword = (EditText) findViewById(R.id.editPassword);

        findViewById(R.id.textViewAlternative).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationPhoneActivity.this, RegistrationEmailActivity.class);
                startActivity(intent);
            }
        });

        // Try to get phone number.
        try {
            TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String mPhoneNumber = tMgr.getLine1Number();
            editTextEmail.setText(mPhoneNumber);
        } catch (Exception e){
            e.printStackTrace();
        }

        findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextEmail.getText().toString().isEmpty()){
                    try {
                        DialogFragment newFragment = MessageDialog.newInstance(getString(R.string.forgot_fill_in_all));
                        newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                    } catch (IllegalStateException e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), getString(R.string.forgot_fill_in_all), Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                if (editTextPassword.getText().toString().isEmpty()){
                    try {
                        DialogFragment newFragment = MessageDialog.newInstance(getString(R.string.forgot_fill_in_all));
                        newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                    } catch (IllegalStateException e){
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), getString(R.string.forgot_fill_in_all), Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                UserUtil.PASSWORD = editTextPassword.getText().toString();
                UserUtil.PHONE_NUMBER = editTextEmail.getText().toString();

                Intent intent = new Intent(RegistrationPhoneActivity.this, VerificationActivity.class);
                startActivity(intent);

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RegistrationPhoneActivity.this, RegistrationActivity.class));
        finish();
    }
}
