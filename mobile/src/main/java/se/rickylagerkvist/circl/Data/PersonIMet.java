package se.rickylagerkvist.circl.Data;

import java.util.HashMap;

/**
 * Created by Ricky on 2016-09-07.
 */
public class PersonIMet {

    private String mName, photoUri;
    private double mLat, mLon;
    private HashMap<String, Object> mTimestampMet;

    public PersonIMet() {
    }

    public PersonIMet(String name, String photoUri, double lat, double lon, HashMap<String, Object> timestampMet) {
        mName = name;
        this.photoUri = photoUri;
        mLat = lat;
        mLon = lon;
        mTimestampMet = timestampMet;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public double getLat() {
        return mLat;
    }

    public void setLat(double lat) {
        mLat = lat;
    }

    public double getLon() {
        return mLon;
    }

    public void setLon(double lon) {
        mLon = lon;
    }

    public HashMap<String, Object> getTimestampMet() {
        return mTimestampMet;
    }

    public void setTimestampMet(HashMap<String, Object> timestampMet) {
        mTimestampMet = timestampMet;
    }
}
