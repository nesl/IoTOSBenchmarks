#include "mbed.h"

// Hardware connection
// a pull up circuit with button. button signal goes to p10.

DigitalOut led(p9);
InterruptIn myButton(p10);
Timer debounceTimer;
Serial pc(USBTX, USBRX);
DigitalOut led1(LED1);

// assume any two key presses within 1000us of each other are caused by mechanical issues with the switch.
// This value may need tweaking depending on the switch used.
const int debounceTimeUS = 1000;

// this variable is set to true when a 1 should be displayed.
// use the volatile keyword since its value is changed by an interrupt.
volatile bool buttonPressed = false;


// this function is called whenever the button is pressed.
// Note - due to glitches caused by the button contacts bouncing this could be called multiple times when button is pressed.
// It could also be called when button is released due to the contacts bouncing around a bit.
// Search for de-bouncing for details on how cope with this if it causes problems.
void onButtonPush() {
    if (debounceTimer.read_us() > debounceTimeUS) {
        if (myButton == 1) {  // very basic sanity check that this wasn't a glitch when the button was released.
            buttonPressed = !buttonPressed; // change the state of the variable
        }
    }
    debounceTimer.reset(); // reset the timer to 0
    debounceTimer.start(); //  probably not needed but just in case a reset stops the timer...
}

// main loop
int main () {
    pc.printf("Start\r\n");
    // start the timer.
    debounceTimer.reset();
    debounceTimer.start();
    led = 0;

    // set the function onButtonPush to be called every time that pin goes from 0 to 1.
    myButton.rise(&onButtonPush);

    bool previousState = false;
    while (true) {
        if (buttonPressed != previousState) { // the button has been pressed.
            previousState = buttonPressed;
            if (buttonPressed ) {
                pc.putc('1');
                led = 1;
                led1 = 1;
            }
            else {
                pc.putc('0');
                led = 0;
                led1 = 0;
            }
        }
    }
}

