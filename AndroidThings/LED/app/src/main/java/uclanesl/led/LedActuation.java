package uclanesl.led;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

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
public class LedActuation extends Activity {

    private static final String TAG = LedActuation.class.getSimpleName();

    private static final int INTERVAL_BETWEEN_BLINKS_MS = 1000;

    private static final String LED_PIN_NAME = "BCM4"; // GPIO port wired to the LED

    private Handler mHandler = new Handler();
    private Gpio mLedGpio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_actuation);

        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            mLedGpio = manager.openGpio(LED_PIN_NAME);
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mHandler.post(mBlinkRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove handler events on close.
        mHandler.removeCallbacks(mBlinkRunnable);

        if (mLedGpio != null) {
            try {
                mLedGpio.close();
            } catch(IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLedGpio == null) {
                return;
            }
            try {
                mLedGpio.setValue(!mLedGpio.getValue());
                mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on Peripheral IO API", e);
            }
        }
    };
}
