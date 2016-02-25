package tag.zombie.contagion;

import com.parse.ParseObject;

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

    public String getParseObjectId()
    {
        return game.getObjectId();
    }


}
