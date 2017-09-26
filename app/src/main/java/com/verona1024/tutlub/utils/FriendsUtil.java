package com.verona1024.tutlub.utils;

import com.verona1024.tutlub.models.FriendObject;
import com.verona1024.tutlub.models.FriendsInvites;
import com.verona1024.tutlub.models.SuggestedPeople;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FriendsUtil {
    public static ArrayList<FriendObject> myFriendObjects = new ArrayList<>();
    public static ArrayList<FriendObject> myPendingFriendObjects = new ArrayList<>();
    public static ArrayList<FriendObject> personFriendObjects = new ArrayList<>();
    public static ArrayList<FriendObject> myFollowingObjects = new ArrayList<>();
    public static ArrayList<SuggestedPeople> mySuggestedFriends = new ArrayList<>();
    public static ArrayList<FriendsInvites> myInvitedFriends = new ArrayList<>();

    public static boolean isFriend(String friendId){
        for (FriendObject friendObject : myFriendObjects){
            if (friendObject.friendId.equalsIgnoreCase(friendId)){
                return true;
            }
        }

        return false;
    }


    public static boolean isFollowingFriend(String friendId){
        for (FriendObject friendObject : myFollowingObjects){
            if (friendObject.friendId.equalsIgnoreCase(friendId)){
                return true;
            }
        }

        return false;
    }

    public static void setFriends(JSONObject friendsListJson) throws JSONException {
        JSONArray following = friendsListJson.getJSONArray("following");
        FriendObject friendObject;
        myFollowingObjects = new ArrayList<>();
        if (following.length() != 0) {
            for (int i = 0; i < following.length(); i++) {
                friendObject = new FriendObject();
                JSONObject jsonObject = following.getJSONObject(i);
                friendObject.friendId = jsonObject.getString("id");
                friendObject.name = jsonObject.getString("name");
                friendObject.picture = jsonObject.getString("picture");
                friendObject.country = jsonObject.getString("country");
                friendObject.city = jsonObject.getString("city");
                friendObject.points = jsonObject.getInt("points");
                friendObject.index = "" + i;
                myFollowingObjects.add(friendObject);
            }
        }

        JSONArray friends = friendsListJson.getJSONArray("friends");
        myFriendObjects = new ArrayList<>();
        if (friends.length() != 0) {
            for (int i = 0; i < friends.length(); i++) {
                friendObject = new FriendObject();
                JSONObject jsonObject = friends.getJSONObject(i);
                friendObject.friendId = jsonObject.getString("id");
                friendObject.name = jsonObject.getString("name");
                friendObject.picture = jsonObject.getString("picture");
                friendObject.country = jsonObject.getString("country");
                friendObject.city = jsonObject.getString("city");
                friendObject.points = jsonObject.getInt("points");
                friendObject.index = "" + i;
                myFriendObjects.add(friendObject);
            }
        }

        JSONArray pendingInvitations = friendsListJson.getJSONArray("pendingInvitations");
        myPendingFriendObjects = new ArrayList<>();
        if (pendingInvitations.length() != 0) {
            for (int i = 0; i < pendingInvitations.length(); i++) {
                friendObject = new FriendObject();
                JSONObject jsonObject = pendingInvitations.getJSONObject(i);
                friendObject.friendId = jsonObject.getString("id");
                friendObject.name = jsonObject.getString("name");
                friendObject.picture = jsonObject.getString("picture");
                friendObject.country = jsonObject.getString("country");
                friendObject.city = jsonObject.getString("city");
                friendObject.points = jsonObject.getInt("points");
                friendObject.index = "" + i;
                myPendingFriendObjects.add(friendObject);
            }
        }
    }

    public static void setPendingFriends(String body) throws JSONException {
        JSONObject responseJson = new JSONObject(body);
        JSONArray pendingInvitations = responseJson.getJSONObject("friends").getJSONArray("pendingInvitations");
        myInvitedFriends = new ArrayList<>();
        for(int i=0; i < pendingInvitations.length(); i++) {
            try {
                FriendsInvites friendsInvites = new FriendsInvites();
                friendsInvites.peopleId = pendingInvitations.getJSONObject(i).getString("id");
                friendsInvites.name = pendingInvitations.getJSONObject(i).getString("name");
                friendsInvites.picture = pendingInvitations.getJSONObject(i).getString("picture");
                friendsInvites.country = pendingInvitations.getJSONObject(i).getString("country");
                friendsInvites.city = pendingInvitations.getJSONObject(i).getString("city");
                friendsInvites.points = pendingInvitations.getJSONObject(i).getInt("points");
                myInvitedFriends.add(friendsInvites);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setSuggestedFriends(String body) throws JSONException {
        JSONObject responseJson = new JSONObject(body);
        JSONArray suggestions = responseJson.getJSONArray("suggestions");
        mySuggestedFriends = new ArrayList<>();
        for(int i=0; i < suggestions.length(); i++) {
            try {
                SuggestedPeople suggestedPeople = new SuggestedPeople();
                suggestedPeople._id = suggestions.getJSONObject(i).getString("_id");
                suggestedPeople.peopleId = suggestions.getJSONObject(i).getString("id");
                suggestedPeople.name = suggestions.getJSONObject(i).getString("name");
                suggestedPeople.picture = suggestions.getJSONObject(i).getString("picture");
                suggestedPeople.country = suggestions.getJSONObject(i).getString("country");
                suggestedPeople.city = suggestions.getJSONObject(i).getString("city");
                suggestedPeople.points = suggestions.getJSONObject(i).getInt("points");
                suggestedPeople.index = suggestions.getJSONObject(i).getInt("index");
                mySuggestedFriends.add(suggestedPeople);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setFriendsObjects(String body) throws  JSONException {
        myFriendObjects = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(body);
        JSONArray jsonArray = jsonObject.getJSONObject("friends").getJSONArray("friends");
        for (int i = 0; i < jsonArray.length(); i++){
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                FriendObject friendObject = new FriendObject();
                friendObject.friendId = object.getString("id");
                friendObject.name = object.getString("name");
                friendObject.picture = object.getString("picture");
                friendObject.country = object.getString("country") == null ? "" : object.getString("country");
                friendObject.city = object.getString("city") == null ? "" : object.getString("city");
                friendObject.points = object.getInt("points");
                myFriendObjects.add(friendObject);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
