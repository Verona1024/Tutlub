package com.verona1024.tutlub.utils;

import android.os.Handler;
import android.os.Looper;

import com.farhanahmed.httpfilerequest.Attachment;
import com.farhanahmed.httpfilerequest.ClientUtil;
import com.farhanahmed.httpfilerequest.DefaultTimeoutPolicy;
import com.farhanahmed.httpfilerequest.Executor;
import com.farhanahmed.httpfilerequest.ProgressRequestBody;
import com.farhanahmed.httpfilerequest.listener.Listener;
import com.verona1024.tutlub.models.Request;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

import static com.verona1024.tutlub.utils.RequestUtil.TUTKEY_TOKEN;

//import okhttp3.OkHttpClient;

public class RequestManager implements Request {
    private String url;
    private ArrayList<Attachment> attachments;
    private Map<String,String> formData;
    public RequestManager(String url) {
        this.url = url;
        this.attachments = new ArrayList<>();
        this.formData = new HashMap<>();
    }

    @Override
    public Request add(String name, File file) {
        attachments.add(new Attachment(name,file));
        return this;
    }

    @Override
    public Request setParams(Map<String, String> formData) {
        this.formData = formData;
        return this;
    }

    @Override
    public Executor listener(Listener listener) {
        return new ExecuteRequest(listener,this.url,this.attachments,this.formData);
    }

    private static class ExecuteRequest implements Executor{
        private Listener listener;
        private String url;
        private ArrayList<Attachment> attachments;
        private Map<String,String> formData;
        private DefaultTimeoutPolicy policy = new DefaultTimeoutPolicy();

        ExecuteRequest(Listener listener, String url, ArrayList<Attachment> attachments, Map<String, String> formData) {
            this.listener = listener;
            this.url = url;
            this.attachments = attachments;
            this.formData = formData;
        }

        @Override
        public Executor connectTimeOut(int time) {
            this.policy.setConnectTimeout(time);
            return this;
        }

        @Override
        public Executor readTimeOut(int time) {
            this.policy.setReadTimeout(time);
            return this;
        }

        @Override
        public Call run() {
            if (formData.size() == 0 || attachments.size() == 0)
            {
                return null;
            }
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            OkHttpClient client = ClientUtil.getClient();
            final Handler handler = new Handler(Looper.getMainLooper());
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for (Map.Entry<String, String> entry : formData.entrySet()) {
                builder.addFormDataPart(entry.getKey(), entry.getValue());
            }
            for (Attachment attachment : attachments) {
                if (attachment != null && attachment.file.exists())
                {
                    MediaType fileType = MediaType.parse("image/jpeg");
                    builder.addFormDataPart(attachment.name, "media", RequestBody.create(fileType, attachment.file));
//                    Log.d("addFormDataPart",attachment.name + ", "+FilenameUtils.getName(attachment.file.getAbsolutePath()));
                }
            }
            MultipartBody body = builder.build();
            ProgressRequestBody requestBody = new ProgressRequestBody(body, new ProgressRequestBody.Listener() {
                @Override
                public void onProgress(final int progress) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onProgress(progress);
                        }
                    });
                }
            });
            final okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .header(TUTKEY_TOKEN, RequestUtil.OAUTH_HEADER)
                    .post(requestBody).build();
            Callback callback = new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(e);
                        }
                    });

                }

                @Override
                public void onResponse(Call call, final okhttp3.Response response) {

                    final com.farhanahmed.httpfilerequest.Response res = new com.farhanahmed.httpfilerequest.Response();
                    res.code = response.code();
                    res.message = response.message();
                    try {
                        res.body = response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (response.isSuccessful()) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onResponse(res);
                            }
                        });

                    } else if (response.code() != 200) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onErrorResponse(res);
                            }
                        });
                    }

                }
            };
            Call call = client.newCall(request);
            call.enqueue(callback);
            return call;
        }
    }
}
