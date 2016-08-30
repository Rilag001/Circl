package se.rickylagerkvist.circl;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class AlertActivity extends AppCompatActivity {

    String mDisplayName, tempPhoto;
    Uri mPhotoUri;
    TextView mContactName;
    ImageView mContactImage;
    boolean isInFront;
    MediaPlayer mp;
    int rawSound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        rawSound = R.raw.bellsbellsbells;
        mp = MediaPlayer.create(getApplicationContext(), rawSound);
        mp.setLooping(true);

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
        mp.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mp.stop();
        isInFront = false;
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("ALERT_IS_INFRONT", isInFront).apply();
        Toast.makeText(AlertActivity.this, "AlertActivity is NOT in front", Toast.LENGTH_SHORT).show();
    }


    public void stopActivity(View view) {
        mp.stop();
        finish();
    }

    public void stopApp(View view) {
        mp.stop();
        finish();
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
    }

}
