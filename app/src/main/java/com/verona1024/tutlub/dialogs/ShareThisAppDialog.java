package com.verona1024.tutlub.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.models.CommentObject;

import java.util.ArrayList;

public class ShareThisAppDialog extends DialogFragment {
    public static ShareThisAppDialog newInstance() {

        Bundle args = new Bundle();

        ShareThisAppDialog fragment = new ShareThisAppDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public interface NoticeDialogListener {
        void onDialogShareClick(ArrayList<CommentObject> commentObjects);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_share_this_app, null);


        view.findViewById(R.id.buttonShareFacebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallbackManager callbackManager = CallbackManager.Factory.create();
                ShareDialog shareDialog = new ShareDialog(getActivity());
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
                                .setContentTitle(getString(R.string.share_title))
                                .setContentDescription(getString(R.string.share_message))
                                .setContentUrl(Uri.parse(getString(R.string.share_url)))
                                .build();

                        shareDialog.show(linkContent);
                    }
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }
        });

        view.findViewById(R.id.buttonShareWhatsApp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackageManager pm = getActivity().getPackageManager();
                try {

                    Intent waIntent = new Intent(Intent.ACTION_SEND);
                    waIntent.setType("text/plain");
                    String text = getString(R.string.share_message);

                    PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                    //Check if package exists or not. If not then code
                    //in catch block will be called
                    waIntent.setPackage("com.whatsapp");

                    waIntent.putExtra(Intent.EXTRA_TEXT, text);
                    startActivity(Intent.createChooser(waIntent, "Share with"));

                } catch (PackageManager.NameNotFoundException e) {
                    DialogFragment newFragment = MessageDialog.newInstance("Your device doesn't support Whatsapp!");
                    newFragment.show(getActivity().getSupportFragmentManager().beginTransaction(), "dialog");
                }
            }
        });

        view.findViewById(R.id.buttonShareTwitter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackageManager pm = getActivity().getPackageManager();
                try {
                    // Check if the Twitter app is installed on the phone.
                    getActivity().getPackageManager().getPackageInfo("com.twitter.android", 0);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setClassName("com.twitter.android", "com.twitter.android.composer.ComposerActivity");
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message));
                    startActivity(intent);

                } catch (Exception e) {
                    Toast.makeText(getActivity(),"Twitter is not installed on this device",Toast.LENGTH_LONG).show();

                }
            }
        });

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);
        return builder.create();
    }
}
