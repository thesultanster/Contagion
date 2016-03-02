package tag.zombie.contagion.Util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by sultankhan on 1/11/16.
 */
public class ProviderLocationTracker implements LocationListener, LocationTracker {

    // The minimum distance to change Updates in millimeters
    private long MIN_UPDATE_DISTANCE = 10000;

    // The minimum time between updates in milliseconds
    private long MIN_UPDATE_TIME = 400000;

    private LocationManager lm;

    public enum ProviderType{
        NETWORK,
        GPS
    };
    private String provider;

    private Location lastLocation;
    private long lastTime;

    private boolean isRunning;

    private LocationUpdateListener listener;

    // Constructors
    public ProviderLocationTracker(Context context, ProviderType type) {

        // Set Location Manager
        lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        // Select Provider Type
        if(type == ProviderType.NETWORK){
            provider = LocationManager.NETWORK_PROVIDER;
        }
        else{
            provider = LocationManager.GPS_PROVIDER;
        }
    }

    public ProviderLocationTracker(Context context, ProviderType type, long MIN_UPDATE_TIME) {

        // Set Time Interval
        this.MIN_UPDATE_TIME = MIN_UPDATE_TIME;

        // Set Location Manager
        lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        // Select Provider Type
        if(type == ProviderType.NETWORK){
            provider = LocationManager.NETWORK_PROVIDER;
        }
        else{
            provider = LocationManager.GPS_PROVIDER;
        }
    }

    public ProviderLocationTracker(Context context, ProviderType type, long MIN_UPDATE_TIME, long MIN_UPDATE_DISTANCE) {

        // Set Time Interval
        this.MIN_UPDATE_TIME = MIN_UPDATE_TIME;
        this.MIN_UPDATE_DISTANCE = MIN_UPDATE_DISTANCE;

        // Set Location Manager
        lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        // Select Provider Type
        if(type == ProviderType.NETWORK){
            provider = LocationManager.NETWORK_PROVIDER;
        }
        else{
            provider = LocationManager.GPS_PROVIDER;
        }
    }

    // Start provider if not already started
    public void start(){
        if(isRunning){
            //Already running, do nothing
            return;
        }

        //The provider is on, so start getting updates.  Update current location
        isRunning = true;
        lm.requestLocationUpdates(provider, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, this);
        lastLocation = null;
        lastTime = 0;
        return;
    }

    // Start with custom listener
    public void start(LocationUpdateListener update) {
        start();
        listener = update;
    }


    public void stop(){
        if(isRunning){
            lm.removeUpdates(this);
            isRunning = false;
            listener = null;
        }
    }

    // If there is a recent location
    public boolean hasLocation(){
        if(lastLocation == null){
            return false;
        }
        if(System.currentTimeMillis() - lastTime > 5 * MIN_UPDATE_TIME){
            return false; //stale
        }
        return true;
    }

    // If there is an old location
    public boolean hasPossiblyStaleLocation(){
        if(lastLocation != null){
            return true;
        }
        return lm.getLastKnownLocation(provider)!= null;
    }

    // Return most recent location
    public Location getLocation(){
        if(lastLocation == null){
            return null;
        }
        if(System.currentTimeMillis() - lastTime > 5 * MIN_UPDATE_TIME){
            return null; //stale
        }
        return lastLocation;
    }

    // Return old location if there is no recent location
    public Location getPossiblyStaleLocation(){
        if(lastLocation != null){
            return lastLocation;
        }
        return lm.getLastKnownLocation(provider);
    }

    public void onLocationChanged(Location newLoc) {
        long now = System.currentTimeMillis();
        if(listener != null){
            listener.onUpdate(lastLocation, lastTime, newLoc, now);
        }
        lastLocation = newLoc;
        lastTime = now;
    }

    public void onProviderDisabled(String arg0) {

    }

    public void onProviderEnabled(String arg0) {

    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }

    public Boolean isGPSTurnedOn(){
        if (!lm.isProviderEnabled( LocationManager.GPS_PROVIDER ))
            return false;
        return true;
    }
}
