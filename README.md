# chat-filiw

Android application created as Part 2 of a project for the course Distributed Systems at AUEB.

The goal was to create an app in which the users would be able to connect to topics (conversations), and in those send and receive messages in text, image, and video form, using the code created for [Part 1](https://github.com/tassos37000/distributed-sytems) of the project.

It was build using [Android Studio](https://developer.android.com/studio).

## Description
---
### Client

Each user acts as a Client object. A Client is both a Publisher (able to send content to Brokers) and a Consumer (able to receive content from Brokers) at a given topic.
In order to connect to a topic, it first connects to a random broker and based on information received it may need to esatblish a connection with a different Broker.

### Broker

Each Broker is resposible for some topics, based on a hash taken from the topic's name.
The Broker has a connection open for each Client currently using a topic it's responsible for.

## How to use
---
### Requirements

* Minimum SDK for device (physical device, or emulator in Android Studio): 28

### Run

1. Clone project, and open in Android Studio

2. Edit Broker IP Addresses in file ```app/src/main/java/com/example/filiw/backend/Node.java``` for Brokers

3. Edit Broker IP Adresses in file ```app/src/main/res/raw/config.properties``` for application

4. Run each Broker seperately.\
In order to run in cmd:
    ```
    cd app\src\main\java
    javac ./com/example/filiw/backend/Broker1.java
    java com.example.filiw.backend.Broker1
    ```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(Repeat for Broker2, Broker3)\
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**Note:** Brokers may be run in any order

5. Run app in devices