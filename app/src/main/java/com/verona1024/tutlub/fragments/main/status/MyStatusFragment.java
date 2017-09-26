package com.verona1024.tutlub.fragments.main.status;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.verona1024.tutlub.R;
import com.verona1024.tutlub.activities.main.LevelActivity;
import com.verona1024.tutlub.utils.RankUtil;
import com.verona1024.tutlub.utils.UserUtil;

public class MyStatusFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate view.
        View view = inflater.inflate(R.layout.fragment_my_score, container, false);

        ((TextView) view.findViewById(R.id.textViewPrayerStatus)).setText(RankUtil.getNameByPoints(getActivity().getApplicationContext(), UserUtil.points));
        ((TextView) view.findViewById(R.id.textViewBointsNumber)).setText(RankUtil.pointsToNextLevel(UserUtil.points));
        ((TextView) view.findViewById(R.id.textViewPointNumber)).setText(getString(R.string.label_your_points) + UserUtil.points);
        ((ImageView) view.findViewById(R.id.imageViewImage)).setImageResource(RankUtil.imageBigByPoints(UserUtil.points));

        view.findViewById(R.id.linearLayoutPints).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LevelActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
