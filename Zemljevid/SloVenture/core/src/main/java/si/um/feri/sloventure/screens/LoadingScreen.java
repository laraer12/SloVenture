package si.um.feri.sloventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import si.um.feri.sloventure.SloVentureGame;

public class LoadingScreen implements Screen {

    private final SloVentureGame game;

    private Stage stage;
    private ProgressBar progressBar;
    private Texture backgroundTexture;

    public LoadingScreen(SloVentureGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());

        backgroundTexture = new Texture(Gdx.files.internal("images/triglav.jpg"));
        Image background = new Image(backgroundTexture);
        background.setFillParent(true);
        stage.addActor(background);

        Label title = createTitleLabel("SloVenture Map");
        title.setPosition(
            Gdx.graphics.getWidth() / 2f,
            Gdx.graphics.getHeight() * 0.65f,
            Align.center
        );
        stage.addActor(title);

        progressBar = createProgressBar();
        float barHeight = 0f;
        float barWidth  = Gdx.graphics.getWidth();

        progressBar.setSize(barWidth, barHeight);
        progressBar.setPosition(
            0,
            0
        );

        stage.addActor(progressBar);

        game.assets.load();
    }

    private Label createTitleLabel(String text) {
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/bokor_font.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter params =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        params.size = 96;
        params.color = Color.valueOf("E7D692");
        params.borderWidth = 2f;
        params.borderColor = Color.valueOf("432C11");

        BitmapFont font = generator.generateFont(params);
        generator.dispose();

        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);

        Label label = new Label(text, style);
        label.setAlignment(Align.center);

        return label;
    }

    private ProgressBar createProgressBar() {
        int BAR_HEIGHT = 30;

        Pixmap bgPixmap = new Pixmap(1, BAR_HEIGHT, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(Color.valueOf("432C11"));
        bgPixmap.fill();

        Texture bgTexture = new Texture(bgPixmap);
        bgPixmap.dispose();

        TextureRegionDrawable bgDrawable =
            new TextureRegionDrawable(new TextureRegion(bgTexture));
        bgDrawable.setMinHeight(BAR_HEIGHT);

        Pixmap fillPixmap = new Pixmap(1, BAR_HEIGHT, Pixmap.Format.RGBA8888);
        fillPixmap.setColor(Color.valueOf("E7D692"));
        fillPixmap.fill();

        Texture fillTexture = new Texture(fillPixmap);
        fillPixmap.dispose();

        TextureRegionDrawable fillDrawable =
            new TextureRegionDrawable(new TextureRegion(fillTexture));
        fillDrawable.setMinHeight(BAR_HEIGHT);

        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = bgDrawable;
        style.knobBefore = fillDrawable;

        ProgressBar bar = new ProgressBar(0f, 1f, 0.01f, false, style);
        bar.setAnimateDuration(0.15f);

        return bar;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        boolean finished = game.assets.update();
        progressBar.setValue(game.assets.getProgress());

        stage.act(delta);
        stage.draw();

        if (finished) {
            game.setScreen(new MapScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
