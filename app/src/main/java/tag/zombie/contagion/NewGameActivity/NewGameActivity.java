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
import tag.zombie.contagion.MapPickerActivity;
import tag.zombie.contagion.R;

/**
 * Created by Leia on 3/1/16.
 */
public class NewGameActivity extends AppCompatActivity {

    EditText nameEditText;
    Button safeZoneButton;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        nameEditText = (EditText)findViewById(R.id.nameEditText);
        safeZoneButton = (Button)findViewById(R.id.safeZoneButton);

        safeZoneButton.setOnClickListener(safeZoneButtonListener);
    }

    View.OnClickListener safeZoneButtonListener = new View.OnClickListener() {
        public void onClick(View v) {

            // TODO: create new safe zone
            safeZoneButton.setEnabled(false);
            Log.d("Contagion", "Create new safe zone.");

            Bundle args = new Bundle();
            args.putString("gameRoomName", nameEditText.getText().toString());

            Intent intent = new Intent(getApplicationContext(), MapPickerActivity.class);
            intent.putExtras(args);
            startActivity(intent);
            safeZoneButton.setEnabled(true);
        }
    };

}
