package com.verona1024.tutlub;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

/**
 * Created by verona1024 on 28.04.17.
 */

public class ThisApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(options)
                .threadPoolSize(6)
                .memoryCache(new WeakMemoryCache())
                .build();

        ImageLoader.getInstance().init(config);

        FacebookSdk.sdkInitialize(getApplicationContext());
    }
}
