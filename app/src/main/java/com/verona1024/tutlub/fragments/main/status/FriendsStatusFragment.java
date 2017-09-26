package com.verona1024.tutlub.fragments.main.status;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
import com.verona1024.tutlub.activities.main.PersonProfileActivity;
import com.verona1024.tutlub.models.FriendObject;
import com.verona1024.tutlub.utils.FriendsUtil;
import com.verona1024.tutlub.utils.RequestUtil;
import com.verona1024.tutlub.utils.UserUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class FriendsStatusFragment extends Fragment {
    private RecyclerView recyclerView;
    private FriendsListAdapter mAdapter;
    private ArrayList<FriendObject> displayFriends;

    public static FriendsStatusFragment newInstance() {

        Bundle args = new Bundle();

        FriendsStatusFragment fragment = new FriendsStatusFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate view.
        View view = inflater.inflate(R.layout.fragment_friends_score, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyslerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        displayFriends = FriendsUtil.myFriendObjects;
        Collections.sort(displayFriends, new Comparator<FriendObject>() {
            @Override
            public int compare(FriendObject o1, FriendObject o2) {
                if (o1.points < o2.points){
                    return 1;
                }

                if (o1.points > o2.points){
                    return -1;
                }

                return 0;
            }
        });
        mAdapter = new FriendsListAdapter(getActivity(), recyclerView, displayFriends);
        recyclerView.setAdapter(mAdapter);

        requestData();

        return view;
    }

    private void requestData(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TutLubProvider.BASE_URL)
                .build();
        TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

        Call<ResponseBody> call = trackerInternetProvider.getFriends(RequestUtil.OAUTH_HEADER);
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

                    FriendsUtil.setFriendsObjects(body);
                    displayFriends = FriendsUtil.myFriendObjects;
                    Collections.sort(displayFriends, new Comparator<FriendObject>() {
                        @Override
                        public int compare(FriendObject o1, FriendObject o2) {
                            if (o1.points < o2.points){
                                return 1;
                            }

                            if (o1.points > o2.points){
                                return -1;
                            }

                            return 0;
                        }
                    });
                    mAdapter.resetListData();
                    mAdapter.insertData(displayFriends);

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
            public TextView user;
            public TextView points;
            public TextView pointsNumber;
            public CircleImageView image;

            public ViewHolder(View v) {
                super(v);
                user = (TextView) v.findViewById(R.id.textViewName);
                points = (TextView) v.findViewById(R.id.textViewRang);
                pointsNumber = (TextView) v.findViewById(R.id.textViewPoints);
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
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_score, parent, false);
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
                final FriendObject c = items.get(position);

                String classe = c.name + ", " + c.country;
                Spannable spannable = new SpannableString(classe);
                spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark)), classe.indexOf(c.name + ", "), classe.indexOf(c.name + ", ") + (c.name + ", ").length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                vItem.points.setText(spannable);

                vItem.user.setText(c.name);
                vItem.pointsNumber.setText(getString(R.string.label_points) + c.points);
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
