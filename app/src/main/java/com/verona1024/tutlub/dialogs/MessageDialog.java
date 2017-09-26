package com.verona1024.tutlub.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.verona1024.tutlub.R;

public class MessageDialog extends DialogFragment {
    public static String MESSAGE = "message";

    public static MessageDialog newInstance(String message) {
        MessageDialog f = new MessageDialog();

        Bundle args = new Bundle();
        args.putString(MESSAGE, message);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getArguments().getString(MESSAGE))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        return builder.create();
    }
}