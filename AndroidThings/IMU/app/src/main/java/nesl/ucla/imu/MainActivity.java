package nesl.ucla.imu;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

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
public class MainActivity extends Activity {


    private static String TAG = MainActivity.class.getSimpleName();


    // I2C device name
    private static final String I2C_BUS = "I2C1";

    private Mpu9250Driver mIMUDriver;
    private SensorManager mSensorManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "MainActivity");

        PeripheralManager manager = PeripheralManager.getInstance();

        List<String> deviceList = manager.getI2cBusList();

        if(deviceList.isEmpty()) {
            Log.i(TAG, "No I2C bus available on this device.");
        } else {
            Log.i(TAG, "List of available devices: " + deviceList);
        }

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        try {
            // Open I2C device
            mIMUDriver = new Mpu9250Driver(I2C_BUS);
            mSensorManager.registerDynamicSensorCallback(mDynamicsSensorCallback);
            mIMUDriver.registerAccelerometer();
            mIMUDriver.registerGyroscope();

        } catch(IOException e) {
            Log.e(TAG, "Peripheral IO exception", e);
        }
    }


    private SensorManager.DynamicSensorCallback mDynamicsSensorCallback = new SensorManager.DynamicSensorCallback() {
        @Override
        public void onDynamicSensorConnected(Sensor sensor) {
            super.onDynamicSensorConnected(sensor);
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mSensorManager.registerListener(mAccelerometerListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                mSensorManager.registerListener(mGyroscopeListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
            }
        }

        @Override
        public void onDynamicSensorDisconnected(Sensor sensor) {
            super.onDynamicSensorDisconnected(sensor);
        }
    };

    private SensorEventListener mAccelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            //todo: show accel value
            Log.d(TAG, "accel " + event.values[0] +","+ event.values[1] +"," +event.values[2]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // todo: show sensor acc changed
        }
    };


    private SensorEventListener mGyroscopeListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.d(TAG, "gyro " + event.values[0] +","+ event.values[1] +"," +event.values[2]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
        mSensorManager.unregisterListener(mAccelerometerListener);

        mSensorManager.unregisterDynamicSensorCallback(mDynamicsSensorCallback);

        if (mIMUDriver != null) {
            try {
                mIMUDriver.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mIMUDriver = null;
            }
        }
    }
}
