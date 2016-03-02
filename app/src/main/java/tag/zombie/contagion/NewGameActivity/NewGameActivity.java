package tag.zombie.contagion.NewGameActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;

import tag.zombie.contagion.GameActivity;
import tag.zombie.contagion.R;

/**
 * Created by Leia on 3/1/16.
 */
public class NewGameActivity extends AppCompatActivity {

    EditText nameEditText;
    Button createButton;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        nameEditText = (EditText)findViewById(R.id.nameEditText);
        createButton = (Button)findViewById(R.id.createButton);

        createButton.setOnClickListener(buttonListener);
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        public void onClick(View v) {

            // TODO: create new game room
            createButton.setEnabled(false);
            Log.d("Contagion", "Create new game room.");

            if (nameEditText.getText().toString().isEmpty()) {
                // TODO: print error
                AlertDialog alertDialog = new AlertDialog.Builder(NewGameActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Need to enter game room name.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            createButton.setEnabled(true);
                        }
                });
                alertDialog.show();
            } else {
                //CREATE NEW GAME
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("gameName", nameEditText.getText().toString());

                ParseCloud.callFunctionInBackground("newGame", params, new FunctionCallback<String>() {
                    public void done(String response, ParseException e) {
                        if (e == null) {
                            Log.d("<CLOUD CODE BITCH>", response);
                            // go to game activity
                            Intent intent = new Intent(context, GameActivity.class);
                            context.startActivity(intent);
                        } else {
                            Log.d("<CLOUD CODE BITCH>", "SOMETHING IS WRONG: newGame");
                            Log.d("<CLOUD CODE BITCH>", e.toString());
                            createButton.setEnabled(true);
                        }
                    }
                });
            }
        }
    };

}
