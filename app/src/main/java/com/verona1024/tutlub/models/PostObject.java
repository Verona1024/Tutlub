package com.verona1024.tutlub.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PostObject implements Parcelable {
    public String _id;
    public String postId;
    public String posted_on;
    public String owner_name;
    public String owner_image;
    public String owner_id;
    public String text;
    public String media_url;
    public int type;
    public int number_shares;
    public int number_comments;
    public int number_amins;
    public int number_likes;
    public boolean has_media;
    public boolean shared;
    public boolean liked;
    public boolean amined;
    public ArrayList<CommentObject> commentObjects;
    public ArrayList<LikerObject> aminers;
    public ArrayList<LikerObject> likers;
    public ArrayList<LikerObject> shared_by;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this._id);
        dest.writeString(this.postId);
        dest.writeString(this.posted_on);
        dest.writeString(this.owner_name);
        dest.writeString(this.owner_image);
        dest.writeString(this.owner_id);
        dest.writeString(this.text);
        dest.writeString(this.media_url);
        dest.writeInt(this.type);
        dest.writeInt(this.number_shares);
        dest.writeInt(this.number_comments);
        dest.writeInt(this.number_amins);
        dest.writeInt(this.number_likes);
        dest.writeByte(this.has_media ? (byte) 1 : (byte) 0);
        dest.writeByte(this.shared ? (byte) 1 : (byte) 0);
        dest.writeByte(this.liked ? (byte) 1 : (byte) 0);
        dest.writeByte(this.amined ? (byte) 1 : (byte) 0);
        dest.writeTypedList(this.commentObjects);
        dest.writeTypedList(this.aminers);
        dest.writeTypedList(this.likers);
        dest.writeTypedList(this.shared_by);
    }

    public PostObject() {
    }

    protected PostObject(Parcel in) {
        this._id = in.readString();
        this.postId = in.readString();
        this.posted_on = in.readString();
        this.owner_name = in.readString();
        this.owner_image = in.readString();
        this.owner_id = in.readString();
        this.text = in.readString();
        this.media_url = in.readString();
        this.type = in.readInt();
        this.number_shares = in.readInt();
        this.number_comments = in.readInt();
        this.number_amins = in.readInt();
        this.number_likes = in.readInt();
        this.has_media = in.readByte() != 0;
        this.shared = in.readByte() != 0;
        this.liked = in.readByte() != 0;
        this.amined = in.readByte() != 0;
        this.commentObjects = in.createTypedArrayList(CommentObject.CREATOR);
        this.aminers = in.createTypedArrayList(LikerObject.CREATOR);
        this.likers = in.createTypedArrayList(LikerObject.CREATOR);
        this.shared_by = in.createTypedArrayList(LikerObject.CREATOR);
    }

    public static final Creator<PostObject> CREATOR = new Creator<PostObject>() {
        @Override
        public PostObject createFromParcel(Parcel source) {
            return new PostObject(source);
        }

        @Override
        public PostObject[] newArray(int size) {
            return new PostObject[size];
        }
    };
}
