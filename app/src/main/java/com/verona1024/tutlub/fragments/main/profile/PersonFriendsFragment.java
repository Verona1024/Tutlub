package com.verona1024.tutlub.fragments.main.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
import com.verona1024.tutlub.activities.main.PersonProfileActivity;
import com.verona1024.tutlub.models.FriendObject;
import com.verona1024.tutlub.utils.FriendsUtil;
import com.verona1024.tutlub.utils.RankUtil;
import com.verona1024.tutlub.utils.RequestUtil;
import com.verona1024.tutlub.utils.UserUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by verona1024 on 09.05.17.
 */

public class PersonFriendsFragment extends Fragment {
    private RecyclerView recyclerView;
    private FriendsListAdapter mAdapter;
    private EditText editTextSearch;
    private ArrayList<FriendObject> displayFriends;
    private String personId;

    public static PersonFriendsFragment newInstance() {
        
        Bundle args = new Bundle();
        
        PersonFriendsFragment fragment = new PersonFriendsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static PersonFriendsFragment newInstance(String personId) {

        Bundle args = new Bundle();
        args.putString("personid", personId);

        PersonFriendsFragment fragment = new PersonFriendsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        personId = getArguments().getString("personid");

        // Inflate view.
        View view = inflater.inflate(R.layout.fragment_person_friends, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        if (personId == null) {
            displayFriends = FriendsUtil.myFriendObjects;
        } else {
            displayFriends = FriendsUtil.personFriendObjects;
        }
        mAdapter = new FriendsListAdapter(getActivity(), recyclerView, displayFriends);
        recyclerView.setAdapter(mAdapter);

        editTextSearch = (EditText) view.findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                displayFriends.clear();
                for (FriendObject friendObject : personId == null ? FriendsUtil.myFriendObjects : FriendsUtil.personFriendObjects){
                    if (friendObject.name.toLowerCase().contains(charSequence.toString().toLowerCase())){
                        displayFriends.add(friendObject);
                    }
                }
                mAdapter.resetListData();
                mAdapter.insertData(displayFriends);
            }

            @Override
            public void afterTextChanged(Editable editable) {
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

//        Log.e("123", "Start");
        Call<ResponseBody> call;
        if (personId == null) {
            call = trackerInternetProvider.getFriends(RequestUtil.OAUTH_HEADER);
        } else {
            call = trackerInternetProvider.getUserById(personId, RequestUtil.OAUTH_HEADER);
        }
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
//                Log.e("123", "onResponse");
//                Log.e("OAUTH_HEADER", RequestUtil.OAUTH_HEADER);
                try {
                    String body;
                    if (response.body() == null){
                        body = response.errorBody().string();
                        Log.e("123", body);
                    } else {
                        body = response.body().string();
                    }

                    ArrayList<FriendObject> friendObjects = new ArrayList<FriendObject>();
                    JSONObject jsonObject = new JSONObject(body);
                    JSONArray jsonArray;
                    if (personId == null) {
                        jsonArray = jsonObject.getJSONObject("friends").getJSONArray("friends");
                    } else {
                        jsonArray = jsonObject.getJSONObject("profile").getJSONObject("friendsList").getJSONArray("friends");
                    }
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
                            friendObjects.add(friendObject);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    if (personId == null) {
                        FriendsUtil.myFriendObjects = friendObjects;
                    } else {
                        FriendsUtil.personFriendObjects = friendObjects;
                    }
                    displayFriends = personId == null ? FriendsUtil.myFriendObjects : FriendsUtil.personFriendObjects;
                    if (editTextSearch.getText().toString().isEmpty()) {
                        displayFriends = personId == null ? FriendsUtil.myFriendObjects : FriendsUtil.personFriendObjects;
                        mAdapter.resetListData();
                        mAdapter.insertData(displayFriends);
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            requestData();
                        }
                    }, 10 * 1000);
//                    Log.e("123", body);
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

    public class FriendsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        //        private static final int HEADER = 2;
        private static final int OTHER = 3;

        private final int VIEW_ITEM = 1;
        private final int VIEW_PROG = 0;
        private boolean loading;

        private List<FriendObject> items = new ArrayList<>();
        private Context ctx;
        private ImageLoader imgloader = ImageLoader.getInstance();

        private OnLoadMoreListener onLoadMoreListener;
//        private OnItemClickListener mOnItemClickListener;

//        public interface OnItemClickListener {
//            void onItemClick(View view, Category obj, int position);
//        }

//        public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
//            this.mOnItemClickListener = mItemClickListener;
//        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView user;
            public TextView points;
//            public TextView country;
            public CircleImageView image;

            public ViewHolder(View v) {
                super(v);
                user = (TextView) v.findViewById(R.id.textViewName);
                points = (TextView) v.findViewById(R.id.textViewRang);
//                country = (TextView) v.findViewById(R.id.textViewCountry);
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
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
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

//                vItem.country.setText(c.city + ", " + c.country);

                String classe = RankUtil.getNameByPoints(c.points) + ", " + c.country;
                Spannable spannable = new SpannableString(classe);
                spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark)), classe.indexOf(RankUtil.getNameByPoints(c.points) + ", "), classe.indexOf(RankUtil.getNameByPoints(c.points) + ", ") + (RankUtil.getNameByPoints(c.points) + ", ").length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                vItem.points.setText(spannable);

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
//                vItem.points.setText(RankUtil.getNameByPoints(c.points) + ", ");
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
