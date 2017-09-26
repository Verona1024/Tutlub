package com.verona1024.tutlub;


import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by verona1024.
 */
public interface TutLubProvider {

    /**
     * Base url for server.
     */
    String BASE_URL = "http://178.62.121.165/";

    @FormUrlEncoded
    @POST("users")
    Call<ResponseBody> registerUser(@Field("name") String name,
                                    @Field("email") String email,
                                    @Field("password") String password,
                                    @Field("platform") String platform,
                                    @Field("country") String country,
                                    @Field("device_token") String token
    );

    @FormUrlEncoded
    @POST("users")
    Call<ResponseBody> registerUserUsePhone(@Field("name") String name,
                                    @Field("phone") String phone,
                                    @Field("password") String password,
                                    @Field("platform") String platform,
                                    @Field("country") String country,
                                    @Field("device_token") String token
    );

    @FormUrlEncoded
    @POST("login")
    Call<ResponseBody> loginUser(@Field("login") String email,
                                 @Field("password") String password,
                                 @Field("device_token") String token
    );

    @FormUrlEncoded
    @POST("forgot")
    Call<ResponseBody> forgotUser(
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("report")
    Call<ResponseBody> report(@Field("post_id") String post_id,
                              @Field("reason_id") int reason_id,
                              @Header("tutkey-token") String token
    );

    @FormUrlEncoded
    @POST("notifications")
    Call<ResponseBody> notifications(@Field("id") String id,
                              @Header("tutkey-token") String token
    );

    @GET("my/posts/{start}")
    Call<ResponseBody> myPosts(@Path("start") int start,
                              @Header("tutkey-token") String token
    );

    @FormUrlEncoded
    @POST("posts")
    Call<ResponseBody> postNewsWithoutImage(@Field("content") String content,
                                        @Field("type") int type,
                                        @Header("tutkey-token") String token
    );

    @FormUrlEncoded
    @POST("search")
    Call<ResponseBody> search(@Field("searchString") String searchString,
                                            @Header("tutkey-token") String token
    );


    @Multipart
    @POST("posts")
    Call<ResponseBody> postNewsWithImage(@Part("content") String content,
                                         @Part("media") RequestBody file,
                                         @Part("type") int type,
                                         @Header("tutkey-token") String token
    );

    @FormUrlEncoded
    @POST("comments")
    Call<ResponseBody> commentPost(@Field("post_id") String post_id,
                                   @Field("comment") String comment,
                                   @Header("tutkey-token") String token
    );

    @FormUrlEncoded
    @POST("comments/news")
    Call<ResponseBody> commentNews(@Field("post_id") String post_id,
                                   @Field("comment") String comment,
                                   @Header("tutkey-token") String token
    );

    @FormUrlEncoded
    @POST("share")
    Call<ResponseBody> share(@Field("post_id") String post_id,
                              @Header("tutkey-token") String token
    );

    @DELETE("/posts/{id}")
    Call<ResponseBody> deletePost(@Path("id") String id,
                             @Header("tutkey-token") String token
    );

    @FormUrlEncoded
    @POST("posts/{id}")
    Call<ResponseBody> updatePost(@Path("id") String id,
                                  @Header("tutkey-token") String token,
                                  @Field("content") String content
    );

    @GET("posts/{id}")
    Call<ResponseBody> getPostById(@Path("id") String id,
                                  @Header("tutkey-token") String token
    );

    @FormUrlEncoded
    @POST("like")
    Call<ResponseBody> like(@Field("post_id") String post_id,
                             @Header("tutkey-token") String token
    );

    @FormUrlEncoded
    @POST("unlike")
    Call<ResponseBody> unlike(@Field("post_id") long post_id,
                            @Header("tutkey-token") String token
    );

    @FormUrlEncoded
    @POST("amin")
    Call<ResponseBody> amin(@Field("post_id") String post_id,
                              @Header("tutkey-token") String token
    );

    @FormUrlEncoded
    @POST("accept/friends")
    Call<ResponseBody> acceptFriend(@Field("id") String id,
                            @Header("tutkey-token") String token
    );

    @FormUrlEncoded
    @POST("unfollow")
    Call<ResponseBody> unfollowFriend(@Field("id") String id,
                                    @Header("tutkey-token") String token
    );

    @FormUrlEncoded
    @POST("remove/friends")
    Call<ResponseBody> unfriendFriend(@Field("id") String id,
                                      @Header("tutkey-token") String token
    );

    @Headers({
            "Content-Type: application/json; charset=utf-8"
    })
    @PUT("profile")
    Call<ResponseBody> putUserInfo(@Body RequestBody requestBody,
                                    @Header("tutkey-token") String token,
                                   @Header("Content-Type") String contentType
    );

    @FormUrlEncoded
    @POST("decline/friends")
    Call<ResponseBody> declineFriend(@Field("id") String id,
                                    @Header("tutkey-token") String token
    );

    @FormUrlEncoded
    @POST("add/friends")
    Call<ResponseBody> addFriend(@Field("id") String id,
                                    @Header("tutkey-token") String token
    );

    @GET("suggested")
    Call<ResponseBody> suggested(@Header("tutkey-token") String token);

    @GET("my/posts")
    Call<ResponseBody> getMyPosts(@Header("tutkey-token") String token);

    @GET("supplications")
    Call<ResponseBody> getMySupplications(@Header("tutkey-token") String token);

    @GET("friends/list")
    Call<ResponseBody> getFriends(@Header("tutkey-token") String token);

    @POST("feeds")
    Call<ResponseBody> getFeeds(@Query("start") Integer start,
                                @Header("tutkey-token") String token);

    @GET("users/{id}")
    Call<ResponseBody> getUserById(@Path("id") String id,
                                    @Header("tutkey-token") String token);

    @GET("news")
    Call<ResponseBody> getNews(@Header("tutkey-token") String token);

    @GET("list/posts/{start}")
    Call<ResponseBody> getListPosts(@Path("start") Integer start,
                                    @Header("tutkey-token") String token);

    @GET("supplications/{start}")
    Call<ResponseBody> getSupplications(@Path("start") Integer start,
                                    @Header("tutkey-token") String token);
}
