package com.verona1024.tutlub.fragments.main.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
import com.verona1024.tutlub.activities.main.CommentActivity;
import com.verona1024.tutlub.activities.main.LikersListActivity;
import com.verona1024.tutlub.dialogs.MakingPostDialog;
import com.verona1024.tutlub.dialogs.MessageDialog;
import com.verona1024.tutlub.models.CommentObject;
import com.verona1024.tutlub.models.LikerObject;
import com.verona1024.tutlub.models.PostObject;
import com.verona1024.tutlub.utils.PostsUtil;
import com.verona1024.tutlub.utils.RequestUtil;
import com.verona1024.tutlub.utils.UserUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.verona1024.tutlub.utils.RequestUtil.OAUTH_HEADER;

public class PersonPostsFragment extends Fragment {
    private static final String TYPE = "type";
    private static final String PERSON_ID = "personid";

    RecyclerView recyclerView;
    PostListAdapter mAdapter;
    private int type;
    private String personId;

    public static PersonPostsFragment newInstance(int type) {
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        
        PersonPostsFragment fragment = new PersonPostsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static PersonPostsFragment newInstance(int type, String personId) {
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        args.putString(PERSON_ID, personId);

        PersonPostsFragment fragment = new PersonPostsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        type = getArguments().getInt(TYPE);
        personId = getArguments().getString(PERSON_ID);

        // Inflate view.
        View view = inflater.inflate(R.layout.fragment_person_posts, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        if (personId == null) {
            mAdapter = new PostListAdapter(getActivity(), recyclerView, type == 0 ? PostsUtil.mySupplications : PostsUtil.myPosts);
        } else {
            mAdapter = new PostListAdapter(getActivity(), recyclerView, type == 0 ? PostsUtil.personSupplications : PostsUtil.personPosts);
        }
        recyclerView.setAdapter(mAdapter);

        requestData();

        return view;
    }

    private void requestData(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TutLubProvider.BASE_URL)
                .build();
        TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

        Call<ResponseBody> call;
        // TODO : Remove after server API update. Bad solution.
        if (personId == null) {
            if (type == 0) {
                call = trackerInternetProvider.getMySupplications(OAUTH_HEADER);
            } else {
                call = trackerInternetProvider.getMyPosts(OAUTH_HEADER);
            }

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

                        ArrayList<PostObject> posts = new ArrayList<PostObject>();
                        ArrayList<PostObject> supplications = new ArrayList<PostObject>();
                        JSONObject jsonObject = new JSONObject(body);
                        JSONArray jsonArray = jsonObject.getJSONArray("posts");
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
                                if (postObject.type == 1) {
                                    posts.add(postObject);
                                } else {
                                    supplications.add(postObject);
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                        PostsUtil.myPosts = posts;
                        PostsUtil.mySupplications = supplications;
                        mAdapter.resetListData();
                        mAdapter.insertData(type == 0 ? PostsUtil.mySupplications : PostsUtil.myPosts);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                requestData();
                            }
                        }, 10 * 1000);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                }
            });
        } else {
            call = trackerInternetProvider.getUserById(personId, OAUTH_HEADER);
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

                        ArrayList<PostObject> posts = new ArrayList<PostObject>();
                        ArrayList<PostObject> supplications = new ArrayList<PostObject>();
                        JSONObject jsonObject = new JSONObject(body);
                        JSONArray jsonArray = jsonObject.getJSONArray("posts");
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
                                if (postObject.type == 1) {
                                    posts.add(postObject);
                                } else {
                                    supplications.add(postObject);
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                        PostsUtil.personPosts = posts;

                        jsonArray = jsonObject.getJSONArray("supplications");
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
                                if (postObject.type == 1) {
                                    posts.add(postObject);
                                } else {
                                    supplications.add(postObject);
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                        PostsUtil.personSupplications = supplications;
                        mAdapter.resetListData();
                        mAdapter.insertData(type == 0 ? PostsUtil.personSupplications : PostsUtil.personPosts);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                requestData();
                            }
                        }, 10 * 1000);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    // TODO: Make universal when design will be ready.
    private class PostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int OTHER = 3;

        private final int VIEW_ITEM = 1;
        private final int VIEW_PROG = 0;
        private boolean loading;

        private List<PostObject> items = new ArrayList<>();
        private Context ctx;
        private ImageLoader imgloader = ImageLoader.getInstance();

        private OnLoadMoreListener onLoadMoreListener;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView user;
            public RelativeTimeTextView date;
            public TextView text;
            public TextView amin;
            public TextView likes;
            public TextView share;
            public TextView comment;
            public ImageView tag;
            public ImageView image;
            public ImageView userLogo;
            public ImageView action;
            public RelativeLayout rShare;
            public RelativeLayout rComment;
            public TextView tShare;
            public TextView tComment;
            public ImageView imShare;
            public ImageView imComment;
            public Button mainAction;

            public ViewHolder(View v) {
                super(v);
                user = (TextView) v.findViewById(R.id.textViewName);
                date = (RelativeTimeTextView) v.findViewById(R.id.textViewDate);
                text = (TextView) v.findViewById(R.id.textViewText);
                amin = (TextView) v.findViewById(R.id.textViewAmin);
                likes = (TextView) v.findViewById(R.id.textViewLike);
                share = (TextView) v.findViewById(R.id.textViewShare);
                comment = (TextView) v.findViewById(R.id.textViewComment);
                tag = (ImageView) v.findViewById(R.id.imageViewTag);
                image = (ImageView) v.findViewById(R.id.imageViewImage);
                userLogo = (ImageView) v.findViewById(R.id.imageViewUser);
                action = (ImageView) v.findViewById(R.id.imageViewAction);
                rShare = (RelativeLayout) v.findViewById(R.id.layoutShare);
                rComment = (RelativeLayout) v.findViewById(R.id.layoutComment);
                tShare = (TextView) v.findViewById(R.id.textViewShareAction);
                tComment = (TextView) v.findViewById(R.id.textViewCommentAction);
                imShare = (ImageView) v.findViewById(R.id.imageViewShareAction);
                imComment = (ImageView) v.findViewById(R.id.imageViewCommentAction);
                mainAction = (Button) v.findViewById(R.id.buttonMainAction);
            }
        }

        public class ProgressViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public ProgressViewHolder(View v) {
                super(v);
                progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
            }
        }

        public PostListAdapter(Activity activity, RecyclerView view, List<PostObject> items) {
            this.ctx = activity;
            this.items = items;
            lastItemViewDetector(view);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            if (viewType == VIEW_ITEM) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
                vh = new ViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
                vh = new ProgressViewHolder(v);
            }
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ViewHolder) {
                final ViewHolder vItem = (ViewHolder) holder;
                final PostObject c = items.get(position);
                vItem.user.setText(c.owner_name);
                SimpleDateFormat format = new SimpleDateFormat(PostsUtil.POST_DATA_FORMAT);
                try {
                    Date date = format.parse(c.posted_on);
                    vItem.date.setReferenceTime(date.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (c.text.isEmpty()){
                    vItem.text.setVisibility(View.GONE);
                } else {
                    vItem.text.setText(c.text);
                    vItem.text.setVisibility(View.VISIBLE);
                }
                vItem.text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ctx, CommentActivity.class);
                        intent.putExtra(CommentActivity.POST_FLAG, c);
                        startActivity(intent);
                    }
                });
                imgloader.displayImage(c.owner_image, vItem.userLogo);

                if (c.number_amins != 0) {
                    vItem.amin.setVisibility(View.VISIBLE);
                    vItem.amin.setText("" + c.number_amins + getString(R.string.label_amin));
                    vItem.amin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ctx, LikersListActivity.class);
                            intent.putExtra(LikersListActivity.POST_FLAG, c);
                            intent.putExtra(LikersListActivity.TYPE_FLAG, LikersListActivity.FLAG_AMIN);
                            startActivity(intent);
                        }
                    });
                } else {
                    vItem.amin.setVisibility(View.GONE);
                }

                if (c.number_likes != 0) {
                    vItem.likes.setVisibility(View.VISIBLE);
                    vItem.likes.setText("" + c.number_likes + getString(R.string.label_like));
                    vItem.likes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ctx, LikersListActivity.class);
                            intent.putExtra(LikersListActivity.POST_FLAG, c);
                            intent.putExtra(LikersListActivity.TYPE_FLAG, LikersListActivity.FLAG_LIKE);
                            startActivity(intent);
                        }
                    });
                } else {
                    vItem.likes.setVisibility(View.GONE);
                }

                if (c.number_comments != 0) {
                    vItem.comment.setVisibility(View.VISIBLE);
                    vItem.comment.setText("" + c.number_comments + getString(R.string.label_comment));
                    vItem.comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ctx, CommentActivity.class);
                            intent.putExtra(CommentActivity.POST_FLAG, c);
                            startActivity(intent);
                        }
                    });
                } else {
                    vItem.comment.setVisibility(View.GONE);
                }

                if (c.number_shares != 0) {
                    vItem.share.setVisibility(View.VISIBLE);
                    vItem.share.setText("" + c.number_shares + getString(R.string.label_share));
                    vItem.share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ctx, LikersListActivity.class);
                            intent.putExtra(LikersListActivity.POST_FLAG, c);
                            intent.putExtra(LikersListActivity.TYPE_FLAG, LikersListActivity.FLAG_SHARE);
                            startActivity(intent);
                        }
                    });
                } else {
                    vItem.share.setVisibility(View.GONE);
                }

                vItem.image.setVisibility(View.GONE);
                if (!c.media_url.isEmpty()) {
                    imgloader.displayImage(c.media_url, vItem.image, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            vItem.image.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    });
                }

                if (c.type == 1){
                    vItem.tag.setImageResource(R.drawable.ic_tag_post);
                    vItem.mainAction.setText(getString(R.string.label_like));
                    vItem.imComment.setVisibility(View.VISIBLE);
                    vItem.tComment.setVisibility(View.VISIBLE);
                    vItem.rComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ctx, CommentActivity.class);
                            intent.putExtra(CommentActivity.POST_FLAG, c);
                            startActivity(intent);
                        }
                    });
                } else {
                    vItem.tag.setImageResource(R.drawable.ic_tag_supplication);
                    vItem.mainAction.setText(getString(R.string.label_amin));
                    vItem.imComment.setVisibility(View.INVISIBLE);
                    vItem.tComment.setVisibility(View.INVISIBLE);
                    vItem.rComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                }

                if (c.type == 1 && c.liked){
                    vItem.mainAction.setOnClickListener(null);

                } else if (c.type == 1 && !c.liked){
                    vItem.mainAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl(TutLubProvider.BASE_URL)
                                    .build();
                            TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);
                            vItem.mainAction.setBackgroundResource(R.drawable.bacground_button_primary);
                            vItem.mainAction.setTextColor(Color.WHITE);

                            Call<ResponseBody> call =  trackerInternetProvider.like(c.postId, RequestUtil.OAUTH_HEADER);
                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                                    try {
                                        String body;
                                        if (response.body() == null){
                                            body = response.errorBody().string();
                                        } else {
                                            body = response.body().string();
                                            vItem.mainAction.setBackgroundResource(R.drawable.bacground_button_primary);
                                            vItem.mainAction.setTextColor(Color.WHITE);
                                            c.number_likes++;
                                            vItem.likes.setVisibility(View.VISIBLE);
                                            vItem.likes.setText("" + c.number_likes + " Likes");
                                            c.liked = true;
                                            vItem.mainAction.setOnClickListener(null);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    t.printStackTrace();
                                }
                            });
                        }
                    });
                } else if (c.type == 2 && !c.amined){
                    vItem.mainAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl(TutLubProvider.BASE_URL)
                                    .build();
                            TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);
                            vItem.mainAction.setBackgroundResource(R.drawable.bacground_button_primary);
                            vItem.mainAction.setTextColor(Color.WHITE);

                            Call<ResponseBody> call =  trackerInternetProvider.amin(c.postId, OAUTH_HEADER);
                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                                    try {
                                        String body;
                                        if (response.body() == null){
                                            body = response.errorBody().string();
                                        } else {
                                            body = response.body().string();
                                            vItem.mainAction.setBackgroundResource(R.drawable.bacground_button_primary);
                                            vItem.mainAction.setTextColor(Color.WHITE);
                                            c.number_amins++;
                                            vItem.amin.setVisibility(View.VISIBLE);
                                            vItem.amin.setText("" + c.number_amins + " " + getString(R.string.label_amin));
                                            c.amined = true;
                                            vItem.mainAction.setOnClickListener(null);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    t.printStackTrace();
                                }
                            });
                        }
                    });
                } else if (c.type == 2 && c.amined){
                    vItem.mainAction.setOnClickListener(null);
                }

                if (c.shared){
                    vItem.tShare.setText(getString(R.string.label_shared));
                    vItem.imShare.setImageResource(R.drawable.ic_shared);
                    vItem.rShare.setOnClickListener(null);
                } else {
                    vItem.tShare.setText(getString(R.string.label_share));
                    vItem.imShare.setImageResource(R.drawable.ic_share);
                    vItem.rShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl(TutLubProvider.BASE_URL)
                                    .build();
                            TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                            Call<ResponseBody> call =  trackerInternetProvider.share(c.postId, OAUTH_HEADER);
                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                                    try {
                                        String body;
                                        if (response.body() == null){
                                            body = response.errorBody().string();
                                        } else {
                                            body = response.body().string();
                                            vItem.tShare.setText(getString(R.string.label_shared));
                                            vItem.imShare.setImageResource(R.drawable.ic_shared);
                                            vItem.rShare.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                }
                                            });
//                                            Log.e("Share ", body);
                                            c.number_shares++;
                                            vItem.share.setText("" + c.number_shares + " " + getString(R.string.label_shared));
                                            vItem.share.setVisibility(View.VISIBLE);
                                            vItem.rShare.setOnClickListener(null);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    t.printStackTrace();
                                }
                            });
                        }
                    });
                }

                if (c.amined || c.liked){
                    vItem.mainAction.setBackgroundResource(R.drawable.bacground_button_primary);
                    vItem.mainAction.setTextColor(Color.WHITE);
                } else {
                    vItem.mainAction.setBackgroundResource(R.drawable.border_button_primary);
                    vItem.mainAction.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
                }

                vItem.action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                if (c.owner_id.equals(UserUtil.userId)) {
                                    PopupMenu popup = new PopupMenu(getApplicationContext(), view);
                                    popup.getMenuInflater().inflate(R.menu.popup_post_item_menu_my, popup.getMenu());
                                    popup.show();
                                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem menuItem) {
                                            if (menuItem.getItemId() == R.id.delete) {
                                                Retrofit retrofit = new Retrofit.Builder()
                                                        .baseUrl(TutLubProvider.BASE_URL)
                                                        .build();
                                                TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                                                Call<ResponseBody> call = trackerInternetProvider.deletePost(c.postId, OAUTH_HEADER);
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
                                                            DialogFragment newFragment = MessageDialog.newInstance(jsonObject.getString("message"));
                                                            newFragment.show(getFragmentManager().beginTransaction(), "dialog");

                                                            requestData();
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
                                                return true;
                                            } else if (menuItem.getItemId() == R.id.edit){
                                                DialogFragment newFragment = MakingPostDialog.newInstance(c.type, c);
                                                newFragment.show(getFragmentManager().beginTransaction(), "dialog");
                                                return true;
                                            }
                                            return false;
                                        }
                                    });
                                } else {
                                    PopupMenu popup = new PopupMenu(getApplicationContext(), view);
                                    popup.getMenuInflater().inflate(R.menu.popup_post_item_menu_user, popup.getMenu());
                                    popup.show();
                                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem menuItem) {
                                            if (menuItem.getItemId() == R.id.report) {
                                                Retrofit retrofit = new Retrofit.Builder()
                                                        .baseUrl(TutLubProvider.BASE_URL)
                                                        .build();
                                                TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                                                Call<ResponseBody> call = trackerInternetProvider.report(c.postId, 0, OAUTH_HEADER);
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
                                                            DialogFragment newFragment = MessageDialog.newInstance(jsonObject.getString("message"));
                                                            newFragment.show(getFragmentManager().beginTransaction(), "dialog");
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
                                                return true;
                                            }
                                            return false;
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }
            else {
                ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
            }

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (this.items.get(position) != null) {
                return VIEW_ITEM;
            } else {
                return VIEW_PROG;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void insertData(List<PostObject> items) {
            setLoaded();
            int positionStart = getItemCount();
            int itemCount = items.size();
            this.items.addAll(items);
            notifyItemRangeInserted(positionStart, itemCount);
        }

        public void setLoaded() {
            loading = false;
            for (int i = 0; i < getItemCount(); i++) {
                if (items.get(i) == null) {
                    items.remove(i);
                    notifyItemRemoved(i);
                }
            }
        }

        public void setLoading() {
            if (getItemCount() != 0) {
                this.items.add(null);
                notifyItemInserted(getItemCount() - 1);
            }
        }

        public void resetListData() {
            this.items = new ArrayList<>();
            notifyDataSetChanged();
        }

        public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
            this.onLoadMoreListener = onLoadMoreListener;
        }

        private void lastItemViewDetector(RecyclerView recyclerView) {
            if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        int lastPos = layoutManager.findLastVisibleItemPosition();
                        //Log.e("SCROLLED", lastPos + " == " + (getItemCount() - 1));
                        if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                            int current_page = getItemCount() / 20;
                            onLoadMoreListener.onLoadMore(current_page);
                            loading = true;
                        }
                    }
                });
            }
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }
}
