package tag.zombie.contagion;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sultan on 10/3/2015.
 */
public class GameListActivityRecyclerAdapter extends RecyclerView.Adapter<GameListActivityRecyclerAdapter.MyViewHolder> {

    // emptyList takes care of null pointer exception
    List<GameListActivityRecyclerInfo> data = Collections.emptyList();
    LayoutInflater inflator;
    Context context;
    //List<GameListActivityRecyclerInfo>mDataSet;

    public GameListActivityRecyclerAdapter(GameListActivity context, List<GameListActivityRecyclerInfo> data) {
        this.context = context;
        inflator = LayoutInflater.from(context);
        this.data = data;
    }


    /*picture = (Bitmap) ex.get("data");
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    picture.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    // get byte array here
    bytearray= stream.toByteArray();*/

    public void addRow(GameListActivityRecyclerInfo row) {
        data.add(row);
        notifyItemInserted(getItemCount() - 1);
    }

    public void clearData() {
        int size = this.data.size();

        data.clear();

        this.notifyItemRangeRemoved(0, size);
    }

    // Called when the recycler view needs to create a new row
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final View view = inflator.inflate(R.layout.row_game_list, parent, false);
        final MyViewHolder holder = new MyViewHolder(view, new MyViewHolder.MyViewHolderClicks() {

            public void rowClick(View caller, int position) {
                android.util.Log.d("rowClick", "rowClicks");


                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("gameId", data.get(position).getParseObjectId());

                ParseCloud.callFunctionInBackground("addPersonToGame", params, new FunctionCallback<String>() {
                    public void done(String response, ParseException e) {
                        if (e == null) {
                            Log.d("<CLOUD CODE BITCH>", response);
                            Intent intent = new Intent(context, GameActivity.class);
                            context.startActivity(intent);
                        } else {
                            Log.d("<CLOUD CODE BITCH>", "SOMETHING IS WRONG");
                            Log.d("<CLOUD CODE BITCH>", e.toString());
                        }
                    }
                });

            }




        });
        return holder;
    }

    // Setting up the data for each row
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        // This gives us current information list object
        GameListActivityRecyclerInfo current = data.get(position);

        holder.name.setText(current.getName());
        holder.playerCount.setText(current.getPlayerCount());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // Created my custom view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;
        TextView playerCount;

        public MyViewHolderClicks mListener;

        // itemView will be my own custom layout View of the row
        public MyViewHolder(View itemView, MyViewHolderClicks listener) {
            super(itemView);

            mListener = listener;
            name = (TextView) itemView.findViewById(R.id.name);
            playerCount = (TextView) itemView.findViewById(R.id.playerCount);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                default:
                    mListener.rowClick(v, getAdapterPosition());
                    break;
            }
        }

        public interface MyViewHolderClicks {
            void rowClick(View caller, int position);
        }
    }


}