package se.rickylagerkvist.circl;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import se.rickylagerkvist.circl.Data.PersonIMet;
import se.rickylagerkvist.circl.Utils.Alarm;
import se.rickylagerkvist.circl.Utils.GeoFireService;

public class AlertActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private String mOtherUserDisplayName, mOtherUseTempPhoto, mOtherUserUidKey, mMyUidKey;
    private Uri mOtherUsePhotoUri;
    private boolean mIsInFront;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private ArrayList<String> mConversationStarters;
    private boolean mEngageAlreadyExecuted, mNotEngagedAlreadyExecuted;

    // Google map, lat and lng
    public GoogleMap mMap;
    //private static final int ERROR_DIALOG_REQUEST = 9001;
    private double mMyLat, mMyLng, mOtherUserLat, mOtherUserLng;

    // Views
    private TextView mName;
    private ImageView mContactImage;
    private FloatingActionButton mStopActivity, mStopApp, mMuteSound;
    View mEngageScreen;
    TextView mPointsTextView, mConversationStarterTextView;

    // FireBase Ref
    private DatabaseReference mIWantToEngage, mPeopleIMet, mAmountOfPeopleIMet;
    private ValueEventListener mWantToEngageListener, mAmountOfPeopleListener;

    // MediaPlayer, Alarm and Vibrator
    private MediaPlayer mMediaPlayer;
    Alarm mAlarm = new Alarm();
    Vibrator mVibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getGeoFireIntents();
        setScreenToBeOn();
        checkIfServicesOK();
        setUpMediaPlayer();
        initUi();
        initFireDataBase();
        addConversationStarters(mConversationStarters);

        // Get instance of Vibrator from current Context
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Start without a delay, Vibrate for 100 milliseconds, Sleep for 1000 milliseconds
        long[] pattern = {0, 1000, 1000};

        // '0' = repeat indefinitely
        mVibrator.vibrate(pattern, 0);

        // FloatingActionButtons
        mStopActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //engageWithUser();
                mIWantToEngage.setValue("true");
                if (mEngageScreen.getVisibility() == View.INVISIBLE){
                    wantToEngageWindow();
                }
            }
        });

        mStopApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //doNotEngageWithUser();
                mIWantToEngage.setValue("false");
                if (mEngageScreen.getVisibility() == View.INVISIBLE){
                    doNotWantToEngageWindow();
                }
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

    private void addConversationStarters(ArrayList<String> conversationStarters) {
        conversationStarters.add("Where did you grow up?");
        conversationStarters.add("Do you sleep with a stuffed animal?");
        conversationStarters.add("Tell me about your first car.");
        conversationStarters.add("Do you believe people are inherently good?");
        conversationStarters.add("What is the most valuable thing that you own?");
        conversationStarters.add("What do you like to do in your spare time?");
        conversationStarters.add("What is your favorite day of the week?");
        conversationStarters.add("Do you play any instruments?");
        conversationStarters.add("If you could meet anyone in history, who would it be?");
    }

    private void initFireDataBase() {

        // set that you want to engageWithUser with them
        mMyUidKey = PreferenceManager.getDefaultSharedPreferences(this).getString("USERUID", "defaultStringIfNothingFound");
        mIWantToEngage = FirebaseDatabase.getInstance().getReference("mEstablishedConnection").child(mMyUidKey);

        mPeopleIMet = FirebaseDatabase.getInstance().getReference("peopleIMet").child(mMyUidKey).child(mOtherUserUidKey);
        mAmountOfPeopleIMet = FirebaseDatabase.getInstance().getReference("amountOfPeopleIMet").child(mMyUidKey);

        // Check if they want to engageWithUser with you
        DatabaseReference userWantsToEngage = FirebaseDatabase.getInstance().getReference("mEstablishedConnection").child(mOtherUserUidKey);
        userWantsToEngage.addValueEventListener(mWantToEngageListener = new ValueEventListener() {
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
        mContactImage = (ImageView) findViewById(R.id.contactImage);

        if(mOtherUsePhotoUri != null){
            mOtherUseTempPhoto =  mOtherUsePhotoUri.toString();
            setNameAndPic();
        }

        mEngageScreen = findViewById(R.id.engageScreen);
        mPointsTextView = (TextView) findViewById(R.id.points);
        mConversationStarterTextView = (TextView) findViewById(R.id.conversationStarter);

        mConversationStarters = new ArrayList<>();
        mEngageScreen.setVisibility(View.INVISIBLE);

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
                goToLocationAndAddMarkers(mMyLat, mMyLng, 15);

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
        mMediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsInFront = false;
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("ALERT_IS_INFRONT", mIsInFront).apply();
    }


    public void engageWithUser() {

        // check if this function have been executed before
        if(!mEngageAlreadyExecuted){

            // this will trigger this function in the other users device
            mIWantToEngage.setValue("true");

            // save person
            HashMap<String, Object> timestampMet = new HashMap<>();
            timestampMet.put("timestamp", ServerValue.TIMESTAMP);
            String address = getAddress(AlertActivity.this, mMyLat, mMyLng);
            PersonIMet person = new PersonIMet(mOtherUserDisplayName, mOtherUseTempPhoto, address, timestampMet, mMyLat, mMyLng);
            mPeopleIMet.setValue(person);

            // add 1 to amountOfPeopleIMet
            mAmountOfPeopleIMet.addListenerForSingleValueEvent(mAmountOfPeopleListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    int mNrAmountOfPeopleIMet = 0;

                    if (dataSnapshot.getValue() == null) {
                        mNrAmountOfPeopleIMet = 0;
                    } else if (dataSnapshot.getValue() != null){
                        mNrAmountOfPeopleIMet = dataSnapshot.getValue(int.class);
                    }
                    mAmountOfPeopleIMet.setValue(mNrAmountOfPeopleIMet + 1);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            // show EngageWindow for 10 s, stop mVibrator and mMediaPlayer
            wantToEngageWindow();

            // do not run this function again
            mEngageAlreadyExecuted = true;
        }


    }

    private void wantToEngageWindow() {
        mVibrator.cancel();

        // mEngageScreen visible
        mEngageScreen.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        mEngageScreen.setVisibility(View.VISIBLE);
        mPointsTextView.setText("1");
        mPointsTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        mConversationStarterTextView.setText(mConversationStarters.get(new Random().nextInt(mConversationStarters.size())));
        mConversationStarterTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));

        if (mMediaPlayer != null){
            mMediaPlayer.stop();
        }

        // stop service
        Intent stopGroService = new Intent(AlertActivity.this, GeoFireService.class);
        stopService(stopGroService);

        // set Alarm
        mAlarm.setAlarm(getApplicationContext());

        // stop in 10 sec
        new CountDownTimer(10000, 1000) { // 5000 = 5 sec

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                finish();
            }
        }.start();

    }

    public void doNotEngageWithUser() {

        if (!mNotEngagedAlreadyExecuted){

            // do not want to engage
            mIWantToEngage.setValue("false");

            // set Online to false
            DatabaseReference onlineUsers = FirebaseDatabase.getInstance().getReference("onlineUsers");
            onlineUsers.child(mMyUidKey).setValue(false);

            // stop service
            Intent stopGroService = new Intent(AlertActivity.this, GeoFireService.class);
            stopService(stopGroService);


            if (mMediaPlayer != null){
                mMediaPlayer.stop();
            }
            // show EngageWindow for 10 s, stop mVibrator and mMediaPlayer
            doNotWantToEngageWindow();

            // do not run this function again
            mNotEngagedAlreadyExecuted = true;
        }
    }

    private void doNotWantToEngageWindow() {
        mVibrator.cancel();

        // mEngageScreen visible
        mEngageScreen.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.color1));
        mEngageScreen.setVisibility(View.VISIBLE);
        mPointsTextView.setText("0");
        mPointsTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color1));
        mConversationStarterTextView.setText(R.string.dont_engage_text);
        mConversationStarterTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.color1));

        if (mMediaPlayer != null){
            mMediaPlayer.stop();
        }

        // set Alarm
        mAlarm.setAlarm(getApplicationContext());

        // stop in 10 sec
        new CountDownTimer(10000, 1000) { // 5000 = 5 sec

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                finish();
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_HOME);
                startActivity(i);
            }
        }.start();
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

    // http://stackoverflow.com/questions/31016722/googleplayservicesutil-vs-googleapiavailability
    // Check if the play services work
    public boolean servicesOK() {

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
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
                    goToLocationAndAddMarkers(mMyLat, mMyLng, 15);
                }
            });


        }
        return (mMap != null);
    }

    // long, lat, zoom,
    private void goToLocationAndAddMarkers(final double lat, double lng, float zoom) {

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

    // http://stackoverflow.com/questions/22096044/getting-a-place-name-in-google-map-in-android
    public String getAddress(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            String city = addresses.get(0).getLocality();
            String country = addresses.get(0).getCountryName();

            return country + ", " +  city;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // destroy listeners
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //mUserWantsToEngage.removeEventListener(mWantToEngageListener);
        if (mAmountOfPeopleListener !=  null) {
            mAmountOfPeopleIMet.removeEventListener(mAmountOfPeopleListener);
        }

        if(mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
