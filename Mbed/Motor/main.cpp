#include "mbed.h"

PwmOut led(LED1);

// Parameters of the servo PWM
double MIN_ACTIVE_PULSE_DURATION_MS = 1;
double MAX_ACTIVE_PULSE_DURATION_MS = 2;
double PULSE_PERIOD_MS = 20;  // Frequency of 50Hz (1000/20)

// Parameters for the servo movement over time
double PULSE_CHANGE_PER_STEP_MS = 0.2;
int INTERVAL_BETWEEN_STEPS_MS = 50;

bool isPulseIncreasing = true;

// specify period first

//led = 0.5f;          // shorthand for led.write()
//led.pulsewidth(2);   // alternative to led.write, set duty cycle time in seconds


// main() runs in its own thread in the OS
int main() {
    double activePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
    //led.period(4.0f);      // 4 second period
    //led.write(0.50f);      // 50% duty cycle, relative to period
    led.period_ms(PULSE_PERIOD_MS);
    led.pulsewidth_ms(activePulseDuration);

    while(true) {
        if (isPulseIncreasing) {
            activePulseDuration += PULSE_CHANGE_PER_STEP_MS;
        } else {
            activePulseDuration -= PULSE_CHANGE_PER_STEP_MS;
        }

        // Bounce activePulseDuration back from the limits
        if (activePulseDuration > MAX_ACTIVE_PULSE_DURATION_MS) {
            activePulseDuration = MAX_ACTIVE_PULSE_DURATION_MS;
            isPulseIncreasing = !isPulseIncreasing;
        } else if (activePulseDuration < MIN_ACTIVE_PULSE_DURATION_MS) {
            activePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
            isPulseIncreasing = !isPulseIncreasing;
        }
        led.pulsewidth_ms(activePulseDuration);
        wait_ms(INTERVAL_BETWEEN_STEPS_MS);
    }
}


