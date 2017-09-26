package com.verona1024.tutlub.activities.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.models.LikerObject;
import com.verona1024.tutlub.models.PostObject;
import com.verona1024.tutlub.utils.UserUtil;

import java.util.ArrayList;
import java.util.List;

public class LikersListActivity extends AppCompatActivity {
    public static final int FLAG_AMIN = 0;
    public static final int FLAG_SHARE = 1;
    public static final int FLAG_LIKE = 2;
    public static final String POST_FLAG = "post_flag";
    public static final String TYPE_FLAG = "type_flag";

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
        setContentView(R.layout.activity_likers);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        PostObject postObject = getIntent().getExtras().getParcelable(POST_FLAG);
        ArrayList<LikerObject> likerObjects = new ArrayList<>();
        int type = getIntent().getExtras().getInt(TYPE_FLAG);
        switch (type){
            case FLAG_AMIN : likerObjects = postObject.aminers; setTitle("Aminers"); break;
            case FLAG_SHARE : likerObjects = postObject.shared_by; setTitle("Shared by"); break;
            case FLAG_LIKE : likerObjects = postObject.likers; setTitle("Likes"); break;
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        LikerListAdapter mAdapter = new LikerListAdapter(this, recyclerView, likerObjects);
        recyclerView.setAdapter(mAdapter);
    }

    public class LikerListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int VIEW_ITEM = 1;
        private final int VIEW_PROG = 0;
        private boolean loading;

        private List<LikerObject> items = new ArrayList<>();
        private Context ctx;
        private ImageLoader imgloader = ImageLoader.getInstance();

        private OnLoadMoreListener onLoadMoreListener;
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView name;
            public ImageView person;

            public ViewHolder(View v) {
                super(v);
                name = (TextView) v.findViewById(R.id.textViewName);
                person = (ImageView) v.findViewById(R.id.imageViewImage);
            }
        }

        public class ProgressViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public ProgressViewHolder(View v) {
                super(v);
                progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
            }
        }

        public LikerListAdapter(Activity activity, RecyclerView view, List<LikerObject> items) {
            this.ctx = activity;
            this.items = items;
            lastItemViewDetector(view);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            if (viewType == VIEW_ITEM) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_liker, parent, false);
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
                final LikerObject c = items.get(position);

                imgloader.displayImage(c.liker_picture, vItem.person);
                if (c.liker_id != null) {
                    if (!c.liker_id.equals(UserUtil.userId)) {
                        vItem.person.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(LikersListActivity.this, PersonProfileActivity.class);
                                intent.putExtra(PersonProfileActivity.PERSON_ID, "" + c.liker_id);
                                startActivity(intent);
                            }
                        });
                    } else {
                        vItem.person.setOnClickListener(null);
                    }
                }
                vItem.name.setText(c.liker_name);
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
//            return items.get(position)._id;
            return position;
        }

        public void insertData(List<LikerObject> items) {
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
