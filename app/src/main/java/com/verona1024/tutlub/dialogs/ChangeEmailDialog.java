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

import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
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

/**
 * Created by verona1024 on 04.05.17.
 */

public class ChangeEmailDialog extends DialogFragment {
    public static ChangeEmailDialog newInstance() {

        Bundle args = new Bundle();

        ChangeEmailDialog fragment = new ChangeEmailDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();


        View view = inflater.inflate(R.layout.dialog_change_email, null);
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

                    JSONObject jsonObject = new JSONObject();

                    try {
                        jsonObject.put("email", text.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JSONObject jsonRequest = new JSONObject();

                    try {
                        jsonRequest.put("fields", jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(TutLubProvider.BASE_URL)
                            .build();
                    TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                    Log.e("request is", jsonRequest.toString());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonRequest.toString());
                    Call<ResponseBody> call = trackerInternetProvider.putUserInfo(body, RequestUtil.OAUTH_HEADER, "application/json; charset=utf-8");
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                            try {
                                String body;
                                if (response.body() == null) {
                                    body = response.errorBody().string();
                                } else {
                                    body = response.body().string();
                                }
                                Log.e("update profile info", body);
                                JSONObject jsonObject = new JSONObject(body);
                                if (jsonObject.getString("status").equals("success")) {
                                    UserUtil.userId = jsonObject.getJSONObject("profile").getString("id");
                                    UserUtil.name = jsonObject.getJSONObject("profile").getString("name");
                                    UserUtil.description = jsonObject.getJSONObject("profile").getString("description");
                                    UserUtil.education = jsonObject.getJSONObject("profile").getString("education");
                                    UserUtil.country = jsonObject.getJSONObject("profile").getString("country");
                                    UserUtil.points = jsonObject.getJSONObject("profile").getInt("points");
                                    UserUtil.saveUser(getActivity().getApplicationContext(), text.getText().toString(), UserUtil.getUserPass(getActivity().getApplicationContext()));
                                    dismiss();
                                }

//                            finish();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
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
