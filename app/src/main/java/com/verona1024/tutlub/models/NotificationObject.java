package com.verona1024.tutlub.models;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationObject implements Parcelable {
    public String message;
    public String image;
    public String when;
    public long postid;
    public boolean seen;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.message);
        dest.writeString(this.image);
        dest.writeString(this.when);
        dest.writeLong(this.postid);
        dest.writeByte(this.seen ? (byte) 1 : (byte) 0);
    }

    public NotificationObject() {
    }

    protected NotificationObject(Parcel in) {
        this.message = in.readString();
        this.image = in.readString();
        this.when = in.readString();
        this.postid = in.readLong();
        this.seen = in.readByte() != 0;
    }

    public static final Creator<NotificationObject> CREATOR = new Creator<NotificationObject>() {
        @Override
        public NotificationObject createFromParcel(Parcel source) {
            return new NotificationObject(source);
        }

        @Override
        public NotificationObject[] newArray(int size) {
            return new NotificationObject[size];
        }
    };
}
