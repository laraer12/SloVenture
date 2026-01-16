package si.um.feri.sloventure.data.attraction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import si.um.feri.sloventure.config.GameConfig;

public class Terrain {
    private final Pixmap heightmap;
    private final Texture terrainTexture;

    public Array<Model> chunkModels = new Array<>();

    public Terrain(String heightmapPath, Texture terrainTexture) {
        heightmap = new Pixmap(Gdx.files.internal(heightmapPath));
        this.terrainTexture = terrainTexture;
        createTerrainChunks(heightmapPath);
    }

    public Pixmap getHeightmap() {
        return heightmap;
    }

    public void dispose() {
        heightmap.dispose();
    }

    private float getHeight(Pixmap pixmap, int x, int z) {
        int pixel = pixmap.getPixel(x, z);
        int value = (pixel >> 16) & 0xff;
        return (value / 255f) * GameConfig.HEIGHT_SCALE;
    }


    public Array<ModelInstance> createTerrainChunks(String heightmapPath) {
        Array<ModelInstance> chunkInstances = new Array<>();
        Pixmap pixmap = new Pixmap(Gdx.files.internal(heightmapPath));

        int width = pixmap.getWidth();
        int height = pixmap.getHeight();

        float offsetX = -width / 2f;
        float offsetZ = -height / 2f;

        for (int startX = 0; startX < width - 1; startX += GameConfig.CHUNK_SIZE) {
            for (int startZ = 0; startZ < height - 1; startZ += GameConfig.CHUNK_SIZE) {

                int chunkWidth = Math.min(GameConfig.CHUNK_SIZE, width - startX - 1);
                int chunkHeight = Math.min(GameConfig.CHUNK_SIZE, height - startZ - 1);

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
        return chunkInstances;
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

                Vector3 v1 = new Vector3((px + offsetX) * GameConfig.TERRAIN_SCALE, h1, (pz + offsetZ) * GameConfig.TERRAIN_SCALE);
                Vector3 v2 = new Vector3((px + 1 + offsetX) * GameConfig.TERRAIN_SCALE, h2, (pz + offsetZ) * GameConfig.TERRAIN_SCALE);
                Vector3 v3 = new Vector3((px + 1 + offsetX) * GameConfig.TERRAIN_SCALE, h3, (pz + 1 + offsetZ) * GameConfig.TERRAIN_SCALE);
                Vector3 v4 = new Vector3((px + offsetX) * GameConfig.TERRAIN_SCALE, h4, (pz + 1 + offsetZ) * GameConfig.TERRAIN_SCALE);

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

    private boolean isTransparent(Pixmap pixmap, int x, int z) {
        x = Math.max(0, Math.min(x, pixmap.getWidth() - 1));
        z = Math.max(0, Math.min(z, pixmap.getHeight() - 1));

        int pixel = pixmap.getPixel(x, z);
        int alpha = pixel & 0xff;

        return alpha == 0;
    }
}
