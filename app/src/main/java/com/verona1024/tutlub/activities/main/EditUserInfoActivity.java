package com.verona1024.tutlub.activities.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.farhanahmed.httpfilerequest.listener.ListenerAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
import com.verona1024.tutlub.dialogs.ProgressCustomDialog;
import com.verona1024.tutlub.interfaces.ProgressUpdater;
import com.verona1024.tutlub.models.Request;
import com.verona1024.tutlub.utils.HttpFileRequest;
import com.verona1024.tutlub.utils.RequestUtil;
import com.verona1024.tutlub.utils.UserUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.verona1024.tutlub.utils.RequestUtil.MEDIAS_STORAGE_UPDATE_PROFILE;


public class EditUserInfoActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE_PROFILE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_IMAGES = 2;
    private static final int RESULT_LOAD_IMAGE_WALLPAPER = 3;
    private ImageLoader imgloader = ImageLoader.getInstance();
    private Bitmap bitmap_profile, bitmap_wallpaper;
    private ImageView wallpaper, profileImage;
    private Boolean w = false, p = false;
    private boolean profileUpdated = false, coverUpdated = false, imageUpdated = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE_PROFILE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            Uri bitmapUri = selectedImage;
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            bitmap_profile = BitmapFactory.decodeFile(picturePath);

            profileImage.setImageBitmap(bitmap_profile);
            p = true;

        } else if (requestCode == RESULT_LOAD_IMAGE_WALLPAPER && resultCode == RESULT_OK && null != data){
            Uri selectedImage = data.getData();
            Uri bitmapUri = selectedImage;
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            bitmap_wallpaper = BitmapFactory.decodeFile(picturePath);

            wallpaper.setImageBitmap(bitmap_wallpaper);
            wallpaper.setVisibility(View.VISIBLE);
            w = true;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_edit_user_info);
        setTitle("Edit info");

        final EditText name = (EditText) findViewById(R.id.editTextName);
        final EditText country = (EditText) findViewById(R.id.editTextCountry);
        final EditText workAt = (EditText) findViewById(R.id.editTextWorkAt);
        final EditText gender = (EditText) findViewById(R.id.editTextGender);

        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (p || w) {
                    imageUpdated = false;
                    File file_profile = null;
                    if (p && bitmap_profile != null) {
                        file_profile = new File(getCacheDir(), "media");
                        try {
                            file_profile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream bos_profile = new ByteArrayOutputStream();
                        bitmap_profile.compress(Bitmap.CompressFormat.JPEG, 30, bos_profile);
                        byte[] bitmapdata_p = bos_profile.toByteArray();

                        FileOutputStream fos_profile = null;
                        try {
                            fos_profile = new FileOutputStream(file_profile);
                            fos_profile.write(bitmapdata_p);
                            fos_profile.flush();
                            fos_profile.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    File file_wallpaper = null;
                    if (w && bitmap_wallpaper != null) {
                        file_wallpaper = new File(getCacheDir(), "media");
                        try {
                            file_wallpaper.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream bos_wallpaper = new ByteArrayOutputStream();
                        bitmap_wallpaper.compress(Bitmap.CompressFormat.JPEG, 50, bos_wallpaper);
                        byte[] bitmapdata = bos_wallpaper.toByteArray();

                        FileOutputStream fos_wallpaper = null;
                        try {
                            fos_wallpaper = new FileOutputStream(file_wallpaper);
                            fos_wallpaper.write(bitmapdata);
                            fos_wallpaper.flush();
                            fos_wallpaper.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    HashMap<String, String> parems = new HashMap<>();
                    parems.put("pic", "");

                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    final DialogFragment progressDialog = ProgressCustomDialog.newInstance();
                    progressDialog.show(getSupportFragmentManager(), "dialog");

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(logging)
                            .build();

                    Request request = new HttpFileRequest()
                            .client(client)
                            .url(MEDIAS_STORAGE_UPDATE_PROFILE);
                    if (w) {
                        request.add("cover", file_wallpaper);
                    }

                    if (p) {
                        request.add("picture", file_profile);
                    }

                    request.setParams(parems)
                            .listener(new ListenerAdapter() {
                                @Override
                                public void onResponse(com.farhanahmed.httpfilerequest.Response response) {
                                    super.onResponse(response);
                                    Log.e("onResponse", response.body);
                                    progressDialog.dismiss();
//                                    finish();
                                    try {
                                        UserUtil.picture = "" +  (new JSONObject(response.body)).getJSONObject("data").getJSONObject("user").getString("picture");
                                        UserUtil.cover = "" +  (new JSONObject(response.body)).getJSONObject("data").getJSONObject("user").getString("cover");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    imageUpdated = true;
                                }

                                @Override
                                public void onProgress(int progress) {
                                    Log.e("onProgress", progress + "");
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
                                    Log.e("onErrorResponse", response.body);
                                    progressDialog.dismiss();
                                }
                            })
                            .run();
                } else {
                    imageUpdated = true;
                }

                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject.put("name", name.getText().toString());
                    jsonObject.put("country", country.getText().toString());
                    jsonObject.put("education", workAt.getText().toString());
                    jsonObject.put("description", gender.getText().toString());
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


                profileUpdated = false;
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
                            JSONObject jsonObject = new JSONObject(body);
                            if (jsonObject.getString("status").equals("success")) {
                                UserUtil.userId = jsonObject.getJSONObject("profile").getString("id");
                                UserUtil.name = jsonObject.getJSONObject("profile").getString("name");
                                UserUtil.description = jsonObject.getJSONObject("profile").getString("description");
                                UserUtil.education = jsonObject.getJSONObject("profile").getString("education");
                                UserUtil.country = jsonObject.getJSONObject("profile").getString("country");
                                UserUtil.points = jsonObject.getJSONObject("profile").getInt("points");
                            }

                            profileUpdated = true;
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                    }
                });

                updateIsFinish();
            }
        });

        wallpaper = (ImageView) findViewById(R.id.imageViewWallpaper);
        wallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(EditUserInfoActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(EditUserInfoActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        ActivityCompat.requestPermissions(EditUserInfoActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_IMAGES);
                    }
                } else {
                    Intent in = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    EditUserInfoActivity.this.startActivityForResult(in, RESULT_LOAD_IMAGE_WALLPAPER);
                }
            }
        });
        findViewById(R.id.frameWallpaper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(EditUserInfoActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(EditUserInfoActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {

                        ActivityCompat.requestPermissions(EditUserInfoActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_IMAGES);

                    }
                } else {
                    Intent in = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    EditUserInfoActivity.this.startActivityForResult(in, RESULT_LOAD_IMAGE_WALLPAPER);
                }
            }
        });
        profileImage = (ImageView) findViewById(R.id.imageViewProfileImage);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(EditUserInfoActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(EditUserInfoActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {

                        ActivityCompat.requestPermissions(EditUserInfoActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_IMAGES);

                    }
                } else {
                    Intent in = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    EditUserInfoActivity.this.startActivityForResult(in, RESULT_LOAD_IMAGE_PROFILE);
                }
            }
        });

        wallpaper.setVisibility(View.INVISIBLE);
        imgloader.displayImage(UserUtil.cover, wallpaper, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                wallpaper.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
        imgloader.displayImage(UserUtil.picture, profileImage);

        name.setText(UserUtil.name.equalsIgnoreCase("null") ? "" : UserUtil.name);
        country.setText(UserUtil.country.equalsIgnoreCase("null") ? "" :  UserUtil.country);
        workAt.setText(UserUtil.education.equalsIgnoreCase("null") ? "" : UserUtil.education);
        gender.setText(UserUtil.description.equalsIgnoreCase("null") ? "" : UserUtil.description);
    }

    private void updateIsFinish(){
        if (profileUpdated && imageUpdated){
            finish();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                  updateIsFinish();
                }
            }, 500);
        }
    }
}
