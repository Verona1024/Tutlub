package com.verona1024.tutlub.utils;

import com.verona1024.tutlub.models.CommentObject;
import com.verona1024.tutlub.models.LikerObject;
import com.verona1024.tutlub.models.PostObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PostsUtil {
    public static final String POST_DATA_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S'Z'";
    // Todo: optimisation. Fast solution.

    public static ArrayList<PostObject> posts = new ArrayList<PostObject>();
    public static ArrayList<PostObject> myPosts = new ArrayList<PostObject>();
    public static ArrayList<PostObject> mySupplications = new ArrayList<PostObject>();
    public static ArrayList<PostObject> personPosts = new ArrayList<PostObject>();
    public static ArrayList<PostObject> personSupplications = new ArrayList<PostObject>();

    public static void setPosts(JSONObject jsonObject) throws JSONException {
        JSONArray jsonArray = jsonObject.getJSONArray("posts");
        posts = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++){
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                PostObject postObject = new PostObject();
                postObject._id = object.getString("_id");
                postObject.postId = object.getString("id");
                postObject.number_amins = object.getInt("number_amins");
                postObject.number_comments = object.getInt("number_comments");
                postObject.number_shares = object.getInt("number_shares");
                postObject.number_likes = object.getInt("number_likes");
                postObject.owner_image = object.getJSONObject("owner").getString("picture");
                postObject.owner_name = object.getJSONObject("owner").getString("name");
                postObject.owner_id = object.getJSONObject("owner").getString("id");
                postObject.posted_on = object.getString("posted_on");
                postObject.media_url = object.getString("media_url");
                postObject.type = object.getInt("type");
                postObject.has_media = object.getBoolean("has_media");
                postObject.shared = object.getBoolean("shared");
                postObject.liked = object.getBoolean("liked");
                postObject.amined = object.getBoolean("amined");
                postObject.text = object.getString("text");
                JSONArray comments = object.getJSONArray("comments");
                ArrayList<CommentObject> commentObjects = new ArrayList<>();
                if (comments.length() != 0) {
                    for (int j = 0; j < comments.length(); j++) {
                        try {
                            JSONObject comment = comments.getJSONObject(j);
                            CommentObject commentObject = new CommentObject();
                            commentObject._id = comment.getString("_id");
                            commentObject.commentId = comment.getString("id");
                            commentObject.posted_on = comment.getString("posted_on");
                            commentObject.comment = comment.getString("comment");
                            commentObject.poster_id = comment.getJSONObject("poster").getString("id");
                            commentObject.poster_name = comment.getJSONObject("poster").getString("name");
                            commentObject.poster_picture = comment.getJSONObject("poster").getString("picture");
                            commentObjects.add(commentObject);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                postObject.commentObjects = commentObjects;

                JSONArray sharedBy = object.getJSONArray("shared_by");
                ArrayList<LikerObject> sharedByObjects = new ArrayList<>();
                if (sharedBy.length() != 0) {
                    for (int j = 0; j < sharedBy.length(); j++) {
                        JSONObject jsonObjectLiker = sharedBy.getJSONObject(j);
                        LikerObject liker = new LikerObject();
                        liker.liker_id = jsonObjectLiker.getString("id");
                        liker.liker_name = jsonObjectLiker.getString("name");
                        liker.liker_picture = jsonObjectLiker.getString("picture");
                        sharedByObjects.add(liker);
                    }
                }
                postObject.shared_by = sharedByObjects;

                JSONArray aminersJ = object.getJSONArray("aminers");
                ArrayList<LikerObject> aminers = new ArrayList<>();
                if (aminersJ.length() != 0) {
                    for (int j = 0; j < aminersJ.length(); j++) {
                        JSONObject jsonObjectLiker = aminersJ.getJSONObject(j);
                        LikerObject liker = new LikerObject();
                        liker.liker_id = jsonObjectLiker.getString("id");
                        liker.liker_name = jsonObjectLiker.getString("name");
                        liker.liker_picture = jsonObjectLiker.getString("picture");
                        aminers.add(liker);
                    }
                }
                postObject.aminers = aminers;

                JSONArray likersJ = object.getJSONArray("likers");
                ArrayList<LikerObject> likers = new ArrayList<>();
                if (likersJ.length() != 0) {
                    for (int j = 0; j < likersJ.length(); j++) {
                        JSONObject jsonObjectLiker = likersJ.getJSONObject(j);
                        LikerObject liker = new LikerObject();
                        liker.liker_id = jsonObjectLiker.getString("id");
                        liker.liker_name = jsonObjectLiker.getString("name");
                        liker.liker_picture = jsonObjectLiker.getString("picture");
                        likers.add(liker);
                    }
                }
                postObject.likers = likers;
                posts.add(postObject);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void updateLocalPost(PostObject postObject){
        for (int i = 0 ; i < posts.size(); i++){
            PostObject post = posts.get(i);
            if (post.postId.equals(postObject.postId)){
                posts.get(i).text = postObject.text;
                posts.get(i).amined = postObject.amined;
                posts.get(i).liked = postObject.liked;
                posts.get(i).shared = postObject.shared;
                posts.get(i).number_amins = postObject.number_amins;
                posts.get(i).number_comments = postObject.number_comments;
                posts.get(i).number_likes = postObject.number_likes;
                posts.get(i).number_shares = postObject.number_shares;
            }
        }
    }
}
