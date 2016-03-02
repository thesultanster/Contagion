package tag.zombie.contagion.LobbyActivity;

import android.content.Context;
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
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import tag.zombie.contagion.GameActivity;
import tag.zombie.contagion.R;

public class LobbyActivity extends AppCompatActivity {

    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView recyclerView;
    LobbyActivityRecyclerAdapter adapter;
    Button startButton;

    ArrayList<ParseObject> LobbyActivity = new ArrayList<ParseObject>();
    ParseObject game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        //Setup Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Lobby");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                game = (ParseObject) object.get("gameId");
            }
        });

        InflateVariables();

        Refresh();
    }

    View.OnClickListener buttonListener = new View.OnClickListener() {
        public void onClick(View v) {

            Intent intent = new Intent(LobbyActivity.this, GameActivity.class);
            startActivity(intent);
            Log.d("Contagion", "Going to game room.");
        }
    };

    void InflateVariables() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new LobbyActivityRecyclerAdapter(LobbyActivity.this, new ArrayList<LobbyActivityRecyclerInfo>());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(LobbyActivity.this));
        startButton = (Button) findViewById(R.id.startButton);

        startButton.setOnClickListener(buttonListener);

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
        adapter = new LobbyActivityRecyclerAdapter(LobbyActivity.this, new ArrayList<LobbyActivityRecyclerInfo>());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(LobbyActivity.this));
        startButton = (Button) findViewById(R.id.startButton);

        LobbyActivity.clear();

        if (game != null) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
            query.include("players");
            query.getInBackground(game.getObjectId(), new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    List<ParseObject> players = (ArrayList<ParseObject>) object.get("players");
                    for (ParseObject player : players) {
                        adapter.addRow(new LobbyActivityRecyclerInfo(player));
                    }
                    final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                    if (toolbar != null) {
                        getSupportActionBar().setTitle(object.getString("name") + " Lobby");
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

}
