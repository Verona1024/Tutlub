package com.verona1024.tutlub.utils;

import com.verona1024.tutlub.models.NewsObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewsUtil {
    public static ArrayList<NewsObject> news = new ArrayList<>();

    public static void setNews(String body) throws JSONException {
        news = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(body);
        JSONArray jsonArray = jsonObject.getJSONArray("news");
        for (int i = 0; i < jsonArray.length(); i++){
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                NewsObject newsObject = new NewsObject();
                newsObject._id = object.getString("_id");
                newsObject.postId = object.getString("id");
                newsObject.title = object.getString("title");
                newsObject.text = object.getString("text");
                newsObject.media_url = object.getString("media_url");
                newsObject.publish_time = object.getString("publish_time");
                newsObject.tag_id = object.getJSONObject("tag").getString("id");
                newsObject.tag_label = object.getJSONObject("tag").getString("label");
                newsObject.__v = object.getInt("__v");
                newsObject.posted_on = object.getString("posted_on");
                newsObject.number_comments = object.getInt("number_comments");
                newsObject.number_amins = object.getInt("number_amins");
                newsObject.number_likes = object.getInt("number_likes");
                newsObject.has_media = object.getBoolean("has_media");
                news.add(newsObject);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
