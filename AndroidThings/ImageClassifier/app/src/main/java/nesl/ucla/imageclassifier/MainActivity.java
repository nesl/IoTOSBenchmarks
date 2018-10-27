package nesl.ucla.imageclassifier;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.ImageView;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

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

    // Matches the images used to train the TensorFlow model
    private static final Size MODEL_IMAGE_SIZE = new Size(224, 224);

    private TensorFlowImageClassifier mTensorFlowClassifier;

    //Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Started onCreate");

        ImageView imageView = (ImageView)findViewById(R.id.ImageView);

        imageView.setImageResource(getResources().getIdentifier("@drawable/pic1", null, getPackageName()));


        try {
            mTensorFlowClassifier = new TensorFlowImageClassifier(MainActivity.this,
                    MODEL_IMAGE_SIZE.getWidth(), MODEL_IMAGE_SIZE.getHeight());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot initialize TFLite Classifier", e);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pic1, options);
        final Collection<Recognition> results = mTensorFlowClassifier.doRecognize(bitmap);
        Log.i(TAG, "Got the following results from Tensorflow: " + results);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if (mTensorFlowClassifier != null) {
                mTensorFlowClassifier.destroyClassifier();
            }
        } catch(Throwable t) {
            // Close quietly
        } finally {
            mTensorFlowClassifier = null;
        }

    }
}