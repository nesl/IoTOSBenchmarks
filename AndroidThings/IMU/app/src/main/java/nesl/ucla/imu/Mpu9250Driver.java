package nesl.ucla.imu;

import android.hardware.Sensor;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.sensor.UserSensor;
import com.google.android.things.userdriver.sensor.UserSensorDriver;
import com.google.android.things.userdriver.sensor.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

public class Mpu9250Driver implements AutoCloseable {
    private static final String TAG = Mpu9250Driver.class.getSimpleName();
    private static final String DRIVER_VENDOR = "InvenSense";
    private static final String DRIVER_NAME = "MPU9250";


    private Mpu9250 mDevice;


    private AccelerometerUserDriver mAccelerometerUserDriver;
    private GyroscopeUserDriver mGyroscopeUserDriver;
    // todo: uncomment this
    // private MagnetometerUserDriver mMagnetometerUserDriver;

    public Mpu9250Driver(String bus) throws IOException {
        mDevice = new Mpu9250(bus);

    }

    public Mpu9250Driver(String bus, int address) throws IOException {
        mDevice = new Mpu9250(bus, address);
    }

    public void registerAccelerometer() throws IOException{
        //todo: register accelerometer
        if (mDevice == null) {
            throw new IllegalStateException("cannot register closed driver");
        }

        if (mAccelerometerUserDriver == null) {
            mAccelerometerUserDriver = new AccelerometerUserDriver();
            mAccelerometerUserDriver.setEnabled(true);
            UserDriverManager.getInstance().registerSensor(mAccelerometerUserDriver.getUserSensor());

        }
    }

    public void registerGyroscope() throws IOException {
        if (mDevice == null) {
            throw new IllegalStateException("cannot register closed driver");
        }

        if (mGyroscopeUserDriver == null) {
            mGyroscopeUserDriver = new GyroscopeUserDriver();
            mGyroscopeUserDriver.setEnabled(true);
            UserDriverManager.getInstance().registerSensor(mGyroscopeUserDriver.getUserSensor());
        }
    }

    public void registerMagnetometer() {
        // todo: register magnetometer
    }

    public void unregisterAccelerometer() {
        if (mAccelerometerUserDriver != null) {
            UserDriverManager.getInstance().unregisterSensor(mAccelerometerUserDriver.getUserSensor());
            mAccelerometerUserDriver = null;
        }
    }

    public void unregisterGyroscope() {
        if (mGyroscopeUserDriver != null) {
            UserDriverManager.getInstance().unregisterSensor(mGyroscopeUserDriver.getUserSensor());
            mGyroscopeUserDriver = null;
        }
    }




    /**
     * Close the driver and the underlying device.
     * @throws IOException
     */
    @Override
    public void close() throws IOException{

        unregisterAccelerometer();
        unregisterGyroscope();
        //unregisterMagnetometer();

        if (mDevice != null) {
            try {
                mDevice.close();
            }
            finally {
                mDevice = null;
            }
        }
    }

    private class AccelerometerUserDriver implements UserSensorDriver {
        private static final int DRIVER_VERSION = 1;

        private boolean mEnabled;
        private UserSensor mUserSensor;

        //TODO: check sensor parameters
        private UserSensor getUserSensor(){
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setType(Sensor.TYPE_ACCELEROMETER)
                        .setDriver(this)
                        .setUuid(UUID.randomUUID())
                        .setVersion(DRIVER_VERSION)
                        .setVendor(DRIVER_VENDOR)
                        .setName(DRIVER_NAME)
                        .build();
            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException{
            return new UserSensorReading(mDevice.getAccel());
        }

        @Override
        public void setEnabled(boolean enabled) throws IOException {
            mEnabled = enabled;
            if (enabled) {
                mDevice.enableAccelerometer();
            } else {
                mDevice.disableAccelerometer();
            }

            //TODO: something here
        }

        private boolean isEnabled() {
            return mEnabled;
        }
    }


    private class GyroscopeUserDriver implements UserSensorDriver {
        private static final int DRIVER_VERSION = 1;
        //TODO: check sensor parameters
        private boolean mEnabled;
        private UserSensor mUserSensor;

        private UserSensor getUserSensor(){
            if (mUserSensor == null) {
                mUserSensor = new UserSensor.Builder()
                        .setType(Sensor.TYPE_GYROSCOPE)
                        .setDriver(this)
                        .setUuid(UUID.randomUUID())
                        .setVersion(DRIVER_VERSION)
                        .setVendor(DRIVER_VENDOR)
                        .setName(DRIVER_NAME)
                        .build();
            }
            return mUserSensor;
        }

        @Override
        public UserSensorReading read() throws IOException{
            return new UserSensorReading(mDevice.getGyro());
        }

        @Override
        public void setEnabled(boolean enabled) throws IOException {
            mEnabled = enabled;
            if (enabled) {
                mDevice.enableGyroscope();
            } else {
                mDevice.disableGyroscope();
            }
        }

        private boolean isEnabled() {
            return mEnabled;
        }
    }


}
