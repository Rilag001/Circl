package se.rickylagerkvist.circl;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import se.rickylagerkvist.circl.Data.PersonIMet;

public class AlertActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private String mOtherUserDisplayName, mOtherUseTempPhoto, mOtherUserUidKey, mMyUidKey;
    private Uri mOtherUsePhotoUri;
    private boolean mIsInFront;
    private int mNrAmountOfPeopleIMet;

    private MediaPlayer mMediaPlayer;

    public GoogleMap mMap;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private double mMyLat, mMyLng, mOtherUserLat, mOtherUserLng;

    private TextView mName, mConversationStarter;
    private ImageView mContactImage;

    private FloatingActionButton mStopActivity, mStopApp, mMuteSound;

    private DatabaseReference mIWantToEngage, mUserWantsToEngage, mOnlineUsers, mPeopleIMet, mAmountOfPeopleIMet;
    private ValueEventListener mWantToEngageListener, mAmountOfPeopleListener;

    Alarm mAlarm = new Alarm();

    private PersonIMet person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getGeoFireIntents();
        setScreenToBeOn();
        checkIfServicesOK();
        setUpMediaPlayer();
        initUi();
        initFireDataBase();

        // FloatingActionButtons
        mStopActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //engageWithUser();
                mIWantToEngage.setValue("true");
            }
        });

        mStopApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //doNotEngageWithUser();
                mIWantToEngage.setValue("false");
            }
        });

        mMuteSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer.isPlaying()){
                    mMediaPlayer.pause();
                    mMuteSound.setImageResource(R.drawable.ic_volume_up_white_48dp);
                } else {
                    mMediaPlayer.start();
                    mMuteSound.setImageResource(R.drawable.ic_volume_off_white_48dp);
                }

            }
        });
    }

    private void initFireDataBase() {

        // set that you want to engageWithUser with them
        mMyUidKey = PreferenceManager.getDefaultSharedPreferences(this).getString("USERUID", "defaultStringIfNothingFound");
        mIWantToEngage = FirebaseDatabase.getInstance().getReference("mEstablishedConnection").child(mMyUidKey);
        //mIWantToEngage.setValue("");

        mPeopleIMet = FirebaseDatabase.getInstance().getReference("peopleIMet").child(mMyUidKey).child(mOtherUserUidKey);
        mAmountOfPeopleIMet = FirebaseDatabase.getInstance().getReference("amountOfPeopleIMet").child(mMyUidKey);

        // Check if they want to engageWithUser with you
        mUserWantsToEngage = FirebaseDatabase.getInstance().getReference("mEstablishedConnection").child(mOtherUserUidKey);
        mUserWantsToEngage.addValueEventListener(mWantToEngageListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);

                if (data.equalsIgnoreCase("false")){
                    doNotEngageWithUser();
                } else if (data.equalsIgnoreCase("true")) {
                    engageWithUser();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initUi() {

        mStopActivity = (FloatingActionButton) findViewById(R.id.stopActivityFab);
        mStopApp = (FloatingActionButton) findViewById(R.id.stopAppFab);
        mMuteSound = (FloatingActionButton)findViewById(R.id.muteFab);

        mName = (TextView) findViewById(R.id.otherUserName);
        //mConversationStarter = (TextView) findViewById(R.id.conversationStarter);
        mContactImage = (ImageView) findViewById(R.id.contactImage);

        if(mOtherUsePhotoUri != null){
            // change image size
            mOtherUseTempPhoto =  mOtherUsePhotoUri.toString();//.replace("s96-c", "s150-c");
            setNameAndPic();
        }

    }

    private void getGeoFireIntents() {
        Intent i = getIntent();
        Bundle b = i.getExtras();

        if (b != null){
            if (b.get("USER_NAME")!=null && b.get("USER_PHOTO") != null) {
                mOtherUserDisplayName = (String) b.get("USER_NAME");
                mOtherUsePhotoUri =  Uri.parse((String) b.get("USER_PHOTO"));

                //Coordinates
                mMyLat = (double) b.get("YOUR_LAT");
                mMyLng = (double) b.get("YOUR_LON");
                mOtherUserLat = (double) b.get("OTHER_USER_LAT");
                mOtherUserLng = (double) b.get("OTHER_USER_LON");

                // key
                mOtherUserUidKey = (String) b.get("USER_KEY");
            }
        }
    }


    private void setUpMediaPlayer() {
        // user selected sound
        int rawSound = R.raw.bellsbellsbells;
        // Create player
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), rawSound);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }

    private void checkIfServicesOK() {
        if (servicesOK()) {
            setContentView(R.layout.activity_alert_2);

            if (initMap()) {
                goToLocation(mMyLat, mMyLng, 15);

            } else {
                Toast.makeText(AlertActivity.this, "Map not connected!", Toast.LENGTH_SHORT).show();
            }

        } else {
            onDestroy();;
        }
    }

    private void setScreenToBeOn() {
        // show if locked http://stackoverflow.com/questions/14554616/start-an-activity-even-when-android-phone-is-in-locked-mode-on-lock-screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                + WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                + WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    public void setNameAndPic(){
        // set name and image
        mName.setText(mOtherUserDisplayName);
        Glide.with(this).load(Uri.parse(mOtherUseTempPhoto)).into(mContactImage);

    }

    @Override
    public void onResume() {
        super.onResume();
        mIsInFront = true;
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("ALERT_IS_INFRONT", mIsInFront).apply();
        //Toast.makeText(AlertActivity.this, "AlertActivity is in front", Toast.LENGTH_SHORT).show();
        mMediaPlayer.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Toast.makeText(AlertActivity.this, "AlertActivity is OnStart", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsInFront = false;
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("ALERT_IS_INFRONT", mIsInFront).apply();
        //Toast.makeText(AlertActivity.this, "AlertActivity is NOT in front", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mUserWantsToEngage.removeEventListener(mWantToEngageListener);
        if (mAmountOfPeopleListener !=  null) {
            mAmountOfPeopleIMet.removeEventListener(mAmountOfPeopleListener);
        }

        if(mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void engageWithUser() {
        mIWantToEngage.setValue("true");

        // save person WRONG NAME?
        HashMap<String, Object> timestampMet = new HashMap<>();
        timestampMet.put("timestamp", ServerValue.TIMESTAMP);
        PersonIMet person = new PersonIMet(mOtherUserDisplayName, mOtherUseTempPhoto, mMyLat, mMyLng, timestampMet);
        //DatabaseReference newListRef = mPeopleIMet.push();
        mPeopleIMet.setValue(person);
        //newListRef.setValue(person);

        // add 1 to amountOfPeopleIMet
        mAmountOfPeopleIMet.addValueEventListener(mAmountOfPeopleListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    mNrAmountOfPeopleIMet = 0;
                } else {
                    mNrAmountOfPeopleIMet = dataSnapshot.getValue(int.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mAmountOfPeopleIMet.setValue(mNrAmountOfPeopleIMet + 1);

        // set Online to false
        mOnlineUsers = FirebaseDatabase.getInstance().getReference("onlineUsers");
        mOnlineUsers.child(mMyUidKey).setValue(false);

        //mIWantToEngage.setValue("");

        Intent stopGroService = new Intent(AlertActivity.this, GeoFireService.class);
        stopService(stopGroService);

        // set Alarm
        mAlarm.setAlarm(getApplicationContext());

        mMediaPlayer.stop();
        finish();
    }

    public void doNotEngageWithUser() {

        // do not want to engage
        mIWantToEngage.setValue("false");

        // set Online to false
        mOnlineUsers = FirebaseDatabase.getInstance().getReference("onlineUsers");
        mOnlineUsers.child(mMyUidKey).setValue(false);

        // stop service
        Intent stopGroService = new Intent(AlertActivity.this, GeoFireService.class);
        stopService(stopGroService);

        // set Alarm
        mAlarm.setAlarm(getApplicationContext());

        //
        mMediaPlayer.stop();
        finish();
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
    }


    // map
    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // Check if the play services work
    public boolean servicesOK() {

        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog =
                    GooglePlayServicesUtil.getErrorDialog(isAvailable, this, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(AlertActivity.this, "Can´t connect to mapping services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    // initialise map, check if it was successful
    private boolean initMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    mMap.getUiSettings().setZoomControlsEnabled(false);
                    goToLocation(mMyLat, mMyLng, 15);
                }
            });


        }
        return (mMap != null);
    }

    // long, lat, zoom,
    private void goToLocation(final double lat, double lng, float zoom) {

        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);



        Marker you = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mMyLat, mMyLng))
                .anchor(0.5f, 0.5f));


        Marker otherUser = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mOtherUserLat, mOtherUserLng))
                .anchor(0.5f, 0.5f)
                .title(mOtherUserDisplayName));


        otherUser.showInfoWindow();
    }
}
