package tag.zombie.contagion;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.nfc.NdefRecord;

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

public class GameActivity extends AppCompatActivity implements OnMapReadyCallback, CreateNdefMessageCallback{

    /* UI Stuff */
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

    /* Parse Stuff */
    ParseObject game;
    WorkerThread listenerThread;

    /* Google Map Stuff */
    GoogleMap map;

    /* Nfc Stuff */
    NfcAdapter mNfcAdapter;
    public static final String MIME_TYPE = "application/tag.zombie.contagion";
    boolean nfc = true;

    /* onCreate */
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

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        //will equal NULL if phone does not have an NFC adapter
        if (mNfcAdapter == null) {
            nfc = false;
        }
        //request to enable NFC if bot enabled
        else if (!mNfcAdapter.isEnabled()) {
            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
            startActivity(intent);
            nfc = true;
        }

        //check if nfc is enabled if it is then handle nfc intent
        if (nfc) {
            handleIntent(getIntent());
        }
        //
        if(nfc)  mNfcAdapter.setNdefPushMessageCallback(this,this);
    }

    /* Start of Activity */

    @Override
    protected void onStart() {
        super.onStart();
        View view = findViewById(R.id.zombienotification);
        zombieAlert = (RelativeLayout) view;
        zombieAlert.setVisibility(View.INVISIBLE);


        StartHeartAnimation();
    }

    /* UI STUFF */

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

    /* Game Stuff */

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
                                    Log.d("<CLOUD CODE BITCH>", "SOMETHING IS WRONG: leaveGame");
                                    Log.d("<CLOUD CODE BITCH>", e.toString());
                                }
                            }
                        });


                        Log.d("MyApp", "Anonymous user left game");

                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();

                        /* Before CloudCode
                        // remove user to array of players
                        List<ParseObject> players = objects.get(0).getList("players");
                        players.remove(ParseUser.getCurrentUser());
                        objects.get(0).put("players", players);
                        objects.get(0).saveInBackground();
                        objects.get(0).increment("healthyCount", -1);
                        objects.get(0).saveInBackground();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        Log.d("MyApp", "Anonymous user logged in.");
                       */
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
                                if (nfc) {
                                    if (mNfcAdapter.invokeBeam(GameActivity.this))
                                        Log.d("<NFC BITCH>", "INITIATE BEAM...BITCH");
                                    else
                                        Log.d("<NFC BITCH>", "YOU DIDN'T INITIATE BEAM...YOU BITCH");
                                }
                            } else {
                                Log.d("<CLOUD CODE BITCH>", "SOMETHING IS WRONG: addInfected");
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

                    /*Before Cloud Code
                    // Remove from healthy list
                    List<ParseObject> healthyPlayers = game.getList("healthyPlayers");
                    healthyPlayers.remove(ParseUser.getCurrentUser());
                    game.put("healthyPlayers", healthyPlayers);
                    game.saveInBackground();

                    // Increment infected
                    game.increment("infectedCount", 1);

                    // Decrement healthy
                    game.increment("healthyCount", -1);

                    itButton.setVisibility(view.GONE);

                    userStateTextView.setText("Infected");
                    userStateLayout.setBackgroundColor(Color.parseColor("#FF6600"));
                    heartImage.setBackgroundResource(R.drawable.heart_animation_infected);
                    frameAnimation = (AnimationDrawable) heartImage.getBackground();
                    frameAnimation.start();
                    */
                }
            }
        });
    }


    /* GoogleMaps Stuff */

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

    /*===NFC Stuff===*/

    private void handleIntent(Intent intent) {

        Log.d("<Contagion> NFC", "I SMELL THE NFC MESSAGE");

        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();

            //Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            //new NdefReaderTask().execute(tag);
            if (MIME_TYPE.equals(type)) {
                Log.d("<Contagion> NFC", "I HAVE THE RIGHT NFC MESSAGE");

                //TAG THE BITCH
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("gameId", "m3rnAai0Hf");

                ParseCloud.callFunctionInBackground("addInfected", params, new FunctionCallback<String>() {
                    public void done(String response, ParseException e) {
                        if (e == null) {
                            Log.d("<CLOUD CODE BITCH>", response);
                            /*
                            if (nfc) {
                                if (mNfcAdapter.invokeBeam(GameActivity.this))
                                    Log.d("<NFC BITCH>", "INITIATE BEAM...BITCH");
                                else
                                    Log.d("<NFC BITCH>", "YOU DIDN'T INITIATE BEAM...YOU BITCH");
                            }
                            */

                        } else {
                            Log.d("<CLOUD CODE BITCH>", "SOMETHING IS WRONG: addInfected");
                            Log.d("<CLOUD CODE BITCH>", e.toString());
                        }
                    }
                });

                itButton.setVisibility(View.GONE);

                userStateTextView.setText("Infected");
                userStateLayout.setBackgroundColor(Color.parseColor("#FF6600"));
                heartImage.setBackgroundResource(R.drawable.heart_animation_infected);
                frameAnimation = (AnimationDrawable) heartImage.getBackground();
                frameAnimation.start();
            } else {
                Log.d("<Contagion> NFC", "Wrong mime type: " + type);
            }
        }
    }

    //creates the NdefMessage that will be used when connecting with nfc
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = ("Tag YAH BIATCH!");
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { NdefRecord.createMime(MIME_TYPE, text.getBytes())
                        /**
                         * The Android Application Record (AAR) is commented out. When a device
                         * receives a push with an AAR in it, the application specified in the AAR
                         * is guaranteed to run. The AAR overrides the tag dispatch system.
                         * You can add it back in to guarantee that this
                         * activity starts when receiving a beamed message. For now, this code
                         * uses the tag dispatch system.
                         */
                        //,NdefRecord.createApplicationRecord()
                });
        return msg;
    }


}
