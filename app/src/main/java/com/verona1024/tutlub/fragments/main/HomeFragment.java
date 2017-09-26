package com.verona1024.tutlub.fragments.main;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
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
import com.verona1024.tutlub.activities.main.PersonProfileActivity;
import com.verona1024.tutlub.dialogs.MakingPostDialog;
import com.verona1024.tutlub.dialogs.MessageDialog;
import com.verona1024.tutlub.dialogs.ShareThisAppDialog;
import com.verona1024.tutlub.interfaces.TabsManager;
import com.verona1024.tutlub.models.PostObject;
import com.verona1024.tutlub.utils.PostsUtil;
import com.verona1024.tutlub.utils.RequestUtil;
import com.verona1024.tutlub.utils.UserUtil;

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
import static com.verona1024.tutlub.R.id.rPost;
import static com.verona1024.tutlub.R.id.rSupplication;
import static com.verona1024.tutlub.utils.PostsUtil.POST_DATA_FORMAT;
import static com.verona1024.tutlub.utils.RequestUtil.OAUTH_HEADER;

public class HomeFragment extends Fragment implements TabsManager, MakingPostDialog.NoticeDialogListener  {

    private PostListAdapter mAdapter;
    private RecyclerView recyclerView;
    private AppBarLayout mainAppBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate view.
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (event.getAction() != KeyEvent.ACTION_DOWN) {
                        return true;
                    }
                    new AlertDialog.Builder(getActivity())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.closing_tutlub)
                            .setMessage(R.string.closing_message)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finish();
                                }

                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                    return true;
                }

                return false;
            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        mainAppBar = (AppBarLayout) view.findViewById(R.id.mainappbar);

        mAdapter = new PostListAdapter(getActivity(), recyclerView, PostsUtil.posts);
        recyclerView.setAdapter(mAdapter);
        view.findViewById(rPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = MakingPostDialog.newInstance(1);
                newFragment.show(getFragmentManager().beginTransaction(), "dialog");
            }
        });
        view.findViewById(rSupplication).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = MakingPostDialog.newInstance(2);
                newFragment.show(getFragmentManager().beginTransaction(), "dialog");
            }
        });

        requestData();

        return view;
    }

    private void requestData(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TutLubProvider.BASE_URL)
                .build();
        TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

        Call<ResponseBody> call =  trackerInternetProvider.getFeeds(0, OAUTH_HEADER);
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
                    PostsUtil.setPosts(jsonObject);
                    updateLocalList();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            requestData();
                        }
                    }, 10 * 1000);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            requestData();
                        }
                    }, 1 * 1000);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        requestData();
                    }
                }, 10 * 1000);
            }
        });
    }

    @java.lang.Override
    public void goToTheTop() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        layoutManager.scrollToPositionWithOffset(0, 0);
        mainAppBar.setExpanded(true);
    }

    @Override
    public void onDialogShareClick() {
        updateLocalList();
        requestData();
    }

    public void updateLocalList(){
        mAdapter.resetListData();
        mAdapter.insertData(PostsUtil.posts);
    }

    public class PostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int OTHER = 3;
        private boolean onBind;

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

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            onBind = true;
            if (holder instanceof ViewHolder) {
                final ViewHolder vItem = (ViewHolder) holder;
                final PostObject c = items.get(position);
                vItem.user.setText(c.owner_name);
                SimpleDateFormat format = new SimpleDateFormat(POST_DATA_FORMAT);
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
                vItem.userLogo.setImageResource(R.drawable.photo_profile_placeholder);
                imgloader.displayImage(c.owner_image, vItem.userLogo);

                if (!c.owner_id.equals(UserUtil.userId)) {
                    vItem.userLogo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), PersonProfileActivity.class);
                            intent.putExtra(PersonProfileActivity.PERSON_ID, "" + c.owner_id);
                            startActivity(intent);
                        }
                    });
                } else {
                    vItem.userLogo.setOnClickListener(null);
                }

                if (c.number_amins != 0) {
                    vItem.amin.setVisibility(View.VISIBLE);
                    vItem.amin.setText("" + c.number_amins + getString(R.string.amin));
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
                    vItem.likes.setText("" + c.number_likes + getString(R.string.likers));
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
                    vItem.comment.setText("" + c.number_comments + getString(R.string.comment));
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
                    vItem.share.setText("" + c.number_shares + getString(R.string.share));
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
                    vItem.mainAction.setText(R.string.label_like);
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
                    vItem.mainAction.setText(R.string.label_amin);
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

                            if (UserUtil.getUserFirstTime(getApplicationContext())){
                                DialogFragment newFragment = ShareThisAppDialog.newInstance();
                                newFragment.show(getFragmentManager().beginTransaction(), "dialog");
                                UserUtil.setUserFirstTimeDone(getApplicationContext());
                            }

                            c.number_likes++;
                            c.liked = true;
                            vItem.mainAction.setBackgroundResource(R.drawable.bacground_button_primary);
                            vItem.mainAction.setTextColor(Color.WHITE);

                            Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl(TutLubProvider.BASE_URL)
                                    .build();
                            TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

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
                                            vItem.likes.setVisibility(View.VISIBLE);
                                            vItem.likes.setText("" + c.number_likes + " Likes");
                                            vItem.mainAction.setOnClickListener(null);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    t.printStackTrace();
                                    Log.e("like failure", "=(");
                                }
                            });
                            PostsUtil.updateLocalPost(c);
                            updateLocalList();
                        }
                    });
                } else if (c.type == 2 && !c.amined){
                    vItem.mainAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (UserUtil.getUserFirstTime(getApplicationContext())){
                                DialogFragment newFragment = ShareThisAppDialog.newInstance();
                                newFragment.show(getFragmentManager().beginTransaction(), "dialog");
                                UserUtil.setUserFirstTimeDone(getApplicationContext());
                            }

                            c.number_amins++;
                            c.amined = true;
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
                                            vItem.amin.setVisibility(View.VISIBLE);
                                            vItem.amin.setText("" + c.number_amins + " Amin");
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
                            PostsUtil.updateLocalPost(c);
                            updateLocalList();
                        }
                    });
                } else if (c.type == 2 && c.amined){
                    vItem.mainAction.setOnClickListener(null);
                }

                if (c.shared){
                    vItem.tShare.setText(R.string.label_shared);
                    vItem.imShare.setImageResource(R.drawable.ic_shared);
                    vItem.rShare.setOnClickListener(null);
                } else {
                    vItem.tShare.setText(R.string.label_share);
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
                                            vItem.tShare.setText(R.string.label_shared);
                                            vItem.imShare.setImageResource(R.drawable.ic_shared);
                                            vItem.rShare.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                }
                                            });
//                                            Log.e("Share ", body);
                                            c.number_shares++;
                                            vItem.share.setText("" + c.number_shares + " Share");
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
                                                            requestData();
                                                            JSONObject jsonObject = new JSONObject(body);
                                                            DialogFragment newFragment = MessageDialog.newInstance(jsonObject.getString("message"));
                                                            newFragment.show(getFragmentManager().beginTransaction(), "dialog");
                                                        } catch (IOException | JSONException e) {
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
                                                        } catch (IOException | JSONException e) {
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
            onBind = false;
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

        public void resetListData() {
            this.items = new ArrayList<>();
            if(!onBind) {
                notifyDataSetChanged();
            }
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
