package si.um.feri.sloventure;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;

import si.um.feri.sloventure.assets.AssetDescriptors;

public class Assets {
    AssetManager assetManager = new AssetManager();

    public void load() {
        assetManager.load(AssetDescriptors.CABIN);
        assetManager.load(AssetDescriptors.OTHER);
        assetManager.load(AssetDescriptors.PERSON);
        assetManager.load(AssetDescriptors.UI_SKIN);
    }

    public boolean update() {
        return assetManager.update();
    }
    public float getProgress() {
        return assetManager.getProgress();
    }

    public void finishLoading() {
        assetManager.finishLoading();
    }

    public <T> T get(AssetDescriptor<T> descriptor) {
        return assetManager.get(descriptor);
    }

    public void dispose() {
        assetManager.dispose();
    }}
