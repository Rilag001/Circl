package se.rickylagerkvist.circl;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gigamole.navigationtabbar.ntb.NavigationTabBar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import se.rickylagerkvist.circl.Fragments.AchievementsFragment;
import se.rickylagerkvist.circl.Fragments.ChatFragment;
import se.rickylagerkvist.circl.Fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION = 2;
    FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initUI();
        checkForLocationPermit();

        // init to false
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("ALERT_IS_INFRONT", false).apply();

    }

    // check for locationPermit
    private void checkForLocationPermit() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION);
        }
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

            // Start LoginActivity
            Intent startLoginActivity = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(startLoginActivity);

            Intent stopService = new Intent(MainActivity.this, GeoFireService.class);
            stopService(stopService);

        } else if (id == R.id.stop_geoTracking) {
            Intent stopService = new Intent(MainActivity.this, GeoFireService.class);
            stopService(stopService);

        } else  if (id == R.id.start_geoTracking) {
            Intent startService = new Intent(MainActivity.this, GeoFireService.class);
            startService(startService);

        } else  if (id == R.id.settings) {
            Intent startSettingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(startSettingsActivity);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }


    private void initUI() {
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        final Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        final SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        final ViewPager mViewPager = (ViewPager) findViewById(R.id.vp_horizontal_ntb);
        mViewPager.setAdapter(sectionsPagerAdapter);

        // NavigationTabBar
        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_history_white_24dp),
                        R.color.colorPrimary)
                        //.selectedIcon(getResources().getDrawable(R.drawable.rewards_trophy_fill))
                        .title(getString(R.string.Achievements))
                        .badgeTitle("NEW")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_perm_identity_white_24dp),
                        R.color.colorPrimary)
                        //.selectedIcon(getResources().getDrawable(R.drawable.chat_bubble_square_fill))
                        .title(getString(R.string.Chat))
                        .badgeTitle("NEW")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_chat_bubble_outline_white_24dp),
                        R.color.colorPrimary)
                        //.selectedIcon(getResources().getDrawable(R.drawable.user_chat_fill))
                        .title(getString(R.string.Profile))
                        .badgeTitle("NEW")
                        .build()
        );

        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(mViewPager, 1);
        mToolbar.setTitle(R.string.Chat);

        //IMPORTANT: ENABLE SCROLL BEHAVIOUR IN COORDINATOR LAYOUT
        navigationTabBar.setBehaviorEnabled(true);

        navigationTabBar.setOnTabBarSelectedIndexListener(new NavigationTabBar.OnTabBarSelectedIndexListener() {
            @Override
            public void onStartTabSelected(NavigationTabBar.Model model, int index) {

            }

            @Override
            public void onEndTabSelected(NavigationTabBar.Model model, int index) {
                model.hideBadge();
            }
        });
        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position == 0) {
                    mToolbar.setTitle(R.string.Achievements);
                    mFab.hide();
                } else if (position == 1) {
                    mToolbar.setTitle(R.string.Chat);
                    mFab.show();
                } else if (position == 2) {
                    mToolbar.setTitle(R.string.Profile);
                    mFab.hide();
                }

                //animateFab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.parent);
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                /*Intent startAlertActivity = new Intent(MainActivity.this, AlertActivity.class);
                startActivity(startAlertActivity);*/

                /*coordinatorLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final Snackbar snackbar = Snackbar.make(navigationTabBar, "Woho!", Snackbar.LENGTH_SHORT);
                        snackbar.getView().setBackgroundColor(Color.parseColor("#9b92b3"));
                        ((TextView) snackbar.getView().findViewById(R.id.snackbar_text))
                                .setTextColor(Color.parseColor("#423752"));
                        snackbar.show();
                    }
                }, 1000);*/
            }
        });

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            if (position == 0) {
                fragment = new AchievementsFragment();
            } else if (position == 1) {
                fragment = new ChatFragment();
            } else if (position == 2) {
                fragment = new ProfileFragment();
            }

            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

}
