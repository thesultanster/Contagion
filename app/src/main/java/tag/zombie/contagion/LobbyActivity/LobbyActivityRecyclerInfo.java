package tag.zombie.contagion.LobbyActivity;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Leia on 3/2/16.
 */
public class LobbyActivityRecyclerInfo {

    ParseObject user;

    public LobbyActivityRecyclerInfo(ParseObject user)
    {
        super();
        this.user = user;

    }

    public String getName()
    {
        if (user.getString("nickname").isEmpty()) {
            return "Anonymous";
        } else {
            return user.getString("nickname");
        }
    }

    public String getParseObjectId()
    {
        return user.getObjectId();
    }

}
