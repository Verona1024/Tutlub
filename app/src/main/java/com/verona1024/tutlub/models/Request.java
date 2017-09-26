package com.verona1024.tutlub.models;

import com.farhanahmed.httpfilerequest.Executor;
import com.farhanahmed.httpfilerequest.listener.Listener;

import java.io.File;
import java.util.Map;


public interface Request {
    Request add(String fileName, File file);
    Request setParams(Map <String, String> formData);
    Executor listener(Listener listener);
}
