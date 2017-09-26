package com.verona1024.tutlub.fragments.main;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.verona1024.tutlub.R;
import com.verona1024.tutlub.fragments.main.status.FriendsStatusFragment;
import com.verona1024.tutlub.fragments.main.status.MyStatusFragment;
import com.verona1024.tutlub.interfaces.TabsManager;
import com.verona1024.tutlub.interfaces.TabsStateListener;

public class ScoreboardFragment extends Fragment {
    View view;
    Fragment fragmentMyScore, fragmentFriendScore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate view.
        view = inflater.inflate(R.layout.fragment_score, container, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return true;
                }

                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (getActivity() instanceof TabsStateListener){
                        ((TabsStateListener) getActivity()).setFirstTab();
                    }
                    return true;
                }

                return false;
            }
        });

        fragmentFriendScore = new FriendsStatusFragment();
        fragmentMyScore = new MyStatusFragment();

        final TextView myScore = (TextView) view.findViewById(R.id.textViewMyScore);
        final TextView friendsScore = (TextView) view.findViewById(R.id.textViewFriendsScore);

        myScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFragment(fragmentMyScore);
                // TODO: what colours?
                myScore.setTextColor(Color.parseColor("#212121"));
                friendsScore.setTextColor(Color.parseColor("#BDBDBD"));
            }
        });

        friendsScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFragment(fragmentFriendScore);
                // TODO: what colours?
                friendsScore.setTextColor(Color.parseColor("#212121"));
                myScore.setTextColor(Color.parseColor("#BDBDBD"));
            }
        });

        setFragment(fragmentMyScore);

        return view;
    }


    private void setFragment(Fragment fragment){
        if (fragment != null && !fragment.isAdded()) {
            if (view.findViewById(R.id.frame_content) != null) {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_content, fragment);
                transaction.commit();
            }
        } else if (fragment != null && fragment.isAdded() && fragment instanceof TabsManager){
            ((TabsManager) fragment).goToTheTop();
        }
    }
}
