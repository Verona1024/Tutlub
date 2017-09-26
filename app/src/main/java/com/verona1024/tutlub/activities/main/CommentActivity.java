package com.verona1024.tutlub.activities.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.verona1024.tutlub.dialogs.MessageDialog;
import com.verona1024.tutlub.dialogs.WriteCommentDialog;
import com.verona1024.tutlub.models.CommentObject;
import com.verona1024.tutlub.models.PostObject;
import com.verona1024.tutlub.utils.PostsUtil;
import com.verona1024.tutlub.utils.RequestUtil;
import com.verona1024.tutlub.utils.UserUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.verona1024.tutlub.utils.RequestUtil.OAUTH_HEADER;


public class CommentActivity extends AppCompatActivity implements WriteCommentDialog.NoticeDialogListener {
    public static final String POST_FLAG = "post_flag";

    private PostObject postObject;
    private ImageLoader imgloader = ImageLoader.getInstance();
    private RecyclerView recyclerView;
    private CommentListAdapter mAdapter;
    private TextView comment;

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
        setContentView(R.layout.activity_comment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        postObject = getIntent().getExtras().getParcelable(POST_FLAG);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        mAdapter = new CommentListAdapter(this, recyclerView, postObject.commentObjects);
        recyclerView.setAdapter(mAdapter);

        TextView user = (TextView) findViewById(R.id.textViewName);
        RelativeTimeTextView date = (RelativeTimeTextView) findViewById(R.id.textViewDate);
        TextView text = (TextView) findViewById(R.id.textViewText);
        final TextView amin = (TextView) findViewById(R.id.textViewAmin);
        final TextView likes = (TextView) findViewById(R.id.textViewLike);
        final TextView share = (TextView) findViewById(R.id.textViewShare);
        comment = (TextView) findViewById(R.id.textViewComment);
        ImageView tag = (ImageView) findViewById(R.id.imageViewTag);
        final ImageView image = (ImageView) findViewById(R.id.imageViewImage);
        ImageView userLogo = (ImageView) findViewById(R.id.imageViewUser);
        ImageView action = (ImageView) findViewById(R.id.imageViewAction);
        final RelativeLayout rShare = (RelativeLayout) findViewById(R.id.layoutShare);
        RelativeLayout rComment = (RelativeLayout) findViewById(R.id.layoutComment);
        final TextView tShare = (TextView) findViewById(R.id.textViewShareAction);
        TextView tComment = (TextView) findViewById(R.id.textViewCommentAction);
        final ImageView imShare = (ImageView) findViewById(R.id.imageViewShareAction);
        ImageView imComment = (ImageView) findViewById(R.id.imageViewCommentAction);
        final Button mainAction = (Button) findViewById(R.id.buttonMainAction);

        user.setText(postObject.owner_name);
        SimpleDateFormat format = new SimpleDateFormat(PostsUtil.POST_DATA_FORMAT);
        try {
            Date dateFormat = format.parse(postObject.posted_on);
            date.setReferenceTime(dateFormat.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (postObject.text.isEmpty()){
            text.setVisibility(View.GONE);
        } else {
            text.setText(postObject.text);
            text.setVisibility(View.VISIBLE);
        }
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CommentActivity.class);
                intent.putExtra(CommentActivity.POST_FLAG, postObject);
                startActivity(intent);
            }
        });
        imgloader.displayImage(postObject.owner_image, userLogo);
        if (!postObject.owner_id.equals(UserUtil.userId)) {
            userLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(CommentActivity.this, PersonProfileActivity.class);
                    intent.putExtra(PersonProfileActivity.PERSON_ID, "" + postObject.owner_id);
                    startActivity(intent);
                }
            });
        } else {
            userLogo.setOnClickListener(null);
        }

        if (postObject.number_amins != 0) {
            amin.setVisibility(View.VISIBLE);
            amin.setText("" + postObject.number_amins + " " + getString(R.string.label_amin));
        } else {
            amin.setVisibility(View.GONE);
        }

        if (postObject.number_likes != 0) {
            likes.setVisibility(View.VISIBLE);
            likes.setText("" + postObject.number_likes + " " + getString(R.string.label_like));
        } else {
            likes.setVisibility(View.GONE);
        }

        if (postObject.number_comments != 0) {
            comment.setVisibility(View.VISIBLE);
            comment.setText("" + postObject.number_comments + " " + getString(R.string.label_comment));
        } else {
            comment.setVisibility(View.GONE);
        }

        if (postObject.number_shares != 0) {
            share.setVisibility(View.VISIBLE);
            share.setText("" + postObject.number_shares + " " + getString(R.string.label_share));
        } else {
            share.setVisibility(View.GONE);
        }

        TextView editTextComment = (TextView) findViewById(R.id.editTextComment);
        editTextComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = WriteCommentDialog.newInstance(postObject);
                newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
            }
        });

        image.setVisibility(View.GONE);
        if (!postObject.media_url.isEmpty()) {
            imgloader.displayImage(postObject.media_url, image, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    image.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
        }

        if (postObject.type == 1){
            tag.setImageResource(R.drawable.ic_tag_post);
            mainAction.setText(getString(R.string.label_like));
            imComment.setVisibility(View.INVISIBLE);
            tComment.setVisibility(View.INVISIBLE);
            setTitle(getString(R.string.label_comment));
        } else {
            tag.setImageResource(R.drawable.ic_tag_supplication);
            mainAction.setText(getString(R.string.label_amin));
            imComment.setVisibility(View.INVISIBLE);
            tComment.setVisibility(View.INVISIBLE);
            setTitle(getString(R.string.label_supplication));
            findViewById(R.id.relativeComments).setVisibility(View.GONE);
        }

        if (postObject.type == 1 && postObject.liked){
            mainAction.setOnClickListener(null);
        } else if (postObject.type == 1 && !postObject.liked){
            mainAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(TutLubProvider.BASE_URL)
                            .build();
                    TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                    Call<ResponseBody> call =  trackerInternetProvider.like(postObject.postId, RequestUtil.OAUTH_HEADER);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                            try {
                                String body;
                                if (response.body() == null){
                                    body = response.errorBody().string();
                                } else {
                                    body = response.body().string();
                                    mainAction.setBackgroundResource(R.drawable.bacground_button_primary);
                                    mainAction.setTextColor(Color.WHITE);
                                    postObject.number_likes++;
                                    likes.setVisibility(View.VISIBLE);
                                    likes.setText("" + postObject.number_likes + " " + getString(R.string.label_like));
                                    postObject.liked = true;
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
        } else if (postObject.type == 2 && !postObject.amined){
            mainAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(TutLubProvider.BASE_URL)
                            .build();
                    TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                    Call<ResponseBody> call =  trackerInternetProvider.amin(postObject.postId, OAUTH_HEADER);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                            try {
                                String body;
                                if (response.body() == null){
                                    body = response.errorBody().string();
                                } else {
                                    body = response.body().string();
                                    mainAction.setBackgroundResource(R.drawable.bacground_button_primary);
                                    mainAction.setTextColor(Color.WHITE);
                                    postObject.number_amins++;
                                    amin.setVisibility(View.VISIBLE);
                                    amin.setText("" + postObject.number_amins + " " + getString(R.string.label_amin));
                                    postObject.amined = true;
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
        } else if (postObject.type == 2 && postObject.amined){
            mainAction.setOnClickListener(null);
        }

        if (postObject.shared){
            tShare.setText(getString(R.string.label_shared));
            imShare.setImageResource(R.drawable.ic_shared);
            rShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        } else {
            tShare.setText(getString(R.string.label_share));
            imShare.setImageResource(R.drawable.ic_share);
            rShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(TutLubProvider.BASE_URL)
                            .build();
                    TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                    Call<ResponseBody> call =  trackerInternetProvider.share(postObject.postId, OAUTH_HEADER);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                            try {
                                String body;
                                if (response.body() == null){
                                    body = response.errorBody().string();
                                } else {
                                    body = response.body().string();
                                    tShare.setText(getString(R.string.label_shared));
                                    imShare.setImageResource(R.drawable.ic_shared);
                                    rShare.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    });
                                    postObject.number_shares++;
                                    share.setText("" + postObject.number_shares + " "  + getString(R.string.label_share));
                                    share.setVisibility(View.VISIBLE);
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

        if (postObject.amined || postObject.liked){
            mainAction.setBackgroundResource(R.drawable.bacground_button_primary);
            mainAction.setTextColor(Color.WHITE);
        } else {
            mainAction.setBackgroundResource(R.drawable.border_button_primary);
            mainAction.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }

        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        PopupMenu popup = new PopupMenu(getApplicationContext(), view);
                        popup.getMenuInflater().inflate(R.menu.popup_post_item_menu_my, popup.getMenu());
                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                if (menuItem.getItemId() == R.id.report){
                                    Retrofit retrofit = new Retrofit.Builder()
                                            .baseUrl(TutLubProvider.BASE_URL)
                                            .build();
                                    TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                                    Call<ResponseBody> call =  trackerInternetProvider.report(postObject.postId,0, OAUTH_HEADER);
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
                                                JSONObject jsonObject = new JSONObject(body);
                                                DialogFragment newFragment = MessageDialog.newInstance(jsonObject.getString("message"));
                                                newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
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
                });
            }
        });
    }

    @Override
    public void onDialogShareClick(ArrayList<CommentObject> commentObjects) {
        postObject.commentObjects = commentObjects;
        postObject.number_comments++;
        comment.setText("" + postObject.number_comments + " "  + getString(R.string.label_comment));

        mAdapter.resetListData();
        mAdapter.insertData(postObject.commentObjects);

        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public class CommentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int VIEW_ITEM = 1;
        private final int VIEW_PROG = 0;
        private boolean loading;

        private List<CommentObject> items = new ArrayList<>();
        private Context ctx;
        private ImageLoader imgloader = ImageLoader.getInstance();

        private OnLoadMoreListener onLoadMoreListener;

        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView name;
            public RelativeTimeTextView date;
            public TextView comment;
            public ImageView person;

            public ViewHolder(View v) {
                super(v);
                name = (TextView) v.findViewById(R.id.textViewName);
                date = (RelativeTimeTextView) v.findViewById(R.id.textViewTime);
                comment = (TextView) v.findViewById(R.id.textViewComment);
                person = (ImageView) v.findViewById(R.id.imageViewPerson);
            }
        }

        public class ProgressViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public ProgressViewHolder(View v) {
                super(v);
                progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
            }
        }

        public CommentListAdapter(Activity activity, RecyclerView view, List<CommentObject> items) {
            this.ctx = activity;
            this.items = items;
            lastItemViewDetector(view);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            if (viewType == VIEW_ITEM) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
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
                final CommentObject c = items.get(position);

                imgloader.displayImage(c.poster_picture, vItem.person);
                if (!c.poster_id.equals(UserUtil.userId)) {
                    vItem.person.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(CommentActivity.this, PersonProfileActivity.class);
                            intent.putExtra(PersonProfileActivity.PERSON_ID, "" + c.poster_id);
                            startActivity(intent);
                        }
                    });
                } else {
                    vItem.person.setOnClickListener(null);
                }
                vItem.name.setText(c.poster_name);
                SimpleDateFormat format = new SimpleDateFormat(PostsUtil.POST_DATA_FORMAT);
                try {
                    Date date = format.parse(c.posted_on);
                    vItem.date.setReferenceTime(date.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                vItem.comment.setText(c.comment);

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

        public void insertData(List<CommentObject> items) {
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
