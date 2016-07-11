package se.rickylagerkvist.circl;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class AlertActivity extends AppCompatActivity {

    String mDisplayName, tempPhoto;
    Uri mPhotoUri;
    TextView mContactName;
    ImageView mContactImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        // get name and image
        mDisplayName = PreferenceManager.getDefaultSharedPreferences(this).getString("DISPLAY_NAME", "defaultStringIfNothingFound");
        mPhotoUri = Uri.parse(PreferenceManager.getDefaultSharedPreferences(this).getString("PHOTO_URL", "defaultStringIfNothingFound"));

        mContactName = (TextView) findViewById(R.id.contactName);
        mContactImage = (ImageView) findViewById(R.id.contactImage);

        // change image size
        tempPhoto =  mPhotoUri.toString().replace("s96-c", "s250-c");

        // set name and image
        mContactName.setText("   " + mDisplayName);
        Glide.with(this).load(Uri.parse(tempPhoto)).into(mContactImage);

    }
}
