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

        Log.d("Contagion", "Logging in anonymous user.");
        playButton.setEnabled(false);
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
            if (e != null) {
                Log.d("Contagion", "Anonymous login failed.");
                playButton.setEnabled(true);
            } else {
                Intent intent = new Intent(getApplicationContext(), SplashScreenLoadGPS.class);
                startActivity(intent);
                finish();
                Log.d("Contagion", "Anonymous user logged in.");
            }
            }
        });

        }
    };

}
