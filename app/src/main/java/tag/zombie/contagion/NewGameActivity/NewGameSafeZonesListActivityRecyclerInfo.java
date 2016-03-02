package tag.zombie.contagion.NewGameActivity;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Leia on 3/1/16.
 */
public class NewGameSafeZonesListActivityRecyclerInfo {

    ParseObject game;

    public NewGameSafeZonesListActivityRecyclerInfo(ParseObject game)
    {
        super();
        this.game = game;

    }
    public String getName()
    {
        return game.getString("name");
    }

    public String getPlayerCount() {
        List<ParseObject> players = game.getList("players");
        if (players != null) {
            return Integer.toString(players.size()) + " players";
        } else {
            return "0 players";
        }
    }

    public String getParseObjectId()
    {
        return game.getObjectId();
    }

}
