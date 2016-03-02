package tag.zombie.contagion;

import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.nfc.NfcAdapter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.nfc.NdefRecord;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;

import android.media.MediaPlayer;

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
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.logging.ErrorManager;

public class GameActivity extends AppCompatActivity implements OnMapReadyCallback, CreateNdefMessageCallback {

    /*===Parse Stuff===*/
    ParseObject game;
    WorkerThread listenerThread;

    /*===UI Stuff===*/
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
    MediaPlayer soundEffects;
    Uri screamEffect;


    /*===Google Map Stuff===*/
    GoogleMap map;

    /*===Nfc Stuff===*/
    final private int REQUEST_ENABLE_BT = 1;
    NfcAdapter mNfcAdapter;
    public static final String MIME_TYPE = "application/tag.zombie.contagion";
    boolean nfc = true;

    /*===Bluetooth Stuff===*/
    boolean hunted = true;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeAdvertiser advertiserBLE;
    private AdvertiseSettings advertiseSettings;
    private AdvertiseData advertiseData;

    private BluetoothLeScanner scannerBLE;
    private ScanSettings scanSettings;
    private ArrayList<ScanFilter> scanFilters;
    private String totallyLegitUuid = "38494638-8cf0-11bd-b23e-10b96e4ef00d";

    /*===onCreate===*/
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

        //check if nfc is enabled if it is then handle nfc intent
        if (nfc) {
            handleIntent(getIntent());
        }
        //sets the NdefMessage to push during beam
        if (nfc) mNfcAdapter.setNdefPushMessageCallback(this, this);
    }

    /*===Start of Activity===*/

    @Override
    protected void onStart() {
        super.onStart();
        View view = findViewById(R.id.zombienotification);
        zombieAlert = (RelativeLayout) view;
        zombieAlert.setVisibility(View.INVISIBLE);


        StartHeartAnimation();
    }

    /*===UI STUFF===*/

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

        //NFC
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //will equal NULL if phone does not have an NFC adapter
        if (mNfcAdapter == null) {
            nfc = false;
        }
        //request to enable NFC if not enabled
        else if (!mNfcAdapter.isEnabled()) {
            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
            startActivity(intent);
            nfc = true;
        }

        //Media to play the scream effect
        PackageManager m = getPackageManager();
        String s = getPackageName();
        try {
            PackageInfo p = m.getPackageInfo(s, 0);
            s = p.applicationInfo.dataDir;
        } catch (Exception e) {

        }
        screamEffect = Uri.parse(s + "/scream.mp3");
        soundEffects = MediaPlayer.create(this, R.raw.scream);
        soundEffects.setLooping(false);
//        soundEffects.start();


        //Bluetooth
        bluetoothManager = (BluetoothManager) getSystemService(GameActivity.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //ParcelUid to put into AdvertiseData
        ParcelUuid acceptableUuid = new ParcelUuid(UUID.fromString(totallyLegitUuid));

        advertiserBLE = mBluetoothAdapter.getBluetoothLeAdvertiser();
        advertiseData = new AdvertiseData.Builder().addServiceUuid(acceptableUuid).build();
        advertiseSettings = new AdvertiseSettings.Builder().setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW).setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY).build();

        scannerBLE = mBluetoothAdapter.getBluetoothLeScanner();
        scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
        ScanFilter temp = new ScanFilter.Builder().setServiceUuid(acceptableUuid).build();
        scanFilters = new ArrayList<ScanFilter>();
        scanFilters.add(temp);
    }

    /*===Makes the Heart beat==*/
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

    /*===Game Stuff===*/

    private void UpdateGame() {



        ParseQuery<ParseObject> query = new ParseQuery("Game");
        query.whereEqualTo("objectId", "m3rnAai0Hf");
        query.include("healthyPlayers");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                game = objects.get(0);

                healthyPlayers.setText(game.getInt("healthyCount") + "");
                infectedPlayers.setText(game.getInt("infectedCount") + "");


                if (map != null) {
                    //map.clear();



                    ParseQuery<ParseUser> innerQuery = new ParseQuery<ParseUser>("_User");
                    innerQuery.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objects, ParseException e) {

                            Log.d("user query", String.valueOf(objects.size()));
                            for (ParseUser user : objects) {
                                map.addMarker(new MarkerOptions()
                                        .position(new LatLng(user.getParseGeoPoint("location").getLongitude(), user.getParseGeoPoint("location").getLatitude()))
                                        .title("Healthy Player"));

                                Log.d("map", "added marker");
                            }
                        }
                    });

                    // If user is healthy
                    if (ParseUser.getCurrentUser().getString("status").equals("healthy")) {



                    } else if (ParseUser.getCurrentUser().getString("status").equals("infected")) {

                    }
                }


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

        bluetoothUpdate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        quitButton.performClick();
    }

    class WorkerThread extends Thread {
        volatile boolean running = true;

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Thread.sleep(2000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (running)
                                UpdateGame();
                        }
                    });
                }

                if (isInterrupted()) {
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

                        stopBluetooh();

                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();


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

                    taggedUpdateBLE();

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


    /*===GoogleMaps Stuff===*/

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setMyLocationEnabled(true);
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

                taggedUpdateBLE();
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
                new NdefRecord[]{NdefRecord.createMime(MIME_TYPE, text.getBytes())}
        );
        return msg;
    }

    /*===Bluetooth Stuff===*/
    public void bluetoothUpdate() {
        //BLE Scan for zombies
        if (hunted) {
            //healthy people code
            Log.d("<Game Update>", "Still running from them damn zombeez :(");
            scannerBLE.stopScan(scanBLEcallBack);
            scannerBLE.startScan(scanFilters, scanSettings, scanBLEcallBack);
        } else {
            //zombie people code
            Log.d("<GAME Update>", "GET DEM BRAINZ");
            advertiserBLE.startAdvertising(advertiseSettings, advertiseData, advertiseCallback);
        }
    }

    public AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d("<Bluetooth Advertising>", "I AM A ZOMBEE");
//            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.d("<Bluetooth Advertising>", "IT'S NOT WORKING:");
            super.onStartFailure(errorCode);
        }
    };

    public ScanCallback scanBLEcallBack = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("<Scan For Zombeez>", "HEARD SOMETHING");
            if (!soundEffects.isPlaying()) soundEffects.start();
//            BluetoothDevice btDevice = result.getDevice();
//            connectToDevice(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.d("<Scan For Zombeez>", "HEARD A BUNCH OF THINGS");
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d("<Scan Failed>", "Error Code: " + errorCode);
        }
    };

    //stops both advertising and scanning
    private void stopBluetooh() {
        scannerBLE.stopScan(scanBLEcallBack);
        advertiserBLE.stopAdvertising(advertiseCallback);
    }

    private void taggedUpdateBLE() {
        hunted = false;
        scannerBLE.flushPendingScanResults(scanBLEcallBack);
        scannerBLE.stopScan(scanBLEcallBack);
    }


}
