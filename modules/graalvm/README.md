# Docker + GraalVM native-image

1. create locally the `graalvm-native-image` container using `graalvm-native-image/build.sh`. This container will be used to build the native image.
2. run `sbt "graalvm-cli/dockerGraalvmNative"`: this will generate the `docker-graalvm-native-test` container
3. run `sbt "graalvm-cli/docker:publishLocal"`: this will generate the `docker-test` continer
4. run in shell: `time docker run --rm docker-graalvm-native-test`
5. run in shell: `time docker run --rm docker-test` and compare the timing
6. run `docker images | grep docker-` and compare the image size