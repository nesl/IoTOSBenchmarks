package nesl.ucla.camera;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.nio.ByteBuffer;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity {

    private static String TAG = MainActivity.class.getSimpleName();


    private PiCamera mCamera;
    private Handler mCameraHandler;
    private HandlerThread mCameraThread;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // We need permission to access the camera
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // A problem occurred auto-granting the permission
            Log.e(TAG, "No permission");
            return;
        }

        PiCamera.dumpFormatInfo(this);


        mCamera = PiCamera.getInstance();

        mCameraThread = new HandlerThread("Camera Thread");
        mCameraThread.start();


        mCameraHandler = new Handler(mCameraThread.getLooper());

        mCamera.initializeCamera(this, mCameraHandler, mOnImageAvailableListener);

        Button button = findViewById(R.id.captureButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture();
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.shutDown();
        mCameraThread.quitSafely();
    }



    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

            //TODO: process the image

            Image image = reader.acquireLatestImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            final Bitmap myBitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,null);

            image.close();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageView imageView = findViewById(R.id.imageView);
                    imageView.setImageBitmap(myBitmap);
                }
            });

            Log.d(TAG, "Have an image available");
        }
    };

}
