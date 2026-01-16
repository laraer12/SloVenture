package si.um.feri.sloventure.data.attraction;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import si.um.feri.sloventure.config.GameConfig;

public class Attraction {
    public AttractionData data;
    public Vector3 worldPosition;
    public ModelInstance modelInstance;
    public boolean visible = false;
    public BoundingBox boundingBox = new BoundingBox();

    public Attraction(AttractionData data, ModelInstance modelInstance, Pixmap pixmap) {
        this.data = data;
        this.modelInstance = modelInstance;
        this.worldPosition = convertToWorldPosition(pixmap, data.lat, data.lon);

        modelInstance.transform.idt();
        modelInstance.transform.translate(worldPosition);
        modelInstance.transform.scale(GameConfig.MODEL_SIZE, GameConfig.MODEL_SIZE, GameConfig.MODEL_SIZE);

        modelInstance.calculateBoundingBox(boundingBox);
        boundingBox.mul(modelInstance.transform);
    }

    private Vector3 convertToWorldPosition(Pixmap pixmap, float lat, float lon) {
        float nx = (lon - GameConfig.MIN_LON) / (GameConfig.MAX_LON - GameConfig.MIN_LON);

        float nz = 1f - (lat - GameConfig.MIN_LAT) / (GameConfig.MAX_LAT - GameConfig.MIN_LAT);

        float px = nx * (pixmap.getWidth() - 1);
        float pz = nz * (pixmap.getHeight() - 1);

        float offsetX = -pixmap.getWidth() / 2f;
        float offsetZ = -pixmap.getHeight() / 2f;

        float worldX = (px + offsetX) * GameConfig.TERRAIN_SCALE;
        float worldZ = (pz + offsetZ) * GameConfig.TERRAIN_SCALE;

        float height = getHeight(pixmap, (int) px, (int) pz) + GameConfig.MODEL_HEIGHT_CORRECTION;

        return new Vector3(worldX, height, worldZ);
    }

    private float getHeight(Pixmap pixmap, int x, int z) {
        int pixel = pixmap.getPixel(x, z);
        int value = (pixel >> 16) & 0xff;
        return (value / 255f) * GameConfig.HEIGHT_SCALE;
    }
}
