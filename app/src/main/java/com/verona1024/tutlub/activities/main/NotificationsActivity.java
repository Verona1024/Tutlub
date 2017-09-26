package com.verona1024.tutlub.activities.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
import com.verona1024.tutlub.models.CommentObject;
import com.verona1024.tutlub.models.LikerObject;
import com.verona1024.tutlub.models.NotificationObject;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class NotificationsActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        setTitle("Notifications");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                (new LinearLayoutManager(this)).getOrientation());
        dividerItemDecoration.setDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.divider, null));
        recyclerView.addItemDecoration(dividerItemDecoration);

        Collections.reverse(UserUtil.notifications);
        NotificationsListAdapter mAdapter = new NotificationsListAdapter(this, recyclerView, UserUtil.notifications);
        recyclerView.setAdapter(mAdapter);
    }

    public class NotificationsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int VIEW_ITEM = 1;
        private final int VIEW_PROG = 0;
        private boolean loading;

        private List<NotificationObject> items = new ArrayList<>();
        private Context ctx;
        private ImageLoader imgloader = ImageLoader.getInstance();

        private OnLoadMoreListener onLoadMoreListener;

        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView action;
            public RelativeTimeTextView time;
            public ImageView imageSquare;
            public CircleImageView imageCircle;
            public LinearLayout linearLayout;

            public ViewHolder(View v) {
                super(v);
                action = (TextView) v.findViewById(R.id.textViewAction);
                time = (RelativeTimeTextView) v.findViewById(R.id.textViewTime);
                imageCircle = (CircleImageView) v.findViewById(R.id.imageCircle);
                imageSquare = (ImageView) v.findViewById(R.id.imageSquare);
                linearLayout = (LinearLayout) v.findViewById(R.id.linearLayout);
            }
        }

        public class ProgressViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public ProgressViewHolder(View v) {
                super(v);
                progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
            }
        }

        public NotificationsListAdapter(Activity activity, RecyclerView view, List<NotificationObject> items) {
            this.ctx = activity;
            this.items = items;
            lastItemViewDetector(view);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            if (viewType == VIEW_ITEM) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
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
                final NotificationObject c = items.get(position);

                if (c.seen){
                    vItem.linearLayout.setBackgroundColor(Color.parseColor("#E0E0E0"));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        vItem.linearLayout.setElevation(0);
                    }
                } else {
                    vItem.linearLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        vItem.linearLayout.setElevation(3);
                    }
                }

                SimpleDateFormat format = new SimpleDateFormat(PostsUtil.POST_DATA_FORMAT);
                try {
                    Date date = format.parse(c.when);
                    vItem.time.setReferenceTime(date.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Spanned durationSpanned;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    durationSpanned = Html.fromHtml(c.message, Html.FROM_HTML_MODE_LEGACY);
                } else {
                    durationSpanned = Html.fromHtml(c.message);
                }
                vItem.action.setText(durationSpanned);
                imgloader.displayImage(c.image, vItem.imageCircle);
                vItem.imageSquare.setVisibility(View.GONE);

                vItem.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        goToPost(c.postid);
                        UserUtil.removeNotification(position);
                        notifyDataSetChanged();
                    }
                });
            } else {
                ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
            }

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return this.items.get(position) != null ? VIEW_ITEM : VIEW_PROG;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void insertData(List<NotificationObject> items) {
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

    private void goToPost(final Long postId){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TutLubProvider.BASE_URL)
                .build();
        TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);
        Call<ResponseBody> call;
        call = trackerInternetProvider.getPostById("" + postId, RequestUtil.OAUTH_HEADER);
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
                    JSONObject object = new JSONObject(body).getJSONObject("post");
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

                    Intent intent = new Intent(getApplicationContext(), CommentActivity.class);
                    intent.putExtra(CommentActivity.POST_FLAG, postObject);
                    startActivity(intent);
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
