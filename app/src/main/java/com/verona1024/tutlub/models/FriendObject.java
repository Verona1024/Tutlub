package com.verona1024.tutlub.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by verona1024 on 10.05.17.
 */

public class FriendObject implements Parcelable {
    public String friendId;
    public String name;
    public String picture;
    public String country;
    public String city;
    public int points;
    public String index;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.friendId);
        dest.writeString(this.name);
        dest.writeString(this.picture);
        dest.writeString(this.country);
        dest.writeString(this.city);
        dest.writeInt(this.points);
        dest.writeString(this.index);
    }

    public FriendObject() {
    }

    protected FriendObject(Parcel in) {
        this.friendId = in.readString();
        this.name = in.readString();
        this.picture = in.readString();
        this.country = in.readString();
        this.city = in.readString();
        this.points = in.readInt();
        this.index = in.readString();
    }

    public static final Creator<FriendObject> CREATOR = new Creator<FriendObject>() {
        @Override
        public FriendObject createFromParcel(Parcel source) {
            return new FriendObject(source);
        }

        @Override
        public FriendObject[] newArray(int size) {
            return new FriendObject[size];
        }
    };
}
