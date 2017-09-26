package com.verona1024.tutlub.activities.main;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.dialogs.WriteNewsCommentDialog;
import com.verona1024.tutlub.models.NewsObject;
import com.verona1024.tutlub.utils.PostsUtil;

import java.text.SimpleDateFormat;
import java.util.Date;


public class NewsActivity extends AppCompatActivity implements WriteNewsCommentDialog.NoticeDialogListener {
    public static final String NEWS_ITEM = "news_item";
    private ImageLoader imgloader = ImageLoader.getInstance();

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
        setContentView(R.layout.activity_news);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(getString(R.string.activity_news_title));

        final NewsObject newsObject = getIntent().getExtras().getParcelable(NEWS_ITEM);

        ((TextView) findViewById(R.id.textViewTitle)).setText(newsObject.title);
        ((TextView) findViewById(R.id.textView)).setText(fromHtml(newsObject.text));
        ((TextView) findViewById(R.id.textViewComments)).setText(newsObject.number_comments + " comment");
        ((TextView) findViewById(R.id.textViewTag)).setText(fromHtml(newsObject.tag_label));
        imgloader.displayImage(newsObject.media_url, (ImageView) findViewById(R.id.imageView), new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                findViewById(R.id.imageView).setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });

        SimpleDateFormat format = new SimpleDateFormat(PostsUtil.POST_DATA_FORMAT);
        RelativeTimeTextView relativeTimeTextView = (RelativeTimeTextView) findViewById(R.id.textViewDate);
        try {
            Date date = format.parse(newsObject.posted_on);
            relativeTimeTextView.setReferenceTime(date.getTime());
        } catch (Exception e) {
            relativeTimeTextView.setText("" + newsObject.posted_on);
            e.printStackTrace();
        }

        findViewById(R.id.textViewComment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = WriteNewsCommentDialog.newInstance(newsObject);
                newFragment.show(getSupportFragmentManager().beginTransaction(), "dialog");
            }
        });
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

    @Override
    public void onDialogShareClick(int comments) {
        ((TextView) findViewById(R.id.textViewComments)).setText(comments + " comment");
    }
}
