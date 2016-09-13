package se.rickylagerkvist.circl.Fragments;


import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import se.rickylagerkvist.circl.Data.PersonIMet;
import se.rickylagerkvist.circl.PeopleIMetCardAdapter;
import se.rickylagerkvist.circl.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AchievementsFragment extends Fragment {

    private TextView mPointsTextView;
    private ListView mListView;
    private DatabaseReference mPoints, mPeopleIMet;
    private PeopleIMetCardAdapter mPeopleIMetCardAdapter;

    public AchievementsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_achievements, container, false);

        mListView = (ListView) rootView.findViewById(R.id.listviwe_achievements);
        mPointsTextView = (TextView) rootView.findViewById(R.id.points);

        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(),"shadowsIntoLight.ttf"); // create a typeface from the raw ttf
        mPointsTextView.setTypeface(typeface); // apply the typeface to the textview

        // get my key
        String mMyUidKey = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("USERUID", "defaultStringIfNothingFound");

        // firebase ref
        mPoints = FirebaseDatabase.getInstance().getReference("amountOfPeopleIMet").child(mMyUidKey);
        mPeopleIMet = FirebaseDatabase.getInstance().getReference("peopleIMet").child(mMyUidKey);

        mPoints.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    int mNrAmountOfPeopleIMet = dataSnapshot.getValue(int.class);
                    mPointsTextView.setText(mPointsTextView.getText() + " " + mNrAmountOfPeopleIMet);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // adapter
        mPeopleIMetCardAdapter = new PeopleIMetCardAdapter(getActivity(), PersonIMet.class,
               R.layout.contact_card, mPeopleIMet);
        mListView.setAdapter(mPeopleIMetCardAdapter);

        return rootView;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        mPeopleIMetCardAdapter.cleanup();
    }
}
