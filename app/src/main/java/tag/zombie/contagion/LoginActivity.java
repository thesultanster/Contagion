package tag.zombie.contagion;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LoginActivity extends AppCompatActivity {

    EditText nameEditText;
    Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        nameEditText = (EditText) findViewById(R.id.nameEditText);
        playButton = (Button) findViewById(R.id.playButton);

        playButton.setOnClickListener(buttonListener);
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        public void onClick(View v) {

            if (ParseUser.getCurrentUser() != null) {
                ParseUser.getCurrentUser().logOut();
                //Intent intent = new Intent(getApplicationContext(), SplashScreenLoadGPS.class);
                //startActivity(intent);
                //finish();
            }


            ParseUser user = new ParseUser();
            user.setUsername(nameEditText.getText().toString());
            user.setPassword("pass");
            user.signUpInBackground(new SignUpCallback() {
                public void done(ParseException e) {
                    if (e != null) {
                        Log.d("Contagion", " login failed.");
                        playButton.setEnabled(true);
                    } else {
                        Intent intent = new Intent(getApplicationContext(), SplashScreenLoadGPS.class);
                        startActivity(intent);
                        finish();
                        Log.d("Contagion", " user logged in.");
                    }
                }
            });


        }
    };

}
