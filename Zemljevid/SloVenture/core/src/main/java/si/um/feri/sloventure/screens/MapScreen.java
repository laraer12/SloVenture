package si.um.feri.sloventure.screens;

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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

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
import si.um.feri.sloventure.data.crowd.CrowdMember;
import si.um.feri.sloventure.data.crowd.CrowdMqttClient;
import si.um.feri.sloventure.data.crowd.DesktopSslFactory;

public class MapScreen implements Screen {
    private final SloVentureGame game;
    private final Assets assets;

    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;

    private Texture terrainTexture;
    private Terrain terrain;

    private Array<ModelInstance> chunkInstances;
    private final Array<Attraction> allAttractions = new Array<>();
    private List<AttractionData> attractionData;

    private Stage stage;
    private AttractionPicker attractionPicker;
    private CrowdMqttClient crowdMqttClient;
    private Skin skin;
    private Stage uiStage;

    public MapScreen(SloVentureGame game) {
        this.game = game;
        this.assets = game.assets;
    }

    @Override
    public void show() {
        modelBatch = new ModelBatch();

        setupCamera();
        setupLight();

        skin = assets.get(AssetDescriptors.UI_SKIN);
        stage = new Stage(new ScreenViewport());

        uiStage = new Stage(new ScreenViewport());

        uiStage.addActor(createDropdown());

        MapCameraController mapController = new MapCameraController(camera);
        attractionPicker = new AttractionPicker(stage, skin, mapController, assets.get(AssetDescriptors.PERSON));

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage);
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

        Properties props = new Properties();
        try {
            props.load(Gdx.files.internal("mqtt/local.properties").read());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String password = props.getProperty("P12_PASSWORD");

        try {
            crowdMqttClient = new CrowdMqttClient(
                DesktopSslFactory.fromP12(
                    "mqtt/android_client.p12",
                    password
                ),
                this::onCrowdMessage
            );
        } catch (Exception e) {
            Gdx.app.error("MQTT", "Failed to connect to MQTT", e);
        }


    }

    @Override
    public void render(float delta) {
        handleAttractionCreation();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Color bg = Color.valueOf("e6fff9");
        Gdx.gl.glClearColor(bg.r, bg.g, bg.b, bg.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        for (ModelInstance instance : chunkInstances) {
            modelBatch.render(instance, environment);
        }

        for (int i = 0; i < Math.min(600, allAttractions.size); i++) {
            Attraction a = allAttractions.get(i);
            if (a.visible) {
                modelBatch.render(allAttractions.get(i).modelInstance, environment);
            }
        }
        modelBatch.end();

        modelBatch.begin(camera);
        for (CrowdMember member : attractionPicker.crowdMembers) {
            modelBatch.render(member.modelInstance);
        }
        attractionPicker.updateCrowd();
        modelBatch.end();

        attractionPicker.updateCamera();
        stage.act(delta);
        stage.draw();
        uiStage.act(delta);
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        uiStage.getViewport().update(width, height, true);
        attractionPicker.onResize();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        terrainTexture.dispose();
        stage.dispose();
        if (crowdMqttClient != null) {
            try {
                crowdMqttClient.disconnect();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }


    private void fetchAttractions() {
        AttractionService.fetchAllAttractions(new AttractionService.Callback() {
            @Override
            public void onSuccess(List<AttractionData> attractions) {
                attractionData = attractions;
                GameManager.INSTANCE.saveAttractions(attractions);
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

            getModelColor(a, instance);

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

    private void onCrowdMessage(CrowdData crowdData) {
        if (allAttractions.size == 0) return;

        for (Attraction attraction : allAttractions) {
            if (attraction.data == null) continue;

            if (attraction.data.id.equals(crowdData.attractionId)) {

                if (attraction.data.crowd == null) {
                    attraction.data.crowd = new java.util.ArrayList<>();
                }
                attraction.data.crowd.add(0, crowdData);

                System.out.println(
                    "MQTT crowd update for " + attraction.data.name +
                        ": " + crowdData.numOfPeople
                );

                break;
            }
        }
    }

    private void getModelColor(AttractionData data, ModelInstance instance) {
        Material material;
        switch (data.locationType.toLowerCase()) {
            case "grad":
                material = new Material(ColorAttribute.createDiffuse(Color.PINK));
                instance.materials.get(0).set(material);
                break;
            case "soteska":
                material = new Material(ColorAttribute.createDiffuse(Color.BROWN));
                instance.materials.get(0).set(material);
                break;
            case "cerkev":
                material = new Material(ColorAttribute.createDiffuse(Color.CORAL));
                instance.materials.get(0).set(material);
                break;
            case "koča":
                material = new Material(ColorAttribute.createDiffuse(Color.WHITE));
                instance.materials.get(0).set(material);
                break;
            case "jezero":
                material = new Material(ColorAttribute.createDiffuse(Color.BLUE));
                instance.materials.get(0).set(material);
                break;
            case "park":
                material = new Material(ColorAttribute.createDiffuse(Color.ORANGE));
                instance.materials.get(0).set(material);
                break;
            case "muzej na prostem":
                material = new Material(ColorAttribute.createDiffuse(Color.YELLOW));
                instance.materials.get(0).set(material);
                break;
            case "kopališče":
                material = new Material(ColorAttribute.createDiffuse(Color.NAVY));
                instance.materials.get(0).set(material);
                break;
            case "razgledni stolp":
                material = new Material(ColorAttribute.createDiffuse(Color.PURPLE));
                instance.materials.get(0).set(material);
                break;
            case "hrib":
                material = new Material(ColorAttribute.createDiffuse(Color.RED));
                instance.materials.get(0).set(material);
                break;
            case "planina":
                material = new Material(ColorAttribute.createDiffuse(Color.FIREBRICK));
                instance.materials.get(0).set(material);
                break;
            default:
                material = new Material(ColorAttribute.createDiffuse(Color.CYAN));
                instance.materials.get(0).set(material);
                break;
        }
    }

    private void filterByType(String type) {
        if (type.equalsIgnoreCase("vse znamenitosti")) {
            for (Attraction a : allAttractions) {
                a.visible = true;
            }
        } else {
            for (Attraction a : allAttractions) {
                a.visible = a.data.locationType.equalsIgnoreCase(type);
            }
        }
    }

    private Actor createDropdown() {
        SelectBox<String> typeList = new SelectBox<>(skin);
        typeList.setItems("vse znamenitosti", "grad", "soteska", "cerkev", "koča", "jezero", "park", "muzej na prostem", "kopališče", "razgledni stolp", "hrib", "planina");
        typeList.setSelected("vse znamenitosti");

        typeList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = typeList.getSelected();
                filterByType(selected);
            }
        });

        Table table = new Table(skin);
        table.setFillParent(true);

        table.top().left();
        table.pad(10);

        table.debug();
        table.add(typeList).left().top();

        return table;
    }
}

