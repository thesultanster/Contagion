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

    Button itButton;
    Button quitButton;

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



                        // remove user to array of players
                        //TODO: Convert this to a parse Cloud Code Function
                        //==============================================================================================
                        ArrayList<String> testStringArrayList = (ArrayList<String>) objects.get(0).get("players");

                        if (testStringArrayList == null)
                            testStringArrayList = new ArrayList<String>();


                        testStringArrayList.remove(ParseUser.getCurrentUser().getObjectId());
                        objects.get(0).put("players", testStringArrayList);
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
    }


}
