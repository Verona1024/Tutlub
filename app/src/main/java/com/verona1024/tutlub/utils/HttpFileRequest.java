package com.verona1024.tutlub.utils;

import com.farhanahmed.httpfilerequest.ClientUtil;

import okhttp3.OkHttpClient;

public class HttpFileRequest {
    public RequestManager url(String url){
        return new RequestManager(url);
    }
    public HttpFileRequest client(OkHttpClient client){
        ClientUtil.setClient(client);
        return this;
    }

}
