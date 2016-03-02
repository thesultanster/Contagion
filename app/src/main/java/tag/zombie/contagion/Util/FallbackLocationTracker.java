package tag.zombie.contagion.Util;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/**
 * Created by sultankhan on 1/11/16.
 *
 * This library provides a coarse location when there is no fine GPS location
 * If there is a fine GPS location, then that will be returned
 * Otherwise a coarse location is returned
 */
public class FallbackLocationTracker  implements LocationTracker, LocationTracker.LocationUpdateListener {


    // Indicated whether the provider is currently running
    private boolean isRunning;

    // Fine location provider
    private ProviderLocationTracker gps;
    // Coarse location provider
    private ProviderLocationTracker net;

    // Listens for any update from fine or coarse
    private LocationUpdateListener listener;

    // The last known location of the user
    Location lastLoc;

    // The last time a location was updated
    long lastTime;

    // Constructor
    public FallbackLocationTracker(Context context) {
        gps = new ProviderLocationTracker(context, ProviderLocationTracker.ProviderType.GPS);
        net = new ProviderLocationTracker(context, ProviderLocationTracker.ProviderType.NETWORK);
    }
    public FallbackLocationTracker(Context context, long MIN_UPDATE_TIME) {
        gps = new ProviderLocationTracker(context, ProviderLocationTracker.ProviderType.GPS, MIN_UPDATE_TIME);
        net = new ProviderLocationTracker(context, ProviderLocationTracker.ProviderType.NETWORK, MIN_UPDATE_TIME);
    }

    public FallbackLocationTracker(Context context, long MIN_UPDATE_TIME, long MIN_UPDATE_DISTANCE) {
        gps = new ProviderLocationTracker(context, ProviderLocationTracker.ProviderType.GPS, MIN_UPDATE_TIME,MIN_UPDATE_DISTANCE);
        net = new ProviderLocationTracker(context, ProviderLocationTracker.ProviderType.NETWORK, MIN_UPDATE_TIME,MIN_UPDATE_DISTANCE);
    }

    // Start both providers if not already started
    public void start(){
        if(isRunning){
            //Already running, do nothing
            return;
        }

        //Start both
        gps.start(this);
        net.start(this);
        isRunning = true;
    }

    // Start with given listener
    public void start(LocationUpdateListener update) {
        start();
        listener = update;
    }


    // Stop running both providers
    public void stop(){
        if(isRunning){
            gps.stop();
            net.stop();
            isRunning = false;
            listener = null;
        }
    }

    // If either providers has a location
    public boolean hasLocation(){
        //If either has a location, use it
        return gps.hasLocation() || net.hasLocation();
    }

    // If either providers have an old location
    public boolean hasPossiblyStaleLocation(){
        //If either has a location, use it
        return gps.hasPossiblyStaleLocation() || net.hasPossiblyStaleLocation();
    }

    // Returns fine location
    public Location getLocation(){
        Location ret = gps.getLocation();
        if(ret == null){
            ret = net.getLocation();
        }
        return ret;
    }

    // Returns coarse location if there is no fine location
    public Location getPossiblyStaleLocation(){
        Location ret = gps.getPossiblyStaleLocation();
        if(ret == null){
            ret = net.getPossiblyStaleLocation();
        }
        return ret;
    }

    public Boolean isGPSTurnedOn(){
        return gps.isGPSTurnedOn() || net.isGPSTurnedOn();
    }


    public void onUpdate(Location oldLoc, long oldTime, Location newLoc, long newTime) {
        boolean update = false;

        // We should update only if there is no last location, the provider is the same,
        // or the provider is more accurate, or the old location is stale
        if(lastLoc == null){
            update = true;
        }
        else if(lastLoc != null && lastLoc.getProvider().equals(newLoc.getProvider())){
            update = true;
        }
        else if(newLoc.getProvider().equals(LocationManager.GPS_PROVIDER)){
            update = true;
        }
        else if (newTime - lastTime > 5 * 60 * 1000){
            update = true;
        }

        if(update){
            if(listener != null){
                listener.onUpdate(lastLoc, lastTime, newLoc, newTime);
            }
            lastLoc = newLoc;
            lastTime = newTime;
        }
    }
}
