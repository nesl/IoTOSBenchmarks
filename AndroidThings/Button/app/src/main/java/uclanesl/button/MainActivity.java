package uclanesl.button;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
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
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String BUTTON_PIN_NAME = "BCM4";
    private static final String LED_PIN_NAME = "BCM6";
//    private Gpio mButtonGpio;

    private Gpio mLedGpio;
    private ButtonInputDriver mButtonInputDriver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "Starting MainActivity");


        PeripheralManager manager = PeripheralManager.getInstance();

//        try {
//            // Open Gpio connection
//            mButtonGpio = manager.openGpio(BUTTON_PIN_NAME);
//            // Adjust trigger direction
//            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
//            // Enable edge trigger event
//            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
//            // Register event callback
//            mButtonGpio.registerGpioCallback(mBtnCallback);
//        } catch(IOException e) {
//            Log.e(TAG, "Error on PeripheralIO API", e);
//        }


        try {
            Log.i(TAG, "Configuring GPIO pins");
            mLedGpio = manager.openGpio(LED_PIN_NAME);
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.e(TAG, "Error configuring GPIO pins", e);
        }

        try {
            Log.i(TAG, "Registering button driver " + BUTTON_PIN_NAME);
            // Initialize and register the InputDriver that will emit SPACE key events
            // on GPIO state changes.
            mButtonInputDriver = new ButtonInputDriver(
                    BUTTON_PIN_NAME,
                    Button.LogicState.PRESSED_WHEN_LOW,
                    KeyEvent.KEYCODE_SPACE);
            mButtonInputDriver.register();

        } catch (IOException e) {
            Log.e(TAG, "Error configuring GPIO pins", e);
        }

    }

//    private GpioCallback mBtnCallback  = new GpioCallback() {
//        @Override
//        public boolean onGpioEdge(Gpio gpio) {
//            Log.i(TAG, "Button pressed");
//            return true;
//        }
//    };


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            // Turn on the LED
            //setLedValue(true);
            Log.i(TAG, "Onkeydown");
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            // Turn off the LED
            //setLedValue(false);
            Log.i(TAG, "Onkeyup");
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Update the value of the LED output.
     */
    private void setLedValue(boolean value) {
        try {
            mLedGpio.setValue(value);
        } catch (IOException e) {
            Log.e(TAG, "Error updating GPIO value", e);
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if (mButtonGpio != null) {
//            mButtonGpio.unregisterGpioCallback(mBtnCallback);
//            try {
//                mButtonGpio.close();
//            } catch (IOException e) {
//                Log.e(TAG, "Error on PeripheralIO API", e);
//            }
//        }


        if (mButtonInputDriver != null) {
            mButtonInputDriver.unregister();
            try {
                Log.d(TAG, "Unregistering button");
                mButtonInputDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing Button driver", e);
            } finally{
                mButtonInputDriver = null;
            }
        }


        if (mLedGpio != null) {
            try {
                Log.d(TAG, "Unregistering LED.");
                mLedGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing LED GPIO", e);
            } finally{
                mLedGpio = null;
            }
        }
    }
}
