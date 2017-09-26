package com.verona1024.tutlub.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
import com.verona1024.tutlub.utils.RequestUtil;
import com.verona1024.tutlub.utils.UserUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ChangePasswordDialog extends DialogFragment {
    public static ChangePasswordDialog newInstance() {

        Bundle args = new Bundle();

        ChangePasswordDialog fragment = new ChangePasswordDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();


        View view = inflater.inflate(R.layout.dialog_change_password, null);
        final EditText text = (EditText) view.findViewById(R.id.editTextText);

        view.findViewById(R.id.textViewCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        view.findViewById(R.id.buttonShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!text.getText().toString().trim().isEmpty()) {

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(TutLubProvider.BASE_URL)
                            .build();
                    TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                    Call<ResponseBody> call =  trackerInternetProvider.forgotUser(
                            UserUtil.getUserLogin(getActivity().getApplicationContext()),
                            text.getText().toString());
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

                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(body);

                                    try {
                                        DialogFragment newFragment = MessageDialog.newInstance(jsonObject.getString("message"));
                                        newFragment.show(getActivity().getSupportFragmentManager().beginTransaction(), "dialog");
                                    } catch (Exception e){
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), "" + jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                                    }

                                    if (jsonObject.getString("status").equals("success")) {
                                        UserUtil.saveUser(getApplicationContext(), UserUtil.getUserLogin(getApplicationContext()), text.getText().toString());
                                        dismiss();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Log.e("message", body);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            t.printStackTrace();
                        }
                    });
                } else {
                    DialogFragment newFragment = MessageDialog.newInstance("Please, put some text");
                    newFragment.show(getFragmentManager().beginTransaction(), "dialog");
                }
            }
        });

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);
        return builder.create();
    }
}
