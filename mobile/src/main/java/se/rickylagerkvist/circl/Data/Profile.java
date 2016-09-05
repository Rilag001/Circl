package se.rickylagerkvist.circl.Data;

/**
 * Created by Ricky on 2016-07-07.
 */
public class Profile {

    private String mName;
    private String mPhotoUri;
    private String mAboutMe;


    public Profile() {
    }

    public Profile(String name, String photoUri, String aboutMe) {
        this.mName = name;
        this.mPhotoUri = photoUri;
        this.mAboutMe = aboutMe;


    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getPhotoUri() {
        return mPhotoUri;
    }

    public void setPhotoUri(String photoUri) {
        mPhotoUri = photoUri;
    }

    public String getAboutMe() {
        return mAboutMe;
    }

    public void setAboutMe(String aboutMe) {
        mAboutMe = aboutMe;
    }
}
