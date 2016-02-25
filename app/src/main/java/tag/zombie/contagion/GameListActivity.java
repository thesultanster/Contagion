package tag.zombie.contagion;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;


public class GameListActivity extends AppCompatActivity{

    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView recyclerView;
    GameListActivityRecyclerAdapter adapter;

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


    void InflateVariables() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new GameListActivityRecyclerAdapter(GameListActivity.this, new ArrayList<GameListActivityRecyclerInfo>());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(GameListActivity.this));

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

        GameListActivity.clear();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                mSwipeRefreshLayout.setRefreshing(false);
                for (ParseObject game : objects){
                    adapter.addRow(new GameListActivityRecyclerInfo(game));
                }
            }
        });
    }

}
