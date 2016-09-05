package se.rickylagerkvist.circl.Fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import se.rickylagerkvist.circl.EditAboutMeActivty;
import se.rickylagerkvist.circl.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    TextView mNameTextView, mAboutMeTextView;
    String mUserUid, mDisplayName, mPhotoString;
    ImageButton mEditAboutMeButton;
    ImageView mPhotoImageView;
    Uri mPhotoUri;
    DatabaseReference myProfileRef;
    ValueEventListener profileListener;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // database ref
        myProfileRef = FirebaseDatabase.getInstance().getReference("profiles").child(mUserUid).child("aboutMe");
        getUserInfo();
        initUI(view);


        // open EditAboutMeActivty to edit aboutMe
        mEditAboutMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editAboutMeActivty = new Intent(getActivity(), EditAboutMeActivty.class);
                startActivity(editAboutMeActivty);
            }
        });


        myProfileRef.addValueEventListener(profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String text = dataSnapshot.getValue(String.class);
                mAboutMeTextView.setText(text);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void initUI(View view) {
        mNameTextView = (TextView) view.findViewById(R.id.profile_name);
        mPhotoImageView = (ImageView) view.findViewById(R.id.profile_pic);
        mAboutMeTextView = (TextView) view.findViewById(R.id.aboutMeInput);
        mEditAboutMeButton = (ImageButton) view.findViewById(R.id.editAboutMeButton);

        // change image size
        String biggerPhoto =  mPhotoUri.toString().replace("s96-c", "s150-c");

        // set profile info
        mNameTextView.setText(mDisplayName);
        if (mPhotoString.equals("defaultStringIfNothingFound")) {
            mPhotoImageView.setImageResource(R.color.colorPrimary);
        } else {
            Glide.with(this).load(biggerPhoto).into(mPhotoImageView);
        }
    }

    private void getUserInfo() {
        // get user info
        mUserUid = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("USERUID", "defaultStringIfNothingFound");
        mDisplayName = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("DISPLAY_NAME", "defaultStringIfNothingFound");
        mPhotoString = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("PHOTO_URL", "defaultStringIfNothingFound");
        mPhotoUri = Uri.parse(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("PHOTO_URL", "defaultStringIfNothingFound"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myProfileRef.removeEventListener(profileListener);
    }


}
