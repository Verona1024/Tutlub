package com.verona1024.tutlub.activities.main;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.verona1024.tutlub.R;
import com.verona1024.tutlub.activities.feed.LogInActivity;
import com.verona1024.tutlub.dialogs.ChangeEmailDialog;
import com.verona1024.tutlub.dialogs.ChangePasswordDialog;
import com.verona1024.tutlub.models.PostObject;
import com.verona1024.tutlub.utils.PostsUtil;
import com.verona1024.tutlub.utils.UserUtil;

import java.util.ArrayList;

import ru.whalemare.sheetmenu.SheetMenu;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(getString(R.string.activity_settings));

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            ((TextView)findViewById(R.id.textViewVersionNumber)).setText("v" + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            ((TextView)findViewById(R.id.textViewVersionNumber)).setText("");
        }

        findViewById(R.id.textViewLogOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserUtil.saveUser(getApplicationContext(), "", "");
                PostsUtil.myPosts = new ArrayList<PostObject>();
                PostsUtil.mySupplications = new ArrayList<PostObject>();
                PostsUtil.posts = new ArrayList<PostObject>();
                Intent intent = new Intent(SettingsActivity.this, LogInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Switch mobileNotification = (Switch) findViewById(R.id.switchMobile);
        mobileNotification.setChecked(UserUtil.getShowPushNotifications(getApplicationContext()));
        mobileNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UserUtil.setShowPushNotifications(getApplicationContext(), isChecked);
            }
        });

        Switch inAppNotification = (Switch) findViewById(R.id.switchInApp);
        inAppNotification.setChecked(UserUtil.getShowMobileNotifications(getApplicationContext()));
        inAppNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UserUtil.setShowMobileNotifications(getApplicationContext(), isChecked);
            }
        });

        findViewById(R.id.textViewFeedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SheetMenu.with(SettingsActivity.this)
                        .setTitle("Feedback")
                        .setMenu(R.menu.menu_feedback)
                        .setAutoCancel(true)
                        .setClick(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getItemId() == R.id.feedback_like_it){
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.tutlub.tutlub")));
                                    return true;
                                } else if (item.getItemId() == R.id.feedback_need_help){
                                    Intent i = new Intent(Intent.ACTION_SEND);
                                    i.setType("message/rfc822");
                                    i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"tutlubhelp@gmail.com"});
                                    i.putExtra(Intent.EXTRA_SUBJECT, "TutLub team, I need help!");
                                    i.putExtra(Intent.EXTRA_TEXT   , "My question is...");
                                    try {
                                        startActivity(Intent.createChooser(i, "Send mail..."));
                                    } catch (android.content.ActivityNotFoundException ex) {
                                        Toast.makeText(SettingsActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                                    }

                                    return true;
                                }
                                return false;
                            }
                        }).show();
            }
        });

        findViewById(R.id.textViewPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = ChangePasswordDialog.newInstance();
                newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
            }
        });

        findViewById(R.id.textViewChangeEmail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = ChangeEmailDialog.newInstance();
                newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
            }
        });
    }
}
