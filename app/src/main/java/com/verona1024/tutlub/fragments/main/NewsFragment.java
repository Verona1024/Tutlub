package com.verona1024.tutlub.fragments.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
import com.verona1024.tutlub.activities.main.NewsActivity;
import com.verona1024.tutlub.interfaces.TabsStateListener;
import com.verona1024.tutlub.models.NewsObject;
import com.verona1024.tutlub.utils.NewsUtil;
import com.verona1024.tutlub.utils.RequestUtil;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.verona1024.tutlub.utils.PostsUtil.POST_DATA_FORMAT;

public class NewsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NewsListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate view.
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (event.getAction() != KeyEvent.ACTION_DOWN) {
                        return true;
                    }
                    if (getActivity() instanceof TabsStateListener){
                        ((TabsStateListener) getActivity()).setFirstTab();
                    }
                    return true;
                }

                return false;
            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        mAdapter = new NewsListAdapter(getActivity(), recyclerView, NewsUtil.news);
        recyclerView.setAdapter(mAdapter);

        requestData();

        return view;
    }

    private void requestData(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TutLubProvider.BASE_URL)
                .build();
        TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

        Call<ResponseBody> call = trackerInternetProvider.getNews(RequestUtil.OAUTH_HEADER);
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

                    NewsUtil.setNews(body);
                    mAdapter.resetListData();
                    mAdapter.insertData(NewsUtil.news);

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
                Log.e("123", "onFailure");
            }
        });
    }

    class NewsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int OTHER = 3;

        private final int VIEW_ITEM = 1;
        private final int VIEW_PROG = 0;
        private boolean loading;

        private List<NewsObject> items = new ArrayList<>();
        private Context ctx;
        private ImageLoader imgloader = ImageLoader.getInstance();

        private OnLoadMoreListener onLoadMoreListener;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView tag;
            public TextView title;
            public TextView text;
            public RelativeTimeTextView date;
            public ImageView image;
            public RelativeLayout relativeLayout;

            public ViewHolder(View v) {
                super(v);
                tag = (TextView) v.findViewById(R.id.textViewTag);
                title = (TextView) v.findViewById(R.id.textViewTitle);
                text = (TextView) v.findViewById(R.id.textViewText);
                date = (RelativeTimeTextView) v.findViewById(R.id.textViewDate);
                image = (ImageView) v.findViewById(R.id.imageViewPicture);
                relativeLayout = (RelativeLayout) v.findViewById(R.id.relativeNews);
            }
        }

        public class ProgressViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public ProgressViewHolder(View v) {
                super(v);
                progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
            }
        }

        NewsListAdapter(Activity activity, RecyclerView view, List<NewsObject> items) {
            this.ctx = activity;
            this.items = items;
            lastItemViewDetector(view);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            if (viewType == VIEW_ITEM) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
                vh = new ViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
                vh = new ProgressViewHolder(v);
            }
            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ViewHolder) {
                final ViewHolder vItem = (ViewHolder) holder;
                final NewsObject c = items.get(position);
                vItem.title.setText(c.title);
                vItem.text.setText(fromHtml(c.text));
                vItem.tag.setText(c.tag_label);
                vItem.relativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), NewsActivity.class);
                        intent.putExtra(NewsActivity.NEWS_ITEM, c);
                        startActivity(intent);
                    }
                });
                SimpleDateFormat format = new SimpleDateFormat(POST_DATA_FORMAT);
                try {
                    Date date = format.parse(c.posted_on);
                    vItem.date.setReferenceTime(date.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                imgloader.displayImage(c.media_url, vItem.image);
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

        public void insertData(List<NewsObject> items) {
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

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }
}
