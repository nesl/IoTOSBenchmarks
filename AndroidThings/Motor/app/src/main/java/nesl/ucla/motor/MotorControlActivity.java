package nesl.ucla.motor;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

import java.io.IOException;
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
public class MotorControlActivity extends Activity {
    private static String TAG = MotorControlActivity.class.getSimpleName();

    private static String PWM_PIN_NAME = "PWM1";

    // Parameters of the servo PWM
    private static final double MIN_ACTIVE_PULSE_DURATION_MS = 1;
    private static final double MAX_ACTIVE_PULSE_DURATION_MS = 2;
    private static final double PULSE_PERIOD_MS = 20;  // Frequency of 50Hz (1000/20)

    // Parameters for the servo movement over time
    private static final double PULSE_CHANGE_PER_STEP_MS = 0.2;
    private static final int INTERVAL_BETWEEN_STEPS_MS = 50;

    private Pwm mPwm;
    private Handler mHandler = new Handler();

    private boolean mIsPulseIncreasing = true;
    private double mActivePulseDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motor_control);

        PeripheralManager manager = PeripheralManager.getInstance();

        List<String> list = manager.getPwmList();
        Log.i(TAG, "found pwm " + list);


        try {
            mPwm = manager.openPwm(PWM_PIN_NAME);
            mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;

            mPwm.setPwmDutyCycle(100 * mActivePulseDuration / PULSE_PERIOD_MS);
            mPwm.setPwmFrequencyHz(1000/PULSE_PERIOD_MS);
            mPwm.setEnabled(true);

            // Post a Runnable that continuously change PWM pulse width, effectively changing the
            // servo position
            Log.d(TAG, "Start changing PWM pulse");
            mHandler.post(mChangePWMRunnable);
        } catch (IOException e) {
            Log.e(TAG,"Peripheral IO exception", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPwm != null) {
            try{
                mPwm.setEnabled(false);
                mPwm.close();
            } catch(IOException e) {
                Log.e(TAG,"Peripheral IO exception", e);
            } finally {
                mPwm = null;
            }
        }
    }

    private Runnable mChangePWMRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the port is already closed
            if (mPwm == null) {
                Log.w(TAG, "Stopping runnable since mPwm is null");
                return;
            }

            // Change the duration of the active PWM pulse, but keep it between the minimum and
            // maximum limits.
            // The direction of the change depends on the mIsPulseIncreasing variable, so the pulse
            // will bounce from MIN to MAX.
            if (mIsPulseIncreasing) {
                mActivePulseDuration += PULSE_CHANGE_PER_STEP_MS;
            } else {
                mActivePulseDuration -= PULSE_CHANGE_PER_STEP_MS;
            }

            // Bounce mActivePulseDuration back from the limits
            if (mActivePulseDuration > MAX_ACTIVE_PULSE_DURATION_MS) {
                mActivePulseDuration = MAX_ACTIVE_PULSE_DURATION_MS;
                mIsPulseIncreasing = !mIsPulseIncreasing;
            } else if (mActivePulseDuration < MIN_ACTIVE_PULSE_DURATION_MS) {
                mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
                mIsPulseIncreasing = !mIsPulseIncreasing;
            }

            Log.d(TAG, "Changing PWM active pulse duration to " + mActivePulseDuration + " ms");

            try {
                // Duty cycle is the percentage of active (on) pulse over the total duration of the
                // PWM pulse
                mPwm.setPwmDutyCycle(100 * mActivePulseDuration / PULSE_PERIOD_MS);

                // Reschedule the same runnable in {@link #INTERVAL_BETWEEN_STEPS_MS} milliseconds
                mHandler.postDelayed(this, INTERVAL_BETWEEN_STEPS_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };
}
