package si.um.feri.sloventure.sloventureandroid.camera

import android.net.Uri
import android.os.Environment
import android.content.Context
import android.provider.MediaStore
import androidx.camera.core.Preview
import android.content.ContentValues
import androidx.camera.view.PreviewView
import androidx.camera.core.ImageCapture
import androidx.lifecycle.LifecycleOwner
import androidx.core.content.ContextCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider

class CameraController(
    private val context: Context // context aktivnosti (za CameraX in MediaStore)
) {
    private var imageCapture: ImageCapture? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    // zagon kamere in prikaz preview
    fun startCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.surfaceProvider = previewView.surfaceProvider

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    context as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            }
            catch (ex: Exception) {
                ex.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // zajem in shranjevanje slike v galerijo
    fun capturePhoto(filename: String = "my_photo.jpg", onSaved: (uri: Uri?, timestamp: Long) -> Unit) {
        val imageCapture = imageCapture ?: run {
            onSaved(null, 0L)
            return
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(ex: ImageCaptureException) {
                    ex.printStackTrace()
                    onSaved(null, 0L)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val timestamp = System.currentTimeMillis()
                    onSaved(output.savedUri, timestamp)
                }
            }
        )
    }

    fun switchCamera(previewView: PreviewView) {
        cameraSelector =
            if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                CameraSelector.DEFAULT_FRONT_CAMERA
            else
                CameraSelector.DEFAULT_BACK_CAMERA

        startCamera(previewView)
    }

    fun stopCamera() {
        ProcessCameraProvider.getInstance(context).get().unbindAll()
    }
}