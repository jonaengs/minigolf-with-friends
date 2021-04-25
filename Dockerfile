FROM java as BUILD
ADD https://dl.google.com/android/repository/commandlinetools-linux-6858069_latest.zip /sdk/sdk.zip
WORKDIR /sdk
RUN unzip sdk.zip
RUN yes | cmdline-tools/bin/sdkmanager --sdk_root=/sdk --licenses
COPY ./ /build
WORKDIR /build
ENV ANDROID_SDK_ROOT=/sdk
RUN ./gradlew desktop:serverDist

FROM java as DEPLOY
COPY --from=BUILD /build/desktop/build/libs/server-1.0.jar /app/server.jar
WORKDIR /app
CMD ["java", "-jar", "server.jar"]