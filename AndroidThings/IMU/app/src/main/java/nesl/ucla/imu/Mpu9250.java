package nesl.ucla.imu;

import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class Mpu9250 implements AutoCloseable{

    private static final String TAG = Mpu9250.class.getSimpleName();
    /**
     * I2C slave addresses for MPU9250
     */
    public static final int DEFAULT_I2C_ADDRESS = 0b1101000;
    // TODO: add the range of values here


    // Registers
    private static final int PWR_MGMT_1 = 0x6B;
    private static final int PWR_MGMT_2 = 0x6C;



    //PWR_MGMT_2 register masks
    private static final int DISABLE_XA_MASK = 0x20;
    private static final int DISABLE_YA_MASK = 0x10;
    private static final int DISABLE_ZA_MASK = 0x08;
    private static final int DISABLE_XG_MASK = 0x04;
    private static final int DISABLE_YG_MASK = 0x02;
    private static final int DISABLE_ZG_MASK = 0x01;

    private static final int DISABLE_XYZA_MASK = 0x38;
    private static final int DISABLE_XYZG_MASK = 0x07;


    private static final int INT_STATUS = 0x3A;
    private static final int ACCEL_XOUT_H = 0x3B;
    private static final int ACCEL_XOUT_L = 0x3C;
    private static final int ACCEL_YOUT_H = 0x3D;
    private static final int ACCEL_YOUT_L = 0x3E;
    private static final int ACCEL_ZOUT_H = 0x3F;
    private static final int ACCEL_ZOUT_L = 0x40;
    private static final int TEMP_OUT_H = 0x41;
    private static final int TEMP_OUT_L = 0x42;
    private static final int GYRO_XOUT_H = 0x43;
    private static final int GYRO_XOUT_L = 0x44;
    private static final int GYRO_YOUT_H = 0x45;
    private static final int GYRO_YOUT_L = 0x46;
    private static final int GYRO_ZOUT_H = 0x47;
    private static final int GYRO_ZOUT_L = 0x48;



    private I2cDevice mDevice;
    private final byte[] mBuffer = new byte[3]; // for reading sensor values

    public Mpu9250(String bus) throws IOException {
        this(bus, DEFAULT_I2C_ADDRESS);
    }

    public Mpu9250(String bus, int address) throws IOException {
        PeripheralManager pioService = PeripheralManager.getInstance();
        I2cDevice device = pioService.openI2cDevice(bus, address);
        try {
            connect(device);
        } catch(IOException|RuntimeException e) {
            try{
                close();
            } catch(IOException|RuntimeException ignored) {

            }
            throw(e);
        }
    }

    private void connect(I2cDevice device) throws IOException {
        if (mDevice != null) {
            throw new IllegalStateException("Device already connected");
        }
        mDevice = device;
        // todo: set sampling rate
    }


    @Override
    public void close() throws IOException {
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }


    // Accelerometer Methods

//    void enableAccelAxis(int axis) {
//        // TODO: enable accel axis
//    }
//    void disableAccelAxis(int axis) {
//        // TODO: enable accel axis
//    }

    void enableAccelerometer() throws IOException, IllegalStateException {
        int status = readRegister(PWR_MGMT_2);
        status = status & ~DISABLE_XYZA_MASK;
        writeRegister(PWR_MGMT_2, status);

    }

    void disableAccelerometer() throws IOException, IllegalStateException {
        int status = readRegister(PWR_MGMT_2);
        status = status | DISABLE_XYZA_MASK;
        writeRegister(PWR_MGMT_2, status);
    }

    public boolean isAccelXEnabled() throws IOException, IllegalStateException {
        int status = readRegister(PWR_MGMT_2) & DISABLE_XA_MASK;
        return (status == 0);
    }

    public boolean isAccelYEnabled() throws IOException, IllegalStateException {
        int status = readRegister(PWR_MGMT_2) & DISABLE_YA_MASK;
        return (status == 0);
    }

    public boolean isAccelZEnabled() throws IOException, IllegalStateException {
        int status = readRegister(PWR_MGMT_2) & DISABLE_ZA_MASK;
        return (status == 0);
    }


    public float getAccelX() throws IOException, IllegalStateException {
        if(isAccelXEnabled()) {
            return readRegisters(ACCEL_XOUT_H, ACCEL_XOUT_L);
        }
        Log.e(TAG, "accel x is disabled");
        return 0;

    }

    public float getAccelY() throws IOException, IllegalStateException {
        if(isAccelYEnabled()) {
            return readRegisters(ACCEL_YOUT_H, ACCEL_YOUT_L);
        }
        Log.e(TAG, "accel y is disabled");
        return 0;
    }

    public float getAccelZ()  throws IOException, IllegalStateException {
        if (isAccelZEnabled()) {
            return readRegisters(ACCEL_ZOUT_H, ACCEL_ZOUT_L);
        }
        Log.e(TAG, "accel z is disabled");
        return 0;

    }

    public float[] getAccel() throws IOException, IllegalStateException {
        float[] reading = new float[3];
        reading[0] = getAccelX();
        reading[1] = getAccelY();
        reading[2] = getAccelZ();
        return reading;
    }






    /*
     * Gyroscope methods
     */

//    void enableGyroAxis(int axis) {
//        // TODO: enable Gyro axis
//    }
//    void disableGyroAxis(int axis) {
//        // TODO: disable Gyro axis
//    }

    void enableGyroscope() throws IOException, IllegalStateException {
        int status = readRegister(PWR_MGMT_2);
        status = status & ~DISABLE_XYZG_MASK;
        writeRegister(PWR_MGMT_2, status);
    }

    void disableGyroscope() throws IOException, IllegalStateException {
        int status = readRegister(PWR_MGMT_2);
        status = status | DISABLE_XYZG_MASK;
        writeRegister(PWR_MGMT_2, status);

    }

    public boolean isGyroXEnabled() throws IOException, IllegalStateException {
        int status = readRegister(PWR_MGMT_2) & DISABLE_XG_MASK;
        return (status == 0);
    }

    public boolean isGyroYEnabled() throws IOException, IllegalStateException {
        int status = readRegister(PWR_MGMT_2) & DISABLE_YG_MASK;
        return (status == 0);
    }

    public boolean isGyroZEnabled() throws IOException, IllegalStateException {
        int status = readRegister(PWR_MGMT_2) & DISABLE_ZG_MASK;
        return (status == 0);
    }






    public float getGyroX() throws IOException, IllegalStateException {
        if (isGyroXEnabled()) {
            return readRegisters(GYRO_XOUT_H, GYRO_XOUT_L);
        }
        return 0;
    }

    public float getGyroY() throws IOException, IllegalStateException {
        if (isGyroYEnabled()) {
            return readRegisters(GYRO_YOUT_H, GYRO_YOUT_L);
        }
        return 0;
    }

    public float getGyroZ() throws IOException, IllegalStateException {
        if (isGyroZEnabled()) {
            return readRegisters(GYRO_ZOUT_H, GYRO_ZOUT_L);
        }
        return 0;
    }

    public float[] getGyro() throws IOException, IllegalStateException {
        float[] reading = new float[3];
        reading[0] = getGyroX();
        reading[1] = getGyroY();
        reading[2] = getGyroZ();
        return reading;
    }



    /*
     * Private methods
     */
    private int readRegisters (int msbRegAddr, int lsbRegAddr) throws IOException, IllegalStateException{
        return readRegister(msbRegAddr) <<8  | readRegister(lsbRegAddr);
    }

    private int readRegister(int address) throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("I2C device is not open");
        }

        synchronized (mBuffer) {
            return mDevice.readRegByte(address);
        }
    }


    private void writeRegister (int address, int value) throws IOException, IllegalStateException{
        if (mDevice == null) {
            throw new IllegalStateException("I2C device is not open");
        }
        mDevice.writeRegByte(address, (byte)value);
    }
}
