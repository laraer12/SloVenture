package si.um.feri.sloventure;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class MapCameraController extends InputAdapter {

    private final PerspectiveCamera camera;

    private final Vector3 target = new Vector3(0, 0, 0);

    private float distance = 450f;
    private float minDistance = 120f;
    private float maxDistance = 800f;

    private float yaw = 45f;
    private float pitch = 60f;
    private float minPitch = 30f;
    private float maxPitch = 85f;

    private int lastX, lastY;
    private boolean rotating = false;
    private boolean panning = false;

    private float panSpeed = 0.5f;
    private float zoomSpeed = 40f;

    public MapCameraController(PerspectiveCamera camera) {
        this.camera = camera;
        updateCamera();
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        lastX = x;
        lastY = y;

        if (button == Input.Buttons.LEFT) panning = true;
        if (button == Input.Buttons.RIGHT) rotating = true;

        return true;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        panning = false;
        rotating = false;
        return true;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        int dx = x - lastX;
        int dy = y - lastY;

        lastX = x;
        lastY = y;

        if (rotating) {
            yaw -= dx * 0.4f;
            pitch += dy * 0.4f;
            pitch = MathUtils.clamp(pitch, minPitch, maxPitch);
        }

        if (panning) {
            Vector3 right = camera.direction.cpy().crs(Vector3.Y).nor();
            Vector3 forward = new Vector3(camera.direction.x, 0, camera.direction.z).nor();

            target.add(right.scl(-dx * panSpeed));
            target.add(forward.scl(dy * panSpeed));
        }

        updateCamera();
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        distance += amountY * zoomSpeed;
        distance = MathUtils.clamp(distance, minDistance, maxDistance);
        updateCamera();
        return true;
    }

    private void updateCamera() {
        float radYaw = MathUtils.degreesToRadians * yaw;
        float radPitch = MathUtils.degreesToRadians * pitch;

        float x = distance * MathUtils.cos(radPitch) * MathUtils.sin(radYaw);
        float y = distance * MathUtils.sin(radPitch);
        float z = distance * MathUtils.cos(radPitch) * MathUtils.cos(radYaw);

        camera.position.set(
            target.x + x,
            target.y + y,
            target.z + z
        );

        camera.lookAt(target);
        camera.up.set(Vector3.Y);
        camera.update();
    }
}

