package se.rickylagerkvist.circl.Fragments;


import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import se.rickylagerkvist.circl.Data.Profile;
import se.rickylagerkvist.circl.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    TextView mNameTextView;
    CheckBox mLikeMoviesCheckBox, mLikeSportsCheckBox;
    String mUserUid, mDisplayName, mProfilePicURL;
    ImageView mPhotoImageView;
    Uri mPhotoUri;
    //Boolean mLikeMovies, mLikeSports;
    DatabaseReference myProfileRef;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myProfileRef = database.getReference("profiles");

        // get user info
        mUserUid = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("USERUID", "defaultStringIfNothingFound");
        mDisplayName = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("DISPLAY_NAME", "defaultStringIfNothingFound");
        mPhotoUri = Uri.parse(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("PHOTO_URL", "defaultStringIfNothingFound"));



        mNameTextView = (TextView) view.findViewById(R.id.profile_name);
        mPhotoImageView = (ImageView) view.findViewById(R.id.profile_pic);
        // set profile info
        mNameTextView.setText("   " + mDisplayName);
        Glide.with(this).load(mPhotoUri).into(mPhotoImageView);


        mLikeMoviesCheckBox = (CheckBox) view.findViewById(R.id.likes_movies);
        mLikeSportsCheckBox = (CheckBox) view.findViewById(R.id.likes_sports);

        mLikeSportsCheckBox.setChecked(true);

        mProfilePicURL = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("PHOTO_URL", "defaultStringIfNothingFound");
        //check current state of a check box (true or false)
        Boolean mLikeMovies = mLikeMoviesCheckBox.isChecked();
        Boolean mLikeSports = mLikeSportsCheckBox.isChecked();

        Profile profile = new Profile(mDisplayName, mProfilePicURL, mLikeMovies, mLikeSports);
        //myProfileRef.child(mUserUid).setValue(profile);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
