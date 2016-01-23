package tag.zombie.contation.com.contagion.Util;

import com.parse.Parse;

/**
 * Created by sultankhan on 1/22/16.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);

        Parse.initialize(this);
    }
}
