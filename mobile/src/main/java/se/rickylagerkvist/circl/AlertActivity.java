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
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
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
import com.google.firebase.database.ValueEventListener;

public class AlertActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private String mYourDisplayName, mOtherUserDisplayName, mOtherUseTempPhoto, mYourTempPhoto, mUserKey, mUserUid;
    private Uri mOtherUsePhotoUri, mYourPhotoUri;
    private boolean mIsInFront;

    private MediaPlayer mMediaPlayer;

    public GoogleMap mMap;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private double mMyLat, mMyLng, mOtherUserLat, mOtherUserLng;

    private TextView mName, mConversationStarter;
    private ImageView mContactImage;

    private FloatingActionButton mStopActivity, mStopApp;

    private DatabaseReference mIWantToEngage, mUserWantsToEngage, mOnlineUsers;
    private ValueEventListener mWantToEngageListener;

    Alarm mAlarm = new Alarm();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getGeoFireIntents();
        setScreenToBeOn();
        checkIfServicesOK();
        setUpMediaPlayer();
        initUi();

        // set that you want to engageWithUser with them
        mUserUid = PreferenceManager.getDefaultSharedPreferences(this).getString("USERUID", "defaultStringIfNothingFound");
        mIWantToEngage = FirebaseDatabase.getInstance().getReference("mEstablishedConnection").child(mUserUid);
        //mIWantToEngage.setValue("");

        // Check if they want to engageWithUser with you
        mUserWantsToEngage = FirebaseDatabase.getInstance().getReference("mEstablishedConnection").child(mUserKey);
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

        mStopActivity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                int action = MotionEventCompat.getActionMasked(motionEvent);

                switch(action) {
                    case (MotionEvent.ACTION_MOVE) :
                        engageWithUser();
                        return true;
                    case (MotionEvent.ACTION_UP) :
                        engageWithUser();
                        return true;
                }
                return  true;
            }
        });

        mStopApp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                int action = MotionEventCompat.getActionMasked(motionEvent);

                switch(action) {
                    case (MotionEvent.ACTION_MOVE) :
                        doNotEngageWithUser();
                        return true;
                    case (MotionEvent.ACTION_UP) :
                        doNotEngageWithUser();
                        return true;
                }
                return  true;
            }
        });
    }

    private void initUi() {

        mStopActivity = (FloatingActionButton) findViewById(R.id.stopActivity);
        mStopApp = (FloatingActionButton) findViewById(R.id.stopApp);

        mName = (TextView) findViewById(R.id.otherUserName);
        //mConversationStarter = (TextView) findViewById(R.id.conversationStarter);
        mContactImage = (ImageView) findViewById(R.id.contactImage);

        if(mOtherUsePhotoUri != null){
            // change image size
            mOtherUseTempPhoto =  mOtherUsePhotoUri.toString();//.replace("s96-c", "s150-c");
            setNameAndPic();
        }

        mYourPhotoUri = Uri.parse(PreferenceManager.getDefaultSharedPreferences(this).getString("PHOTO_URL", "defaultStringIfNothingFound"));
        mYourTempPhoto = mYourPhotoUri.toString().replace("s96-c", "s150-c");
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
                mUserKey = (String) b.get("USER_KEY");
            }
        } else {
            mOtherUserDisplayName = PreferenceManager.getDefaultSharedPreferences(this).getString("DISPLAY_NAME", "defaultStringIfNothingFound");
            mOtherUsePhotoUri = Uri.parse(PreferenceManager.getDefaultSharedPreferences(this).getString("PHOTO_URL", "defaultStringIfNothingFound"));
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
        mName.setText("   " + mOtherUserDisplayName);
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

        if(mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

    }

    public void engageWithUser() {
        mIWantToEngage.setValue("true");
        mMediaPlayer.stop();
        finish();

        // set Online to false
        mOnlineUsers = FirebaseDatabase.getInstance().getReference("onlineUsers");
        mOnlineUsers.child(mUserUid).setValue(false);

        mIWantToEngage.setValue("");

        Intent stopGroService = new Intent(AlertActivity.this, GeoFireService.class);
        stopService(stopGroService);

        // set Alarm
        mAlarm.setAlarm(getApplicationContext());

    }

    public void doNotEngageWithUser() {
        mIWantToEngage.setValue("false");
        mMediaPlayer.stop();
        finish();
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);

        // set Online to false
        mOnlineUsers = FirebaseDatabase.getInstance().getReference("onlineUsers");
        mOnlineUsers.child(mUserUid).setValue(false);

        mIWantToEngage.setValue("");

        Intent stopGroService = new Intent(AlertActivity.this, GeoFireService.class);
        stopService(stopGroService);

        // set Alarm
        mAlarm.setAlarm(getApplicationContext());
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


    public void muteMusic(View view) {
        mMediaPlayer.stop();
    }
}
