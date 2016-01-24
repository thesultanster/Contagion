package tag.zombie.contation.com.contagion;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

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

    double myX, myY;

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

    private void UpdateGame(){


        ParseQuery<ParseObject> query = new ParseQuery("Game");
        query.whereEqualTo("objectId", "m3rnAai0Hf");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                game = objects.get(0);

                healthyPlayers.setText(game.getInt("healthyCount") + "");
                infectedPlayers.setText(game.getInt("infectedCount") + "");

            }


        });

    }

    private void StartListenerThread(){
        Thread listenerThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UpdateGame();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };


        listenerThread.start();
    }


    private void SetClickListeners(){
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ParseQuery<ParseObject> query = new ParseQuery("Game");
                query.whereEqualTo("objectId", "m3rnAai0Hf");
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {


                        //TODO: Convert this to a parse Cloud Code Function (PlayerQuit)
                        //==============================================================================================
                        // remove user to array of players
                        List<ParseObject> players = objects.get(0).getList("players");



                        players.remove(ParseUser.getCurrentUser());
                        objects.get(0).put("players", players);
                        objects.get(0).saveInBackground();

                        // TODO: Find out if user is healthy or infected and decrement that
                        // Decrement healthy player count
                        objects.get(0).increment("healthyCount", -1);
                        objects.get(0).saveInBackground();
                        //==============================================================================================




                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        Log.d("MyApp", "Anonymous user logged in.");
                    }


                });



            }
        });





        itButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // If game object exists
                if(game != null){


                    // TODO: Convert this to Parse Cloud Code (AddInfected)
                    //==============================================================================================
                    // Remove from healthy list
                    List<ParseObject> healthyPlayers = game.getList("healthyPlayers");
                    healthyPlayers.remove(ParseUser.getCurrentUser());
                    game.put("healthyPlayers", healthyPlayers);
                    game.saveInBackground();

                    // Increment infected
                    game.increment("infectedCount", 1);

                    // Decrement healthy
                    game.increment("healthyCount", -1);

                    //==============================================================================================




                }

            }
        });

    }


}
