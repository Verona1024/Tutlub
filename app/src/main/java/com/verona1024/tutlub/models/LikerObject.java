package com.verona1024.tutlub.models;

import android.os.Parcel;
import android.os.Parcelable;

public class LikerObject implements Parcelable {
    /**
     * ID of liker.
     */
    public String liker_id;

    /**
     * Name of liker.
     */
    public String liker_name;

    /**
     * Picture of liker.
     */
    public String liker_picture;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.liker_id);
        dest.writeString(this.liker_name);
        dest.writeString(this.liker_picture);
    }

    public LikerObject() {
    }

    protected LikerObject(Parcel in) {
        this.liker_id = in.readString();
        this.liker_name = in.readString();
        this.liker_picture = in.readString();
    }

    public static final Parcelable.Creator<LikerObject> CREATOR = new Parcelable.Creator<LikerObject>() {
        @Override
        public LikerObject createFromParcel(Parcel source) {
            return new LikerObject(source);
        }

        @Override
        public LikerObject[] newArray(int size) {
            return new LikerObject[size];
        }
    };
}
