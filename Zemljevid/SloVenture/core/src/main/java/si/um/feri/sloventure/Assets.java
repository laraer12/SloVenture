package si.um.feri.sloventure;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;

import si.um.feri.sloventure.assets.AssetDescriptors;

public class Assets {
    AssetManager assetManager = new AssetManager();

    public void load(){
        assetManager.load(AssetDescriptors.CABIN);
        assetManager.load(AssetDescriptors.CANYON);
        assetManager.load(AssetDescriptors.CASTLE);
        assetManager.load(AssetDescriptors.CHURCH);
        assetManager.load(AssetDescriptors.LAKE);
        assetManager.load(AssetDescriptors.MUSEUM);
        assetManager.load(AssetDescriptors.OTHER);
        assetManager.load(AssetDescriptors.PARK);
        assetManager.load(AssetDescriptors.POOL);
        assetManager.load(AssetDescriptors.PERSON);
        assetManager.load(AssetDescriptors.WATCH_TOWER);
    }

    public void finishLoading(){
        assetManager.finishLoading();
    }

    public void dispose(){
        assetManager.dispose();
    }

    public <T> T get(AssetDescriptor<T> descriptor){
        return assetManager.get(descriptor);
    }
}
