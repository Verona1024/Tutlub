package com.verona1024.tutlub.activities.feed;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.activities.main.MainActivity;
import com.verona1024.tutlub.dialogs.MessageDialog;

public class ActivityInviteFriends extends AppCompatActivity {

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        findViewById(R.id.buttonNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityInviteFriends.this, MainActivity.class));
                finish();
            }
        });

        findViewById(R.id.buttonSMS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + ""));
                intent.putExtra("sms_body", getString(R.string.sms_body));
                startActivity(intent);
            }
        });

        findViewById(R.id.buttonWuP).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackageManager pm = getPackageManager();
                try {

                    Intent waIntent = new Intent(Intent.ACTION_SEND);
                    waIntent.setType("text/plain");

                    PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                    //Check if package exists or not. If not then code
                    //in catch block will be called
                    waIntent.setPackage("com.whatsapp");

                    waIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sms_body));
                    startActivity(Intent.createChooser(waIntent, "Share with"));

                } catch (PackageManager.NameNotFoundException e) {
                    DialogFragment newFragment = MessageDialog.newInstance("Your device doesn't support Whatsapp!");
                    newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
                }
            }
        });

        findViewById(R.id.buttonFaceBook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callbackManager = CallbackManager.Factory.create();
                ShareDialog shareDialog = new ShareDialog(ActivityInviteFriends.this);
                shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {

                    @Override
                    public void onSuccess(Sharer.Result result) {
                        // Do Nothing.
                    }

                    @Override
                    public void onCancel() {
                        // Do Nothing.
                    }

                    @Override
                    public void onError(FacebookException error) {
                    }
                });

                try {
                    if (ShareDialog.canShow(ShareLinkContent.class)) {
                        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                .setContentTitle("Tutlub")
                                .setContentDescription(getString(R.string.sms_body))
                                .setContentUrl(Uri.parse("http://bit.ly/tutlub-h"))
                                .build();

                        shareDialog.show(linkContent);
                    }
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityInviteFriends.this, MainActivity.class));
        finish();
    }
}
