package com.verona1024.tutlub.models;

import android.os.Parcel;
import android.os.Parcelable;

public class CommentObject implements Parcelable {
    /**
     * Comment text.
     */
    public String comment;

    /**
     * Poster ID.
     */
    public String poster_id;

    /**
     * Posters name.
     */
    public String poster_name;

    /**
     * Posters image.
     */
    public String poster_picture;

    /**
     * ID of current comment.
     */
    public String commentId;

    /**
     * _id of comment.
     */
    public String _id;

    /**
     * When was posted.
     */
    public String posted_on;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.comment);
        dest.writeString(this.poster_id);
        dest.writeString(this.poster_name);
        dest.writeString(this.poster_picture);
        dest.writeString(this.commentId);
        dest.writeString(this._id);
        dest.writeString(this.posted_on);
    }

    public CommentObject() {
    }

    protected CommentObject(Parcel in) {
        this.comment = in.readString();
        this.poster_id = in.readString();
        this.poster_name = in.readString();
        this.poster_picture = in.readString();
        this.commentId = in.readString();
        this._id = in.readString();
        this.posted_on = in.readString();
    }

    public static final Parcelable.Creator<CommentObject> CREATOR = new Parcelable.Creator<CommentObject>() {
        @Override
        public CommentObject createFromParcel(Parcel source) {
            return new CommentObject(source);
        }

        @Override
        public CommentObject[] newArray(int size) {
            return new CommentObject[size];
        }
    };
}
