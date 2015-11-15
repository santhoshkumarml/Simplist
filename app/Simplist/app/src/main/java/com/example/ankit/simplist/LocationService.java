package com.example.ankit.simplist;

import android.app.Service;
import java.util.logging.Logger;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Criteria;
import android.support.v4.content.ContextCompat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.widget.Toast;

public class LocationService extends Service {

    private static final Logger LOGGER = Logger.getLogger(LocationService.class.getCanonicalName());
    private Object monitor = new Object();

    private NotificationManager nm;
    private Timer timer = new Timer();
    private TimerTask  task = null;

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


    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        nm = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        startLocationUpdate();
//        Toast.makeText(this,"Service created at " + time.getTime(), Toast.LENGTH_LONG).show();
//        CharSequence text = getText(R.string.service_started);
//        showNotification(text);
//        startTimer();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(locationManager != null){
         if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.removeUpdates(locationListener);
         }
        }
    }

    private void startLocationUpdate(){
        synchronized (monitor){
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.NO_REQUIREMENT);
            provider = locationManager.getBestProvider(criteria, true);
            if(provider == null){
                LOGGER.info("no location provider");
            }
            if(locationManager != null){
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(provider, 5000L, -1, locationListener);
                }
            }

        }
    }
}


