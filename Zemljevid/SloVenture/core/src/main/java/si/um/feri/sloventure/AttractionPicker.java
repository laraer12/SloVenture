package si.um.feri.sloventure;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import si.um.feri.sloventure.data.attraction.Attraction;

public class AttractionPicker extends InputAdapter {
    private MapCameraController mapCameraController;
    public final Stage stage;
    public Array<Attraction> allAttractions;
    private final Skin skin;

    private Vector3 camStart = new Vector3();
    private Vector3 camTarget = new Vector3();
    private float camAlpha = 1f;
    private Vector3 savedTarget = new Vector3();
    private float savedDistance;
    private float startDistance;
    private final Vector3 tmpTarget = new Vector3();
    private Attraction currentAttraction;

    public AttractionPicker(Stage stage, Skin skin, MapCameraController mapCameraController) {
        this.mapCameraController = mapCameraController;
        this.skin = skin;
        this.stage = stage;

        this.skin.getFont("font").getData().setScale(0.6f);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT) return false;

        Attraction attraction = pickAttraction(screenX, screenY);
        if (attraction != null && attraction.visible) {
            currentAttraction = attraction;
            showAttractionWindow(attraction);
            return true;
        }
        return false;
    }

    public Attraction pickAttraction(int screenX, int screenY) {
        Ray ray = mapCameraController.camera.getPickRay(screenX, screenY);

        Attraction closest = null;
        float closestDistance = Float.MAX_VALUE;

        if (allAttractions != null) {
            for (Attraction attraction : allAttractions) {
                if (!attraction.visible) continue;

                Vector3 intersection = new Vector3();
                if (Intersector.intersectRayBounds(ray, attraction.boundingBox, intersection)) {
                    float distance = ray.origin.dst2(intersection);
                    if (distance < closestDistance) {

                        savedTarget.set(mapCameraController.getTarget());
                        savedDistance = mapCameraController.getDistance();

                        focusCameraAnimated(attraction);
                        closestDistance = distance;
                        closest = attraction;
                    }
                }
            }
        }

        return closest;
    }

    private void showAttractionWindow(Attraction attraction) {
        stage.clear();
        Window window = new Window("    " + attraction.data.name, skin);

        float width = stage.getWidth() * 0.4f;
        float height = stage.getHeight();

        window.setSize(width, height);
        window.setPosition(stage.getWidth(), 0); // začni izven zaslona
        window.align(Align.top);

        Table table = new Table(skin);
        table.top().center();

        Label region = new Label(attraction.data.regionName, skin);
        table.add(region).left().top().padTop(stage.getHeight() * 0.02f);
        table.row();

        Image img = getImage(attraction);
        img.setScaling(Scaling.fit);
        table.add(img).width(stage.getWidth() * 0.35f).maxHeight(stage.getHeight() * 0.35f).top().padTop(15);
        table.row();

        Label description = new Label(attraction.data.description, skin);
        description.setWrap(true);

        Table scrollTable = new Table(skin);
        scrollTable.add(description).width(stage.getWidth() * 0.3f).top().left();

        ScrollPane scrollPane = new ScrollPane(scrollTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, false);
        scrollPane.setForceScroll(false, true);
        scrollPane.setOverscroll(false, false);

        table.add(scrollPane).width(stage.getWidth() * 0.35f).height(stage.getHeight() * 0.3f).top().padTop(15);
        table.row();

        table.add(new Label(attraction.data.classification, skin)).top().padTop(15);
        table.row();
        table.add(new Label(attraction.data.locationType, skin)).top().padTop(15);
        table.row();
        table.add(new Label("Ocena: " + String.format("%.1f", attraction.data.rating), skin)).top().padTop(15);
        table.row();


        TextButton close = new TextButton("Zapri", skin);
        close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                window.remove();
                currentAttraction = null;
                mapCameraController.setState(savedTarget, savedDistance);
            }
        });

        table.pack();
        window.add(table).expandX().fillX().top().left().row();
        window.add(close).pad(10);


        window.addAction(
            Actions.moveTo(
                stage.getWidth() - width,
                0,
                0.5f
            )
        );

        stage.addActor(window);
    }


    private Image getImage(Attraction attraction) {
        Texture texture = new Texture(Gdx.files.internal("images/ex.jpeg"));
        if (attraction.data.images != null && !attraction.data.images.isEmpty()) {
            String url = attraction.data.images.get(0).url;

            Pixmap pixmap = null;
            try {
                InputStream is = new URL(url).openStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, read);
                }
                is.close();
                byte[] imageData = baos.toByteArray();

                pixmap = new Pixmap(imageData, 0, imageData.length);

                texture.dispose();
                texture = new Texture(pixmap);
            } catch (Exception e) {
                Gdx.app.error("ImageLoad", "Slike ni možno naložiti: " + url, e);
            } finally {
                if (pixmap != null) pixmap.dispose();
            }
        }

        return new Image(texture);
    }

    private void focusCameraAnimated(Attraction attraction) {
        camStart.set(mapCameraController.getTarget());
        camTarget.set(attraction.worldPosition.x, attraction.worldPosition.y, attraction.worldPosition.z);

        startDistance = mapCameraController.getDistance();
        camAlpha = 0f;
    }

    public void updateCamera() {
        if (camAlpha < 1f) {
            camAlpha = Math.min(1f, camAlpha + Gdx.graphics.getDeltaTime());

            Vector3 target = tmpTarget.set(camStart).lerp(camTarget, camAlpha);
            float distance = MathUtils.lerp(startDistance, 200f, camAlpha);

            mapCameraController.focusOn(target, distance);
        }
    }

    public void onResize() {
        stage.clear();
        if (currentAttraction != null) {
            showAttractionWindow(currentAttraction);
        }
    }
}
