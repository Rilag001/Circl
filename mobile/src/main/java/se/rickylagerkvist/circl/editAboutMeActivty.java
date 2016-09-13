package se.rickylagerkvist.circl;

import android.os.Bundle;
import android.os.CountDownTimer;
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
    String mUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_about_me_activty);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userInput = (EditText) findViewById(R.id.aboutMeUserInput);
        mUserUid = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("USERUID", "defaultStringIfNothingFound");
        myProfileRef = FirebaseDatabase.getInstance().getReference("profiles").child(mUserUid).child("aboutMe");


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

                // finish() in 2 sec
                new CountDownTimer(2000, 1000) { // 2000 = 5 sec

                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        finish();
                    }
                }.start();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
