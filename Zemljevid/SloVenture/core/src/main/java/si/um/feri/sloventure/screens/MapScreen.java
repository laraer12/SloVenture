package si.um.feri.sloventure.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.List;

import si.um.feri.sloventure.Assets;
import si.um.feri.sloventure.AttractionPicker;
import si.um.feri.sloventure.MapCameraController;
import si.um.feri.sloventure.SloVentureGame;
import si.um.feri.sloventure.assets.AssetDescriptors;
import si.um.feri.sloventure.common.GameManager;
import si.um.feri.sloventure.data.attraction.Attraction;
import si.um.feri.sloventure.data.attraction.AttractionData;
import si.um.feri.sloventure.data.attraction.AttractionService;
import si.um.feri.sloventure.data.attraction.Terrain;
import si.um.feri.sloventure.data.crowd.CrowdData;
import si.um.feri.sloventure.data.crowd.CrowdService;

public class MapScreen implements Screen {
    private final SloVentureGame game;
    private final Assets assets;

    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;

    private Texture terrainTexture;
    private Terrain terrain;

    private Array<ModelInstance> chunkInstances;
    private Array<Attraction> allAttractions = new Array<>();
    private List<AttractionData> attractionData;

    private Stage stage;
    private AttractionPicker attractionPicker;

    public MapScreen(SloVentureGame game) {
        this.game = game;
        this.assets = game.assets;
    }

    @Override
    public void show() {
        modelBatch = new ModelBatch();

        setupCamera();
        setupLight();

        Skin skin = assets.get(AssetDescriptors.UI_SKIN);
        stage = new Stage(new ScreenViewport());

        MapCameraController mapController = new MapCameraController(camera);
        attractionPicker = new AttractionPicker(stage, skin, mapController);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(attractionPicker);
        multiplexer.addProcessor(mapController);
        Gdx.input.setInputProcessor(multiplexer);

        terrainTexture = new Texture(Gdx.files.internal("images/slovenia_sat_small.png"));
        terrainTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        terrainTexture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

        terrain = new Terrain("images/slovenia_clipped_4000.png", terrainTexture);
        chunkInstances = terrain.createTerrainChunks("images/slovenia_clipped_4000.png");

        fetchAttractions();
    }
    @Override
    public void render(float delta) {
        handleAttractionCreation();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        //Color bg = Color.valueOf("133F38");
        Color bg = Color.valueOf("09231E");
        Gdx.gl.glClearColor(bg.r, bg.g, bg.b, bg.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        for (ModelInstance instance : chunkInstances) {
            modelBatch.render(instance, environment);
        }

        for (int i = 0; i < Math.min(50, allAttractions.size); i++) {
            Attraction a = allAttractions.get(i);
            a.visible = true;
            modelBatch.render(allAttractions.get(i).modelInstance, environment);
        }
        modelBatch.end();

        attractionPicker.updateCamera();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        attractionPicker.onResize();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        terrainTexture.dispose();
        stage.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}


    private void fetchAttractions() {
        AttractionService.fetchAllAttractions(new AttractionService.Callback() {
            @Override
            public void onSuccess(List<AttractionData> attractions) {
                attractionData = attractions;

                for (AttractionData a : attractions) {
                    CrowdService.fetchCrowdDataForAttraction(
                        a.lat, a.lon,
                        new CrowdService.Callback() {
                            @Override
                            public void onSuccess(List<CrowdData> crowd) {
                                a.crowd = crowd;
                            }

                            @Override
                            public void onFailure(String message) {
                                System.err.println("Crowd error: " + message);
                            }
                        }
                    );
                }
            }

            @Override
            public void onFailure(int status, String message) {
                attractionData = GameManager.INSTANCE.loadAttractions();
            }
        });
    }

    private void handleAttractionCreation() {
        if (attractionData == null) return;

        for (AttractionData a : attractionData) {
            Model model = assets.get(AssetDescriptors.OTHER);
            ModelInstance instance = new ModelInstance(model);

            instance.materials.get(0)
                .set(ColorAttribute.createDiffuse(Color.BLUE));

            allAttractions.add(
                new Attraction(a, instance, terrain.getHeightmap())
            );
        }

        attractionPicker.allAttractions = allAttractions;
        attractionData = null;
    }

    private void setupCamera() {
        camera = new PerspectiveCamera(60,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight());

        camera.near = 1f;
        camera.far = 3000f;
        camera.position.set(0f, 300f, 300f);
        camera.lookAt(0f, 0f, 0f);
        camera.up.set(Vector3.Y);
        camera.update();
    }

    private void setupLight() {
        environment = new Environment();
        environment.set(new ColorAttribute(
            ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));

        environment.add(new DirectionalLight().set(
            1.1f, 1.1f, 1.1f,
            -0.3f, -1f, -0.2f));
    }
}
