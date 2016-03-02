package tag.zombie.contagion;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tag.zombie.contagion.NewGameActivity.NewGameActivity;
import tag.zombie.contagion.Util.FallbackLocationTracker;
import tag.zombie.contagion.Util.LocationTracker;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    List<ParseObject> safeZoneLocations;
    AppCompatButton setSafeZone;
    Button doneButton;

    FallbackLocationTracker fallbackLocationTracker;
    GoogleMap map;

    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        safeZoneLocations = new ArrayList<ParseObject>();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Set Safe Zone");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        doneButton = (Button) findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent fromIntent = getIntent();
                String gameName = fromIntent.getStringExtra("gameRoomName");

                //CREATE NEW GAME
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("gameName", gameName);

                ParseCloud.callFunctionInBackground("newGame", params, new FunctionCallback<ParseObject>() {
                    public void done(ParseObject newGame, ParseException e) {
                        if (e == null) {
                            Log.d("<CLOUD CODE BITCH>", newGame.toString());
                            newGame.put("safeZones", safeZoneLocations);
                            newGame.saveInBackground();

                            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                            startActivity(intent);
                        } else {
                            Log.d("<CLOUD CODE BITCH>", "SOMETHING IS WRONG: newGame");
                            Log.d("<CLOUD CODE BITCH>", e.toString());
                        }
                    }
                });

            }
        });

        setSafeZone = (AppCompatButton) findViewById(R.id.setSafeZone);
        setSafeZone.setSupportBackgroundTintList(new ColorStateList(new int[][]{new int[0]}, new int[]{getResources().getColor(R.color.colorAccent)}));
        setSafeZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.getCurrentUser().put("location", new ParseGeoPoint(map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude));
                ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                        if (e != null) {
                            Toast.makeText(MapPickerActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

                        } else {
                            // save selected location
                            ParseGeoPoint newSafeZoneGeoPoint = new ParseGeoPoint(
                                    map.getCameraPosition().target.latitude,
                                    map.getCameraPosition().target.longitude);

                            final ParseObject newSafeZone = new ParseObject("SafeZone");
                            newSafeZone.put("location", newSafeZoneGeoPoint);
                            newSafeZone.put("name", "my safe zone");
                            newSafeZone.put("radius", 0.5);
                            newSafeZone.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {

                                    if (e != null) {
                                        Toast.makeText(MapPickerActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        safeZoneLocations.add(newSafeZone);
                                    }
                                }
                            });
                        }
                    }
                });

            }
        });



        // Create instance of custom mapFragment
        // Set up map and touch listener
        final MapFragment mFrag;
        mFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map = mFrag.getMap();
        mFrag.getMapAsync(this);



        // Start Location Tracker
        fallbackLocationTracker = new FallbackLocationTracker(this, 30000);



    }


    @Override
    protected void onStart() {
        super.onStart();

        map.setMyLocationEnabled(true);

        // If GPS is turned off, prompt user to turn it on
        if (fallbackLocationTracker != null && !fallbackLocationTracker.isGPSTurnedOn()) {
            displayPromptForEnablingGPS(this);
        }

        if (fallbackLocationTracker != null) {

            // Set Camera to course last known location
            if (fallbackLocationTracker.hasLocation()) {
                ZoomCameraToCurrentLocation(fallbackLocationTracker.getLocation());
                map.setMyLocationEnabled(false);
                return;
            }

            // Set Camera to course last known location
            if (fallbackLocationTracker.hasPossiblyStaleLocation())
                ZoomCameraToCurrentLocation(fallbackLocationTracker.getPossiblyStaleLocation());

            // Then set camera to fine location once achieved
            fallbackLocationTracker.start(new LocationTracker.LocationUpdateListener() {
                @Override
                public void onUpdate(Location oldLoc, long oldTime, Location newLoc, long newTime) {
                    ZoomCameraToCurrentLocation(newLoc);
                    fallbackLocationTracker.stop();
                    map.setMyLocationEnabled(false);
                }
            });

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        fallbackLocationTracker.stop();
        map.setMyLocationEnabled(false);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMyLocationEnabled(true);
        this.map = map;
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


    // Function to zoom into given location
    private void ZoomCameraToCurrentLocation(Location location) {
        if (location != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 13));


            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        }
    }





}
