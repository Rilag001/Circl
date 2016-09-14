package se.rickylagerkvist.circl.Utils;

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

import se.rickylagerkvist.circl.AlertActivity;
import se.rickylagerkvist.circl.Data.Profile;

public class GeoFireService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "GeoFireService";
    private String mUserUid, mUserName, mUserPhotoUri, mUserEngageData;
    private boolean mClientIsOnline;
    private final String LOG_TAG = "TestApp";

    // GeoFire and FireBase ref
    static public GeoFire mGeoFire;
    private GeoQuery mGeoQuery;
    private DatabaseReference mFireBaseProfiles, mIWantToEngage, mUserWantsToEngage;
    public static DatabaseReference mOnlineUsers;

    // GoogleApiClient
    private GoogleApiClient mGoogleApiClient;

    // EventListeners
    ValueEventListener mFireBaseProfilesListener, mClientOnlineListener, mWantToEngageListener;

    @Override
    public void onCreate() {
        super.onCreate();
        checkUserUid();
        initFireBaseAndGeoFire();
        buildGoogleApiClient();
    }

    // Build ApiClient
    private void buildGoogleApiClient() {
        // Build GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(GeoFireService.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(GeoFireService.this)
                .addOnConnectionFailedListener(GeoFireService.this)
                .build();

        mGoogleApiClient.connect();
    }

    // init FireBase ref
    private void initFireBaseAndGeoFire() {
        // init Firebase database
        FirebaseDatabase dataRef = FirebaseDatabase.getInstance();
        mFireBaseProfiles = dataRef.getReference("profiles");

        mOnlineUsers = dataRef.getReference("onlineUsers");
        mOnlineUsers.child(mUserUid).setValue(true);
        mOnlineUsers.child(mUserUid).onDisconnect().setValue(false);

        mIWantToEngage = FirebaseDatabase.getInstance().getReference("mEstablishedConnection").child(mUserUid);

        // init Geofire
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userLocal");
        mGeoFire = new GeoFire(ref);
    }

    // stop service if USERUID is null (has not been set up properly)
    private void checkUserUid() {
        // get user info
        mUserUid = PreferenceManager.getDefaultSharedPreferences(this).getString("USERUID", "defaultStringIfNothingFound");
        if (mUserUid.equals("defaultStringIfNothingFound") || mUserUid.isEmpty() || mUserUid == null){
            Intent stopGeoFireService = new Intent(getBaseContext(), GeoFireService.class);
            stopService(stopGeoFireService);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // GoogleApiClient functions
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // Update every 10 seconds (in ms)

        // If device does not have location permission, open SplashActivity
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(getBaseContext(), SplashActivity.class);
            startActivity(intent);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection has been suspended");
    }

    @Override
    public void onLocationChanged(final Location myLocation) {
        Log.i(LOG_TAG, myLocation.toString());

        // get user set pref distance, convert to double, divide by 10 for right format
        int geoFireDistanceiInt = PreferenceManager.getDefaultSharedPreferences(this).getInt("SEARCH_AREA_NR", 1) + 1;
        double geoFireDistance = (1.0 * geoFireDistanceiInt) / 10;

        //double geoFireDistance  (0.1 = 100 m)
        Toast.makeText(GeoFireService.this, "" + geoFireDistance, Toast.LENGTH_SHORT).show();

        // set mGeoQuery to this device lat, lon and geoFireDistance
        mGeoQuery = mGeoFire.queryAtLocation(new GeoLocation(myLocation.getLatitude(), myLocation.getLongitude()), geoFireDistance);

        // update GeoFire to listen to this device lat & lon
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
                //System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, otherUserLocation.latitude, otherUserLocation.longitude));

                if (!key.matches(mUserUid)){
                    mIWantToEngage.setValue(key);

                    // init mUserWantsToEngage and set listener
                    mUserWantsToEngage = FirebaseDatabase.getInstance().getReference("mEstablishedConnection").child(key);
                    mUserWantsToEngage.addValueEventListener(mWantToEngageListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mUserEngageData = dataSnapshot.getValue(String.class);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    // proceed if key is online, AlertActivity is not in front and mUserEngageData is set to mUserUid
                    if (keyIsOnline(key) && mUserEngageData.equals(mUserUid)
                            && !PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("ALERT_IS_INFRONT", false)){

                        // get profile info
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

                        // if mUserName & mUserPhotoUri is not null, run AlertActivity
                        if (mUserName != null && mUserPhotoUri !=null) {
                            Intent intent = new Intent(getBaseContext(), AlertActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("USER_NAME", mUserName);
                            intent.putExtra("USER_PHOTO", mUserPhotoUri);
                            intent.putExtra("USER_KEY", key);

                            // my coordinates
                            intent.putExtra("YOUR_LAT", myLocation.getLatitude());
                            intent.putExtra("YOUR_LON", myLocation.getLongitude());

                            // other user coordinates
                            intent.putExtra("OTHER_USER_LAT", otherUserLocation.latitude);
                            intent.putExtra("OTHER_USER_LON", otherUserLocation.longitude);

                            startActivity(intent);
                        }
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GoogleApiClient connection has failed");
    }

    // check if client is online
    private boolean keyIsOnline(String key) {

        mOnlineUsers.child(key).addValueEventListener(mClientOnlineListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null){
                    mClientIsOnline = dataSnapshot.getValue(boolean.class);
                } else
                    mClientIsOnline = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return mClientIsOnline;
    };

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
        if (mWantToEngageListener != null) {
            mUserWantsToEngage.removeEventListener(mWantToEngageListener);
        }

        mOnlineUsers.child(mUserUid).setValue(false);

        Toast.makeText(this, "GeoFireService Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
    }
}