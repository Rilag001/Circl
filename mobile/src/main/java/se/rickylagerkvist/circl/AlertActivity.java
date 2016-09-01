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

public class AlertActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    String mDisplayName, tempPhoto;
    Uri mPhotoUri;
    TextView mContactName;
    ImageView mContactImage;
    boolean isInFront;

    MediaPlayer mMediaPlayer;
    int rawSound;

    GoogleMap mMap;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final double
            STOCKHOLM_LAT = 59.329040,
            STOCKHOLM_LNG = 18.068616;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_alert);

        // show if locked http://stackoverflow.com/questions/14554616/start-an-activity-even-when-android-phone-is-in-locked-mode-on-lock-screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                + WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                + WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        if (servicesOK()) {
            setContentView(R.layout.activity_alert_2);

            if (initMap()) {
                goToLocation(STOCKHOLM_LAT, STOCKHOLM_LNG, 15);

            } else {
                Toast.makeText(AlertActivity.this, "Map not connected!", Toast.LENGTH_SHORT).show();
            }

        }

        rawSound = R.raw.bellsbellsbells;
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), rawSound);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        //mMediaPlayer.start();


        Intent i = getIntent();
        Bundle b = i.getExtras();

        if (b != null){
            if (b.get("USER_NAME")!=null && b.get("USER_PHOTO") != null) {
                mDisplayName = (String) b.get("USER_NAME");
                mPhotoUri =  Uri.parse((String) b.get("USER_PHOTO"));
            }
        } else {
            mDisplayName = PreferenceManager.getDefaultSharedPreferences(this).getString("DISPLAY_NAME", "defaultStringIfNothingFound");
            mPhotoUri = Uri.parse(PreferenceManager.getDefaultSharedPreferences(this).getString("PHOTO_URL", "defaultStringIfNothingFound"));
        }


        mContactName = (TextView) findViewById(R.id.contactName);
        mContactImage = (ImageView) findViewById(R.id.contactImage);

        if(mPhotoUri != null){
            // change image size
            tempPhoto =  mPhotoUri.toString().replace("s96-c", "s200-c");
            setNameAndPic();
        }


    }

    public void setNameAndPic(){
        // set name and image
        mContactName.setText("   " + mDisplayName);
        Glide.with(this).load(Uri.parse(tempPhoto)).into(mContactImage);

    }

    @Override
    public void onResume() {
        super.onResume();
        isInFront = true;
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("ALERT_IS_INFRONT", isInFront).apply();
        Toast.makeText(AlertActivity.this, "AlertActivity is in front", Toast.LENGTH_SHORT).show();
        mMediaPlayer.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(AlertActivity.this, "AlertActivity is OnStart", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        //mMediaPlayer.stop();
        /*mMediaPlayer.release();
        mMediaPlayer = null;*/
        isInFront = false;
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("ALERT_IS_INFRONT", isInFront).apply();
        Toast.makeText(AlertActivity.this, "AlertActivity is NOT in front", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
    }

    public void stopActivity(View view) {
        mMediaPlayer.stop();
        finish();
    }

    public void stopApp(View view) {
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
                    goToLocation(STOCKHOLM_LAT, STOCKHOLM_LNG, 18);
                }
            });

        }
        return (mMap != null);
    }

    // long, lat, zoom,
    private void goToLocation(double lat, double lng, float zoom) {

        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);

        // Add Marker to Map
        MarkerOptions option = new MarkerOptions();
        option.title("My Location");;
        option.position(latLng);
        Marker currentMarker = mMap.addMarker(option);
        currentMarker.showInfoWindow();
    }
}
