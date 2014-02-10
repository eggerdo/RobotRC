# Robot RC

*** DEVELOPER VERSION ***

## Introduction

The [Robot RC](https://play.google.com/store/apps/details?id=org.dobots.robotrc) app is the further development of the [ZMQ Video Chat](https://github.com/eggerdo/ZmqVideoChat) app. It serves as a showcase application to demonstrate the use of [ØMQ](www.zeromq.org) to control robots and display their video stream remotely. In order to use this application, two android devices are needed. The devices can be connected P2P or with the use of the [robot server](https://github.com/eggerdo/robot_server.node). The first device will connect to the robot, send out video frames and receive remote controls. The second device will receive video frames to display them on the screen and provide a user interface to drive the robot.

The app provides two components:

1. ØMQ remote control features for robots. It connects to the robot and listens to incoming remote control commands over ØMQ. In return, the video stream received from the robot is sent over ØMQ.

2. User interface to remote control a robot over ØMQ. Controls are sent over ØMQ to the robot server and the video stream is received and displayed on the screen.

As of now, the following robots are available: Romo, Brookstone AC13, Brookstone Rover 2.0, I-Spy Tank. More robots will be added later on.

Feel free to contribute and add your own robots to the list or let us know which ones you want to see added!

## Setup

First install / start-up the Robot RC app. On the first start, the app will show the configuration for ØMQ. On later starts, the ØMQ configuration can be accessed through the menu. Depending on the connection type you want to use follow one of following steps.

### P2P

On the first devices which will connect to the robot, enter the IP address of the device itself and select Local. For the message ports, choose a set of numbers, e.g. 4000 (Video), 4010 (Command).

On the second device, enter the same port numbers, the IP address of the first device and select Remote.

Note that the devices have to be connected to the same network!

### Robot Server (recommended)

First set up the robot server, which has to run on a computer with access to the robot's network. I.e. either connect the robot to your network, or if not possible connect the computer directly to the robot's network. Follow the steps described [here](https://github.com/eggerdo/robot_server.node) to install and run the robot server.
    
For the ØMQ configuration, select Remote on both devices, enter the IP address of the computer running the robot server, and the message ports which by default are 4000 (Video), 4010 (Command).

## Run

On the device that will connect to the robot, choose the left button, which leads to the robot selection screen. Select here your robot of choice. If connection with the robot is successful, the video of the robot (and other sensor data if available) will be displayed. You can already control the robot like this. If the robot's IP has changed from it's default IP address and/or port then the connection settings (available from the menu) have to be adapted.

On the second device choose the right button. This will show the remote control buttons, and if connection with the first device is successful, the video will be displayed. Alternatively to the buttons, a virtual joystick can be chosen from the menu. Also the robots might offer additional camera control. E.g. the Rover 2.0 is able to move the camera up and down. To do that remotely, select Camera Control from the menu, then use the slider to move the camera up and down. Or the smartphone used to control the Romo might have two cameras, which can be toggled with the Toggle button. If a robot doesn't support one or all of the camera controls, the respective control will be ignored.

## Addendum

In case you're wondering why we need the second device to control the robot since we can already do that with the first, consider this: If the robot server has access to the internet, and is visible to the outside (ports open, static ip, etc.) then your second device can be anywhere in the world with internet access and still be able to control the robot remotely.

# Project Dependencies

To compile the app from source, the two android library projects [DoBotsUtilities](https://github.com/eggerdo/DoBotsUtilities) and [Robot-Lib](https://github.com/eggerdo/Robot-Lib) are required as well. Import them alongside the RobotRC project, then make sure that the references in the RobotRC's project properties (Project > Properties > Android) are correct.