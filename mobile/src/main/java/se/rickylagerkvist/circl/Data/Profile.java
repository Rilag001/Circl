package se.rickylagerkvist.circl.Data;

/**
 * Created by Ricky on 2016-07-07.
 */
public class Profile {

    private String name;
    private String mPhotoUri;
    private boolean likesMovies;
    private boolean likesSports;

    public Profile() {
    }

    public Profile(String name, String photoUri, boolean likesMovies, boolean likesSports) {
        this.name = name;
        mPhotoUri = photoUri;
        this.likesMovies = likesMovies;
        this.likesSports = likesSports;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUri() {
        return mPhotoUri;
    }

    public void setPhotoUri(String photoUri) {
        mPhotoUri = photoUri;
    }

    public boolean isLikesMovies() {
        return likesMovies;
    }

    public void setLikesMovies(boolean likesMovies) {
        this.likesMovies = likesMovies;
    }

    public boolean isLikesSports() {
        return likesSports;
    }

    public void setLikesSports(boolean likesSports) {
        this.likesSports = likesSports;
    }
}
