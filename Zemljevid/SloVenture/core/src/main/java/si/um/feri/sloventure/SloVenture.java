package si.um.feri.sloventure;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
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

import si.um.feri.sloventure.assets.AssetDescriptors;
import si.um.feri.sloventure.common.GameManager;
import si.um.feri.sloventure.data.attraction.Attraction;
import si.um.feri.sloventure.data.attraction.AttractionData;
import si.um.feri.sloventure.data.attraction.AttractionService;
import si.um.feri.sloventure.data.attraction.Terrain;
import si.um.feri.sloventure.data.crowd.CrowdData;
import si.um.feri.sloventure.data.crowd.CrowdService;

public class SloVenture extends ApplicationAdapter { //TODO uredi premikanje kamere
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;

    private final Array<Model> chunkModels = new Array<>();
    private Array<ModelInstance> chunkInstances = new Array<>();

    private Texture terrainTexture;

    private Assets assets;

    private Array<Attraction> allAttractions;
    private List<AttractionData> attractionData;

    private Skin skin;
    private AttractionPicker attractionPicker;
    private Stage stage;
    private Terrain terrain;

    @Override
    public void create() {
        modelBatch = new ModelBatch();

        assets = new Assets();

        assets.load();
        assets.finishLoading();

        skin = assets.get(AssetDescriptors.UI_SKIN);
        stage = new Stage(new ScreenViewport());
        setupCamera();
        MapCameraController mapController = new MapCameraController(camera);
        attractionPicker = new AttractionPicker(stage, skin, mapController);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(attractionPicker);
        multiplexer.addProcessor(mapController);

        Gdx.input.setInputProcessor(multiplexer);

        allAttractions = new Array<>();

        terrainTexture = new Texture(Gdx.files.internal("images/slovenia_sat_small.png"));
        //terrainTexture = new Texture(Gdx.files.internal("images/slovenia_sat_big.png"));
        terrainTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        terrainTexture.setWrap(
            Texture.TextureWrap.ClampToEdge,
            Texture.TextureWrap.ClampToEdge
        );

        terrain = new Terrain("images/slovenia_clipped_4000.png", terrainTexture);


        setupLight();
        chunkInstances = terrain.createTerrainChunks("images/slovenia_clipped_4000.png");

        // API test (VSE znamenitosti)
        AttractionService.fetchAllAttractions(new AttractionService.Callback() {
            @Override
            public void onSuccess(List<AttractionData> attractions) {
                System.out.println("Število pridobljenih znamenitosti: " + attractions.size() + "\n");

                attractionData = attractions;

                for (AttractionData a : attractions) {
                    // pridobi gnečo glede na lat/lon
                    CrowdService.fetchCrowdDataForAttraction(a.lat, a.lon, new CrowdService.Callback() {
                        @Override
                        public void onSuccess(List<CrowdData> crowdList) {
                            a.crowd = crowdList; // shranim gnečo v AttractionData
                            //printAttraction(a);
                        }

                        @Override
                        public void onFailure(String message) {
                            System.err.println("Failed to fetch crowd data for " + a.name + ": " + message);
                        }
                    });
                }
            }

            @Override
            public void onFailure(int status, String message) {
                System.err.println("Failed to fetch attractions");
                System.err.println("Status: " + status + " | Reason: " + message);
                attractionData = GameManager.INSTANCE.loadAttractions();
            }
        });
    }

    @Override
    public void render() {
        if (attractionData != null) {
            for (AttractionData a : attractionData) {

                Model model = getModelForAttraction(a);

                ModelInstance instance = new ModelInstance(model);

                Material material = new Material(ColorAttribute.createDiffuse(Color.BLUE));
                instance.materials.get(0).set(material);

                Attraction attraction = new Attraction(a, instance, terrain.getHeightmap());
                allAttractions.add(attraction);
            }
            attractionPicker.allAttractions = allAttractions;
            //GameManager.INSTANCE.saveAttractions(attractionData);

            attractionData = null;
        }

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.6f, 0.8f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        for (ModelInstance instance : chunkInstances) {
            modelBatch.render(instance, environment);
        }

        if (allAttractions.size > 50) {
            for (int i = 0; i < 50; i++) {
                allAttractions.get(i).visible = true;
                BoundingBox bbox = new BoundingBox();
                allAttractions.get(i).modelInstance.calculateBoundingBox(bbox);

                modelBatch.render(allAttractions.get(i).modelInstance, environment);
            }
        }
        modelBatch.end();

        attractionPicker.updateCamera();
        attractionPicker.stage.act(Gdx.graphics.getDeltaTime());
        attractionPicker.stage.draw();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        for (Model m : chunkModels) m.dispose();
        terrainTexture.dispose();

        assets.dispose();
    }

    private void setupCamera() {
        camera = new PerspectiveCamera(
            60,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

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
            ColorAttribute.AmbientLight,
            0.4f, 0.4f, 0.4f, 1f
        ));
        environment.add(new DirectionalLight().set(
            1.1f, 1.1f, 1.1f,
            -0.3f, -1f, -0.2f
        ));
    }

    private Model getModelForAttraction(AttractionData data) {

        return assets.get(AssetDescriptors.OTHER);
        /*
        switch (data.locationType.toLowerCase()) {
            case "grad":
                return assets.get(AssetDescriptors.CASTLE);
            case "soteska":
                return assets.get(AssetDescriptors.CANYON);
            case "cerkev":
                return assets.get(AssetDescriptors.CHURCH);
            case "koča":
                return assets.get(AssetDescriptors.CABIN);
            case "jezero":
                return assets.get(AssetDescriptors.LAKE);
            case "park":
                return assets.get(AssetDescriptors.PARK);
            case "muzej":
                return assets.get(AssetDescriptors.MUSEUM);
            case "kopališče":
                return assets.get(AssetDescriptors.POOL);
            case "razgledni_stolp":
                return assets.get(AssetDescriptors.WATCH_TOWER);
            default:
                return assets.get(AssetDescriptors.OTHER);
        }

         */

    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        attractionPicker.stage.getViewport().update(width, height, true);
        attractionPicker.onResize();
    }
}
