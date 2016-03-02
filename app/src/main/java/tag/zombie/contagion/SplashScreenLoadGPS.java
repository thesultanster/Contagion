package tag.zombie.contagion;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;


import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import tag.zombie.contagion.Util.FallbackLocationTracker;
import tag.zombie.contagion.Util.LocationTracker;

public class SplashScreenLoadGPS extends AppCompatActivity {

    FallbackLocationTracker fallbackLocationTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_load_gps);

        // Start Location Tracker
        fallbackLocationTracker = new FallbackLocationTracker(this);


    }


    @Override
    protected void onStart() {
        super.onStart();

        // If GPS is turned off, prompt user to turn it on
        if (fallbackLocationTracker != null && !fallbackLocationTracker.isGPSTurnedOn()) {
            displayPromptForEnablingGPS(this);
        }

        if (fallbackLocationTracker != null) {

            // If has fine last known location
            if (fallbackLocationTracker.hasLocation()) {
                TerminateSplashScreen(fallbackLocationTracker.getLocation());
                return;
            }

            // If has course last known location
            if (fallbackLocationTracker.hasPossiblyStaleLocation()){
                TerminateSplashScreen(fallbackLocationTracker.getPossiblyStaleLocation());
            }

            // Then find fine location
            fallbackLocationTracker.start(new LocationTracker.LocationUpdateListener() {
                @Override
                public void onUpdate(Location oldLoc, long oldTime, Location newLoc, long newTime) {
                    fallbackLocationTracker.stop();
                    TerminateSplashScreen(newLoc);
                    // Go to activity
                }
            });

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        fallbackLocationTracker.stop();
    }

    // Displaying prompt to turn on GPS
    public static void displayPromptForEnablingGPS(final Activity activity) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Please enable your GPS";

        builder.setMessage(message)
                .setPositiveButton("Go To Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                activity.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }

    void TerminateSplashScreen(Location loc) {



        if (ParseUser.getCurrentUser() != null) {
            // Start an intent for the logged in activity
            ParseInstallation.getCurrentInstallation().put("userId", ParseUser.getCurrentUser().getObjectId());
            ParseInstallation.getCurrentInstallation().saveInBackground();

            ParseUser.getCurrentUser().put("location", new ParseGeoPoint(loc.getLatitude(), loc.getLongitude()));
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                        startActivity(new Intent(SplashScreenLoadGPS.this, GameListActivity.class));

                    finish();
                }
            });

        }
        // If User Does not Exist
        else{
            Intent intent = new Intent(SplashScreenLoadGPS.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }


    }
}
