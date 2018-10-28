#include "mbed.h"

/*
Hardware configuration
p20 -> LED -> 10kohm resistor -> GND
*/


DigitalOut led1(p20);

// main() runs in its own thread in the OS
int main() {
    while (true) {
        led1 = !led1;
        wait(0.5);
    }
}
