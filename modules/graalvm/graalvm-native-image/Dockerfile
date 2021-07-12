FROM ghcr.io/graalvm/graalvm-ce:21.1.0
WORKDIR /opt/graalvm
RUN gu install native-image
ENTRYPOINT ["native-image"]