package com.example.ankit.simplist;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;

import java.util.logging.Logger;

public class LocationService extends IntentService {

    private static final Logger LOGGER = Logger.getLogger(LocationService.class.getCanonicalName());
    private Object monitor = new Object();

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

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void startLocationUpdate(){
        synchronized (monitor){
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.NO_REQUIREMENT);
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

        }
    }

    @Override
    public void onHandleIntent(Intent intent) {
        this.locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        startLocationUpdate();
    }

    public LocationService() {super("Location Service");}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(this.locationManager != null){
         if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                this.locationManager.removeUpdates(locationListener);
         }
        }
    }
}


