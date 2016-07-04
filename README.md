# kheperAndroid

Kheper3 software interface

** Kheper3 communicates with the outside using Bluetooth Low Energy (BLE). (Ref: Android ==> https://github.com/googlesamples/android-BluetoothLeGatt/) Input : Commands to adjust and customize the operations. Output : Kheper periodically sends characteristic parameters.

1- Controls

They consist of an initial character, possibly followed by parameters with a fixed format (without separator).

For internal organizational reasons, the end of a command execution is not reported (Commands with long run time are specified later in the text)

The commands act mostly on ignition curves that can be described as follows:

1 curve in use (CRR)

1 curve being edited (CR)

14 types including 3 curves modified by the user (1 to 14)

1 curve at zero delay (0)

Each curve is described by:

a label of 16 characters (uint8_t) maximum
75 points representing the delay angle in 1/100 of degree per 200 rpm(unsigned integer uint16_t)
parity (kheper calculated)
"+" Function: 0.25 deg to add in each of the 75 points of the current curve of service (CRR)

"-" Function: 0.25 deg to subtract in each of the 75 points of the current curve of service (CRR)

"S" Function: Stopping the ignition

"Pxxyyyy;" Function:

changes the value of a point on the curve being edited (CR)
xx: number of the point (0 to 74)
yyyy: point value in 1/100 of a degree (from 0-35999)
if xx = 99 the value is assigned to the 75 points of the currently edited curve (CR)
"Eccc...c;" Function:

changes the label of the curve being edited (CR)
cccc...c: label terminated by "; "(Length limited to 16 characters)
"Wx;" Function:

validation and write in flash of curve being edited (CR)
x: number of the curve (1, 2 or 3)
long run time command (about 1000 msec)
"Vxx;" Function:

curve xx goes in use (0 to 14)
If the selected curve is not valid, the command is ignored
"G;" Function: reset firmware

Example initialization and validation of an ignition curve (Android):

// All points to 1 deg

mBluetoothService.WriteValue ( "P9900100;");

// Point at 1000 rpm to 0.5 deg

mBluetoothService.WriteValue ( "P0500050;");

// Initialize the label tryIt

mBluetoothService.WriteValue ( "EtryIt;");

// Write flash as curve No. 2

mBluetoothService.WriteValue ( "W2;");

// curve no2 in use mBluetoothService.WriteValue ( "V02;");

2- Parameters

Kheper emits periodically (every 500 msec) via the BLE a series of parameters in the following alphanumeric format (fixed format without separator):

Pvvvvvdddddrrccllll..ll \n

with: vvvvv rpm t / min

ddddd delay 1/100 degree

rr position thumbwheel

cc curve # in function

llll ... label of curve in use (up to 16 characters)

Example: P01200002001402tryIt

1200 rpm

2 deg

14 position of thumbwheel

02 ignition curve #

tryIt iginition curve label
