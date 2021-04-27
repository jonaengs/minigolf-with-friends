# Requirements:

This project requires Java and Android SDK to build. Alternativly, if you just want to run the server you can also run
it as a Docker container using Docker Compose.

# Installation

## Gradle

To build this project using gradle, only Java and Android SDK is needed. If you use IntelliJ or Android Studios you can
manage and install Android SDK this way. If you want to build from the command line, you can do this following
instructions similar to the build script in [Dockerfile](./Dockerfile).

### Desktop Client

To build the mobile desktop client, simply run `./gradlew desktop:dist`. You can now start the jar file using java
with `java -jar ./desktop/build/libs/desktop-1.0.jar`. You can also run the game directly from gradle
using `./gradlew desktop:run`.

Note that in the desktop version, use click and drag as touch, and the escape key as the back button.

### Server

To build the server, simply run `./gradlew desktop:serverDist`. You can now start a server
with `java -jar ./desktop/build/libs/server-1.0.jar`.

### Android

To run the Android version using the emulator, simply run `./gradlew android:run`. You can also connect your phone to
IntelliJ or Android Studio and press the play/run button to launch on your phone, instead of the emulator. This requires
that you have setup developer mode on your phone.

## Docker (server image)

We have also added a Dockerfile and docker-compose.yml config to run the server with docker. This will both build the
jar file and run the server without you having to install anything other than docker.

To run the server, simply run `docker-compose up`. This will first build the docker image, if it is the first time and
then start the server. You can also add the `-d` flag to start it in detached mode in the background. If so,
then `docker-compose down` will stop the server.

# Configuration

You can change some environment variables to change how the game runs.

### `SERVER_IP`

Change the environment variable `SERVER_IP` for the client to change the default server it connects to. Example:

````bash
SERVER_IP=localhost java -jar desktop-1.0.jar
````

By default, it will try and connect to `golf.intveld.no`.

### `NUM_TICKS`

Change the environment variable `NUM_TICKS` for the server to change the default tick rate to the server. Reducing this
might increase performance, but also increases latency and lag for the client. Increasing this seems to not have much
visible effect for the client. By default, it is set to `60` which is the same as the default frame rate.
