package se.rickylagerkvist.circl;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditAboutMeActivty extends AppCompatActivity {

    DatabaseReference myProfileRef;
    EditText userInput;
    String mUserUid, intentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_about_me_activty);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userInput = (EditText) findViewById(R.id.aboutMeUserInput);
        mUserUid = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("USERUID", "defaultStringIfNothingFound");
        myProfileRef = FirebaseDatabase.getInstance().getReference("profiles").child(mUserUid).child("aboutMe");

        /*Bundle extras = getIntent().getExtras();
        intentText = extras.getString("i");
        userInput.setText(intentText);*/

        myProfileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String text = dataSnapshot.getValue(String.class);
                userInput.setText(text);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                myProfileRef.setValue(userInput.getText().toString());
                Snackbar.make(view, R.string.about_me_updated, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
