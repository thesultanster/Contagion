package tag.zombie.contagion;

import com.parse.ParseObject;

import java.util.List;

public class GameListActivityRecyclerInfo {

    ParseObject game;

    public GameListActivityRecyclerInfo(ParseObject game)
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
