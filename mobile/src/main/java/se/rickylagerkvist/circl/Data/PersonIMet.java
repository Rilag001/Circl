package se.rickylagerkvist.circl.Data;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

/**
 * Created by Ricky on 2016-09-07.
 */
public class PersonIMet {

    private String mName, mPhotoUri, mAddress;
    private double mLat, mLng;
    private HashMap<String, Object> mTimestampMet;

    public PersonIMet() {
    }

    public PersonIMet(String name, String photoUri, String address, HashMap<String, Object> timestampMet, double lat, double lng) {
        mName = name;
        mPhotoUri = photoUri;
        mAddress = address;
        mTimestampMet = timestampMet;
        mLat = lat;
        mLng = lng;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPhotoUri() {
        return mPhotoUri;
    }

    public void setPhotoUri(String photoUri) {
        mPhotoUri = photoUri;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public HashMap<String, Object> getTimestampMet() {
        return mTimestampMet;
    }

    public void setTimestampMet(HashMap<String, Object> timestampMet) {
        mTimestampMet = timestampMet;
    }

    public double getLat() {
        return mLat;
    }

    public void setLat(double lat) {
        mLat = lat;
    }

    public double getLng() {
        return mLng;
    }

    public void setLng(double lng) {
        mLng = lng;
    }

    /*@JsonIgnore
    public long getTimestampLastChangedLong() {
        return (long) mTimestampMet.get("timestamp");
    }*/

    @Exclude
    public long getTimestampLong(){
        return (long)mTimestampMet.get("timestamp");
    }
}
