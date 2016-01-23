package tag.zombie.contation.com.contagion;

import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {

    RelativeLayout myNotif;
    ImageView heartImage;
    String address;
    View heartView;
    AnimationDrawable frameAnimation;
    RelativeLayout userStateLayout;
    View userStateView;
    TextView userStateTextView;
    double myX, myY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);



    }

    @Override
    protected void onStart() {
        super.onStart();
        View view = findViewById(R.id.zombienotification);
        myNotif = (RelativeLayout) view;
        myNotif.setVisibility(View.INVISIBLE);


        userStateView = findViewById(R.id.userstate);
        userStateLayout = (RelativeLayout) userStateView;
        userStateView = findViewById(R.id.user_state);
        userStateTextView = (TextView) userStateView;

        heartView = findViewById(R.id.heart);
        heartImage = (ImageView) heartView;
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
}
