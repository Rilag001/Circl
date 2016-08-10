package se.rickylagerkvist.circl;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class AlertActivity extends AppCompatActivity
        implements Application.ActivityLifecycleCallbacks{

    String mDisplayName, tempPhoto;
    Uri mPhotoUri;
    TextView mContactName;
    ImageView mContactImage;

    private boolean inForeground;
    private static Context mContext;

    MediaPlayer mp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        this.mContext = this;

        // start playing sound
        mp = MediaPlayer.create(getApplicationContext(), R.raw.bumbibjornarna);
        mp.start();

        // stop service
        Intent startGeoFireService = new Intent(this, GeoFireService.class);
        getApplicationContext().stopService(startGeoFireService);

        Intent i = getIntent();
        Bundle b = i.getExtras();

        // get name and image
        if (b.get("USER_NAME")!=null && b.get("USER_PHOTO") != null) {
            mDisplayName = (String) b.get("USER_NAME");
            mPhotoUri =  Uri.parse((String) b.get("USER_PHOTO"));
        } else {
            mDisplayName = PreferenceManager.getDefaultSharedPreferences(this).getString("DISPLAY_NAME", "defaultStringIfNothingFound");
            mPhotoUri = Uri.parse(PreferenceManager.getDefaultSharedPreferences(this).getString("PHOTO_URL", "defaultStringIfNothingFound"));
        }


        mContactName = (TextView) findViewById(R.id.contactName);
        mContactImage = (ImageView) findViewById(R.id.contactImage);

        // change image size
        tempPhoto =  mPhotoUri.toString().replace("s96-c", "s250-c");

        // set name and image
        mContactName.setText("   " + mDisplayName);
        Glide.with(this).load(Uri.parse(tempPhoto)).into(mContactImage);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("alertActivityActive", true).apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("alertActivityActive", false).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("alertActivityActive", false).apply();
    }

    public void stopActivity(View view) {
        onStop();
        //stop playing
        mp.stop();
        // start service
        Intent startGeoFireService = new Intent(this, GeoFireService.class);
        getApplicationContext().startService(startGeoFireService);
        //exit
        System.exit(1);
    }

    public void goToMainActivity(View view) {
        // start service
        Intent startGeoFireService = new Intent(this, GeoFireService.class);
        getApplicationContext().startService(startGeoFireService);
        //stop playing
        mp.stop();
        Intent intent = new Intent(AlertActivity.this, MainActivity.class);
        startActivity(intent);
    }

    // activitylifecycleCallbacks
    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        inForeground = activity instanceof AlertActivity;

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public boolean isInForeground() {
        return inForeground;
    }

    // get context
    public static Context getContext(){
        return mContext;
    }
}
