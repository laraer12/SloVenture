package si.um.feri.sloventure;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;

import java.util.List;

import si.um.feri.sloventure.assets.AssetDescriptors;
import si.um.feri.sloventure.data.attraction.Attraction;
import si.um.feri.sloventure.data.attraction.AttractionData;
import si.um.feri.sloventure.data.attraction.AttractionImage;
import si.um.feri.sloventure.data.attraction.AttractionService;
import si.um.feri.sloventure.data.crowd.CrowdData;
import si.um.feri.sloventure.data.crowd.CrowdService;

public class SloVenture extends ApplicationAdapter { //TODO uredi premikanje kamere
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;

    private final Array<Model> chunkModels = new Array<>();
    private final Array<ModelInstance> chunkInstances = new Array<>();

    private Texture terrainTexture;
    private float terrainWidth;
    private float terrainDepth;


    private static final int CHUNK_SIZE = 64;
    private static final float HEIGHT_SCALE = 40f;
    private static final float TERRAIN_SCALE = 1f;

    private Model testModel;
    private ModelInstance testInstance;

    private Assets assets;

    private Array<Attraction> allAttractions;
    private List<AttractionData> attractionData;

    private Pixmap pixmap;

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        setupCamera();
        MapCameraController mapController = new MapCameraController(camera);
        Gdx.input.setInputProcessor(mapController);

        allAttractions = new Array<>();

        assets = new Assets();

        assets.load();
        assets.finishLoading();

        terrainTexture = new Texture(Gdx.files.internal("images/slovenia_sat_small.png"));
        //terrainTexture = new Texture(Gdx.files.internal("images/slovenia_sat_big.png"));
        terrainTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        terrainTexture.setWrap(
            Texture.TextureWrap.ClampToEdge,
            Texture.TextureWrap.ClampToEdge
        );

        setupLight();
        createTerrainChunks("images/slovenia_clipped_4000.png");

        // TESTIRANJE 3D modelov
        ModelLoader<?> loader = new G3dModelLoader(new JsonReader());
        //testModel = loader.loadModel(Gdx.files.internal("models/person/Person.g3dj")); // castle/Castle.g3dj // church/Church.g3dj // cabin/Cabin.g3dj // pool/Pool.g3dj // lake/Lake.g3dj // museum/Museum.g3dj // canyon/Canyon.g3dj // park/Park.g3dj watch_tower/WatchTower.g3dj // other/Other.g3dj
        testModel = assets.get(AssetDescriptors.CABIN);
        testInstance = new ModelInstance(testModel);

        // scale modela
        testInstance.transform.idt();
        testInstance.transform.scale(0.01f, 0.01f, 0.01f);

        // lokacija modela
        testInstance.transform.translate(0f, 500f, 0f);

        pixmap = new Pixmap(Gdx.files.internal("images/slovenia_clipped_4000.png"));

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
            }
        });

        // filter po KLASIFIKACIJI
        /*
        AttractionService.fetchByClassification("Kultura", new AttractionService.Callback() { // Kultura, Naravne lepote, Pohodništvo, Poletna osvežitev, Raziskovanje, Supanje
            @Override
            public void onSuccess(List<AttractionData> attractions) {
                System.out.println("Število pridobljenih znamenitosti: " + attractions.size() + "\n");

                for (AttractionData a : attractions) {
                    // pridobi gnečo glede na lat/lon
                    CrowdService.fetchCrowdDataForAttraction(a.lat, a.lon, new CrowdService.Callback() {
                        @Override
                        public void onSuccess(List<CrowdData> crowdList) {
                            a.crowd = crowdList; // shranim gnečo v AttractionData
                            printAttraction(a);
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
            }
        });
        */

        // filter po REGIJI
        /*
        AttractionService.fetchByRegion("6846a8cf845a679bffb1c82e", new AttractionService.Callback() { // to je id Gorenjske
            @Override
            public void onSuccess(List<AttractionData> attractions) {
                System.out.println("Število pridobljenih znamenitosti: " + attractions.size() + "\n");

                for (AttractionData a : attractions) {
                    // pridobi gnečo glede na lat/lon
                    CrowdService.fetchCrowdDataForAttraction(a.lat, a.lon, new CrowdService.Callback() {
                        @Override
                        public void onSuccess(List<CrowdData> crowdList) {
                            a.crowd = crowdList; // shranim gnečo v AttractionData
                            printAttraction(a);
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
            }
        });
        */

        // filter po TIPU LOKACIJE
        /*
        AttractionService.fetchByLocationType("Cerkev", new AttractionService.Callback() { // Cerkev, Drugo, Dvorec, Grad, Hrib, Izvir, Jama, Jezero, Kopališče, Koča, Muzej na prostem, Park, Planina, Razgledni stolp, SUP točka, Slap, Soteska
            @Override
            public void onSuccess(List<AttractionData> attractions) {
                System.out.println("Število pridobljenih znamenitosti: " + attractions.size() + "\n");

                for (AttractionData a : attractions) {
                    // pridobi gnečo glede na lat/lon
                    CrowdService.fetchCrowdDataForAttraction(a.lat, a.lon, new CrowdService.Callback() {
                        @Override
                        public void onSuccess(List<CrowdData> crowdList) {
                            a.crowd = crowdList; // shranim gnečo v AttractionData
                            printAttraction(a);
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
            }
        });
        */
    }

    private void printAttraction(AttractionData a) {
        System.out.println("ATTRACTION - " + a.name +
            "\nRegion - " + a.regionName +
            "\nLocation - LAT: " + a.lat + ", LON: " + a.lon +
            "\nAddress - Street: " + a.street + ", City: " + a.city + ", Postal code: " + a.postalCode +
            "\nDescription - " + a.description +
            "\nClassification - " + a.classification +
            "\nLocation type - " + a.locationType +
            "\nElevation - " + a.elevation + " m" +
            "\nRating - " + a.rating +
            "\nImages - "
        );
        for (AttractionImage img : a.images)
            System.out.println(" URL: " + img.url);

        // poleg znamenitosti pridobim še gnečo ljudi pri njej
        if (a.crowd != null && !a.crowd.isEmpty()) {
            System.out.println("Crowd data:");

            for (CrowdData c : a.crowd)
                System.out.println(" " + c.toString());
        } else
            System.out.println("No crowd data");

        System.out.print("\n");
    }

    @Override
    public void render() {
        if (attractionData != null) {

            for (AttractionData a : attractionData) {

                Model model = getModelForAttraction(a);
                ModelInstance instance = new ModelInstance(model);

                allAttractions.add(new Attraction(a, instance, pixmap));
            }

            pixmap.dispose();

            attractionData = null;
        }

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.6f, 0.8f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        for (ModelInstance instance : chunkInstances) {
            modelBatch.render(instance, environment);
        }
        modelBatch.render(testInstance, environment); // model

        if (allAttractions.size > 50) {
            for (int i = 0; i < 50; i++) {
                if (i == 0) {
                    //System.out.println(allAttractions.get(i).data.name);
                }
                BoundingBox bbox = new BoundingBox();
                allAttractions.get(i).modelInstance.calculateBoundingBox(bbox);


                modelBatch.render(allAttractions.get(i).modelInstance, environment);
            }
        }
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        for (Model m : chunkModels) m.dispose();
        terrainTexture.dispose();

        if (testModel != null)
            testModel.dispose();
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

    private void createTerrainChunks(String heightmapPath) {
        Pixmap pixmap = new Pixmap(Gdx.files.internal(heightmapPath));

        int width = pixmap.getWidth();
        int height = pixmap.getHeight();

        terrainWidth = width * TERRAIN_SCALE;
        terrainDepth = height * TERRAIN_SCALE;

        float offsetX = -width / 2f;
        float offsetZ = -height / 2f;

        for (int startX = 0; startX < width - 1; startX += CHUNK_SIZE) {
            for (int startZ = 0; startZ < height - 1; startZ += CHUNK_SIZE) {

                int chunkWidth = Math.min(CHUNK_SIZE, width - startX - 1);
                int chunkHeight = Math.min(CHUNK_SIZE, height - startZ - 1);

                Model chunkModel = createChunk(
                    pixmap,
                    startX,
                    startZ,
                    chunkWidth,
                    chunkHeight,
                    offsetX,
                    offsetZ
                );

                chunkModels.add(chunkModel);
                chunkInstances.add(new ModelInstance(chunkModel));
            }
        }

        pixmap.dispose();
    }

    private Model createChunk(
        Pixmap pixmap,
        int startX,
        int startZ,
        int chunkWidth,
        int chunkHeight,
        float offsetX,
        float offsetZ
    ) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        Material material = new Material(
            TextureAttribute.createDiffuse(terrainTexture)
        );

        MeshPartBuilder builder = modelBuilder.part(
            "chunk",
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal |
                VertexAttributes.Usage.TextureCoordinates,
            material
        );

        Vector3 normal = new Vector3(0, 1, 0);


        for (int x = 0; x < chunkWidth; x++) {
            for (int z = 0; z < chunkHeight; z++) {

                int px = startX + x;
                int pz = startZ + z;

                float h1 = getHeight(pixmap, px, pz);
                float h2 = getHeight(pixmap, px + 1, pz);
                float h3 = getHeight(pixmap, px + 1, pz + 1);
                float h4 = getHeight(pixmap, px, pz + 1);

                Vector3 v1 = new Vector3((px + offsetX) * TERRAIN_SCALE, h1, (pz + offsetZ) * TERRAIN_SCALE);
                Vector3 v2 = new Vector3((px + 1 + offsetX) * TERRAIN_SCALE, h2, (pz + offsetZ) * TERRAIN_SCALE);
                Vector3 v3 = new Vector3((px + 1 + offsetX) * TERRAIN_SCALE, h3, (pz + 1 + offsetZ) * TERRAIN_SCALE);
                Vector3 v4 = new Vector3((px + offsetX) * TERRAIN_SCALE, h4, (pz + 1 + offsetZ) * TERRAIN_SCALE);

                boolean t1 = isTransparent(pixmap, px, pz);
                boolean t2 = isTransparent(pixmap, px + 1, pz);
                boolean t3 = isTransparent(pixmap, px + 1, pz + 1);
                boolean t4 = isTransparent(pixmap, px, pz + 1);

                if (t1 || t2 || t3 || t4) {
                    continue;
                }

                float u1 = (px / (float) (pixmap.getWidth() - 1));
                float u2 = ((px + 1) / (float) (pixmap.getWidth() - 1));


                float v1t = (pz / (float) (pixmap.getHeight() - 1));
                float v2t = ((pz + 1) / (float) (pixmap.getHeight() - 1));


                MeshPartBuilder.VertexInfo vi1 = new MeshPartBuilder.VertexInfo()
                    .set(v1, normal, null, new Vector2(u1, v1t));

                MeshPartBuilder.VertexInfo vi2 = new MeshPartBuilder.VertexInfo()
                    .set(v2, normal, null, new Vector2(u2, v1t));

                MeshPartBuilder.VertexInfo vi3 = new MeshPartBuilder.VertexInfo()
                    .set(v3, normal, null, new Vector2(u2, v2t));

                MeshPartBuilder.VertexInfo vi4 = new MeshPartBuilder.VertexInfo()
                    .set(v4, normal, null, new Vector2(u1, v2t));

                builder.triangle(vi1, vi3, vi2);
                builder.triangle(vi1, vi4, vi3);

            }
        }

        return modelBuilder.end();
    }

    private float getHeight(Pixmap pixmap, int x, int z) {
        int pixel = pixmap.getPixel(x, z);
        int value = (pixel >> 16) & 0xff;
        return (value / 255f) * HEIGHT_SCALE;
    }

    private boolean isTransparent(Pixmap pixmap, int x, int z) {
        x = Math.max(0, Math.min(x, pixmap.getWidth() - 1));
        z = Math.max(0, Math.min(z, pixmap.getHeight() - 1));

        int pixel = pixmap.getPixel(x, z);
        int alpha = pixel & 0xff;

        return alpha == 0;
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
}
