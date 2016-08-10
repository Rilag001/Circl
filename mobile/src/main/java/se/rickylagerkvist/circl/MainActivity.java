package se.rickylagerkvist.circl;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;

import se.rickylagerkvist.circl.Fragments.AchievementsFragment;
import se.rickylagerkvist.circl.Fragments.ContactsFragment;
import se.rickylagerkvist.circl.Fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FloatingActionButton mFab;
    GeoFire mGeoFire;
    String mUserUid;
    private static final int REQUEST_CODE_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // start service
        Intent startGeoFireService = new Intent(this, GeoFireService.class);
        getApplicationContext().startService(startGeoFireService);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION);
        }


        // get user info
        mUserUid = PreferenceManager.getDefaultSharedPreferences(this).getString("USERUID", "defaultStringIfNothingFound");


        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Does nothing yet :)", Toast.LENGTH_SHORT).show();
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        // Add StartFragment. Are later switched to other fragment with Drawer options
        ProfileFragment profileFragment = new ProfileFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.content_container, profileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        getSupportActionBar().setTitle("Profile");

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.log_out) {
            // Log out of FireBase
            FirebaseAuth.getInstance().signOut();

            // Blank user sharedPref.
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("USERUID", null).apply();
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("DISPLAY_NAME", null).apply();
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("PHOTO_URL", null).apply();
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("EMAIL", null).apply();

            // Start LoginActivity
            Intent startLoginActivity = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(startLoginActivity);
        } else if (id == R.id.stop_geoTacking) {
            mGeoFire.removeLocation("mUserUid");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // 1. Create a ProfileFragment
            ProfileFragment profileFragment = new ProfileFragment();
            // 2. FragmentManager
            FragmentManager manager = getSupportFragmentManager();
            // 3. Start FragmentTransaction
            FragmentTransaction transaction = manager.beginTransaction();
            // 4. Set Action
            transaction.replace(R.id.content_container, profileFragment);
            // Run transaction
            transaction.addToBackStack(null);
            transaction.commit();
            // Set titel
            getSupportActionBar().setTitle(item.getTitle());
            // show fab
            //fab.show();
        } else if (id == R.id.nav_achievements) {
            AchievementsFragment achievementsFragment = new AchievementsFragment();
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.content_container, achievementsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            getSupportActionBar().setTitle(item.getTitle());
            //fab.hide();
        } else if (id == R.id.nav_contacts) {
            ContactsFragment contactsFragment = new ContactsFragment();
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.content_container, contactsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            getSupportActionBar().setTitle(item.getTitle());
            //fab.hide();
        } else if (id == R.id.nav_share) {

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
