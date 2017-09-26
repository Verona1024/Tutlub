package com.verona1024.tutlub.dialogs;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.farhanahmed.httpfilerequest.listener.ListenerAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
import com.verona1024.tutlub.interfaces.ProgressUpdater;
import com.verona1024.tutlub.models.PostObject;
import com.verona1024.tutlub.utils.HttpFileRequest;
import com.verona1024.tutlub.utils.PostsUtil;
import com.verona1024.tutlub.utils.RequestUtil;
import com.verona1024.tutlub.utils.UserUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getCacheDir;
import static com.verona1024.tutlub.utils.RequestUtil.MEDIAS_STORAGE_POSTS;
import static com.verona1024.tutlub.utils.RequestUtil.OAUTH_HEADER;

public class MakingPostDialog extends DialogFragment {
    private static final String TYPE = "type";
    private static final String POST = "post";
    private static final int RESULT_LOAD_IMAGE = 0;
    private static final int TAKE_REQUEST = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_IMAGES = 1;

    private ImageLoader imgloader = ImageLoader.getInstance();
    ImageView imageView;
    Bitmap bitmap;
    Uri bitmapUri;

    public static MakingPostDialog newInstance(int type) {

        Bundle args = new Bundle();
        args.putInt(TYPE, type);

        MakingPostDialog fragment = new MakingPostDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public static MakingPostDialog newInstance(int type, PostObject postObject) {

        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putParcelable(POST, postObject);

        MakingPostDialog fragment = new MakingPostDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public interface NoticeDialogListener {
        public void onDialogShareClick();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            bitmapUri = selectedImage;
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            bitmap = BitmapFactory.decodeFile(picturePath);

            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_IMAGES: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent in = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    MakingPostDialog.this.startActivityForResult(in, RESULT_LOAD_IMAGE);

                } else {
                    // Nothing should do.
                }
                return;
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final PostObject postObject = getArguments().getParcelable(POST);

        View view = inflater.inflate(R.layout.dialog_make_post, null);
        imgloader.displayImage(UserUtil.picture, (ImageView) view.findViewById(R.id.imageViewProfile));
        ((TextView) view.findViewById(R.id.textViewName)).setText(UserUtil.name);

        imageView = (ImageView) view.findViewById(R.id.imageViewImage);
        if (postObject != null){
            imgloader.displayImage(postObject.media_url, imageView, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    imageView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
        }

        view.findViewById(R.id.imageViewTakePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // "STOP showing this message, its irritate users"
                    } else {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_IMAGES);
                    }
                } else {
                    Intent in = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    MakingPostDialog.this.startActivityForResult(in, RESULT_LOAD_IMAGE);
                }
            }
        });

        final EditText text = (EditText) view.findViewById(R.id.editTextText);
        if (postObject != null){
            text.setText(postObject.text);
        }

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
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                    if (postObject != null){
                        postObject.text = text.getText().toString().trim();
                        PostsUtil.updateLocalPost(postObject);
                        Call<ResponseBody> call = trackerInternetProvider.updatePost(postObject.postId, RequestUtil.OAUTH_HEADER, text.getText().toString().trim());
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                                try {
                                    String body;
                                    if (response.body() == null) {
                                        body = response.errorBody().string();
                                        JSONObject jsonObject = new JSONObject(body);
                                        DialogFragment newFragment = MessageDialog.newInstance(jsonObject.getString("message"));
                                        newFragment.show(getFragmentManager().beginTransaction(), "dialog");
                                    } else {
                                        body = response.body().string();
                                        if (getActivity() instanceof NoticeDialogListener) {
                                            ((NoticeDialogListener) getActivity()).onDialogShareClick();
                                        }
                                        dismiss();
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

                        return;
                    }

                    if (bitmap == null) {
                        Call<ResponseBody> call = trackerInternetProvider.postNewsWithoutImage(text.getText().toString().trim(), getArguments().getInt(TYPE), OAUTH_HEADER);
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                                try {
                                    String body;
                                    if (response.body() == null) {
                                        body = response.errorBody().string();
                                        JSONObject jsonObject = new JSONObject(body);
                                        DialogFragment newFragment = MessageDialog.newInstance(jsonObject.getString("message"));
                                        newFragment.show(getFragmentManager().beginTransaction(), "dialog");
                                    } else {
                                        body = response.body().string();
                                        if (getActivity() instanceof NoticeDialogListener) {
                                            ((NoticeDialogListener) getActivity()).onDialogShareClick();
                                        }
                                        dismiss();
                                        Log.e("post", body);
                                    }
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
                        File file = new File(getCacheDir(), "media");
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos);
                        byte[] bitmapdata = bos.toByteArray();

                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(file);
                            fos.write(bitmapdata);
                            fos.flush();
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        HashMap<String, String> parems = new HashMap<>();
                        parems.put("content", text.getText().toString().trim());
                        parems.put("type", "" + getArguments().getInt("type"));

                        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                        OkHttpClient client = new OkHttpClient.Builder()
                                .addInterceptor(logging)
                                .build();

                        final DialogFragment progressDialog = ProgressCustomDialog.newInstance();
                        progressDialog.show(getChildFragmentManager(), "dialog");

                        new HttpFileRequest()
                                .client(client)
                                .url(MEDIAS_STORAGE_POSTS)
                                .add("media", file)
                                .setParams(parems)
                                .listener(new ListenerAdapter() {
                                    @Override
                                    public void onResponse(com.farhanahmed.httpfilerequest.Response response) {
                                        super.onResponse(response);
                                        Log.e("onResponse", response.body);
                                        if (getActivity() instanceof NoticeDialogListener) {
                                            ((NoticeDialogListener) getActivity()).onDialogShareClick();
                                        }
                                        dismiss();
                                        progressDialog.dismiss();
                                    }

                                    @Override
                                    public void onProgress(int progress) {
                                        ((ProgressUpdater) progressDialog).setProgress(progress);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        super.onError(e);
                                        e.printStackTrace();
                                        progressDialog.dismiss();
                                    }

                                    @Override
                                    public void onErrorResponse(com.farhanahmed.httpfilerequest.Response response) {
                                        super.onErrorResponse(response);
                                        progressDialog.dismiss();
                                    }
                                })
                                .run();
                    }
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
