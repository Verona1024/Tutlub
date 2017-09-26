package com.verona1024.tutlub.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.verona1024.tutlub.R;
import com.verona1024.tutlub.interfaces.ProgressUpdater;

import flepsik.github.com.progress_ring.ProgressRingView;

public class ProgressCustomDialog extends DialogFragment implements ProgressUpdater {

    private static ProgressRingView progressRingView;

    public static ProgressCustomDialog newInstance() {

        Bundle args = new Bundle();

        ProgressCustomDialog fragment = new ProgressCustomDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setProgress(float progress) {
        if (progressRingView != null){
            progressRingView.setProgress( ( progress / (float) 100) );
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();


        View view = inflater.inflate(R.layout.dialog_progress, null);
        progressRingView = (ProgressRingView) view.findViewById(R.id.progress_view);
        progressRingView.setProgress( 0f );

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);
        return builder.create();
    }
}
