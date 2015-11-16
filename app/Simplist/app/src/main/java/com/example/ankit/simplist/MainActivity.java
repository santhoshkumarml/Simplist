package com.example.ankit.simplist;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements ConfigFragment.ConfigFragmentInteractionListener    {

    private static final Logger LOGGER = Logger.getLogger(MainActivity.class.getCanonicalName());
    private Object monitor = new Object();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        this.locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        startLocationUpdate();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch(position) {
                case 0 : return ConfigFragment.newInstance();
                case 1 : return NotificationFragment.getInstance(position);
                default:break;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Configuration";
                case 1:
                    return "Notifications";
            }
            return null;
        }
    }


    public static class NotificationFragment extends Fragment {

        List<ConfigElement> notificationElements = new ArrayList<ConfigElement>();
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static NotificationFragment getInstance(int sectionNumber) {
            NotificationFragment fragment = new NotificationFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.notifications, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    @Override
    public void onFragmentInteraction(String id) {

    }


    LocationManager locationManager;

    private String provider = null;
    private volatile Location currentLocation = null;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LOGGER.info("Location update received");
            synchronized (monitor){
                currentLocation = location;
                LOGGER.info(currentLocation.toString());
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            LOGGER.info("Status Chaged"+provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            LOGGER.info("Provider Enabled"+provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            LOGGER.info("Provider Disbled"+provider);
        }
    };

    private void startLocationUpdate(){
       synchronized (monitor){
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);

            this.provider = this.locationManager.getBestProvider(criteria, true);
            if(provider == null){
                LOGGER.info("no location provider");
            }
            if(this.locationManager != null){
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED){
                    this.locationManager.requestLocationUpdates(provider, 0L, 0, this.locationListener);
                }
            }
            this.currentLocation = this.locationManager.getLastKnownLocation(provider);
            LOGGER.info("Location Updates Will Start, currentLocation "+this.currentLocation);
       }
    }

}
