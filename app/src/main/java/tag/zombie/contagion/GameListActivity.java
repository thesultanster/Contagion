package tag.zombie.contagion;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class GameListActivity extends AppCompatActivity{

    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView recyclerView;
    GameListActivityRecyclerAdapter adapter;
    Button addButton;

    ArrayList<ParseObject> GameListActivity = new ArrayList<ParseObject>();


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

            addButton.setEnabled(false);
            Intent intent = new Intent(getApplicationContext(), NewGameActivity.class);
            startActivity(intent);
            Log.d("Contagion", "Going to create new game room.");

        }
    };


    void InflateVariables() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new GameListActivityRecyclerAdapter(GameListActivity.this, new ArrayList<GameListActivityRecyclerInfo>());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(GameListActivity.this));
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
        adapter = new GameListActivityRecyclerAdapter(GameListActivity.this, new ArrayList<GameListActivityRecyclerInfo>());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(GameListActivity.this));
        addButton = (Button) findViewById(R.id.addButton);

        GameListActivity.clear();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    for (ParseObject game : objects) {
                        adapter.addRow(new GameListActivityRecyclerInfo(game));
                    }
                } else {
                    Log.e("Contagion", " Error getting game list: " + e.getMessage());
                }
            }
        });
    }

}
