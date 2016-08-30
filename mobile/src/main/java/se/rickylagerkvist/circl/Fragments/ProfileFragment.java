package se.rickylagerkvist.circl.Fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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

    TextView mNameTextView, mEmailTextView, mAboutMeTextView;
    String mUserUid, mDisplayName, mUserEmail, mPhotoString;
    ImageButton mEditAboutMeButton;
    ImageView mPhotoImageView;
    Uri mPhotoUri;
    DatabaseReference myProfileRef;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // get user info
        mUserUid = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("USERUID", "defaultStringIfNothingFound");
        mDisplayName = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("DISPLAY_NAME", "defaultStringIfNothingFound");
        mPhotoString = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("PHOTO_URL", "defaultStringIfNothingFound");
        mPhotoUri = Uri.parse(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("PHOTO_URL", "defaultStringIfNothingFound"));
        mUserEmail = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("EMAIL", "defaultStringIfNothingFound");

        //
        myProfileRef = FirebaseDatabase.getInstance().getReference("profiles").child(mUserUid).child("aboutMe");


        mNameTextView = (TextView) view.findViewById(R.id.profile_name);
        mEmailTextView = (TextView) view.findViewById(R.id.profile_email);
        mPhotoImageView = (ImageView) view.findViewById(R.id.profile_pic);

        mAboutMeTextView = (TextView) view.findViewById(R.id.aboutMeInput);
        mEditAboutMeButton = (ImageButton) view.findViewById(R.id.editAboutMeButton);

        mEditAboutMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editAboutMeActivty = new Intent(getActivity(), EditAboutMeActivty.class);
                startActivity(editAboutMeActivty);
            }
        });


        // change image size
        String biggerPhoto =  mPhotoUri.toString().replace("s96-c", "s150-c");

        // set profile info
        mNameTextView.setText(mDisplayName);
        mEmailTextView.setText(mUserEmail);
        if (mPhotoString.equals("defaultStringIfNothingFound")) {
            mPhotoImageView.setImageResource(R.color.colorPrimary);
        } else {
            Glide.with(this).load(biggerPhoto).into(mPhotoImageView);
        }

        myProfileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String text = dataSnapshot.getValue(String.class);
                mAboutMeTextView.setText(text);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPhotoImageView.setElevation(10);
        } else {
            mPhotoImageView.bringToFront();
            view.requestLayout();
            view.invalidate();
        }





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
