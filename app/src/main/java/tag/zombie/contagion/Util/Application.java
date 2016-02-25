package tag.zombie.contagion.Util;

import com.parse.Parse;

/**
 * Created by sultankhan on 1/22/16.
 */
public class Application extends android.app.Application {

    public enum GameState {
        WAITING,
        STARTED,
        ENDED
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);

        Parse.initialize(this);
    }



}
