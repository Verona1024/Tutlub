package com.verona1024.tutlub.fragments.main.search;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.activities.main.PersonProfileActivity;
import com.verona1024.tutlub.activities.main.SearchActivity;
import com.verona1024.tutlub.models.FriendObject;
import com.verona1024.tutlub.utils.RankUtil;
import com.verona1024.tutlub.utils.UserUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchPeopleFragment extends Fragment implements SearchActivity.SearchShower{
    private RecyclerView recyclerView;
    private FriendsListAdapter mAdapter;
    private ArrayList<FriendObject> displayFriends = new ArrayList<>();
    private String personId;


    public static SearchPeopleFragment newInstance() {

        Bundle args = new Bundle();

        SearchPeopleFragment fragment = new SearchPeopleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSearchUpdate(JSONArray jsonArray) {
        displayFriends = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++){
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                FriendObject friendObject = new FriendObject();
                friendObject.friendId = object.getString("id");
                friendObject.name = object.getString("name");
                friendObject.picture = object.getString("picture");
                friendObject.country = object.getString("country") == null ? "" : object.getString("country");
                friendObject.city = object.getString("city") == null ? "" : object.getString("city");
                friendObject.points = object.getInt("points");
                displayFriends.add(friendObject);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        mAdapter.resetListData();
        mAdapter.insertData(displayFriends);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        personId = getArguments().getString("personid");

        // Inflate view.
        View view = inflater.inflate(R.layout.fragment_search_person, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        mAdapter = new FriendsListAdapter(getActivity(), recyclerView, displayFriends);
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    public class FriendsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int OTHER = 3;

        private final int VIEW_ITEM = 1;
        private final int VIEW_PROG = 0;
        private boolean loading;

        private List<FriendObject> items = new ArrayList<>();
        private Context ctx;
        private ImageLoader imgloader = ImageLoader.getInstance();

        private OnLoadMoreListener onLoadMoreListener;

        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView user;
            public TextView points;
            public TextView country;
            public CircleImageView image;

            public ViewHolder(View v) {
                super(v);
                user = (TextView) v.findViewById(R.id.textViewName);
                points = (TextView) v.findViewById(R.id.textViewRang);
                country = (TextView) v.findViewById(R.id.textViewCountry);
                image = (CircleImageView) v.findViewById(R.id.profile_image);
            }
        }

        public class ProgressViewHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public ProgressViewHolder(View v) {
                super(v);
                progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
            }
        }

        public FriendsListAdapter(Activity activity, RecyclerView view, List<FriendObject> items) {
            this.ctx = activity;
            this.items = items;
            lastItemViewDetector(view);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            if (viewType == VIEW_ITEM) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_friend, parent, false);
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
                final FriendObject c = items.get(position);
                vItem.country.setText(c.city + ", " + c.country);
                vItem.user.setText(c.name);
                vItem.image.setImageResource(R.drawable.profile_image_placeholder);
                imgloader.displayImage(c.picture, vItem.image);
                if (!c.friendId.equals(UserUtil.userId)) {
                    vItem.image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), PersonProfileActivity.class);
                            intent.putExtra(PersonProfileActivity.PERSON_ID, "" + c.friendId);
                            startActivity(intent);
                        }
                    });
                } else {
                    vItem.image.setOnClickListener(null);
                }
                vItem.points.setText(RankUtil.getNameByPoints(getContext(), c.points) + ", ");
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
//            return items.get(position)._id;
            return position;
        }

        public void insertData(List<FriendObject> items) {
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
