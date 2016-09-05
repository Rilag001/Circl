package se.rickylagerkvist.circl;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import se.rickylagerkvist.circl.Data.Profile;

public class GeoFireService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "GeoFireService";

    static public GeoFire mGeoFire;
    public String mUserUid, mUserName, mUserPhotoUri;
    public GeoQuery mGeoQuery;
    public DatabaseReference mFireBaseProfiles, mOnlineUsers;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    //private static final int REQUEST_CODE_LOCATION = 2;
    private final String LOG_TAG = "TestApp";

    boolean clientIsOnline;

    ValueEventListener mFireBaseProfilesListener, mClientOnlineListener;

    //private ArrayList<String> contacts;


    @Override
    public void onCreate() {
        super.onCreate();
        //contacts = new ArrayList<>();
        checkUserUid();
        initFireBaseAndGeoFire();
        buildGoogleApiClient();
    }

    private void buildGoogleApiClient() {
        // Build GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(GeoFireService.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(GeoFireService.this)
                .addOnConnectionFailedListener(GeoFireService.this)
                .build();

        mGoogleApiClient.connect();
    }

    private void initFireBaseAndGeoFire() {
        // init Firebase database
        FirebaseDatabase dataRef = FirebaseDatabase.getInstance();
        mFireBaseProfiles = dataRef.getReference("profiles");

        mOnlineUsers = dataRef.getReference("onlineUsers");
        mOnlineUsers.child(mUserUid).setValue(true);
        mOnlineUsers.child(mUserUid).onDisconnect().setValue(false);

        // init Geofire
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userLocal");
        mGeoFire = new GeoFire(ref);
    }

    private void checkUserUid() {
        // get user info
        mUserUid = PreferenceManager.getDefaultSharedPreferences(this).getString("USERUID", "defaultStringIfNothingFound");

        if (mUserUid.equals("defaultStringIfNothingFound") || mUserUid.isEmpty() || mUserUid == null){

            Intent stopGeoFireService = new Intent(getBaseContext(), GeoFireService.class);
            stopService(stopGeoFireService);
        }
    }


    public void onDestroy() {
        mGoogleApiClient.disconnect();
        mGeoFire.removeLocation(mUserUid);

        if (mClientOnlineListener != null){
            mOnlineUsers.removeEventListener(mClientOnlineListener);
        }
        if (mFireBaseProfilesListener != null){
            mFireBaseProfiles.removeEventListener(mFireBaseProfilesListener);
        }
        if (mGeoQuery != null) {
            mGeoQuery.removeAllListeners();
        }

        Toast.makeText(this, "GeoFireService Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // local
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000); // Update every 10 seconds (in ms)

        // If device does not have location permission, open SplashActivity
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(getBaseContext(), SplashActivity.class);
            startActivity(intent);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection has been suspended");

    }

    @Override
    public void onLocationChanged(final Location myLocation) {
        Log.i(LOG_TAG, myLocation.toString());

        Toast.makeText(GeoFireService.this, "Your location is." + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();

        // get user set pref distance
        double geoFireDistance = 0.1; // 100 m

        mGeoQuery = mGeoFire.queryAtLocation(new GeoLocation(myLocation.getLatitude(), myLocation.getLongitude()), geoFireDistance);

        // update Geofire
        mGeoFire.setLocation(mUserUid, new GeoLocation(myLocation.getLatitude(), myLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }
        });

        // GeoQueryEventListener
        mGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation otherUserLocation) {
                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, otherUserLocation.latitude, otherUserLocation.longitude));

                Toast.makeText(GeoFireService.this, String.format("Key %s entered the search area at [%f,%f]", key, otherUserLocation.latitude, otherUserLocation.longitude), Toast.LENGTH_SHORT).show();


                // check that key is not you, and key is online and AlertActivity is not visible
                if (!key.matches(mUserUid) && keyIsOnline(key) && !PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("ALERT_IS_INFRONT", false)) {


                    mFireBaseProfiles.child(key).addValueEventListener(mFireBaseProfilesListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Profile profile = dataSnapshot.getValue(Profile.class);
                            mUserName = profile.getName();
                            mUserPhotoUri = profile.getPhotoUri();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    // if mUserName & mUserPhotoUri is not null, open Alert
                    if (mUserName != null && mUserPhotoUri !=null
                            /*&& !contacts.contains(key)*/) {
                        Intent intent = new Intent(getBaseContext(), AlertActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("USER_NAME", mUserName);
                        intent.putExtra("USER_PHOTO", mUserPhotoUri);

                        // my coordinates
                        intent.putExtra("YOUR_LAT", myLocation.getLatitude());
                        intent.putExtra("YOUR_LON", myLocation.getLongitude());

                        // other user coordinates
                        intent.putExtra("OTHER_USER_LAT", otherUserLocation.latitude);
                        intent.putExtra("OTHER_USER_LON", otherUserLocation.longitude);

                        startActivity(intent);
                        //contacts.add(key);
                        //Toast.makeText(GeoFireService.this, key + " added to contacts", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onKeyExited(String key) {
                System.out.println(String.format("Key %s is no longer in the search area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });

    }

    private boolean keyIsOnline(String key) {

        //check that key-client is online
        mOnlineUsers.child(key).addValueEventListener(mClientOnlineListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null){
                    clientIsOnline = dataSnapshot.getValue(boolean.class);
                } else
                    clientIsOnline = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return clientIsOnline;
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GoogleApiClient connection has failed");

    }


}
