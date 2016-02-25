package tag.zombie.contagion;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.List;

public class GameActivity extends AppCompatActivity implements OnMapReadyCallback{

    RelativeLayout zombieAlert;
    ImageView heartImage;
    String address;
    View heartView;
    AnimationDrawable frameAnimation;
    RelativeLayout userStateLayout;
    View userStateView;
    TextView userStateTextView;

    TextView healthyPlayers;
    TextView infectedPlayers;

    Button itButton;
    Button quitButton;

    ParseObject game;

    WorkerThread listenerThread;

    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Inflate Variables
        inflateVariables();

        // Set On Click Listeners
        SetClickListeners();

        //Start Listener Thread
        StartListenerThread();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
        .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        View view = findViewById(R.id.zombienotification);
        zombieAlert = (RelativeLayout) view;
        zombieAlert.setVisibility(View.INVISIBLE);


        StartHeartAnimation();
    }

    private void inflateVariables() {
        // Inflate Variables
        userStateView = findViewById(R.id.userstate);
        userStateLayout = (RelativeLayout) userStateView;
        userStateView = findViewById(R.id.user_state);
        userStateTextView = (TextView) userStateView;
        healthyPlayers = (TextView) findViewById(R.id.healthyPlayers);
        infectedPlayers = (TextView) findViewById(R.id.infectedPlayers);
        heartView = findViewById(R.id.heart);
        heartImage = (ImageView) heartView;
        itButton = (Button) findViewById(R.id.itButton);
        quitButton = (Button) findViewById(R.id.quitButton);

    }

    private void StartHeartAnimation() {

        // set its background to our AnimationDrawable XML resource.
        heartImage.setBackgroundResource(R.drawable.heart_animation_healthy);

        /*
         * Get the background, which has been compiled to an AnimationDrawable
         * object.
         */
        frameAnimation = (AnimationDrawable) heartImage
                .getBackground();

        // Start the animation (looped playback by default).
        frameAnimation.start();
    }

    private void UpdateGame() {

        ParseQuery<ParseObject> query = new ParseQuery("Game");
        query.whereEqualTo("objectId", "m3rnAai0Hf");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                game = objects.get(0);

                healthyPlayers.setText(game.getInt("healthyCount") + "");
                infectedPlayers.setText(game.getInt("infectedCount") + "");

                if (game.getInt("healthyCount") == 0) {

                    listenerThread.stahp();
                    listenerThread.interrupt();

                    HashMap<String, Object> params = new HashMap<String, Object>();

                    ParseCloud.callFunctionInBackground("leaveGame", params, new FunctionCallback<String>() {
                        public void done(String response, ParseException e) {
                            if (e == null) {
                                Log.d("<CLOUD CODE BITCH>", response);

                            } else {
                                Log.d("<CLOUD CODE BITCH>", "SOMETHING IS WRONG");
                                Log.d("<CLOUD CODE BITCH>", e.toString());
                            }
                        }
                    });

                    Log.d("MyApp", "Game is Over!");

                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }


        });

    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng sydney = new LatLng(-33.867, 151.206);

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

        map.addMarker(new MarkerOptions()
                .title("Sydney")
                .snippet("The most populous city in Australia.")
                .position(sydney));
    }

    class WorkerThread extends Thread {
        volatile boolean running = true;

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Thread.sleep(200);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(running)
                                UpdateGame();
                        }
                    });
                }

                if(isInterrupted()){
                    return;
                }


            } catch (InterruptedException e) {
            }
        }

        public void stahp() {
            running = false;
        }
    }

    private void StartListenerThread() {
        listenerThread = new WorkerThread();
        listenerThread.start();
    }


    private void SetClickListeners() {
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                listenerThread.stahp();
                listenerThread.interrupt();

                ParseQuery<ParseObject> query = new ParseQuery("Game");
                query.whereEqualTo("objectId", "m3rnAai0Hf");
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {


                        HashMap<String, Object> params = new HashMap<String, Object>();

                        ParseCloud.callFunctionInBackground("leaveGame", params, new FunctionCallback<String>() {
                            public void done(String response, ParseException e) {
                                if (e == null) {
                                    Log.d("<CLOUD CODE BITCH>", response);


                                } else {
                                    Log.d("<CLOUD CODE BITCH>", "SOMETHING IS WRONG");
                                    Log.d("<CLOUD CODE BITCH>", e.toString());
                                }
                            }
                        });


                        Log.d("MyApp", "Anonymous user left game");

                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();

//                        //TODO: Convert this to a parse Cloud Code Function (PlayerQuit)
//                        //==============================================================================================
//                        // remove user to array of players
//                        List<ParseObject> players = objects.get(0).getList("players");
//
//
//
//                        players.remove(ParseUser.getCurrentUser());
//                        objects.get(0).put("players", players);
//                        objects.get(0).saveInBackground();
//
//                        // TODO: Find out if user is healthy or infected and decrement that
//                        // Decrement healthy player count
//                        objects.get(0).increment("healthyCount", -1);
//                        objects.get(0).saveInBackground();
//                        //==============================================================================================
//
//
//
//
//                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//                        startActivity(intent);
//                        Log.d("MyApp", "Anonymous user logged in.");
                    }


                });


            }
        });


        itButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // If game object exists
                if (game != null) {

                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("gameId", "m3rnAai0Hf");

                    ParseCloud.callFunctionInBackground("addInfected", params, new FunctionCallback<String>() {
                        public void done(String response, ParseException e) {
                            if (e == null) {
                                Log.d("<CLOUD CODE BITCH>", response);
                            } else {
                                Log.d("<CLOUD CODE BITCH>", "SOMETHING IS WRONG");
                                Log.d("<CLOUD CODE BITCH>", e.toString());
                            }
                        }
                    });

                    itButton.setVisibility(view.GONE);

                    userStateTextView.setText("Infected");
                    userStateLayout.setBackgroundColor(Color.parseColor("#FF6600"));
                    heartImage.setBackgroundResource(R.drawable.heart_animation_infected);
                    frameAnimation = (AnimationDrawable) heartImage.getBackground();
                    frameAnimation.start();
//
//                    // TODO: Convert this to Parse Cloud Code (AddInfected)
//                    //==============================================================================================
//                    // Remove from healthy list
//                    List<ParseObject> healthyPlayers = game.getList("healthyPlayers");
//                    healthyPlayers.remove(ParseUser.getCurrentUser());
//                    game.put("healthyPlayers", healthyPlayers);
//                    game.saveInBackground();
//
//                    // Increment infected
//                    game.increment("infectedCount", 1);
//
//                    // Decrement healthy
//                    game.increment("healthyCount", -1);
//
//                    itButton.setVisibility(view.GONE);
//
//                    userStateTextView.setText("Infected");
//                    userStateLayout.setBackgroundColor(Color.parseColor("#FF6600"));
//                    heartImage.setBackgroundResource(R.drawable.heart_animation_infected);
//                    frameAnimation = (AnimationDrawable) heartImage.getBackground();
//                    frameAnimation.start();
//
//                    //==============================================================================================
//


                }

            }
        });

    }


}
