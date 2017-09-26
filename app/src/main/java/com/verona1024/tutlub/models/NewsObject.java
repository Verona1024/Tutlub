package com.verona1024.tutlub.models;

import android.os.Parcel;
import android.os.Parcelable;

public class NewsObject implements Parcelable {
    public String _id;
    public String postId;
    public String title;
    public String text;
    public String media_url;
    public String publish_time;
    public String posted_on;
    public String tag_id;
    public String tag_label;
    public int __v;
    public int number_comments;
    public int number_amins;
    public int number_likes;
    public boolean has_media;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this._id);
        dest.writeString(this.postId);
        dest.writeString(this.title);
        dest.writeString(this.text);
        dest.writeString(this.media_url);
        dest.writeString(this.publish_time);
        dest.writeString(this.posted_on);
        dest.writeString(this.tag_id);
        dest.writeString(this.tag_label);
        dest.writeInt(this.__v);
        dest.writeInt(this.number_comments);
        dest.writeInt(this.number_amins);
        dest.writeInt(this.number_likes);
        dest.writeByte(this.has_media ? (byte) 1 : (byte) 0);
    }

    public NewsObject() {
    }

    protected NewsObject(Parcel in) {
        this._id = in.readString();
        this.postId = in.readString();
        this.title = in.readString();
        this.text = in.readString();
        this.media_url = in.readString();
        this.publish_time = in.readString();
        this.posted_on = in.readString();
        this.tag_id = in.readString();
        this.tag_label = in.readString();
        this.__v = in.readInt();
        this.number_comments = in.readInt();
        this.number_amins = in.readInt();
        this.number_likes = in.readInt();
        this.has_media = in.readByte() != 0;
    }

    public static final Parcelable.Creator<NewsObject> CREATOR = new Parcelable.Creator<NewsObject>() {
        @Override
        public NewsObject createFromParcel(Parcel source) {
            return new NewsObject(source);
        }

        @Override
        public NewsObject[] newArray(int size) {
            return new NewsObject[size];
        }
    };
}
