package tag.zombie.contagion.NewGameActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import tag.zombie.contagion.R;

/**
 * Created by Leia on 3/1/16.
 */
public class NewGameSafeZonesListActivity extends AppCompatActivity {

    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView recyclerView;
    NewGameSafeZonesListActivityRecyclerAdapter adapter;
    Button addButton;

    ArrayList<ParseObject> NewGameSafeZonesListActivity = new ArrayList<ParseObject>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        //Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Game Rooms");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        InflateVariables();

        Refresh();
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        public void onClick(View v) {

//            addButton.setEnabled(false);
//            Intent intent = new Intent(getApplicationContext(), NewGameActivity.class);
//            startActivity(intent);
            Log.d("Contagion", "Going to create new game room.");

        }
    };


    void InflateVariables() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new NewGameSafeZonesListActivityRecyclerAdapter(NewGameSafeZonesListActivity.this, new ArrayList<NewGameSafeZonesListActivityRecyclerInfo>());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(NewGameSafeZonesListActivity.this));
        addButton = (Button) findViewById(R.id.addButton);

        addButton.setOnClickListener(buttonListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                Refresh();
            }
        });
    }

    private void Refresh() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new NewGameSafeZonesListActivityRecyclerAdapter(NewGameSafeZonesListActivity.this, new ArrayList<NewGameSafeZonesListActivityRecyclerInfo>());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(NewGameSafeZonesListActivity.this));
        addButton = (Button) findViewById(R.id.addButton);

        NewGameSafeZonesListActivity.clear();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    for (ParseObject game : objects) {
                        adapter.addRow(new NewGameSafeZonesListActivityRecyclerInfo(game));
                    }
                } else {
                    Log.e("Contagion", " Error getting game list: " + e.getMessage());
                }
            }
        });
    }

}
