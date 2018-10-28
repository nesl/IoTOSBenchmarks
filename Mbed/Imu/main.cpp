#include "mbed.h"
#include "MPU9250.h"

DigitalOut led1(LED1);
Serial pc(USBTX, USBRX); // tx, rx
// main() runs in its own thread in the OS
int main() {
    MPU9250 mpu9250(p28,p27);
    mpu9250.initMPU9250();
    while (true) {
        int16_t gyro_data[3];
        mpu9250.readGyroData(gyro_data);
        pc.printf("%d, %d, %d\r\n", gyro_data[0], gyro_data[1], gyro_data[2]);
        led1 = !led1;
        wait(0.5);
    }
}
