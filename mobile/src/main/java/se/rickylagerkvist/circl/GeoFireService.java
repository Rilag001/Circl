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
import android.support.v7.app.AppCompatActivity;
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

    public GeoFire mGeoFire;
    public String mUserUid, userName, userPhotoUri;
    public GeoQuery geoQuery;
    public DatabaseReference firebaseProfiles;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final int REQUEST_CODE_LOCATION = 2;
    private final String LOG_TAG = "TestApp";


    @Override
    public void onCreate() {
        super.onCreate();
        // get user info
        mUserUid = PreferenceManager.getDefaultSharedPreferences(this).getString("USERUID", "defaultStringIfNothingFound");

        if (mUserUid.equals("defaultStringIfNothingFound") || mUserUid.isEmpty() || mUserUid == null){
            Intent i = new Intent(getBaseContext(), MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }

        // init Firebase database
        FirebaseDatabase dataRef = FirebaseDatabase.getInstance();
        firebaseProfiles = dataRef.getReference("profiles");

        // init Geofire
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userLocal");
        mGeoFire = new GeoFire(ref);

        // Build GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(GeoFireService.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(GeoFireService.this)
                .addOnConnectionFailedListener(GeoFireService.this)
                .build();

        mGoogleApiClient.connect();
    }


    public GeoFireService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    public void onDestroy() {
        mGoogleApiClient.disconnect();
        //mGeoFire.removeLocation(mUserUid);
        Toast.makeText(this, "GeoFireService Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
    }


    // local
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000); // Update every second (in ms)

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            /*ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION); */

        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection has been suspended");

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOG_TAG, location.toString());

        geoQuery = mGeoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 0.6);

        // update Geofire
        mGeoFire.setLocation(mUserUid, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
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
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));

                boolean alertActivityActive = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("alertActivityActive", false);
                AppCompatActivity application = (AppCompatActivity) AlertActivity.getContext();
                AlertActivity a = (AlertActivity) application;

                // check that key is not you and that alertActivityActive is not currently open
                if (!key.matches(mUserUid)) {
                    //Toast.makeText(MainActivity.this, String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude), Toast.LENGTH_SHORT).show();

                    firebaseProfiles.child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Profile profile = dataSnapshot.getValue(Profile.class);
                            userName = profile.getName();
                            userPhotoUri = profile.getPhotoUri();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    /*if (userName != null && !userName.isEmpty()){
                        Intent intent = new Intent(getBaseContext(), AlertActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("USER_NAME", userName);
                        intent.putExtra("USER_PHOTO", userPhotoUri);
                        startActivity(intent);
                    }*/

                    Intent intent = new Intent(getBaseContext(), AlertActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("USER_NAME", userName);
                    intent.putExtra("USER_PHOTO", userPhotoUri);
                    startActivity(intent);
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
}
