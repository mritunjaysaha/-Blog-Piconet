---
title: How to create piconet in android
date: "2020-05-12T22:12:03.284Z"
description: "Connect multiple bluetooth devices to a single device"
---

I am writing this blog because I found it quite to difficult to find any resource to a create a piconet for android devices.

## What is a piconet?

A piconet is a Bluetooth ad-hoc network. It can consists of one master device and a maximum of seven slave devices.

##

We will create a class named Piconet. It will have the required methods needed to connect multiple devices. For this we will need a UUID (There are many online UUID genarators available online).

## How will the device connect?

For the devices to connect the app needs to be running on all the devices. Since the Bluetooth socket needs to be open on all the devices.

Once the app is running we will click the discover button so that all the paired devices are listed on the drop down list. from the drop down list we can select a mobile bluetooth device to connect. Once the devices are connected then it will show a toast and we can send "Hello World" text to the other Bluetooth devices.

![Screenshot](device.jpg)

The bluetooth device that starts the connection is the master bluetooth devie. It can connect upto a maximum of 7 devices (Theoritical limit. Practical limitations depends on the hardware). If the send message button is clicked on the master device then it will broadcast the message. But it is not same for the slave devices, when the send message buttton is pressed on the slave devices then it will send the text messages only to the master device.

The code for this blog is in this [Github Repo](https://github.com/mritunjaysaha/-Blog-Piconet)
