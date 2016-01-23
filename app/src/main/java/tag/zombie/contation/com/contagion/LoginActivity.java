package tag.zombie.contation.com.contagion;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    EditText nameEditText;
    Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        nameEditText = (EditText)findViewById(R.id.nameEditText);
        playButton = (Button)findViewById(R.id.playButton);

        playButton.setOnClickListener(buttonListener);
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        public void onClick(View v) {

            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e != null) {
                        Log.d("MyApp", "Anonymous login failed.");
                    } else {

                        ParseQuery<ParseObject> query = new ParseQuery("Game");
                        query.whereEqualTo("objectId", "m3rnAai0Hf");
                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {



                                // Add user to array of players
                                //TODO: Convert this to a parse Cloud Code Function
                                //==============================================================================================
                                ArrayList<String> testStringArrayList = (ArrayList<String>) objects.get(0).get("players");

                                if (testStringArrayList == null)
                                    testStringArrayList = new ArrayList<String>();

                                testStringArrayList.add(ParseUser.getCurrentUser().getObjectId());
                                objects.get(0).put("players", testStringArrayList);
                                objects.get(0).saveInBackground();

                                // Increment healthy player count
                                objects.get(0).increment("healthyCount");
                                objects.get(0).saveInBackground();
                                //==============================================================================================



                                Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                                startActivity(intent);
                                Log.d("MyApp", "Anonymous user logged in.");
                            }


                        });



                    }
                }
            });



        }
    };


}
