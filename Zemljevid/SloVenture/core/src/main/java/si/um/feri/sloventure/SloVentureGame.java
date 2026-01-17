package si.um.feri.sloventure;

import com.badlogic.gdx.Game;

import si.um.feri.sloventure.screens.LoadingScreen;

public class SloVentureGame extends Game {

    public Assets assets;

    @Override
    public void create() {
        assets = new Assets();
        setScreen(new LoadingScreen(this));
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        assets.dispose();
    }
}

