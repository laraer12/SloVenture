package si.um.feri.sloventure.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class AssetDescriptors {
    public static final AssetDescriptor<Model> CABIN = new AssetDescriptor<>(AssetPaths.MODEL_CABIN, Model.class);
    public static final AssetDescriptor<Model> OTHER = new AssetDescriptor<>(AssetPaths.MODEL_OTHER, Model.class);
    public static final AssetDescriptor<Model> PERSON = new AssetDescriptor<>(AssetPaths.MODEL_PERSON, Model.class);
    public static final AssetDescriptor<Skin> UI_SKIN = new AssetDescriptor<>(AssetPaths.UI_SKIN, Skin.class);
}
